package com.hotel.hotel_stars.Service;


import com.hotel.hotel_stars.DTO.HotelInfoDTO;
import com.hotel.hotel_stars.DTO.HotelRoomDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.apache.logging.log4j.util.Strings;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {
    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    private static final String GEMINI_MODEL = "gemini-2.5-flash";

    @Autowired
    private HotelService hotelService;

    @Autowired
    private HttpSession session;

    @Value("${gemini.api.key}")
    private String apiKey;



    // Dùng danh sách lưu lịch sử hội thoại dạng role-based
    private List<Map<String, String>> conversationHistory = new ArrayList<>();

    public String chat(String prompt) {
        // Lấy thông tin khách sạn từ session hoặc từ DB

        HotelInfoDTO hotelInfo = (HotelInfoDTO) session.getAttribute("hotelInfo");
        if (hotelInfo == null) {
            hotelInfo = hotelService.getHotelFullInfo();
            session.setAttribute("hotelInfo", hotelInfo);
            logger.info("Đã lấy thông tin khách sạn từ DB và lưu vào session.");
        } else {
            logger.info("Lấy thông tin khách sạn từ session cache.");
        }
        // Định dạng dữ liệu khách sạn
        String hotelInfoContext = formatHotelInfo(hotelInfo);

        String structuredPrompt = """
Bạn là một trợ lý tư vấn khách hàng ảo chuyên nghiệp trong lĩnh vực khách sạn.

 Mục tiêu: Trả lời khách ngắn gọn, rõ ràng, đúng trọng tâm, thể hiện sự chuyên nghiệp và hữu ích.

 **Quy tắc khi phản hồi:**
- Chỉ trả lời trong phạm vi thông tin được cung cấp trong phần [Thông tin khách sạn]. Không bịa đặt hoặc suy đoán.
- Trả lời tối đa 4–5 câu, rõ ràng và dễ hiểu.
- Trả lời đúng trọng tâm câu hỏi. Ví dụ: nếu khách hỏi “khách sạn có bãi đậu xe không?”, thì chỉ cần trả lời đúng nội dung đó.
- Không yêu cầu khách truy cập website.
- Nếu thông tin chưa có sẵn, hãy trả lời lịch sự: "Hiện tại hệ thống chưa có thông tin này, bạn vui lòng cung cấp thêm chi tiết hoặc đặt câu hỏi khác để tôi hỗ trợ tốt hơn ạ."
- Giữ giọng văn **chuyên nghiệp, lịch sự và thân thiện**.
- Chỉ tư vấn khách hàng về dịch vụ khách sạn; không hỗ trợ khách đặt phòng hoặc thực hiện giao dịch.
 **Thông tin cố định về thời gian (áp dụng cho mọi khách sạn):**
- Giờ nhận phòng (Check-in): từ 14h00 (2:00 PM)
- Giờ trả phòng (Check-out): trước 12h00 trưa (12:00 PM)
- Giá phòng được tính theo ngày lưu trú, không tính theo đêm hoặc theo giờ.

[Thông tin khách sạn]
%s

[Câu hỏi của khách hàng]
%s

❗**Trường hợp không liên quan:** Nếu khách hỏi nội dung không liên quan đến dịch vụ khách sạn, hãy từ chối nhẹ nhàng:  
"Tôi xin phép chỉ hỗ trợ các nội dung liên quan đến dịch vụ khách sạn. Bạn cần thêm thông tin gì về khách sạn không ạ?"
""".formatted(hotelInfoContext, prompt);


        // Tạo request body JSON
        String fullPrompt = getPromptBody(structuredPrompt);

        // Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Prepare request entity
        fullPrompt = getPromptBody(fullPrompt);
        HttpEntity<String> requestEntity = new HttpEntity<>(fullPrompt, headers);


        // Perform HTTP POST request
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "https://generativelanguage.googleapis.com/v1beta/models/"
                        + GEMINI_MODEL + ":generateContent?key=" + apiKey,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            String responseText = parseGeminiResponse(responseEntity.getBody());

            // Cập nhật lịch sử hội thoại
            conversationHistory.add(Map.of("role", "user", "text", prompt));
            conversationHistory.add(Map.of("role", "assistant", "text", responseText));

            return responseText;
        } else {
            throw new RuntimeException("Gemini API thất bại: " + responseEntity.getStatusCode());
        }
    }

    private String parseGeminiResponse(String jsonResponse) {
        try {
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(jsonResponse);
            JSONArray candidates = (JSONArray) jsonObject.get("candidates");
            JSONObject firstCandidate = (JSONObject) candidates.get(0);
            JSONObject content = (JSONObject) firstCandidate.get("content");
            JSONArray parts = (JSONArray) content.get("parts");
            JSONObject part = (JSONObject) parts.get(0);
            return (String) part.get("text");
        } catch (Exception e) {
            logger.error("Lỗi parse Gemini response", e);
            return "Xin lỗi, tôi gặp lỗi khi xử lý phản hồi.";
        }
    }
    public String getPromptBody(String prompt) {
        // Create prompt for generating summary in document language
        JSONObject promptJson = new JSONObject();

        // Array to contain all the content-related data, including the text and role
        JSONArray contentsArray = new JSONArray();
        JSONObject contentsObject = new JSONObject();
        contentsObject.put("role", "user");

        // Array to hold the specific parts (or sections) of the user's input text
        JSONArray partsArray = new JSONArray();
        JSONObject partsObject = new JSONObject();
        partsObject.put("text", prompt);
        partsArray.add(partsObject);
        contentsObject.put("parts", partsArray);

        contentsArray.add(contentsObject);
        promptJson.put("contents", contentsArray);

        // Array to hold various safety setting objects to ensure the content is safe and appropriate
        JSONArray safetySettingsArray = new JSONArray();

        // Adding safety settings for hate speech
        JSONObject hateSpeechSetting = new JSONObject();
        hateSpeechSetting.put("category", "HARM_CATEGORY_HATE_SPEECH");
        hateSpeechSetting.put("threshold", "BLOCK_ONLY_HIGH");
        safetySettingsArray.add(hateSpeechSetting);

        // Adding safety settings for dangerous content
        JSONObject dangerousContentSetting = new JSONObject();
        dangerousContentSetting.put("category", "HARM_CATEGORY_DANGEROUS_CONTENT");
        dangerousContentSetting.put("threshold", "BLOCK_ONLY_HIGH");
        safetySettingsArray.add(dangerousContentSetting);

        // Adding safety settings for sexually explicit content
        JSONObject sexuallyExplicitSetting = new JSONObject();
        sexuallyExplicitSetting.put("category", "HARM_CATEGORY_SEXUALLY_EXPLICIT");
        sexuallyExplicitSetting.put("threshold", "BLOCK_ONLY_HIGH");
        safetySettingsArray.add(sexuallyExplicitSetting);

        // Adding safety settings for harassment content
        JSONObject harassmentSetting = new JSONObject();
        harassmentSetting.put("category", "HARM_CATEGORY_HARASSMENT");
        harassmentSetting.put("threshold", "BLOCK_ONLY_HIGH");
        safetySettingsArray.add(harassmentSetting);

        promptJson.put("safetySettings", safetySettingsArray);

        // Creating and setting generation configuration parameters such as temperature and topP
        JSONObject parametersJson = new JSONObject();
        parametersJson.put("temperature", 0.5);
        parametersJson.put("topP", 0.99);
        promptJson.put("generationConfig", parametersJson);

        // Convert the JSON object to a JSON string
        return promptJson.toJSONString();
    }
    private String formatHotelInfo(HotelInfoDTO info) {
        return info.getHotelInfo() + "\n\n"
                + info.getRoomTypes() + "\n\n"
                + info.getRoomAmenities() + "\n"
                + info.getRoomServices() + "\n"
                + info.getHotelServices() + "\n"
                + info.getHotelAmenities();
    }

    // Tùy chọn: reset lịch sử


}


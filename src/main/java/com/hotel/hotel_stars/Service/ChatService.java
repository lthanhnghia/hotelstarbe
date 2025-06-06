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
    private static final String GEMINI_MODEL = "gemini-1.5-flash";

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
        Map<String, String> dateMap = extractBookingDates(prompt);
        String startDate = dateMap.get("startDate");
        String endDate = dateMap.get("endDate");
        // --- Bước 2: Quản lý cache thông tin phòng trống trong Session ---
        List<HotelRoomDTO> hotelRoom = null;
        String roomSessionKey = "hotelRooms_" + startDate + "_" + endDate; // Key cache theo ngày

        if (!startDate.isEmpty() && !endDate.isEmpty()) { // Chỉ cache nếu có ngày hợp lệ
            hotelRoom = (List<HotelRoomDTO>) session.getAttribute(roomSessionKey);

            if (hotelRoom == null) {
                // logger.info("Đang truy vấn thông tin phòng trống từ DB cho ngày {} đến {}", startDate, endDate);
                System.out.println("Đang truy vấn thông tin phòng trống từ DB cho ngày " + startDate + " đến " + endDate);
                hotelRoom = hotelService.getHotelRoom(startDate, endDate, null, null);
                if (hotelRoom != null && !hotelRoom.isEmpty()) {
                    session.setAttribute(roomSessionKey, hotelRoom);
                    // logger.info("Đã lưu thông tin phòng trống vào session cache với key: {}", roomSessionKey);
                    System.out.println("Đã lưu thông tin phòng trống vào session cache với key: " + roomSessionKey);
                } else {
                    // logger.info("Không tìm thấy phòng trống hoặc lỗi truy vấn cho ngày {} đến {}", startDate, endDate);
                    System.out.println("Không tìm thấy phòng trống hoặc lỗi truy vấn cho ngày " + startDate + " đến " + endDate);
                    // Có thể lưu một list rỗng để tránh truy vấn lại nếu chắc chắn không có phòng
                    // session.setAttribute(roomSessionKey, new ArrayList<HotelRoomDTO>());
                }
            } else {
                // logger.info("Lấy thông tin phòng trống từ session cache với key: {}", roomSessionKey);
                System.out.println("Lấy thông tin phòng trống từ session cache với key: " + roomSessionKey);
            }
        } else {
            // Nếu không có startDate hoặc endDate hợp lệ, coi như không có thông tin phòng trống
            hotelRoom = new ArrayList<>(); // Gán một list rỗng để tránh NullPointerException
            // logger.info("Không có ngày hợp lệ để truy vấn thông tin phòng trống. Trả về danh sách rỗng.");
            System.out.println("Không có ngày hợp lệ để truy vấn thông tin phòng trống. Trả về danh sách rỗng.");
        }
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
        String hotelRoomContext = formatHotelRoomListForPrompt(hotelRoom);
        String structuredPrompt = """

Bạn đóng vai trò là một nhân viên tư vấn và chăm sóc khách hàng chuyên nghiệp trong lĩnh vực khách sạn, có hơn 3 năm kinh nghiệm làm việc thực tế.

Mục tiêu: Trả lời khách ngắn gọn, rõ ràng, đúng trọng tâm, thể hiện sự thân thiện và chuyên nghiệp.

**Quy tắc bắt buộc khi phản hồi:**
- Chỉ trả lời trong phạm vi thông tin được cung cấp trong phần [Thông tin khách sạn] và [Thông tin phòng trống]. Không bịa đặt hoặc suy đoán.
- Phản hồi rõ ràng, tối đa 4–5 câu. Không trình bày dài dòng hay lặp ý.
- Trả lời đúng trọng tâm câu hỏi của khách. Ví dụ: nếu khách hỏi “khách sạn ở đâu?”, chỉ cần cung cấp địa chỉ rõ ràng, không nói lan man về loại phòng hay dịch vụ.
- Không yêu cầu khách truy cập website. Bạn đang hỗ trợ trực tiếp tại đây.
- Nếu thông tin được hỏi chưa có trong phần [Thông tin khách sạn] hoặc [Thông tin phòng trống], hãy nói rõ: "Hiện tại hệ thống chưa kiểm tra được thông tin này, bạn vui lòng cho biết thêm chi tiết để hỗ trợ tốt hơn."
- Có thể đề xuất linh hoạt, nhưng không vượt ngoài thông tin hiện có.
- Luôn thêm câu sau vào cuối câu trả lời về tình trạng phòng, sau khi cung cấp thông tin chi tiết: "Vì lượng đặt phòng có thể thay đổi nhanh chóng, Để đảm bảo giữ được phòng, bạn vui lòng hoàn tất đặt phòng sớm nhé!"
- Giữ giọng văn thân thiện, dễ hiểu, chuyên nghiệp, và tạo cảm giác tự nhiên như đang trò chuyện thực tế.

**Thông tin cố định về thời gian (áp dụng cho mọi khách sạn):**
- Giờ nhận phòng (Check-in): từ 14h00 (2:00 PM)
- Giờ trả phòng (Check-out): trước 12h00 trưa (12:00 PM)
- Giá phòng được tính theo ngày lưu trú không tính theo đêm, không theo giờ.
[Thông tin khách sạn]
%s
[Thông tin truy vấn ]
%s
[Câu hỏi của khách hàng]
%s
**Trường hợp không liên quan:**  
Nếu khách hỏi nội dung không liên quan đến dịch vụ khách sạn, hãy từ chối nhẹ nhàng và gợi ý quay lại chủ đề phù hợp, ví dụ: "Mình xin phép chỉ hỗ trợ các nội dung liên quan đến dịch vụ khách sạn, bạn cần hỗ trợ đặt phòng hay thông tin gì thêm không ạ?"
""".formatted(hotelInfoContext,hotelRoom.toString(),prompt);

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
                "https://generativelanguage.googleapis.com/v1beta/models/" + GEMINI_MODEL + ":generateContent?key=" + apiKey,
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
    public Map<String, String> extractBookingDates(String userQuestion) {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Prompt hướng dẫn mô hình hiểu và chuyển đổi ngày mơ hồ sang định dạng cụ thể
        String prompt = """
Trích xuất ngày bắt đầu và ngày kết thúc đặt phòng từ câu hỏi dưới đây.

Nếu người dùng nói các cụm mơ hồ như "hôm nay", "ngày mai", "cuối tuần", "thứ 7 tuần sau"... bạn cần chuyển chúng sang định dạng ngày cụ thể theo định dạng yyyy-MM-dd dựa trên ngày hiện tại là %s.

Chỉ trả về JSON có cấu trúc:
{
  "startDate": "yyyy-MM-dd",
  "endDate": "yyyy-MM-dd"
}
Nếu không tìm thấy ngày nào thì trả về {"startDate": "", "endDate": ""}

Câu hỏi: %s
""".formatted(today.format(formatter), userQuestion); // Sử dụng today.format(formatter) để đảm bảo định dạng đúng

        // Tạo request JSON theo cấu trúc Gemini API
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> userPart = new HashMap<>();
        userPart.put("text", prompt);
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("parts", List.of(userPart));
        contents.add(userMessage);

        JSONObject requestJson = new JSONObject();
        requestJson.put("contents", contents);

        // Định nghĩa cấu trúc JSON mong muốn trong generationConfig
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.0);
        generationConfig.put("topP", 0.9);
        generationConfig.put("responseMimeType", "application/json"); // Yêu cầu phản hồi là JSON

        // Định nghĩa schema cho JSON trả về
        Map<String, Object> responseSchema = new HashMap<>();
        responseSchema.put("type", "OBJECT");
        Map<String, Object> properties = new HashMap<>();

        Map<String, Object> startDateProp = new HashMap<>();
        startDateProp.put("type", "STRING");
        properties.put("startDate", startDateProp);

        Map<String, Object> endDateProp = new HashMap<>();
        endDateProp.put("type", "STRING");
        properties.put("endDate", endDateProp);

        responseSchema.put("properties", properties);
        responseSchema.put("propertyOrdering", List.of("startDate", "endDate")); // Đảm bảo thứ tự các thuộc tính

        generationConfig.put("responseSchema", responseSchema);
        requestJson.put("generationConfig", generationConfig);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestJson.toJSONString(), headers);

        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    "https://generativelanguage.googleapis.com/v1beta/models/" + GEMINI_MODEL + ":generateContent?key=" + apiKey,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                String rawJson = responseEntity.getBody();

                // Khi sử dụng responseMimeType và responseSchema, phản hồi trực tiếp là JSON
                // mà không cần phải trích xuất từ "parts[0].text" nữa.
                // Mô hình sẽ trả về một đối tượng JSON trực tiếp trong "candidates[0].content.parts[0].text"
                // nhưng với cấu trúc JSON đã được đảm bảo bởi responseSchema.
                // Vì vậy, ta vẫn cần parse rawJson để lấy phần text chứa JSON.
                JSONObject jsonObject = (JSONObject) new JSONParser().parse(rawJson);
                JSONArray candidates = (JSONArray) jsonObject.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    JSONObject firstCandidate = (JSONObject) candidates.get(0);
                    JSONObject content = (JSONObject) firstCandidate.get("content");
                    if (content != null) {
                        JSONArray parts = (JSONArray) content.get("parts");
                        if (parts != null && !parts.isEmpty()) {
                            JSONObject part = (JSONObject) parts.get(0);
                            String jsonText = (String) part.get("text"); // Đây sẽ là chuỗi JSON trực tiếp

                            // Parse chuỗi JSON bên trong
                            JSONObject dateJson = (JSONObject) new JSONParser().parse(jsonText);

                            String startDate = (String) dateJson.getOrDefault("startDate", "");
                            String endDate = (String) dateJson.getOrDefault("endDate", "");

                            Map<String, String> result = new HashMap<>();
                            result.put("startDate", startDate);
                            result.put("endDate", endDate);
                            return result;
                        }
                    }
                }
                // Nếu không tìm thấy cấu trúc mong muốn
                // logger.warn("Không tìm thấy cấu trúc JSON mong muốn trong phản hồi API.");
                return Map.of("startDate", "", "endDate", "");

            } else {
                // logger.error("extractBookingDates API thất bại, status: {}", responseEntity.getStatusCodeValue());
                System.err.println("extractBookingDates API thất bại, status: " + responseEntity.getStatusCodeValue());
            }
        } catch (ParseException e) {
            // logger.error("Lỗi parse JSON từ phản hồi API", e);
            System.err.println("Lỗi parse JSON từ phản hồi API: " + e.getMessage());
        } catch (Exception e) {
            // logger.error("Lỗi gọi extractBookingDates API", e);
            System.err.println("Lỗi gọi extractBookingDates API: " + e.getMessage());
        }

        // Trả về mặc định nếu lỗi
        return Map.of("startDate", "", "endDate", "");
    }
    private String formatHotelRoomListForPrompt(List<HotelRoomDTO> rooms) {
        if (rooms == null || rooms.isEmpty()) {
            return "Hiện tại không có phòng trống nào.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Danh sách phòng trống hiện có:\n");
        for (HotelRoomDTO room : rooms) {
            sb.append(String.format("- Loại phòng: %s (ID: %d)\n", room.getRoomTypeName(), room.getRoomTypeId()));
            sb.append(String.format("  + Số lượng phòng trống: %d\n", room.getAvailableRoomCount()));
            sb.append(String.format("  + Sức chứa khách: %d người\n", room.getGuestLimits()));
            sb.append(String.format("  + Giá: %.0f VND/ngày\n", room.getPriceTypeRoom()));
            sb.append(String.format("  + Tiện nghi: %s\n", String.join(", ", room.getAmenitiesList())));
            sb.append(String.format("  + Mô tả: %s\n", room.getDescribe()));
            sb.append("\n"); // Thêm dòng trống để dễ đọc
        }
        return sb.toString();
    }
}


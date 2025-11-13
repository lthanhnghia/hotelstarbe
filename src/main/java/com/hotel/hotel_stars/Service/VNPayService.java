package com.hotel.hotel_stars.Service;
import com.hotel.hotel_stars.Config.VNPayConfig;
import com.hotel.hotel_stars.Entity.*;
import com.hotel.hotel_stars.Repository.*;
import com.hotel.hotel_stars.utils.paramService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VNPayService {
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    private DiscountRepository discountRepository;
    @Autowired
    private DiscountAccountRepository discountAccountRepositorys;
    @Autowired
    private StatusBookingRepository statusBookingRepository;
    @Autowired
    @Lazy
    BookingService bookingService;
    @Autowired
    private paramService paramServices;
    public String createOrder(int total, String orderInfor, String urlReturn, HttpServletRequest request) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = VNPayConfig.getRandomNumber(8);

        // ✅ Lấy IP động
        String vnp_IpAddr = request.getRemoteAddr();

        String vnp_TmnCode = VNPayConfig.vnp_TmnCode;
        String orderType = "other";

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(total * 100));
        vnp_Params.put("vnp_CurrCode", "VND");

        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", orderInfor);
        vnp_Params.put("vnp_OrderType", orderType);

        String locate = "vn";
        vnp_Params.put("vnp_Locale", locate);

        // ✅ Ghép URL Return chuẩn

        vnp_Params.put("vnp_ReturnUrl", VNPayConfig.vnp_Returnurl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        // ✅ Thời gian tạo và hết hạn
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh"); // ép về giờ Việt Nam
        Calendar cld = Calendar.getInstance(timeZone);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(timeZone); // cũng phải set formatter về VN

        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        Date currentDate = cld.getTime();
        ZonedDateTime utcNow = ZonedDateTime.now(ZoneId.of("UTC"));
        ZonedDateTime vnNow = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        System.out.println("🕒 vnp_CreateDate = " + vnp_CreateDate);
        System.out.println("🕒 vnp_ExpireDate = " + vnp_ExpireDate);
        System.out.println("🕒 Server Date (VN) = " + currentDate);
        System.out.println("🌍 Server Time UTC = " + utcNow);
        System.out.println("🇻🇳 Server Time VN = " + vnNow);


        // ✅ Build data
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (Iterator<String> itr = fieldNames.iterator(); itr.hasNext(); ) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                        .append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                if (itr.hasNext()) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }

        // ✅ Hash Secure
        String vnp_SecureHash = VNPayConfig.hmacSHA512(VNPayConfig.vnp_HashSecret, hashData.toString());
        query.append("&vnp_SecureHash=").append(vnp_SecureHash);
        String paymentUrl = VNPayConfig.vnp_PayUrl + "?" + query;
        System.out.println("🌐 Final VNPay URL: " + paymentUrl);
        return paymentUrl;
    }


    public int orderReturn(HttpServletRequest request){
        Map fields = new HashMap();
        for (Enumeration params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = null;
            String fieldValue = null;
            try {
                fieldName = URLEncoder.encode((String) params.nextElement(), StandardCharsets.US_ASCII.toString());
                fieldValue = URLEncoder.encode(request.getParameter(fieldName), StandardCharsets.US_ASCII.toString());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        if (fields.containsKey("vnp_SecureHashType")) {
            fields.remove("vnp_SecureHashType");
        }
        if (fields.containsKey("vnp_SecureHash")) {
            fields.remove("vnp_SecureHash");
        }
        String signValue = VNPayConfig.hashAllFields(fields);
        if (signValue.equals(vnp_SecureHash)) {
            if ("00".equals(request.getParameter("vnp_TransactionStatus"))) {
                return 1;
            } else {
                return 0;
            }
        } else {
            return -1;
        }
    }

    public void HandleVNPayCallbacks(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String orderInfo = request.getParameter("vnp_OrderInfo");
        StatusBooking statusBooking= statusBookingRepository.findById(2).get();
        int paymentStatus = orderReturn(request);
        Booking booking = bookingRepository.findById(Integer.valueOf(orderInfo)).get();
        if( paymentStatus == 1){
            try {
                if (orderInfo != null) {
                    orderInfo = URLDecoder.decode(orderInfo, "UTF-8");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            List<BookingRoom> bookingRoomList = booking.getBookingRooms();
            double total = bookingRoomList.stream().mapToDouble(BookingRoom::getPrice).sum();
            if(booking.getDiscountPercent()!=null && booking.getDiscountPercent()!=null){
                double discountAmount = total * (booking.getDiscountPercent() / 100);
                total=total-discountAmount;
            }
            System.out.println();
            String formattedAmount = NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(total);
            LocalDate startDate=paramServices.convertInstallToLocalDate(booking.getStartAt());
            LocalDate endDate=paramServices.convertInstallToLocalDate(booking.getEndAt());
            booking.setStatus(statusBooking);
            String roomsString = bookingRoomList.stream()
                    .map(bookingRoom -> bookingRoom.getRoom().getRoomName())  // Extract roomName from each BookingRoom
                    .collect(Collectors.joining(", "));
            String idBk = "BK" + LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy")) + "TT" + booking.getId();
            booking.setStatusPayment(true);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            String startDateStr = startDate.format(formatter);
            String endDateStr = endDate.format(formatter);
            paramServices.sendEmails(booking.getAccount().getEmail(),"thông tin đơn hàng",
                    paramServices.pdfDownload(idBk,booking,startDateStr,endDateStr ,formattedAmount,roomsString, paramServices.getImage()));


            try {
                bookingRepository.save(booking);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String redirectUrl = null;
            try {
                String paymentStatuss = "success";
                String messages = "Bạn đã đặt phòng thành công vui lòng vào email để xem chi tiết đơn đặt hàng ";
                System.out.println(paymentStatuss+" _ "+messages);
                redirectUrl = String.format(
                        "https://hotelstar.vercel.app/client/booking-room?status=%s&message=%s",
                        URLEncoder.encode(paymentStatuss, "UTF-8"),
                        URLEncoder.encode(messages, "UTF-8")
                );
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Redirecting to URL: " + redirectUrl);

            // Redirect the user to the frontend
            try {
                response.sendRedirect(redirectUrl);
            } catch (IOException e) {

                throw new RuntimeException(e);
            }
        }else{
            if(booking.getDiscountName()!=null && booking.getDiscountPercent()!=null){
                System.out.println("mã này đã hồi phục");
                Discount discount = (discountRepository.findByDiscountName(booking.getDiscountName())!=null)?discountRepository.findByDiscountName(booking.getDiscountName()):null;
                DiscountAccount discountAccount= discountAccountRepositorys.findByDiscountAndAccount(discount.getId(),booking.getAccount().getId());
                discountAccount.setStatusDa(false);
                discountAccountRepositorys.save(discountAccount);
            }
            StatusBooking statusBooking1 = statusBookingRepository.findById(6).get();
            booking.setStatus(statusBooking1);
            booking.setStatusPayment(false);
            bookingRepository.save(booking);
            Boolean flag=bookingService.deleteBookingAndRelatedRooms(booking);
            String paymentStatuss = "error";
            String messages = "Thanh toán thất bại";
            String redirectUrl = null;
            redirectUrl = String.format(
                    "https://hotelstar.vercel.app/client/booking-room?status=%s&message=%s",
                    URLEncoder.encode(paymentStatuss, "UTF-8"),
                    URLEncoder.encode(messages, "UTF-8")
            );

            try {
                response.sendRedirect(redirectUrl);
            } catch (IOException e) {

                throw new RuntimeException(e);
            }
        }
    }

}


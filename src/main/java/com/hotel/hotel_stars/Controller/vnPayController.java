package com.hotel.hotel_stars.Controller;

import com.hotel.hotel_stars.Entity.*;
import com.hotel.hotel_stars.Repository.BookingRepository;
import com.hotel.hotel_stars.Repository.DiscountAccountRepository;
import com.hotel.hotel_stars.Repository.DiscountRepository;
import com.hotel.hotel_stars.Repository.StatusBookingRepository;
import com.hotel.hotel_stars.Service.BookingService;
import com.hotel.hotel_stars.Service.VNPayService;
import com.hotel.hotel_stars.utils.SessionService;
import com.hotel.hotel_stars.utils.paramService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@CrossOrigin("*")
public class vnPayController {
    @Autowired
    VNPayService vnPayService;
    @GetMapping("/vnpay-payment")
    public void handleVNPayPayment(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
        try {
            vnPayService.HandleVNPayCallbacks(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                response.sendRedirect("https://hotelstar.vercel.app/client/booking-room?status=error&message=Lỗi hệ thống");
            } catch (IOException ioException) {
                throw new RuntimeException(ioException);
            }
        }
    }
}
package com.hotel.hotel_stars.Controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hotel.hotel_stars.DTO.ApiResponseDto;
import com.hotel.hotel_stars.DTO.BookingStatisticsDTO;
import com.hotel.hotel_stars.DTO.Select.RoomUsageDTO;
import com.hotel.hotel_stars.DTO.selectDTO.ResponseBookingDTO;
import com.hotel.hotel_stars.Models.DeleteBookingModel;
import com.hotel.hotel_stars.DTO.selectDTO.BookingHistoryDTOs;
import com.hotel.hotel_stars.Service.VNPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hotel.hotel_stars.Config.JwtService;
import com.hotel.hotel_stars.DTO.StatusResponseDto;
import com.hotel.hotel_stars.DTO.accountHistoryDto;
import com.hotel.hotel_stars.DTO.Select.BookingDetailDTO;
import com.hotel.hotel_stars.DTO.Select.PaymentInfoDTO;
import com.hotel.hotel_stars.Exception.ErrorsService;
import com.hotel.hotel_stars.Models.bookingModel;
import com.hotel.hotel_stars.Models.bookingModelNew;
import com.hotel.hotel_stars.Models.bookingRoomModel;
import com.hotel.hotel_stars.Repository.BookingRepository;
import com.hotel.hotel_stars.Repository.BookingRoomRepository;
import com.hotel.hotel_stars.Repository.StatusBookingRepository;
import com.hotel.hotel_stars.Service.BookingService;
import com.hotel.hotel_stars.utils.SessionService;
import com.hotel.hotel_stars.utils.paramService;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/booking")
public class BookingController {
    @Autowired
    private BookingService bookingService;
    @Lazy
    @Autowired
    private ErrorsService errorsServices;
    @Autowired
    private BookingRoomRepository bookingRoomRepository;
    @Autowired
    private StatusBookingRepository statusBookingRepository;
    @Autowired
    private paramService paramServices;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    VNPayService vnPayService;
    @Autowired
    SessionService sessionService;

    // khoi
    @GetMapping("")
    public ResponseEntity<List<accountHistoryDto>> getBookings(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        List<accountHistoryDto> bookings = bookingService.getAllBooking(startDate, endDate);
        return ResponseEntity.ok(bookings);
    }

    @PutMapping("/update-checkIn/{id}")
    public ResponseEntity<?> updateCheckIn(@PathVariable("id") Integer id,
            @RequestParam("roomId") List<Integer> roomId,
            @RequestBody List<bookingRoomModel> model) {
        Map<String, String> response = new HashMap<String, String>();
        boolean update = bookingService.updateStatusCheckInBooking(id, roomId, model);

        if (update == true) {
            response = paramServices.messageSuccessApi(201, "success",
                    "Cập nhật trạng thái thành công");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            response = paramServices.messageSuccessApi(400, "error", "Cập nhật trạng thái thất bại");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<?> getById(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(bookingService.getByIdBooking(id));
    }

    @GetMapping("/account/payment-info/{id}")
    public ResponseEntity<List<PaymentInfoDTO>> getBookingPaymentInfo(@PathVariable Integer id) {
        return ResponseEntity.ok(bookingService.getPaymentInfoByAccountId(id));
    }

    @PostMapping("/booking-offline")
    public ResponseEntity<?> postBookingOffline(@Valid @RequestBody bookingModel bookingModels) {
        Map<String, String> response = new HashMap<String, String>();
        errorsServices.errorBooking(bookingModels);
        accountHistoryDto flag = bookingService.addBookingOffline(bookingModels);
        if (flag != null) {
            return ResponseEntity.ok(flag);
        } else {
            return ResponseEntity.ok(flag);
        }
    }

    @GetMapping("/confirmBooking")
    public ResponseEntity<?> updateBooking(@RequestParam("token") String token) {
        // nghia, hàm này được push sáng ngày 18 tháng 12 năm 2024
        try {
            Integer id = jwtService.extractBookingId(token);
            return ResponseEntity
                    .ok(bookingService.confirmBookings(id));
        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Token đã hết hạn. Vui lòng liên lạc qua số điện thoại 1900 6522");
        } catch (Exception e) {
            // Xử lý các ngoại lệ khác nếu cần
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Đã có lỗi xảy ra.");
        }
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<BookingDetailDTO>> getBookingDetails(@PathVariable Integer accountId) {
        return ResponseEntity.ok(bookingService.getBookingDetailsByAccountId(accountId));
    }

    @GetMapping("/accountId/{id}")
    public ResponseEntity<?> getBookingByAccount(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(bookingService.getListByAccountId(id));
    }

    @GetMapping("/downloadPdf")
    public ResponseEntity<?> downloadPdf(@RequestParam String id) {
        // nghia, hàm này được push sáng ngày 18 tháng 12 năm 2024
        try {
           return bookingService.downloadFilePFD(Integer.parseInt(id));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Có lỗi xảy ra khi tạo PDF.");
        }
    }

    @PostMapping("/sendBooking")
    public ResponseEntity<?> postBooking(@Valid @RequestBody bookingModel bookingModels, HttpServletRequest request) {
        // nghia, hàm này được push sáng ngày 18 tháng 12 năm 2024

        ResponseBookingDTO responseBookingDTO = bookingService.addBookingOnline(bookingModels,request);
        return ResponseEntity.status(responseBookingDTO.getCode()).body(responseBookingDTO);

    }

    @PutMapping("/update-status/{id}/{idStatus}")
    public ResponseEntity<?> updateStatus(@PathVariable("id") Integer idBooking,
            @PathVariable("idStatus") Integer idStatus, @RequestBody bookingModelNew bookingModel) {
        // Gọi phương thức updateStatusBooking từ service
        Map<String, String> response = new HashMap<String, String>();
        boolean update = bookingService.updateStatusBooking(idBooking, idStatus, bookingModel);

        if (update == true) {
            response = paramServices.messageSuccessApi(201, "success",
                    "Cập nhật trạng thái thành công");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            response = paramServices.messageSuccessApi(400, "error", "Cập nhật trạng thái thất bại");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/reservation")
    public ResponseEntity<List<BookingStatisticsDTO>> getBookingStatistics(
            @RequestParam("startDate") LocalDate startDate, @RequestParam("endDate") LocalDate endDate) {
        List<BookingStatisticsDTO> statistics = bookingService.getStatistics(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/by-start-date-with-invoice")
    public ResponseEntity<?> getBookingsByStartAtWithInvoice(@RequestParam("date") LocalDate date) {
        return ResponseEntity.ok(bookingService.getBookingsByStartAtWithInvoice(date));
    }

    @GetMapping("/booking-history-account")
    public ResponseEntity<?> getBookings(@RequestParam Integer accountId) {
        List<BookingHistoryDTOs> bookings = bookingService.getBookingsByAccountId(accountId);
        return ResponseEntity.ok(bookings);
    }



    @GetMapping("/booking-by-room/{id}")
    public ResponseEntity<?> getBookingByRoom(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(bookingService.getBookingByRoom(id));
    }

    @PutMapping("/cancel-booking/{id}")
    public StatusResponseDto cancelBooking(@PathVariable("id") Integer id,
            @RequestParam("descriptions") String descriptions) {
        boolean flag = bookingService.cancelBooking(id, descriptions);
        if (flag) {
            return new StatusResponseDto("200", "success", "Hủy đặt phòng thành công");
        } else {
            return new StatusResponseDto("400", "error", "Hủy đặt phòng thất bại");
        }
    }

    @PostMapping("/booking-maintenance")
    public ResponseEntity<?> postMaintenanceSchedule(@Valid @RequestBody bookingModel bookingModels,
            @RequestParam("userName") String username) {
        System.out.println(bookingModels.getUserName());
        Map<String, String> response = new HashMap<String, String>();
        System.out.println("2");
        StatusResponseDto errorBookings = errorsServices.errorBookings(bookingModels);
        System.out.println("3");
        if (errorBookings != null) {
            return ResponseEntity.ok(errorBookings);
        }
        System.out.println("4");
        Boolean flag = bookingService.createMaintenanceSchedule(bookingModels, username);
        if (flag == true) {
            response = paramServices.messageSuccessApi(201, "success", "Tạo lịch thành công");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            response = paramServices.messageSuccessApi(400, "error", "Tạo lịch thất bại");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/room-usage")
    public ResponseEntity<?> getRoomUsage(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        if ("null".equals(startDate)) {
            startDate = null;
        }
        if ("null".equals(endDate)) {
            endDate = null;
        }

        // Gọi service để lấy thông tin về công suất phòng
        RoomUsageDTO roomUsage = bookingService.getRoomUsage(startDate, endDate);

        // Trả về kết quả
        return ResponseEntity.ok(roomUsage);
    }

    @PutMapping("/delete-booking")
    public ResponseEntity<?> deleteBooking(@Valid @RequestBody DeleteBookingModel bookingModels) {
        ApiResponseDto responseDto = bookingService.deleteBookings(bookingModels);
        return ResponseEntity.status(responseDto.getCode()).body(responseDto);
    }
}

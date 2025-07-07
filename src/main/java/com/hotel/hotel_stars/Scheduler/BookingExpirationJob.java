package com.hotel.hotel_stars.Scheduler;


import com.hotel.hotel_stars.Entity.Booking;
import com.hotel.hotel_stars.Entity.StatusBooking;
import com.hotel.hotel_stars.Repository.BookingRepository;
import com.hotel.hotel_stars.Repository.StatusBookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class BookingExpirationJob {
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    StatusBookingRepository statusBookingRepository;
    @Scheduled(fixedRate = 300000) // 2 phút = 120000 or 15 phút = 900000 or 5 phút = 300000 (120000 ms) â
    public void cancelExpiredBookings() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(15);
        List<Booking> expiredBookings = bookingRepository.findExpiredBookings(threshold);

        if (expiredBookings.isEmpty()) return;

        StatusBooking cancelledStatus = new StatusBooking();
        cancelledStatus.setId(6); // Đã hủy

        for (Booking booking : expiredBookings) {
            booking.setDescriptions("Hủy do chưa xác nhận!");
            booking.setStatus(cancelledStatus);
            System.out.println("Auto-cancel booking ID: " + booking.getId());
        }

        bookingRepository.saveAll(expiredBookings);
    }
}

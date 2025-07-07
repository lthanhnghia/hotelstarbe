package com.hotel.hotel_stars.DTO.selectDTO;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseBookingDTO {
    int code;
    String message;
    String vnPayURL;
    String status;
}

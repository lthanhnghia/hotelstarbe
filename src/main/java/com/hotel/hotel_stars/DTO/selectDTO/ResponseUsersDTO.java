package com.hotel.hotel_stars.DTO.selectDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseUsersDTO {
    int code;
    String message;
    String token;
    String status;
}

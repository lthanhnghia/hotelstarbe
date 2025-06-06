package com.hotel.hotel_stars.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelInfoDTO {
    private String hotelInfo;
    private String roomTypes;
    private String roomAmenities;
    private String roomServices;
    private String hotelServices;
    private String hotelAmenities;
}

package com.hotel.hotel_stars.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelRoomDTO {
    private List<Integer> listRoomId;
    private List<String> listRoomName;
    private Integer roomTypeId;
    private String roomTypeName;
    private Double priceTypeRoom;
    private Double acreage;
    private Integer guestLimits;
    private List<String> amenitiesList;
    private Double estCost;
    private List<String> listImages;
    private String describe;
    private List<String> bedNameList;
    private Long availableRoomCount;
}

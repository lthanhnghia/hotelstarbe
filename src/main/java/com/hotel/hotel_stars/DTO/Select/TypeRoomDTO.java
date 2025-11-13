package com.hotel.hotel_stars.DTO.Select;

import com.hotel.hotel_stars.Entity.TypeRoom;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TypeRoomDTO {
    private Integer id;
    private String typeRoomName;
    private Double price;
    public TypeRoomDTO(TypeRoom typeRoom) {
        this.id = typeRoom.getId();
        this.typeRoomName = typeRoom.getTypeRoomName();
        this.price = typeRoom.getPrice();
    }
}

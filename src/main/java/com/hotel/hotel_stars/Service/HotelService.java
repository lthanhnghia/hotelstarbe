package com.hotel.hotel_stars.Service;

import com.hotel.hotel_stars.DTO.HotelDto;
import com.hotel.hotel_stars.DTO.HotelInfoDTO;
import com.hotel.hotel_stars.DTO.HotelRoomDTO;
import com.hotel.hotel_stars.DTO.StatusResponseDto;
import com.hotel.hotel_stars.Entity.Hotel;
import com.hotel.hotel_stars.Models.HotelModel;
import com.hotel.hotel_stars.Repository.HotelRepository;
import com.hotel.hotel_stars.utils.paramService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class HotelService {
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    HotelRepository hotelRepository;
    @Autowired
    paramService paramServices;
    public HotelDto convertHotelDto(Hotel hotel) {
        return new HotelDto(hotel.getId(), hotel.getHotelName(), hotel.getDescriptions(),
                hotel.getProvince(), hotel.getDistrict(), hotel.getWard(), hotel.getAddress(), hotel.getHotelPhone());
    }

    public HotelDto getAllHotels() {
        List<Hotel> hotels = hotelRepository.findAll();
        return hotels.stream()
                .findFirst() // Lấy khách sạn đầu tiên
                .map(this::convertHotelDto) // Chuyển đổi thành HotelDto
                .orElse(null); // Trả về null nếu không có khách sạn
    }
    public HotelDto getHotel(){
        Optional<Hotel> hotel = hotelRepository.findById(1);
        return convertHotelDto(hotel.get());
    }
    public StatusResponseDto updateHotel(HotelModel hotelModel) {
        Optional<Hotel> optionalHotel = hotelRepository.findById(hotelModel.getId());

        if (!optionalHotel.isPresent()) {
            // Return 404 Not Found response details
            return new StatusResponseDto("404", "Not Found", "Khách sạn không tồn tại");
        }

        Hotel hotel = optionalHotel.get();

        try {
            // Update hotel details
            hotel.setHotelName(hotelModel.getHotelName());
            hotel.setDescriptions(hotelModel.getDescriptions());
            hotel.setProvince(hotelModel.getProvince());
            hotel.setDistrict(hotelModel.getDistrict());
            hotel.setWard(hotelModel.getWard());
            hotel.setAddress(hotelModel.getAddress());

            // Save changes
            hotelRepository.save(hotel);

            // Return 200 OK response details
            return new StatusResponseDto("200", "Success", "Cập nhật dữ liệu thành công");
        } catch (Exception e) {
            e.printStackTrace();
            // Return 500 Internal Server Error response details
            return new StatusResponseDto("500", "Internal Server Error", "Đã xảy ra lỗi: " + e.getMessage());
        }
    }
    public HotelInfoDTO getHotelFullInfo() {
        Object[] result = (Object[]) hotelRepository.getHotelFullInfoRaw();

        return new HotelInfoDTO(
                (String) result[0], // hotelInfo
                (String) result[1], // roomTypes
                (String) result[2], // roomAmenities
                (String) result[3], // roomServices
                (String) result[4], // hotelServices
                (String) result[5]  // hotelAmenities
        );
    }
    public List<HotelRoomDTO> getHotelRoom(String startDate, String endDate, Integer guestLimit, Integer typeRoomID) {
        LocalDate startDates = paramServices.convertStringToLocalDate(startDate);
        LocalDate endDates = paramServices.convertStringToLocalDate(endDate);

        // Mặc định guestLimit = 1 nếu null
        int guests = (guestLimit == null || guestLimit < 1) ? 1 : guestLimit;

        List<Object[]> results = hotelRepository.findAvailableRoomsNoPagination(
                startDates, endDates, guests, typeRoomID);

        return results.stream().map(resultsRow -> {
            String roomId = (String) resultsRow[0];
            List<Integer> listRoomId = Arrays.stream(roomId.split(","))
                    .map(Integer::valueOf)
                    .toList();

            String roomName = (String) resultsRow[1];
            List<String> listRoomName = Arrays.stream(roomName.split(","))
                    .map(String::valueOf)
                    .toList();

            Integer roomTypeId = (Integer) resultsRow[2];
            String roomTypeName = (String) resultsRow[3];

            Double priceTypeRoom = (Double) resultsRow[4];
            Double acreage = (Double) resultsRow[5];
            Integer guestLimits = (Integer) resultsRow[6];

            String amenitiesTypeRoomDetails = (String) resultsRow[7];
            List<String> amenitiesList = Arrays.stream(amenitiesTypeRoomDetails.split(","))
                    .map(String::trim)
                    .toList();

            Double estCost = (Double) resultsRow[8];

            String imagesString = (String) resultsRow[9];
            List<String> listImages = Arrays.stream(imagesString.split(","))
                    .map(String::trim)
                    .toList();

            String describe = (String) resultsRow[10];

            String bedName = (String) resultsRow[11];
            List<String> bedNameList = Arrays.stream(bedName.split(","))
                    .map(String::trim)
                    .toList();

            // Vị trí trường availableRoomCount trong kết quả query (ví dụ ở index 12)
            Long availableRoomCount = ((Number) resultsRow[12]).longValue();

            return new HotelRoomDTO(
                    listRoomId, listRoomName, roomTypeId, roomTypeName, priceTypeRoom, acreage,
                    guestLimits, amenitiesList, estCost, listImages, describe, bedNameList, availableRoomCount);
        }).toList();
    }
}

package com.hotel.hotel_stars.Repository;

import com.hotel.hotel_stars.Entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel, Integer> {
    @Query(value = """
        SELECT
            (
                SELECT GROUP_CONCAT(
                    CONCAT(
                        'Tên khách sạn: ', hotel_name, ', ',
                        'Địa chỉ: ', address, ', ',
                        'Phường: ', ward, ', ',
                        'Quận/Huyện: ', district, ', ',
                        'Tỉnh/Thành: ', province, ', ',
                        'Mô tả: ', descriptions, ', ',
                        'Số điện thoại: ', hotel_phone
                    ) SEPARATOR '\\n\\n'
                )
                FROM hotel
            ) AS hotelInfo,

            (
                SELECT GROUP_CONCAT(
                    CONCAT(
                        'Tên phòng: ', tr.type_room_name,
                        ', Giá: ', tr.price,
                        ', Sức chứa: ', tr.guest_limit, ' khách'
                    ) SEPARATOR ' | '
                )
                FROM type_room tr
            ) AS roomTypes,

            CONCAT('Tiện nghi phòng: ', IFNULL((
                SELECT GROUP_CONCAT(atr.amenities_type_room_name SEPARATOR ', ')
                FROM amenities_type_room atr
            ), 'Không có')) AS roomAmenities,

            CONCAT('Dịch vụ phòng: ', IFNULL((
                SELECT GROUP_CONCAT(sr.service_room_name SEPARATOR ', ')
                FROM service_room sr
            ), 'Không có')) AS roomServices,

            CONCAT('Dịch vụ khách sạn: ', IFNULL((
                SELECT GROUP_CONCAT(sh.service_hotel_name SEPARATOR ', ')
                FROM service_hotel sh
            ), 'Không có')) AS hotelServices,

            CONCAT('Tiện nghi khách sạn: ', IFNULL((
                SELECT GROUP_CONCAT(ah.amenities_hotel_name SEPARATOR ', ')
                FROM amenities_hotel ah
            ), 'Không có')) AS hotelAmenities
        """, nativeQuery = true)
    Object getHotelFullInfoRaw();


    @Query(value = """
    SELECT
        GROUP_CONCAT(DISTINCT r.id ORDER BY r.id) AS roomId,
        GROUP_CONCAT(DISTINCT r.room_name ORDER BY r.room_name) AS roomNames,
        tr.id AS typeroomId,
        tr.type_room_name,
        tr.price,
        tr.acreage,
        tr.guest_limit,
        GROUP_CONCAT(DISTINCT CONCAT(atr.amenities_type_room_name) SEPARATOR ', ') AS amenitiesTypeRoomDetails,
        (TIMESTAMPDIFF(DAY, :startDate, :endDate) * tr.price) AS estCost,
        GROUP_CONCAT(DISTINCT tpi.image_name) AS image_name,
        tr.describes,
        type_bed.bed_name,
        COUNT(DISTINCT r.id) AS availableRoomCount
    FROM
        type_room tr
    JOIN
        room r ON tr.id = r.type_room_id
    LEFT JOIN
        booking_room br ON br.room_id = r.id
    LEFT JOIN
        booking b ON br.booking_id = b.id
        AND (
            :startDate <= DATE(b.end_at)
            AND :endDate >= DATE(b.start_at)
        )
    JOIN
        type_room_amenities_type_room trat ON tr.id = trat.type_room_id
    JOIN
        amenities_type_room atr ON trat.amenities_type_room_id = atr.id
    JOIN
        type_room_image tpi ON tpi.type_room_id = tr.id
    JOIN
        type_bed ON tr.type_bed_id = type_bed.id
    WHERE
        NOT EXISTS (
            SELECT 1
            FROM booking_room br_inner
            JOIN booking b_inner ON br_inner.booking_id = b_inner.id
            WHERE br_inner.room_id = r.id
            AND (
                DATE(b_inner.start_at) <= :endDate
                AND DATE(b_inner.end_at) >= :startDate
            ) AND b_inner.status_id NOT IN (6)
        )
        AND (:typeRoomID IS NULL OR tr.id = :typeRoomID)
        AND tr.guest_limit >= COALESCE(:guestLimit, 1)
    GROUP BY
        tr.id
    ORDER BY
        (CASE
            WHEN tr.guest_limit = COALESCE(:guestLimit, 1) THEN 1
            WHEN tr.guest_limit > COALESCE(:guestLimit, 1) THEN 2
            WHEN tr.guest_limit < COALESCE(:guestLimit, 1) THEN 3
        END),
        (CASE
            WHEN tr.guest_limit > COALESCE(:guestLimit, 1) THEN tr.guest_limit
            WHEN tr.guest_limit < COALESCE(:guestLimit, 1) THEN -tr.guest_limit
            ELSE 0
        END)
    """, nativeQuery = true)
    List<Object[]> findAvailableRoomsNoPagination(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("guestLimit") Integer guestLimit,
            @Param("typeRoomID") Integer typeRoomID);
}
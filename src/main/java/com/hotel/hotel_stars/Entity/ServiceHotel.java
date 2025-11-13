package com.hotel.hotel_stars.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "service_hotel", schema = "hotel_manager")
public class ServiceHotel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "service_hotel_name")
    private String serviceHotelName;

    @Column(name = "price")
    private Double price;

    @Column(name = "image")
    private String imageName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id")
    @JsonIgnore
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Hotel hotel;

    @OneToMany(mappedBy = "serviceHotel")
    @JsonIgnore
    private List<BookingRoomServiceHotel> serviceHotelList;
}
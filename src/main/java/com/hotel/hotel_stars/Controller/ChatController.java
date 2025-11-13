package com.hotel.hotel_stars.Controller;

import com.hotel.hotel_stars.DTO.HotelRoomDTO;
import com.hotel.hotel_stars.DTO.Select.TypeRoomDTO;
import com.hotel.hotel_stars.DTO.TypeRoomDto;
import com.hotel.hotel_stars.Entity.TypeRoom;
import com.hotel.hotel_stars.Repository.TypeRoomRepository;
import com.hotel.hotel_stars.Service.AccountService;
import com.hotel.hotel_stars.Service.ChatService;
import com.hotel.hotel_stars.Service.HotelService;
import com.hotel.hotel_stars.Service.TypeRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ChatController {
    @Autowired
    ChatService aiService;
    @Autowired
    AccountService aService;
    @Autowired
    TypeRoomRepository tRepo;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody String prompt) {

        return ResponseEntity.status(200).body(aiService.chat(prompt));
    }
    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

}

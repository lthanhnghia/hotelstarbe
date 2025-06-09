package com.hotel.hotel_stars.Controller;

import com.hotel.hotel_stars.DTO.HotelRoomDTO;
import com.hotel.hotel_stars.Service.ChatService;
import com.hotel.hotel_stars.Service.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api")
public class ChatController {
    @Autowired
    ChatService aiService;
    @PostMapping("/chat")
    public String chat(@RequestBody String prompt) {
        return this.aiService.chat(prompt);
    }
    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
    @GetMapping("/demo")
    public String keepAlive() {
        return "demo";
    }

}

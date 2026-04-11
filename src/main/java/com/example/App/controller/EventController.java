package com.example.App.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.App.dto.ApiResponse;
import com.example.App.entity.Event;
import com.example.App.entity.Seat;
import com.example.App.repository.EventRepository;
import com.example.App.repository.SeatRepository;
import com.example.App.security.BookingService;
import com.example.App.service.WaitingRoomService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    private final BookingService bookingService;
    private final WaitingRoomService waitingRoomService;


    // Định nghĩa một cái "khuôn" nhẹ hơn ngay trong Controller
public record EventDTO(
    Long id, 
    String title, 
    String description, 
    LocalDateTime startTime, 
    LocalDateTime endTime,
    String venueName
) {}

@GetMapping("")
public ResponseEntity<ApiResponse<List<EventDTO>>> getAllEvents() {
    List<Event> events = eventRepository.findAll();
    
    // Chuyển từ Entity khổng lồ sang DTO siêu nhẹ
    List<EventDTO> eventDTOs = events.stream()
        .map(e -> new EventDTO(
            e.getId(), 
            e.getTitle(), 
            e.getDescription(), 
            e.getStartTime(), 
            e.getEndTime(),
            e.getVenue() != null ? e.getVenue().getName() : "N/A"
        ))
        .toList();

    return ResponseEntity.ok(new ApiResponse<>(200, "Thành công", eventDTOs));
}

    // @GetMapping("/{id}/seats")
    // public ResponseEntity<ApiResponse<List<Seat>>> getEventSeats(@PathVariable Long id) {
    //     List<Seat> seats = seatRepository.findByEventId(id);
    
    // // Chuyển đổi sang Map hoặc DTO để giảm tải dữ liệu
    // List<Map<String, Object>> lightSeats = seats.stream().map(seat -> {
    //     Map<String, Object> map = new HashMap<>();
    //     map.set("id", seat.getId());
    //     map.set("row", seat.getRowName());
    //     map.set("number", seat.getSeatNumber());
    //     map.set("status", seat.getStatus());
    //     map.set("price", seat.getPrice());
    //     return map;
    // }).toList();

    // return ResponseEntity.ok(new ApiResponse<>(200, "Thành công", lightSeats));
    // }


    @GetMapping("/{id}/seats")
public ResponseEntity<ApiResponse<List<SeatDTO>>> getEventSeats(@PathVariable Long id) {
    List<Seat> seats = seatRepository.findByEventId(id);
    
    List<SeatDTO> lightSeats = seats.stream()
        .map(seat -> new SeatDTO(
            seat.getId(), 
            seat.getRowName(), 
            seat.getSeatNumber(), 
            seat.getStatus(), 
            seat.getPrice()
        ))
        .toList();

    return ResponseEntity.ok(new ApiResponse<>(200, "Thành công", lightSeats));
}


@GetMapping("/{id}/queue-status")
public ResponseEntity<ApiResponse<Long>> getQueueStatus(Authentication auth) {
    Long position = waitingRoomService.getQueuePosition(auth.getName());
    return ResponseEntity.ok(new ApiResponse<>(200, "Vị trí hàng chờ", position));
}

@PostMapping("/{id}/join-queue")
public ResponseEntity<ApiResponse<String>> joinQueue(Authentication auth) {
    waitingRoomService.joinQueue(auth.getName());
    return ResponseEntity.ok(new ApiResponse<>(200, "Đã gia nhập hàng chờ", null));
}

@PostMapping("/hold")
public ResponseEntity<ApiResponse<String>> holdSeats(
        @RequestBody List<Long> seatIds, 
        Authentication authentication) {
    
    if (authentication == null) {
        return ResponseEntity.status(401).body(new ApiResponse<>(401, "Bạn cần đăng nhập", null));
    }
    
    try {
        // SỬA DÒNG NÀY: Gọi processBooking thay vì holdSeats
        String orderId = bookingService.processBooking(seatIds, authentication.getName());
        
        return ResponseEntity.ok(new ApiResponse<>(200, 
            "Giữ ghế thành công! Mã đơn: " + orderId, null));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(new ApiResponse<>(400, e.getMessage(), null));
    }
}



public record SeatDTO(
    Long id, 
    String row, 
    String number, 
    Seat.SeatStatus status, 
    Double price
) {}


}
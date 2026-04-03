package com.example.App.config;

import com.example.App.entity.Event;
import com.example.App.entity.Seat;
import com.example.App.entity.Venue;
import com.example.App.repository.EventRepository;
import com.example.App.repository.SeatRepository;
import com.example.App.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final VenueRepository venueRepository;
    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (eventRepository.count() > 0) return; // Nếu có dữ liệu rồi thì thôi

        // 1. Tạo Địa điểm (Venue)
        Venue venue = new Venue();
        venue.setName("Trung tâm Hội nghị Quốc gia");
        venue.setAddress("Phạm Hùng, Hà Nội");
        venue.setCapacity(2000);
        venueRepository.save(venue);

        // 2. Tạo Sự kiện (Event)
        Event event = new Event();
        event.setTitle("CONCERT: ANH TRAI " + LocalDateTime.now().getYear());
        event.setDescription("Đêm nhạc hội tụ những ngôi sao hàng đầu Việt Nam");
        event.setStartTime(LocalDateTime.now().plusDays(30));
        event.setEndTime(LocalDateTime.now().plusDays(30).plusHours(4));
        event.setStatus(Event.EventStatus.UPCOMING);
        event.setVenue(venue);
        eventRepository.save(event);

        // 3. Tạo 1000 cái ghế (20 hàng x 50 ghế)
        List<Seat> seats = new ArrayList<>();
        for (int row = 1; row <= 20; row++) {
            String rowName = String.valueOf((char) ('A' + row - 1));
            for (int num = 1; num <= 50; num++) {
                Seat seat = new Seat();
                seat.setEvent(event);
                seat.setRowName(rowName);
                seat.setSeatNumber(String.valueOf(num));
                seat.setPrice(500000.0); // 500k/vé
                seat.setStatus(Seat.SeatStatus.AVAILABLE);
                seats.add(seat);
            }
        }
        
        // Lưu hàng loạt (Batch Save) để tăng tốc độ
        seatRepository.saveAll(seats);

        System.out.println(">>> ĐÃ TẠO XONG 1000 GHẾ CHO SỰ KIỆN ID: " + event.getId());
    }
}
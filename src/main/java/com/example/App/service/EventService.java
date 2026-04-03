package com.example.App.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.App.entity.Event;
import com.example.App.entity.Seat;
import com.example.App.repository.EventRepository;
import com.example.App.repository.SeatRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;

    @Transactional
    public Event createEventWithSeats(Event event, int rows, int seatsPerRow) {
        Event savedEvent = eventRepository.save(event);
        
        List<Seat> seats = new ArrayList<>();
        for (int i = 1; i <= rows; i++) {
            for (int j = 1; j <= seatsPerRow; j++) {
                Seat seat = new Seat();
                seat.setEvent(savedEvent);
                seat.setRowName(String.valueOf((char) ('A' + i - 1))); // Hàng A, B, C...
                seat.setSeatNumber(String.valueOf(j));
                seat.setPrice(event.getVenue().getCapacity() > 100 ? 500000.0 : 200000.0);
                seat.setStatus(Seat.SeatStatus.AVAILABLE);
                seats.add(seat);
            }
        }
        seatRepository.saveAll(seats); // Lưu hàng loạt để tối ưu hiệu năng
        return savedEvent;
    }
}

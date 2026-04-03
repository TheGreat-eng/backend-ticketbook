package com.example.App.security;

import com.example.App.dto.BookingEvent;
import com.example.App.entity.Booking;
import com.example.App.entity.Seat;
import com.example.App.repository.BookingRepository;
import com.example.App.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;

    @KafkaListener(topics = "booking_topic", groupId = "booking_group_v2")
@Transactional
public void listenBookingEvents(BookingEvent event) {
    // LOG NÀY PHẢI HIỆN RA MÀU TRẮNG TRÊN CONSOLE
    System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    System.out.println("KAFKA CONSUMER NHẬN ĐƯỢC: " + event.getOrderId());
    System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

    try {
        // Kiểm tra xem Idempotency có chặn nhầm không
        if (bookingRepository.existsByOrderId(event.getOrderId())) {
            log.warn("Đơn hàng này đã xử lý rồi: {}", event.getOrderId());
            return;
        }

        Booking booking = new Booking();
        booking.setOrderId(event.getOrderId());
        booking.setUserEmail(event.getUserEmail());
        booking.setTotalAmount(event.getTotalAmount());
        booking.setBookingTime(LocalDateTime.now());
        booking.setStatus(Booking.BookingStatus.PAID);
        
        bookingRepository.save(booking);
        log.info("Ghi DB thành công đơn: {}", event.getOrderId());

    } catch (Exception e) {
        log.error("LỖI KHI XỬ LÝ CONSUMER: ", e);
    }
}
}
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

    @KafkaListener(topics = "booking_topic", groupId = "booking_group")
    @Transactional
    public void listenBookingEvents(BookingEvent event) {
        log.info("<<< Nhận được đơn hàng {} từ Kafka. Bắt đầu xử lý ghi DB...", event.getOrderId());

        try {
            // 1. CHỐNG TRÙNG LẶP (Idempotency): Kiểm tra đơn này đã xử lý chưa
            if (bookingRepository.existsByOrderId(event.getOrderId())) {
                log.warn("Đơn hàng {} đã được xử lý trước đó, bỏ qua.", event.getOrderId());
                return;
            }

            // 2. Tạo bản ghi Booking chính thức
            Booking booking = new Booking();
            booking.setOrderId(event.getOrderId());
            booking.setUserEmail(event.getUserEmail());
            booking.setTotalAmount(event.getTotalAmount());
            booking.setBookingTime(LocalDateTime.now());
            booking.setStatus(Booking.BookingStatus.PAID); // Giả định thanh toán thành công
            bookingRepository.save(booking);

            // 3. Cập nhật trạng thái Ghế từ HOLDING -> SOLD
            List<Seat> seats = seatRepository.findAllById(event.getSeatIds());
            for (Seat seat : seats) {
                if (seat.getStatus() == Seat.SeatStatus.HOLDING) {
                    seat.setStatus(Seat.SeatStatus.SOLD);
                }
            }
            seatRepository.saveAll(seats);

            log.info("√√√ Đơn hàng {} hoàn tất. Ghế đã được bán!", event.getOrderId());

        } catch (Exception e) {
            log.error("XXX Lỗi khi xử lý đơn hàng Kafka: {}", e.getMessage());
            // Ở Phase 5 chúng ta sẽ đẩy tin nhắn lỗi này vào Dead Letter Queue (DLQ)
        }
    }
}
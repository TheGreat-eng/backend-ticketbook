package com.example.App.security;

import com.example.App.dto.BookingEvent;
import com.example.App.entity.Booking;
import com.example.App.entity.Seat;
import com.example.App.repository.BookingRepository;
import com.example.App.repository.SeatRepository;
import com.example.App.config.RabbitMQConfig; // Import cấu hình Rabbit
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate; // Import RabbitTemplate
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final RabbitTemplate rabbitTemplate; // Inject RabbitTemplate để gửi sang Phase 5
    private final SimpMessagingTemplate messagingTemplate; // Thêm cái này




    @KafkaListener(topics = "booking_topic", groupId = "booking_group_v5")
    @Transactional
    public void listenBookingEvents(BookingEvent event) {
        log.info("<<<< [KAFKA] Đang chốt đơn: {}", event.getOrderId());

        try {
            if (bookingRepository.existsByOrderId(event.getOrderId())) {
                log.warn("Đơn hàng này đã xử lý rồi: {}", event.getOrderId());
                return;
            }

            // 1. Ghi nhận hóa đơn vào DB
            Booking booking = new Booking();
            booking.setOrderId(event.getOrderId());
            booking.setUserEmail(event.getUserEmail());
            booking.setTotalAmount(event.getTotalAmount());
            booking.setBookingTime(LocalDateTime.now());
            booking.setStatus(Booking.BookingStatus.PAID);
            bookingRepository.save(booking);
            
            // 2. Chốt trạng thái ghế SOLD
            List<Seat> seats = seatRepository.findAllById(event.getSeatIds());
            for (Seat seat : seats) {
                seat.setStatus(Seat.SeatStatus.SOLD);
            }
            seatRepository.saveAll(seats);

            messagingTemplate.convertAndSend("/topic/seats", event.getSeatIds());

            log.info("📢 Đã phát tín hiệu Real-time cho các ghế: {}", event.getSeatIds());

            // 3. [PHASE 5] Bàn giao cho RabbitMQ để gửi Email & QR Code
            // Lưu ý: Việc này nằm cuối cùng, sau khi DB đã xong xuôi
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE, 
                RabbitMQConfig.NOTIFICATION_ROUTING_KEY, 
                event
            );

            log.info("√√√ Đã chốt vé & đẩy yêu cầu gửi mail sang RabbitMQ cho đơn: {}", event.getOrderId());

        } catch (Exception e) {
            log.error("LỖI KHI XỬ LÝ CONSUMER: ", e);
        }
    }
}
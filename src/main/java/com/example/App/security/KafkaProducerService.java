package com.example.App.security;

import com.example.App.dto.BookingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    // Phải khớp với Generic Type trong KafkaConfig
    private final KafkaTemplate<String, BookingEvent> kafkaTemplate;
    private static final String TOPIC = "booking_topic";

    public void sendBookingEvent(BookingEvent event) {
        log.info(">>> Đẩy đơn hàng {} vào Kafka...", event.getOrderId());
        kafkaTemplate.send(TOPIC, event.getOrderId(), event);
    }
}
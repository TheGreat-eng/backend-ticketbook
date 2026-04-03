package com.example.App.service;

import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisKeyExpiredEvent;
import org.springframework.stereotype.Component;

import com.example.App.entity.Seat;
import com.example.App.repository.SeatRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisExpirationListener {
    private final SeatRepository seatRepository;

    @EventListener
    public void handleRedisKeyExpired(RedisKeyExpiredEvent<String> event) {
        String expiredKey = event.getSource().toString();
        
        if (expiredKey.startsWith("hold:seat:")) {
            Long seatId = Long.parseLong(expiredKey.replace("hold:seat:", ""));
            
            Seat seat = seatRepository.findById(seatId).orElse(null);
            if (seat != null && seat.getStatus() == Seat.SeatStatus.HOLDING) {
                seat.setStatus(Seat.SeatStatus.AVAILABLE);
                seatRepository.save(seat);
                log.info("Ghế {} đã hết hạn giữ chỗ, đã mở lại cho mọi người", seatId);
            }
        }
    }
}

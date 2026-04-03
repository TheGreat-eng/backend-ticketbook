package com.example.App.security;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import com.example.App.dto.BookingEvent;
import com.example.App.entity.Seat;
import com.example.App.repository.SeatRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {
    private final SeatRepository seatRepository;
    private final RedissonClient redissonClient;
    private final KafkaProducerService kafkaProducerService;

@Transactional
    public String processBooking(List<Long> seatIds, String userEmail) {
        // 1. Thực hiện Hold ghế bằng Redis Lock (Phase 3 bạn đã làm cực tốt)
        this.holdSeats(seatIds, userEmail); 

        // 2. Tạo một OrderId tạm thời
        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 3. Đẩy vào Kafka để xử lý thanh toán & tạo vé ở Background
        BookingEvent event = new BookingEvent(
            orderId, 
            userEmail, 
            seatIds, 
            1L, // Tạm thời lấy Event ID = 1
            seatIds.size() * 500000.0, 
            System.currentTimeMillis()
        );
        
        kafkaProducerService.sendBookingEvent(event);

        return orderId; // Trả về mã đơn ngay lập tức cho Frontend
    }





    @Transactional
    public void holdSeats(List<Long> seatIds, String userEmail) {
        for (Long seatId : seatIds) {
            // 1. Tạo Lock cho từng ghế để tránh tranh chấp
            String lockKey = "lock:seat:" + seatId;
            RLock lock = redissonClient.getLock(lockKey);

            try {
                // Thử khóa trong 3 giây, giữ khóa 5 giây để ghi DB
                if (lock.tryLock(3, 5, TimeUnit.SECONDS)) {
                    Seat seat = seatRepository.findById(seatId)
                            .orElseThrow(() -> new RuntimeException("Ghế không tồn tại: " + seatId));

                    if (seat.getStatus() != Seat.SeatStatus.AVAILABLE) {
                        throw new RuntimeException("Ghế " + seat.getRowName() + seat.getSeatNumber() + " đã có người nhanh tay hơn!");
                    }

                    // 2. Cập nhật DB sang trạng thái HOLDING
                    seat.setStatus(Seat.SeatStatus.HOLDING);
                    seatRepository.save(seat);

                    // 3. Đặt "bom hẹn giờ" trên Redis 10 phút để tự động mở khóa ghế
                    String holdKey = "hold:seat:" + seatId;
                    redissonClient.getBucket(holdKey).set(userEmail, 10, TimeUnit.MINUTES);
                    
                } else {
                    throw new RuntimeException("Hệ thống đang bận xử lý ghế này, vui lòng thử lại!");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Lỗi hệ thống");
            } finally {
                if (lock.isHeldByCurrentThread()) lock.unlock();
            }
        }
    }







}
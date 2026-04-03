// FILE: src/main/java/com/example/App/entity/Booking.java
package com.example.App.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderId; // Mã đơn từ Kafka (Dùng để chống trùng lặp - Idempotency)
    private String userEmail;
    private Double totalAmount;
    private LocalDateTime bookingTime;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    public enum BookingStatus {
        PENDING, PAID, CANCELLED
    }
}
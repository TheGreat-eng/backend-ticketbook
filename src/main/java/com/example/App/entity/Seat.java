// FILE: src/main/java/com/example/App/entity/Seat.java
package com.example.App.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "seats")
@Data
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version // Chống Over-booking
    private Long version;

    private String rowName;
    private String seatNumber;
    private Double price;

    @Enumerated(EnumType.STRING)
    private SeatStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    @JsonIgnore
    private Event event; // Hết lỗi đỏ ở đây!

    public enum SeatStatus {
        AVAILABLE, HOLDING, SOLD
    }
}
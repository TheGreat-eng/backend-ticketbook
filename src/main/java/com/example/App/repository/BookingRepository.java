package com.example.App.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.App.entity.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Boolean existsByOrderId(String id);
}

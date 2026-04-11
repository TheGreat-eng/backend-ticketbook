package com.example.App.repository;

import com.example.App.entity.Booking;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Phải dùng đúng tên thuộc tính orderId
    boolean existsByOrderId(String orderId);

List<Booking> findByUserEmail(String userEmail);
}
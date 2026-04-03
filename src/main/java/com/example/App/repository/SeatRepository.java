package com.example.App.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.App.entity.Seat;

public interface SeatRepository extends JpaRepository<Seat, Long>{
    List<Seat> findByEventId(long id);
}

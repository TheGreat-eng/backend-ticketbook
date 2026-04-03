package com.example.App.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.App.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long>{
    
}

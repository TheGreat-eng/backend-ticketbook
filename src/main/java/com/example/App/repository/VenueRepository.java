package com.example.App.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.App.entity.Venue;

public interface VenueRepository extends JpaRepository<Venue, Long>{
    
}

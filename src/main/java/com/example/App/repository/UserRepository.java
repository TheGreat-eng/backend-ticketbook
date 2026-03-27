package com.example.App.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.App.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email); // Phải là findByEmail mới khớp với logic loadUser




    Boolean existsByEmail(String email);

}


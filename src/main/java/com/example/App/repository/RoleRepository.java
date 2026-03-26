package com.example.App.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.App.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long>{
    
}

// FILE: src/main/java/com/example/App/entity/Venue.java
package com.example.App.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "venues")
@Data
public class Venue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address;
    private Integer capacity; // Sức chứa

    @Column(columnDefinition = "TEXT")
    private String mapLayoutJson; // Lưu sơ đồ ghế dạng JSON (cho Phase 2)

    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL)
    private List<Event> events;
}
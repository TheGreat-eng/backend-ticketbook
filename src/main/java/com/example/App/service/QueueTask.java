package com.example.App.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import lombok.RequiredArgsConstructor;

// src/main/java/com/example/App/service/QueueTask.java
@Component
@RequiredArgsConstructor
@Slf4j

public class QueueTask {
    private final WaitingRoomService waitingRoomService;

    @Scheduled(fixedDelay = 5000) 
public void processQueue() {
    log.info("--- ĐANG MỜI NGƯỜI VÀO PHÒNG... ---"); // Thêm log này
    waitingRoomService.allowUsersIn();
}
}

package com.example.App.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

// src/main/java/com/example/App/service/QueueTask.java
@Component
@RequiredArgsConstructor
public class QueueTask {
    private final WaitingRoomService waitingRoomService;

    @Scheduled(fixedDelay = 20000) // 10 giây thả người mới vào 1 lần
    public void processQueue() {
        waitingRoomService.allowUsersIn();
    }
}

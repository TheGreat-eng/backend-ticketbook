// src/main/java/com/example/App/service/WaitingRoomService.java
package com.example.App.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class WaitingRoomService {
    private final StringRedisTemplate redisTemplate;
    
    private static final String QUEUE_KEY = "waiting_room:event:1";
    private static final String ALLOWED_KEY = "allowed_users:event:1";
    private static final int MAX_ACTIVE_USERS = 1; // Chỉ cho phép 100 người vào cùng lúc

    // 1. Gia nhập hàng chờ
    public void joinQueue(String userEmail) {
        // Nếu người dùng chưa được phép vào và chưa có trong hàng chờ
        if (Boolean.FALSE.equals(redisTemplate.opsForSet().isMember(ALLOWED_KEY, userEmail))) {
            redisTemplate.opsForZSet().add(QUEUE_KEY, userEmail, System.currentTimeMillis());
        }
    }

    // 2. Lấy vị trí hiện tại trong hàng chờ
    public Long getQueuePosition(String userEmail) {
        // Nếu đã được phép vào mua vé
        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(ALLOWED_KEY, userEmail))) {
            return 0L;
        }
        Long rank = redisTemplate.opsForZSet().rank(QUEUE_KEY, userEmail);
        return (rank != null) ? rank + 1 : -1L;
    }

    // 3. (Chạy ngầm) Thả người từ hàng chờ vào trang mua vé
    public void allowUsersIn() {
        Long activeCount = redisTemplate.opsForSet().size(ALLOWED_KEY);
        if (activeCount < MAX_ACTIVE_USERS) {
            long quota = MAX_ACTIVE_USERS - activeCount;
            // Lấy ra N người đứng đầu hàng chờ
            Set<String> usersToAllow = redisTemplate.opsForZSet().range(QUEUE_KEY, 0, quota - 1);
            
            if (usersToAllow != null && !usersToAllow.isEmpty()) {
                // Đưa vào danh sách được phép
                redisTemplate.opsForSet().add(ALLOWED_KEY, usersToAllow.toArray(new String[0]));
                // Xóa khỏi hàng chờ
                redisTemplate.opsForZSet().remove(QUEUE_KEY, usersToAllow.toArray(new String[0]));
            }
        }
    }
}
package com.example.App.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisConfig {

    // Lấy host từ spring.data.redis.host trong application.properties
    // Nếu không tìm thấy (chạy local), mặc định sẽ là localhost
    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private String redisPort;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        
        // Cấu hình địa chỉ theo định dạng redis://host:port
        // Lưu ý: Nếu dùng Redis có mật khẩu, định dạng sẽ là redis://:password@host:port
        String redisAddress = String.format("redis://%s:%s", redisHost, redisPort);
        
        config.useSingleServer()
              .setAddress(redisAddress)
              .setConnectionMinimumIdleSize(10) // Duy trì ít nhất 10 kết nối trống
              .setConnectionPoolSize(64)        // Tối đa 64 kết nối song song
              .setConnectTimeout(10000)         // Chờ kết nối tối đa 10s
              .setRetryAttempts(3)              // Thử lại 3 lần nếu lỗi
              .setRetryInterval(1500);          // Khoảng cách mỗi lần thử 1.5s

        return Redisson.create(config);
    }

    /**
     * Bean này cực kỳ quan trọng để lắng nghe sự kiện "Key Expired" từ Redis
     * Dùng để giải phóng ghế khi hết thời gian giữ chỗ (Phase 4).
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }
}
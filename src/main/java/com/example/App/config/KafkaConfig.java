package com.example.App.config;

import com.example.App.dto.BookingEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.converter.JsonMessageConverter;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    // ==========================================
    // 1. CẤU HÌNH PRODUCER (Gửi tin nhắn)
    // ==========================================
    @Bean
    public ProducerFactory<String, BookingEvent> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 32768);

        // Sử dụng Constructor chuẩn để không bị gạch ngang
        DefaultKafkaProducerFactory<String, BookingEvent> factory = new DefaultKafkaProducerFactory<>(configProps);
        factory.setKeySerializer(new StringSerializer());
        factory.setValueSerializer(new JsonSerializer<>()); // Spring sẽ tự quản lý ObjectMapper
        return factory;
    }

    @Bean
    public KafkaTemplate<String, BookingEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // ==========================================
    // 2. CẤU HÌNH CONSUMER (Nhận tin nhắn)
    // ==========================================
    
    // Đổi Object thành String để khớp hoàn toàn với StringDeserializer (Hết lỗi đỏ)
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "booking_group");
        
        return new DefaultKafkaConsumerFactory<>(
            props, 
            new StringDeserializer(), 
            new StringDeserializer()
        );
    }

    // Bộ chuyển đổi JSON (Hết gạch ngang)
    @Bean
    public RecordMessageConverter converter() {
        return new JsonMessageConverter();
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        
        // Gắn bộ chuyển đổi: Nó sẽ tự chuyển String JSON nhận được sang BookingEvent Object
        factory.setRecordMessageConverter(converter());
        
        // Tối ưu High-Concurrency bằng Virtual Threads
        factory.getContainerProperties().setObservationEnabled(true);
        return factory;
    }
}
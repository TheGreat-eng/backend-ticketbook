package com.example.App.config;

import com.example.App.dto.BookingEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.HashMap;
import java.util.Map;

@EnableKafka // QUAN TRỌNG NHẤT: Bật tính năng Consumer lắng nghe
@Configuration
public class KafkaConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); 
        return mapper;
    }

    // ==========================================
    // 1. PRODUCER 
    // ==========================================
    @Bean
    public ProducerFactory<String, BookingEvent> producerFactory(ObjectMapper objectMapper) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 5);

        JsonSerializer<BookingEvent> valueSerializer = new JsonSerializer<>(objectMapper);
        valueSerializer.setAddTypeInfo(true); // SỬA THÀNH TRUE: Đính kèm thông tin Class để Consumer hiểu

        return new DefaultKafkaProducerFactory<>(
            configProps, 
            new StringSerializer(), 
            valueSerializer
        );
    }

    @Bean
    public KafkaTemplate<String, BookingEvent> kafkaTemplate(ProducerFactory<String, BookingEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    // ==========================================
    // 2. CONSUMER 
    // ==========================================
    @Bean
    public ConsumerFactory<String, BookingEvent> consumerFactory(ObjectMapper objectMapper) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Sử dụng trực tiếp JsonDeserializer thay vì StringDeserializer
        JsonDeserializer<BookingEvent> jsonDeserializer = new JsonDeserializer<>(BookingEvent.class, objectMapper);
        jsonDeserializer.addTrustedPackages("*"); // Cấu hình tin tưởng mọi package bằng code
        jsonDeserializer.setUseTypeMapperForKey(false);

        return new DefaultKafkaConsumerFactory<>(
            props, 
            new StringDeserializer(), 
            jsonDeserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BookingEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String, BookingEvent> consumerFactory) {
            
        ConcurrentKafkaListenerContainerFactory<String, BookingEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        
        // Không cần JsonMessageConverter nữa vì ConsumerFactory đã trả về thẳng BookingEvent
        return factory;
    }
}
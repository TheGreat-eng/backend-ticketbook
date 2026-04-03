package com.example.App.config;

import com.example.App.dto.BookingEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.converter.JsonMessageConverter;
import org.springframework.kafka.support.serializer.JsonSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    // TỰ ĐỊNH NGHĨA OBJECTMAPPER ĐỂ FIX LỖI "Bean not found"
    // Và hỗ trợ LocalDateTime (JavaTimeModule)
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); 
        return mapper;
    }

    // ==========================================
    // 1. PRODUCER (Sử dụng ObjectMapper vừa tạo)
    // ==========================================
    @Bean
    public ProducerFactory<String, BookingEvent> producerFactory(ObjectMapper objectMapper) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 5);

        JsonSerializer<BookingEvent> valueSerializer = new JsonSerializer<>(objectMapper);
        valueSerializer.setAddTypeInfo(false); 

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
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "booking_group_v3"); // Đổi Group ID để reset lại từ đầu
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new DefaultKafkaConsumerFactory<>(
            props, 
            new StringDeserializer(), 
            new StringDeserializer()
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            ObjectMapper objectMapper) {
            
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        
        // Sử dụng ObjectMapper để giải mã JSON có hỗ trợ LocalDateTime
        factory.setRecordMessageConverter(new JsonMessageConverter(objectMapper));
        
        return factory;
    }
}
package com.example.App.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingEvent {
    private String orderId;    // Mã đơn hàng tạm thời
    private String userEmail;  // Người mua
    private List<Long> seatIds; // Danh sách ghế
    private Long eventId;      // Sự kiện nào
    private Double totalAmount; // Tổng tiền
    private Long timestamp;    // Thời điểm đặt
}
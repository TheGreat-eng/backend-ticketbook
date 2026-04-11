package com.example.App.service;

import com.example.App.config.RabbitMQConfig;
import com.example.App.dto.BookingEvent;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleNotification(BookingEvent event) {
        log.info("=== [RABBITMQ] Đang khởi tạo vé & QR cho đơn hàng: {} ===", event.getOrderId());
        
        try {
            // 1. Tạo QR Code (Dạng Base64 để nhúng vào Email)
            String qrCodeBase64 = generateQRCodeBase64("Ticket-ID:" + event.getOrderId());

            // 2. Gửi Email (Giao diện HTML)
            sendEmail(event.getUserEmail(), event.getOrderId(), qrCodeBase64);
            
            log.info("√√√ Đã gửi Email vé thành công cho: {}", event.getUserEmail());
        } catch (Exception e) {
            log.error("XXX Lỗi khi gửi thông báo: {}", e.getMessage());
            // RabbitMQ sẽ tự động retry nếu chúng ta throw Exception ở đây
        }
    }

    private String generateQRCodeBase64(String text) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 250, 250);
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return Base64.getEncoder().encodeToString(pngOutputStream.toByteArray());
    }

    private void sendEmail(String to, String orderId, String qrBase64) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setTo(to);
        helper.setSubject("Vé xem Concert của bạn - " + orderId);
        
        String htmlContent = "<h1>Chúc mừng bạn đã đặt vé thành công!</h1>" +
                             "<p>Mã đơn hàng: <b>" + orderId + "</b></p>" +
                             "<p>Vui lòng xuất trình mã QR này tại cổng soát vé:</p>" +
                             "<img src='data:image/png;base64," + qrBase64 + "'/>";
        
        helper.setText(htmlContent, true);
        mailSender.send(message);
    }
}
package com.hub.payment_vnpay.kafka.event;

import com.hub.payment_vnpay.model.enumeration.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentSucceededEvent(
        Long orderId,
        BigDecimal amount,
        String paymentMethod,
        PaymentStatus paymentStatus,
        String transactionId,
        String studentId,
        LocalDateTime createdAt
) {
    public PaymentSucceededEvent(Long orderId,
                                 BigDecimal amount,
                                 String paymentMethod,
                                 PaymentStatus paymentStatus,
                                 String transactionId,
                                 String studentId) {
        this(orderId, amount, paymentMethod, paymentStatus, transactionId, studentId, LocalDateTime.now());
    }
}

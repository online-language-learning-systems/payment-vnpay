package com.hub.payment_vnpay.kafka.event;

import com.hub.payment_vnpay.model.enumeration.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentFailedEvent(
        Long orderId,
        String studentId,
        BigDecimal amount,
        PaymentStatus paymentStatus,
        String paymentMethod,
        String transactionId,
        String failureReason,
        LocalDateTime createdAt
) {
    public PaymentFailedEvent(Long orderId,
                              String studentId,
                              BigDecimal amount,
                              PaymentStatus paymentStatus,
                              String paymentMethod,
                              String transactionId,
                              String failureReason) {
        this(orderId, studentId, amount, paymentStatus, paymentMethod, transactionId, failureReason, LocalDateTime.now());
    }
}

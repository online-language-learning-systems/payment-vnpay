package com.hub.payment_vnpay.kafka.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentRefundedEvent {
    private Long orderId;
    private BigDecimal refundAmount;
    private String refundReason;
    private LocalDateTime createdAt;

    // Constructor tiện lợi
    public PaymentRefundedEvent(Long orderId, BigDecimal refundAmount, String refundReason) {
        this.orderId = orderId;
        this.refundAmount = refundAmount;
        this.refundReason = refundReason;
        this.createdAt = LocalDateTime.now();
    }

    // Getters
    public Long getOrderId() { return orderId; }
    public BigDecimal getRefundAmount() { return refundAmount; }
    public String getRefundReason() { return refundReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

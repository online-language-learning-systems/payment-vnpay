package com.hub.payment_vnpay.model.dto;

import com.hub.payment_vnpay.model.enumeration.PaymentStatus;

public record VnPayResponseDto(
        String paymentUrl,
        PaymentStatus status,
        String message,
        String transactionId
) {}


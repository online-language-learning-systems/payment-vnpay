package com.hub.payment_vnpay.model.dto;

import java.math.BigDecimal;

public record VnPayRequestDto(
        Long orderId,
        String userId,
        BigDecimal amount,
        String orderInfo,
        String returnUrl,
        Integer paymentMethodId
) {}

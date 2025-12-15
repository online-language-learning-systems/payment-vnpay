package com.hub.payment_vnpay.kafka.processor;

import com.hub.payment_vnpay.kafka.event.OrderPlacedEvent;
import com.hub.payment_vnpay.model.dto.VnPayResponseDto;
import com.hub.payment_vnpay.service.VnPayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentProducer {

    private final VnPayService vnPayService;

    @Bean
    public Consumer<OrderPlacedEvent> processOrder() {
        return order -> {
            log.info("📩 Received OrderPlacedEvent: {}", order.getOrderId());

            try {
                // Tạo link thanh toán
                VnPayResponseDto paymentResp = vnPayService.createPaymentRequest(order).block();
                log.info("✅ VNPay Payment URL created: {}", paymentResp.paymentUrl());

            } catch (Exception e) {
                log.error("❌ Error creating payment URL for order {}: {}", order.getOrderId(), e.getMessage());

                // Nếu tạo payment request lỗi, vẫn emit PaymentFailedEvent trực tiếp
                vnPayService.emitPaymentResult(vnPayService.createFailedEvent(order,
                        "Exception while creating payment request"));

            }
        };
    }
}

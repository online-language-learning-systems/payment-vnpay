package com.hub.payment_vnpay.controller;

import com.hub.payment_vnpay.kafka.event.OrderPlacedEvent;
import com.hub.payment_vnpay.model.Payment;
import com.hub.payment_vnpay.model.dto.VnPayResponseDto;
import com.hub.payment_vnpay.repository.PaymentRepository;
import com.hub.payment_vnpay.service.VnPayService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class VnPayController {

    private final VnPayService vnPayService;
    private final PaymentRepository paymentRepository;
    // Tạo link thanh toán
    @PostMapping("/create-payment")
    public Mono<VnPayResponseDto> createPayment(@RequestBody OrderPlacedEvent orderPlacedEvent) {
        return vnPayService.createPaymentRequest(orderPlacedEvent);
    }
    @PostMapping("/ipn")
    public Mono<String> handleIpn(@RequestParam Map<String, String> params) {
        return vnPayService.processIpnCallback(params);
    }

    @GetMapping("/callback")
    public Mono<String> handleCallback(@RequestParam Map<String, String> params) {
        return vnPayService.handlePaymentCallback(params);
    }
    @GetMapping("/orders/{orderId}/payment-url")
    public Mono<Map<String, String>> getPaymentUrl(@PathVariable Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
        Map<String, String> resp = new HashMap<>();

        if (payment != null) {
            if ("PENDING".equals(payment.getPaymentStatus())) {
                resp.put("paymentUrl", payment.getPaymentUrl());
            } else {
                resp.put("paymentStatus", payment.getPaymentStatus());
            }
        } else {
            resp.put("error", "Payment not found for order " + orderId);
        }

        return Mono.just(resp);
    }
}



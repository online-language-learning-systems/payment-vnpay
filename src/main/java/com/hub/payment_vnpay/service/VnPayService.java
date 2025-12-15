package com.hub.payment_vnpay.service;

import com.hub.payment_vnpay.config.VnPayConfig;
import com.hub.payment_vnpay.kafka.event.OrderPlacedEvent;
import com.hub.payment_vnpay.kafka.event.PaymentFailedEvent;
import com.hub.payment_vnpay.kafka.event.PaymentSucceededEvent;
import com.hub.payment_vnpay.model.Payment;
import com.hub.payment_vnpay.model.PaymentMethod;
import com.hub.payment_vnpay.model.dto.VnPayResponseDto;
import com.hub.payment_vnpay.model.enumeration.PaymentStatus;
import com.hub.payment_vnpay.repository.PaymentMethodRepository;
import com.hub.payment_vnpay.repository.PaymentRepository;
import com.hub.payment_vnpay.utils.VnpayUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class VnPayService {

    private final VnPayConfig config;
    private final PaymentRepository paymentRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final StreamBridge streamBridge;
    private final Map<Long, Sinks.One<PaymentStatus>> paymentStatusSink = new ConcurrentHashMap<>();

    // ========================== Tạo Payment và Payment URL ==========================
    public Mono<VnPayResponseDto> createPaymentRequest(OrderPlacedEvent order) {
        return Mono.fromCallable(() -> createPaymentRequestSync(order));
    }

    @Transactional
    public VnPayResponseDto createPaymentRequestSync(OrderPlacedEvent order) {
        if (order.getTotalPrice() == null || order.getTotalPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Order amount không hợp lệ!");
        }

        PaymentMethod method = paymentMethodRepository.findById(1)
                .orElseGet(() -> {
                    PaymentMethod pm = new PaymentMethod();
                    pm.setMethodName("VNPay");
                    pm.setProvider("VNPay");
                    pm.setActive(true);
                    return paymentMethodRepository.save(pm);
                });

        Payment payment = new Payment();
        payment.setOrderId(order.getOrderId());
        payment.setUserId(order.getStudentId());
        payment.setAmount(order.getTotalPrice());
        payment.setCurrency(config.getCurrency());
        payment.setPaymentMethod(method);
        payment.setPaymentStatus(PaymentStatus.PENDING.name());
        payment.setCreatedOn(ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).toOffsetDateTime());

        // Lưu trước để đảm bảo có paymentId
        payment = paymentRepository.saveAndFlush(payment);

        // Tạo payment URL dựa trên payment.id
        String paymentUrl = generatePaymentUrl(order, payment);
        payment.setPaymentUrl(paymentUrl);

        // Lưu lần 2 để cập nhật payment_url
        payment = paymentRepository.saveAndFlush(payment);

        log.info("💾 Payment created: id={}, url={}", payment.getId(), payment.getPaymentUrl());

        return new VnPayResponseDto(
                paymentUrl,
                PaymentStatus.PENDING,
                "Payment link created successfully!",
                order.getOrderId().toString()
        );
    }

    // ========================== Wait cho Payment ==========================
    public Mono<PaymentStatus> waitForPaymentResult(Long orderId) {
        Sinks.One<PaymentStatus> sink = paymentStatusSink.get(orderId);
        if (sink == null) {
            return Mono.error(new RuntimeException("No payment sink found for order " + orderId));
        }
        return sink.asMono();
    }

    public void createPaymentSink(Long orderId) {
        paymentStatusSink.put(orderId, Sinks.one());
    }

    // ========================== Callback xử lý Payment ==========================
    private void updatePaymentResultAndEmitEvent(Long orderId, boolean success, Payment payment) {
        // Cập nhật sink
        Sinks.One<PaymentStatus> sink = paymentStatusSink.get(orderId);
        if (sink != null) {
            sink.tryEmitValue(success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
            paymentStatusSink.remove(orderId);
        }

        // Bắn event ra Kafka
        Object event = success ?
                new PaymentSucceededEvent(payment.getOrderId(), payment.getAmount(), "VNPay",
                        PaymentStatus.SUCCESS, payment.getOrderId().toString(), payment.getUserId())
                : new PaymentFailedEvent(payment.getOrderId(), payment.getUserId(), payment.getAmount(),
                PaymentStatus.FAILED, "VNPay", payment.getOrderId().toString(),
                "Payment failed or cancelled");

        streamBridge.send("processOrder-out-0", event);
        log.info("📤 Emitted event: {}", event.getClass().getSimpleName());
    }

    public PaymentFailedEvent createFailedEvent(OrderPlacedEvent order, String reason) {
        return new PaymentFailedEvent(
                order.getOrderId(),
                order.getStudentId(),
                order.getTotalPrice(),
                PaymentStatus.FAILED,
                "VNPay",
                order.getOrderId().toString(),
                reason
        );
    }

    public Mono<String> handlePaymentCallback(Map<String, String> params) {
        return Mono.fromCallable(() -> {
            Long orderId = Long.parseLong(params.get("vnp_TxnRef"));
            String responseCode = params.get("vnp_ResponseCode");
            String transactionNo = params.get("vnp_TransactionNo");
            BigDecimal callbackAmount = new BigDecimal(params.get("vnp_Amount"))
                    .divide(BigDecimal.valueOf(100));

            Payment payment = paymentRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new RuntimeException("Payment not found for order " + orderId));

            payment.setTransactionCode(transactionNo);
            boolean success = "00".equals(responseCode) && callbackAmount.compareTo(payment.getAmount()) == 0;
            payment.setPaymentStatus(success ? PaymentStatus.SUCCESS.name() : PaymentStatus.FAILED.name());
            paymentRepository.saveAndFlush(payment);

            log.info("💰 PAYMENT RESULT: {}", payment.getPaymentStatus());

            updatePaymentResultAndEmitEvent(orderId, success, payment);

            return success ? "success" : "failed";
        });
    }

    public Mono<String> processIpnCallback(Map<String, String> params) {
        return Mono.fromCallable(() -> {
            String secureHash = params.remove("vnp_SecureHash");
            params.remove("vnp_SecureHashType");

            Map<String, String> sortedParams = new TreeMap<>(params);
            StringBuilder hashData = new StringBuilder();
            for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
                if (hashData.length() > 0) hashData.append('&');
                hashData.append(entry.getKey()).append('=').append(entry.getValue());
            }

            String checkHash = VnpayUtils.hmacSHA512(config.getHashSecret(), hashData.toString());
            if (!checkHash.equals(secureHash)) {
                return "Invalid signature";
            }

            Long orderId = Long.parseLong(params.get("vnp_TxnRef"));
            String responseCode = params.get("vnp_ResponseCode");
            BigDecimal callbackAmount = new BigDecimal(params.get("vnp_Amount"))
                    .divide(BigDecimal.valueOf(100));

            Payment payment = paymentRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new RuntimeException("Payment not found: " + orderId));

            if (payment.getPaymentStatus().equals(PaymentStatus.SUCCESS.name())) {
                return "OK";
            }

            boolean success = "00".equals(responseCode) && callbackAmount.compareTo(payment.getAmount()) == 0;
            payment.setPaymentStatus(success ? PaymentStatus.SUCCESS.name() : PaymentStatus.FAILED.name());
            paymentRepository.saveAndFlush(payment);

            log.info("💰 IPN PAYMENT RESULT: {}", payment.getPaymentStatus());

            updatePaymentResultAndEmitEvent(orderId, success, payment);

            return success ? "OK" : "FAILED";
        });
    }

    // ========================== Generate Payment URL ==========================
    private String generatePaymentUrl(OrderPlacedEvent order, Payment payment) {
        try {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

            Map<String, String> params = new LinkedHashMap<>();
            params.put("vnp_Version", "2.1.0");
            params.put("vnp_Command", "pay");
            params.put("vnp_TmnCode", config.getTmnCode());
            params.put("vnp_Amount", String.valueOf(payment.getAmount().multiply(BigDecimal.valueOf(100)).longValue()));
            params.put("vnp_CurrCode", "VND");
            params.put("vnp_TxnRef", order.getOrderId().toString());
            params.put("vnp_OrderInfo", "Thanh toan don hang " + order.getOrderId());
            params.put("vnp_OrderType", "other");
            params.put("vnp_Locale", "vn");
            params.put("vnp_ReturnUrl", config.getReturnUrl());
            params.put("vnp_IpAddr", "127.0.0.1");
            params.put("vnp_CreateDate", now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

            Map<String, String> sortedParams = new TreeMap<>(params);
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
                if (hashData.length() > 0) {
                    hashData.append('&');
                    query.append('&');
                }
                hashData.append(entry.getKey()).append("=")
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII));
                query.append(entry.getKey()).append("=")
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            }

            String secureHash = VnpayUtils.hmacSHA512(config.getHashSecret(), hashData.toString());

            return config.getApiUrl() + "?" + query + "&vnp_SecureHash=" + secureHash;

        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo payment URL VNPay", e);
        }
    }

    // ========================== Emit Event ==========================
    public void emitPaymentResult(Object event) {
        streamBridge.send("processOrder-out-0", event);
        log.info("📤 Emitted {} for order {}",
                event.getClass().getSimpleName(),
                (event instanceof PaymentSucceededEvent) ? ((PaymentSucceededEvent) event).orderId() : ((PaymentFailedEvent) event).orderId());
    }

}

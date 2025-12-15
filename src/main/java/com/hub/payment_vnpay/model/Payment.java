package com.hub.payment_vnpay.model;

import com.hub.common_library.model.AbstractAuditEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "payment", schema = "dbo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends AbstractAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", length = 10, nullable = false)
    private String currency = "Đ";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paymentmethod_id", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "payment_status", length = 30, nullable = false)
    private String paymentStatus = "PENDING";

    @Column(name = "transaction_code", length = 100)
    private String transactionCode;

    @Column(name = "payment_url", length = 2000)
    private String paymentUrl;

}

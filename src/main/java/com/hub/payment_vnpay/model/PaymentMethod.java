package com.hub.payment_vnpay.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payment_method")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "paymentmethod_id")
    private Integer id;

    @Column(name = "method_name", nullable = false, length = 50)
    private String methodName;

    @Column(name = "provider", length = 50)
    private String provider;

    @Column(name = "active")
    private Boolean active = true;
}

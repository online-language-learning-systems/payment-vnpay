package com.hub.payment_vnpay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories("com.hub.payment_vnpay.repository")
@EntityScan("com.hub.payment_vnpay.model")
@EnableJpaAuditing
@SpringBootApplication
public class PaymentVnpayApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentVnpayApplication.class, args);
    }
}
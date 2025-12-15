package com.hub.payment_vnpay.repository;

import com.hub.payment_vnpay.model.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Integer> {
}

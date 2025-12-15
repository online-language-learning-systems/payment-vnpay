package com.hub.payment_vnpay.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class VnPayConfig {

    @Value("${vnp.tmnCode}") private String tmnCode;
    @Value("${vnp.hashSecret}") private String hashSecret;
    @Value("${vnp.apiUrl}") private String apiUrl;
    @Value("${vnp.returnUrl}") private String returnUrl;
    @Value("${vnp.ipnUrl}") private String ipnUrl;
    @Value("${vnp.version}") private String version;
    @Value("${vnp.command}") private String command;
    @Value("${vnp.locale}") private String locale;
    @Value("${vnp.currency}") private String currency;
    @Value("${vnp.bankCode}") private String bankCode;

}


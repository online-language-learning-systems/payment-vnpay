package com.hub.payment_vnpay.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class VnpayUtils {
    public static String getRandomNumber(int length) {
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append((char) ('0' + rnd.nextInt(10)));
        }
        return sb.toString();
    }

    public static String hmacSHA512(String key, String data) {
        try {
            if (key == null || data == null) return null;
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hash = new StringBuilder();
            for (byte b : bytes) {
                hash.append(String.format("%02x", b));
            }
            return hash.toString();
        } catch (Exception e) {
            return "";
        }
    }
    public static String getCurrentDateTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        return formatter.format(new Date());
    }

    public static String getDataToHash(Map<String, String> fields) {
        List<String> keys = new ArrayList<>(fields.keySet());
        Collections.sort(keys);
        StringBuilder sb = new StringBuilder();

        for (String key : keys) {
            if ("vnp_SecureHash".equals(key) || "vnp_SecureHashType".equals(key))
                continue;

            String value = fields.get(key);
            if (value != null && !value.trim().isEmpty()) {
                if (sb.length() > 0) sb.append('&');
                sb.append(URLEncoder.encode(key, StandardCharsets.US_ASCII));
                sb.append('=');
                sb.append(URLEncoder.encode(value.trim(), StandardCharsets.US_ASCII));
            }
        }
        return sb.toString();
    }

    public static String hashAllFields(Map<String, String> fields, String secretKey) {
        String data = getDataToHash(fields);
        return hmacSHA512(secretKey, data);
    }

    public static String buildQuery(Map<String, String> fields) {
        List<String> keys = new ArrayList<>(fields.keySet());
        Collections.sort(keys);
        StringBuilder query = new StringBuilder();

        for (String key : keys) {
            if ("vnp_SecureHash".equals(key)) continue;
            String value = fields.get(key);
            if (value != null && !value.trim().isEmpty()) {
                if (query.length() > 0) query.append('&');
                query.append(URLEncoder.encode(key, StandardCharsets.UTF_8));
                query.append('=');
                query.append(URLEncoder.encode(value.trim(), StandardCharsets.UTF_8).replace("%20", "+"));
            }
        }
        return query.toString();
    }
}

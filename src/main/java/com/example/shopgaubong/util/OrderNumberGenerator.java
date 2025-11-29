package com.example.shopgaubong.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OrderNumberGenerator {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * Tạo mã đơn hàng duy nhất
     * Format: ORD-yyyyMMddHHmmss-XXX (XXX là số random)
     */
    public static String generate() {
        String timestamp = LocalDateTime.now().format(formatter);
        int random = (int) (Math.random() * 1000);
        return String.format("ORD-%s-%03d", timestamp, random);
    }
}


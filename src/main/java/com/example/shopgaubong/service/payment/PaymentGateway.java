package com.example.shopgaubong.service.payment;

import com.example.shopgaubong.dto.PaymentRequest;
import com.example.shopgaubong.dto.PaymentResponse;
import com.example.shopgaubong.entity.Payment;

import java.util.Map;

/**
 * Interface cho payment gateway
 */
public interface PaymentGateway {

    /**
     * Tạo URL thanh toán
     */
    PaymentResponse createPaymentUrl(Payment payment, PaymentRequest request);

    /**
     * Xác thực callback từ gateway
     */
    boolean verifyCallback(Map<String, String> params);

    /**
     * Xử lý callback từ gateway
     */
    PaymentResponse processCallback(Map<String, String> params);

    /**
     * Kiểm tra trạng thái thanh toán
     */
    PaymentResponse queryPaymentStatus(String transactionId);

    /**
     * Hoàn tiền
     */
    PaymentResponse refund(Payment payment, java.math.BigDecimal amount, String reason);
}



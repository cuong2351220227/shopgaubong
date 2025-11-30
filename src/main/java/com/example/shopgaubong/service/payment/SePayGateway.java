package com.example.shopgaubong.service.payment;

import com.example.shopgaubong.dto.PaymentRequest;
import com.example.shopgaubong.dto.PaymentResponse;
import com.example.shopgaubong.entity.Payment;
import com.example.shopgaubong.enums.PaymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * SePay Gateway Implementation
 * Tham khảo: https://sepay.vn/
 */
public class SePayGateway implements PaymentGateway {

    private static final Logger logger = LoggerFactory.getLogger(SePayGateway.class);

    // Cấu hình SePay - Nên lưu trong file config hoặc database
    private static final String MERCHANT_ID = "YOUR_MERCHANT_ID";
    private static final String SECRET_KEY = "YOUR_SECRET_KEY";
    private static final String SEPAY_URL = "https://api.sepay.vn/payment/create";
    private static final String RETURN_URL = "http://localhost:8080/payment/sepay/callback";

    @Override
    public PaymentResponse createPaymentUrl(Payment payment, PaymentRequest request) {
        PaymentResponse response = new PaymentResponse();
        try {
            String transactionId = payment.getTransactionId();
            String orderInfo = "Thanh toan don hang " + payment.getOrder().getOrderNumber();
            long amount = payment.getTotalAmount().longValue();
            String returnUrl = request.getReturnUrl() != null ? request.getReturnUrl() : RETURN_URL;

            // Create signature
            String rawSignature = MERCHANT_ID + transactionId + amount + orderInfo + returnUrl;
            String signature = hmacSHA256(SECRET_KEY, rawSignature);

            // Build request parameters
            StringBuilder paymentUrl = new StringBuilder(SEPAY_URL);
            paymentUrl.append("?merchant_id=").append(MERCHANT_ID);
            paymentUrl.append("&transaction_id=").append(transactionId);
            paymentUrl.append("&amount=").append(amount);
            paymentUrl.append("&order_info=").append(orderInfo);
            paymentUrl.append("&return_url=").append(returnUrl);
            paymentUrl.append("&signature=").append(signature);

            response.setSuccess(true);
            response.setPaymentUrl(paymentUrl.toString());
            response.setTransactionId(transactionId);
            response.setMessage("Tạo URL thanh toán SePay thành công");

            logger.info("Created SePay payment URL for transaction: {}", transactionId);

        } catch (Exception e) {
            logger.error("Error creating SePay payment URL", e);
            response.setSuccess(false);
            response.setMessage("Lỗi tạo URL thanh toán SePay: " + e.getMessage());
        }
        return response;
    }

    @Override
    public boolean verifyCallback(Map<String, String> params) {
        try {
            String signature = params.get("signature");
            String merchantId = params.get("merchant_id");
            String transactionId = params.get("transaction_id");
            String amount = params.get("amount");
            String status = params.get("status");

            String rawSignature = merchantId + transactionId + amount + status;
            String expectedSignature = hmacSHA256(SECRET_KEY, rawSignature);

            return expectedSignature.equals(signature);
        } catch (Exception e) {
            logger.error("Error verifying SePay callback", e);
            return false;
        }
    }

    @Override
    public PaymentResponse processCallback(Map<String, String> params) {
        PaymentResponse response = new PaymentResponse();

        String status = params.get("status");
        String transactionId = params.get("transaction_id");
        String sePayTransactionId = params.get("sepay_transaction_id");
        String message = params.get("message");

        response.setTransactionId(transactionId);

        if ("success".equalsIgnoreCase(status)) {
            response.setSuccess(true);
            response.setStatus(PaymentStatus.COMPLETED);
            response.setMessage("Thanh toán SePay thành công");
        } else {
            response.setSuccess(false);
            response.setStatus(PaymentStatus.FAILED);
            response.setMessage("Thanh toán SePay thất bại: " + message);
        }

        logger.info("Processed SePay callback for transaction: {} with result: {}",
                    transactionId, status);

        return response;
    }

    @Override
    public PaymentResponse queryPaymentStatus(String transactionId) {
        PaymentResponse response = new PaymentResponse();
        try {
            // Create signature for query
            String rawSignature = MERCHANT_ID + transactionId;
            String signature = hmacSHA256(SECRET_KEY, rawSignature);

            // TODO: Send HTTP GET request to SePay query endpoint
            logger.info("Query SePay payment status for transaction: {}", transactionId);

            // Mock response
            response.setSuccess(false);
            response.setMessage("Chức năng đang phát triển");

        } catch (Exception e) {
            logger.error("Error querying SePay payment status", e);
            response.setSuccess(false);
            response.setMessage("Lỗi truy vấn trạng thái: " + e.getMessage());
        }
        return response;
    }

    @Override
    public PaymentResponse refund(Payment payment, BigDecimal amount, String reason) {
        PaymentResponse response = new PaymentResponse();
        try {
            String transactionId = payment.getTransactionId();
            String refundId = "REFUND_" + System.currentTimeMillis();
            long refundAmount = amount.longValue();

            // Create signature
            String rawSignature = MERCHANT_ID + transactionId + refundId + refundAmount + reason;
            String signature = hmacSHA256(SECRET_KEY, rawSignature);

            // TODO: Send HTTP POST request to SePay refund endpoint
            logger.info("SePay refund request for transaction: {}, amount: {}", transactionId, refundAmount);

            // Mock response
            response.setSuccess(true);
            response.setMessage("Yêu cầu hoàn tiền SePay đang được xử lý");

        } catch (Exception e) {
            logger.error("Error processing SePay refund", e);
            response.setSuccess(false);
            response.setMessage("Lỗi hoàn tiền SePay: " + e.getMessage());
        }
        return response;
    }

    private String hmacSHA256(String key, String data) {
        try {
            Mac hmac256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac256.init(secretKey);
            byte[] result = hmac256.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            logger.error("Error generating HMAC SHA256", e);
            return "";
        }
    }
}


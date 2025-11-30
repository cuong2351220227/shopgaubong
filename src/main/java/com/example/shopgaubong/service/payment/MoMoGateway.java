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
 * MoMo Gateway Implementation
 * Tham khảo: https://developers.momo.vn/
 */
public class MoMoGateway implements PaymentGateway {

    private static final Logger logger = LoggerFactory.getLogger(MoMoGateway.class);

    // Cấu hình MoMo - Nên lưu trong file config hoặc database
    private static final String PARTNER_CODE = "YOUR_PARTNER_CODE";
    private static final String ACCESS_KEY = "YOUR_ACCESS_KEY";
    private static final String SECRET_KEY = "YOUR_SECRET_KEY";
    private static final String MOMO_URL = "https://test-payment.momo.vn/v2/gateway/api/create";
    private static final String RETURN_URL = "http://localhost:8080/payment/momo/callback";
    private static final String NOTIFY_URL = "http://localhost:8080/payment/momo/notify";

    @Override
    public PaymentResponse createPaymentUrl(Payment payment, PaymentRequest request) {
        PaymentResponse response = new PaymentResponse();
        try {
            String requestId = payment.getTransactionId();
            String orderId = payment.getTransactionId();
            String orderInfo = "Thanh toan don hang " + payment.getOrder().getOrderNumber();
            String redirectUrl = request.getReturnUrl() != null ? request.getReturnUrl() : RETURN_URL;
            String ipnUrl = NOTIFY_URL;
            long amount = payment.getTotalAmount().longValue();
            String requestType = "captureWallet";
            String extraData = ""; // Pass empty string if no extra data

            // Create signature
            String rawSignature = "accessKey=" + ACCESS_KEY +
                    "&amount=" + amount +
                    "&extraData=" + extraData +
                    "&ipnUrl=" + ipnUrl +
                    "&orderId=" + orderId +
                    "&orderInfo=" + orderInfo +
                    "&partnerCode=" + PARTNER_CODE +
                    "&redirectUrl=" + redirectUrl +
                    "&requestId=" + requestId +
                    "&requestType=" + requestType;

            String signature = hmacSHA256(SECRET_KEY, rawSignature);

            // Build JSON request
            String jsonRequest = String.format(
                    "{\"partnerCode\":\"%s\",\"partnerName\":\"ShopGauBong\"," +
                    "\"storeId\":\"ShopGauBong\",\"requestId\":\"%s\",\"amount\":%d," +
                    "\"orderId\":\"%s\",\"orderInfo\":\"%s\",\"redirectUrl\":\"%s\"," +
                    "\"ipnUrl\":\"%s\",\"lang\":\"vi\",\"extraData\":\"%s\"," +
                    "\"requestType\":\"%s\",\"signature\":\"%s\"}",
                    PARTNER_CODE, requestId, amount, orderId, orderInfo,
                    redirectUrl, ipnUrl, extraData, requestType, signature
            );

            // TODO: Send HTTP POST request to MOMO_URL with jsonRequest
            // For now, just log and return mock response
            logger.info("MoMo payment request: {}", jsonRequest);

            // Mock response - In production, parse actual MoMo response
            response.setSuccess(true);
            response.setPaymentUrl(MOMO_URL + "?orderId=" + orderId); // Mock URL
            response.setTransactionId(requestId);
            response.setMessage("Tạo yêu cầu thanh toán MoMo thành công");

            logger.info("Created MoMo payment URL for transaction: {}", requestId);

        } catch (Exception e) {
            logger.error("Error creating MoMo payment URL", e);
            response.setSuccess(false);
            response.setMessage("Lỗi tạo yêu cầu thanh toán MoMo: " + e.getMessage());
        }
        return response;
    }

    @Override
    public boolean verifyCallback(Map<String, String> params) {
        try {
            String signature = params.get("signature");
            String partnerCode = params.get("partnerCode");
            String orderId = params.get("orderId");
            String requestId = params.get("requestId");
            String amount = params.get("amount");
            String orderInfo = params.get("orderInfo");
            String orderType = params.get("orderType");
            String transId = params.get("transId");
            String resultCode = params.get("resultCode");
            String message = params.get("message");
            String payType = params.get("payType");
            String responseTime = params.get("responseTime");
            String extraData = params.get("extraData");

            String rawSignature = "accessKey=" + ACCESS_KEY +
                    "&amount=" + amount +
                    "&extraData=" + extraData +
                    "&message=" + message +
                    "&orderId=" + orderId +
                    "&orderInfo=" + orderInfo +
                    "&orderType=" + orderType +
                    "&partnerCode=" + partnerCode +
                    "&payType=" + payType +
                    "&requestId=" + requestId +
                    "&responseTime=" + responseTime +
                    "&resultCode=" + resultCode +
                    "&transId=" + transId;

            String expectedSignature = hmacSHA256(SECRET_KEY, rawSignature);
            return expectedSignature.equals(signature);
        } catch (Exception e) {
            logger.error("Error verifying MoMo callback", e);
            return false;
        }
    }

    @Override
    public PaymentResponse processCallback(Map<String, String> params) {
        PaymentResponse response = new PaymentResponse();

        String resultCode = params.get("resultCode");
        String orderId = params.get("orderId");
        String transId = params.get("transId");
        String message = params.get("message");

        response.setTransactionId(orderId);

        if ("0".equals(resultCode)) {
            response.setSuccess(true);
            response.setStatus(PaymentStatus.COMPLETED);
            response.setMessage("Thanh toán MoMo thành công");
        } else {
            response.setSuccess(false);
            response.setStatus(PaymentStatus.FAILED);
            response.setMessage("Thanh toán MoMo thất bại: " + message);
        }

        logger.info("Processed MoMo callback for transaction: {} with result: {}",
                    orderId, resultCode);

        return response;
    }

    @Override
    public PaymentResponse queryPaymentStatus(String transactionId) {
        PaymentResponse response = new PaymentResponse();
        // TODO: Implement query payment status API
        response.setSuccess(false);
        response.setMessage("Chức năng đang phát triển");
        return response;
    }

    @Override
    public PaymentResponse refund(Payment payment, BigDecimal amount, String reason) {
        PaymentResponse response = new PaymentResponse();
        try {
            String orderId = payment.getTransactionId();
            String requestId = "REFUND_" + System.currentTimeMillis();
            long refundAmount = amount.longValue();
            String transId = payment.getGatewayTransactionId();
            String description = reason;

            String rawSignature = "accessKey=" + ACCESS_KEY +
                    "&amount=" + refundAmount +
                    "&description=" + description +
                    "&orderId=" + orderId +
                    "&partnerCode=" + PARTNER_CODE +
                    "&requestId=" + requestId +
                    "&transId=" + transId;

            String signature = hmacSHA256(SECRET_KEY, rawSignature);

            // TODO: Send HTTP POST request to MoMo refund endpoint
            logger.info("MoMo refund request for transaction: {}, amount: {}", orderId, refundAmount);

            // Mock response
            response.setSuccess(true);
            response.setMessage("Yêu cầu hoàn tiền MoMo đang được xử lý");

        } catch (Exception e) {
            logger.error("Error processing MoMo refund", e);
            response.setSuccess(false);
            response.setMessage("Lỗi hoàn tiền MoMo: " + e.getMessage());
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



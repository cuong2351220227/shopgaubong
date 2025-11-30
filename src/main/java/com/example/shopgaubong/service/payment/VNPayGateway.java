package com.example.shopgaubong.service.payment;

import com.example.shopgaubong.dto.PaymentRequest;
import com.example.shopgaubong.dto.PaymentResponse;
import com.example.shopgaubong.entity.Payment;
import com.example.shopgaubong.enums.PaymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * VNPay Gateway Implementation
 * Tham khảo: https://sandbox.vnpayment.vn/apis/docs/
 */
public class VNPayGateway implements PaymentGateway {

    private static final Logger logger = LoggerFactory.getLogger(VNPayGateway.class);

    // Cấu hình VNPay - Nên lưu trong file config hoặc database
    private static final String VNP_TMN_CODE = "YOUR_TMN_CODE"; // Mã website
    private static final String VNP_HASH_SECRET = "YOUR_HASH_SECRET"; // Chuỗi bí mật
    private static final String VNP_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private static final String VNP_RETURN_URL = "http://localhost:8080/payment/vnpay/callback";

    @Override
    public PaymentResponse createPaymentUrl(Payment payment, PaymentRequest request) {
        PaymentResponse response = new PaymentResponse();
        try {
            String vnp_Version = "2.1.0";
            String vnp_Command = "pay";
            String vnp_TxnRef = payment.getTransactionId();
            String vnp_IpAddr = request.getIpAddress() != null ? request.getIpAddress() : "127.0.0.1";
            String vnp_OrderInfo = "Thanh toan don hang " + payment.getOrder().getOrderNumber();
            String orderType = "other";

            // Số tiền (VNPay yêu cầu nhân với 100)
            long amount = payment.getTotalAmount().multiply(new BigDecimal("100")).longValue();

            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", vnp_Version);
            vnp_Params.put("vnp_Command", vnp_Command);
            vnp_Params.put("vnp_TmnCode", VNP_TMN_CODE);
            vnp_Params.put("vnp_Amount", String.valueOf(amount));
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
            vnp_Params.put("vnp_OrderType", orderType);
            vnp_Params.put("vnp_Locale", "vn");
            vnp_Params.put("vnp_ReturnUrl", request.getReturnUrl() != null ? request.getReturnUrl() : VNP_RETURN_URL);
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnp_CreateDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

            cld.add(Calendar.MINUTE, 15); // Hết hạn sau 15 phút
            String vnp_ExpireDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

            // Build query string
            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnp_Params.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    // Build hash data
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    // Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }
            String queryUrl = query.toString();
            String vnp_SecureHash = hmacSHA512(VNP_HASH_SECRET, hashData.toString());
            queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
            String paymentUrl = VNP_URL + "?" + queryUrl;

            response.setSuccess(true);
            response.setPaymentUrl(paymentUrl);
            response.setTransactionId(vnp_TxnRef);
            response.setMessage("Tạo URL thanh toán VNPay thành công");

            logger.info("Created VNPay payment URL for transaction: {}", vnp_TxnRef);

        } catch (Exception e) {
            logger.error("Error creating VNPay payment URL", e);
            response.setSuccess(false);
            response.setMessage("Lỗi tạo URL thanh toán: " + e.getMessage());
        }
        return response;
    }

    @Override
    public boolean verifyCallback(Map<String, String> params) {
        try {
            String vnp_SecureHash = params.get("vnp_SecureHash");
            params.remove("vnp_SecureHash");
            params.remove("vnp_SecureHashType");

            // Build hash data
            List<String> fieldNames = new ArrayList<>(params.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = params.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        hashData.append('&');
                    }
                }
            }

            String signValue = hmacSHA512(VNP_HASH_SECRET, hashData.toString());
            return signValue.equals(vnp_SecureHash);
        } catch (Exception e) {
            logger.error("Error verifying VNPay callback", e);
            return false;
        }
    }

    @Override
    public PaymentResponse processCallback(Map<String, String> params) {
        PaymentResponse response = new PaymentResponse();

        String vnp_ResponseCode = params.get("vnp_ResponseCode");
        String vnp_TxnRef = params.get("vnp_TxnRef");
        String vnp_TransactionNo = params.get("vnp_TransactionNo");
        String vnp_Amount = params.get("vnp_Amount");

        response.setTransactionId(vnp_TxnRef);

        if ("00".equals(vnp_ResponseCode)) {
            response.setSuccess(true);
            response.setStatus(PaymentStatus.COMPLETED);
            response.setMessage("Thanh toán thành công");
        } else {
            response.setSuccess(false);
            response.setStatus(PaymentStatus.FAILED);
            response.setMessage("Thanh toán thất bại. Mã lỗi: " + vnp_ResponseCode);
        }

        logger.info("Processed VNPay callback for transaction: {} with result: {}",
                    vnp_TxnRef, vnp_ResponseCode);

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
        // TODO: Implement refund API
        response.setSuccess(false);
        response.setMessage("Chức năng hoàn tiền VNPay đang phát triển");
        logger.info("Refund request for VNPay transaction: {}, amount: {}",
                    payment.getTransactionId(), amount);
        return response;
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            logger.error("Error generating HMAC SHA512", e);
            return "";
        }
    }
}


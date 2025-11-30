package com.example.shopgaubong.service;

import com.example.shopgaubong.dao.OrderDAO;
import com.example.shopgaubong.dao.PaymentDAO;
import com.example.shopgaubong.dao.RefundDAO;
import com.example.shopgaubong.dto.FeeCalculation;
import com.example.shopgaubong.dto.PaymentRequest;
import com.example.shopgaubong.dto.PaymentResponse;
import com.example.shopgaubong.dto.RefundRequest;
import com.example.shopgaubong.entity.Order;
import com.example.shopgaubong.entity.Payment;
import com.example.shopgaubong.entity.Refund;
import com.example.shopgaubong.enums.PaymentMethod;
import com.example.shopgaubong.enums.PaymentStatus;
import com.example.shopgaubong.enums.RefundStatus;
import com.example.shopgaubong.service.payment.*;
import com.example.shopgaubong.util.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service quản lý thanh toán và hoàn tiền
 */
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentDAO paymentDAO;
    private final RefundDAO refundDAO;
    private final OrderDAO orderDAO;
    private final Map<PaymentMethod, PaymentGateway> gateways;

    // Cấu hình phí
    private static final BigDecimal COD_FEE_RATE = new BigDecimal("0.02"); // 2%
    private static final BigDecimal COD_MIN_FEE = new BigDecimal("10000"); // 10,000 VND
    private static final BigDecimal COD_MAX_FEE = new BigDecimal("50000"); // 50,000 VND
    private static final BigDecimal REFUND_FEE_RATE = new BigDecimal("0.01"); // 1% phí hoàn tiền

    public PaymentService() {
        this.paymentDAO = new PaymentDAO();
        this.refundDAO = new RefundDAO();
        this.orderDAO = new OrderDAO();
        this.gateways = new HashMap<>();

        // Khởi tạo các payment gateway
        this.gateways.put(PaymentMethod.VNPAY, new VNPayGateway());
        this.gateways.put(PaymentMethod.MOMO, new MoMoGateway());
        this.gateways.put(PaymentMethod.SEPAY, new SePayGateway());
    }

    /**
     * Tính toán chi tiết các khoản phí
     */
    public FeeCalculation calculateFees(Order order, PaymentMethod paymentMethod) {
        FeeCalculation feeCalc = new FeeCalculation();

        feeCalc.setSubtotal(order.getSubtotal());
        feeCalc.setShippingFee(order.getShippingFee());
        feeCalc.setTax(order.getTax());
        feeCalc.setDiscount(order.getDiscount());

        BigDecimal baseAmount = order.getSubtotal()
                .add(order.getShippingFee())
                .subtract(order.getDiscount());

        // Tính phí COD
        if (paymentMethod == PaymentMethod.COD) {
            BigDecimal codFee = baseAmount.multiply(COD_FEE_RATE);
            // Áp dụng min/max
            if (codFee.compareTo(COD_MIN_FEE) < 0) {
                codFee = COD_MIN_FEE;
            } else if (codFee.compareTo(COD_MAX_FEE) > 0) {
                codFee = COD_MAX_FEE;
            }
            feeCalc.setCodFee(codFee.setScale(0, RoundingMode.HALF_UP));
        }

        // Tính phí gateway
        if (paymentMethod.isGateway()) {
            BigDecimal gatewayFeeRate = new BigDecimal(String.valueOf(paymentMethod.getGatewayFeeRate()));
            BigDecimal gatewayFee = baseAmount.multiply(gatewayFeeRate);
            feeCalc.setGatewayFee(gatewayFee.setScale(0, RoundingMode.HALF_UP));
        }

        // Tính tổng
        feeCalc.calculateTotals();

        logger.info("Calculated fees for order {}: COD={}, Gateway={}, Total={}",
                    order.getOrderNumber(), feeCalc.getCodFee(),
                    feeCalc.getGatewayFee(), feeCalc.getGrandTotal());

        return feeCalc;
    }

    /**
     * Tạo thanh toán mới
     */
    public PaymentResponse createPayment(PaymentRequest request) {
        PaymentResponse response = new PaymentResponse();

        try {
            // Tìm order
            Optional<Order> orderOpt = orderDAO.findById(request.getOrderId());
            if (orderOpt.isEmpty()) {
                response.setSuccess(false);
                response.setMessage("Không tìm thấy đơn hàng");
                return response;
            }

            Order order = orderOpt.get();

            // Tính phí
            FeeCalculation feeCalc = calculateFees(order, request.getPaymentMethod());

            // Tạo Payment entity
            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setMethod(request.getPaymentMethod());
            payment.setStatus(PaymentStatus.PENDING);
            payment.setAmount(feeCalc.getSubtotal());
            payment.setCodFee(feeCalc.getCodFee());
            payment.setGatewayFee(feeCalc.getGatewayFee());
            payment.setTransactionId(generateTransactionId());
            payment.setNotes(request.getNotes());
            payment.setExpiredAt(LocalDateTime.now().plusHours(24)); // Hết hạn sau 24h

            String currentUser = SessionManager.getInstance().getCurrentUsername();
            payment.setCreatedBy(currentUser);
            payment.setUpdatedBy(currentUser);

            // Lưu payment
            payment = paymentDAO.save(payment);

            // Xử lý theo phương thức thanh toán
            if (request.getPaymentMethod().isGateway()) {
                // Tạo URL thanh toán cho gateway
                PaymentGateway gateway = gateways.get(request.getPaymentMethod());
                if (gateway != null) {
                    response = gateway.createPaymentUrl(payment, request);
                    response.setPaymentId(payment.getId());
                } else {
                    response.setSuccess(false);
                    response.setMessage("Gateway chưa được hỗ trợ");
                }
            } else if (request.getPaymentMethod() == PaymentMethod.COD) {
                // COD - không cần xử lý gì thêm
                response.setSuccess(true);
                response.setStatus(PaymentStatus.PENDING);
                response.setMessage("Đơn hàng COD được tạo thành công");
            } else if (request.getPaymentMethod() == PaymentMethod.BANK_TRANSFER) {
                // Chuyển khoản - cần admin xác nhận
                response.setSuccess(true);
                response.setStatus(PaymentStatus.PENDING);
                response.setMessage("Vui lòng chuyển khoản và chờ xác nhận");
            }

            // Set common response fields
            response.setPaymentId(payment.getId());
            response.setTransactionId(payment.getTransactionId());
            response.setPaymentMethod(payment.getMethod());
            response.setAmount(payment.getAmount());
            response.setCodFee(payment.getCodFee());
            response.setGatewayFee(payment.getGatewayFee());
            response.setTotalAmount(payment.getTotalAmount());

            logger.info("Created payment {} for order {} with method {}",
                        payment.getId(), order.getOrderNumber(), request.getPaymentMethod());

        } catch (Exception e) {
            logger.error("Error creating payment", e);
            response.setSuccess(false);
            response.setMessage("Lỗi tạo thanh toán: " + e.getMessage());
        }

        return response;
    }

    /**
     * Xác nhận thanh toán (cho COD hoặc chuyển khoản)
     */
    public boolean confirmPayment(Long paymentId, String transactionRef) {
        try {
            Optional<Payment> paymentOpt = paymentDAO.findById(paymentId);
            if (paymentOpt.isEmpty()) {
                logger.error("Payment not found: {}", paymentId);
                return false;
            }

            Payment payment = paymentOpt.get();
            payment.markAsPaid();
            if (transactionRef != null) {
                payment.setGatewayTransactionId(transactionRef);
            }

            String currentUser = SessionManager.getInstance().getCurrentUsername();
            payment.setUpdatedBy(currentUser);

            paymentDAO.update(payment);
            logger.info("Confirmed payment: {}", paymentId);
            return true;

        } catch (Exception e) {
            logger.error("Error confirming payment", e);
            return false;
        }
    }

    /**
     * Xử lý callback từ gateway
     */
    public PaymentResponse processGatewayCallback(PaymentMethod method, Map<String, String> params) {
        PaymentResponse response = new PaymentResponse();

        try {
            PaymentGateway gateway = gateways.get(method);
            if (gateway == null) {
                response.setSuccess(false);
                response.setMessage("Gateway không được hỗ trợ");
                return response;
            }

            // Verify callback
            if (!gateway.verifyCallback(params)) {
                response.setSuccess(false);
                response.setMessage("Xác thực callback thất bại");
                logger.warn("Invalid callback signature from {}", method);
                return response;
            }

            // Process callback
            response = gateway.processCallback(params);

            // Update payment in database
            String transactionId = response.getTransactionId();
            // Find payment by transaction ID
            List<Payment> payments = paymentDAO.findAll(); // TODO: Add findByTransactionId method
            Payment payment = payments.stream()
                    .filter(p -> transactionId.equals(p.getTransactionId()))
                    .findFirst()
                    .orElse(null);

            if (payment != null) {
                payment.setStatus(response.getStatus());
                payment.setGatewayTransactionId(params.get("gatewayTransactionId"));
                payment.setGatewayResponseCode(params.get("responseCode"));

                if (response.isSuccess()) {
                    payment.markAsPaid();
                } else {
                    payment.markAsFailed(response.getMessage());
                }

                paymentDAO.update(payment);
                logger.info("Updated payment {} from callback: {}", payment.getId(), response.getStatus());
            }

        } catch (Exception e) {
            logger.error("Error processing gateway callback", e);
            response.setSuccess(false);
            response.setMessage("Lỗi xử lý callback: " + e.getMessage());
        }

        return response;
    }

    /**
     * Tạo yêu cầu hoàn tiền
     */
    public Refund createRefundRequest(RefundRequest request) {
        try {
            Optional<Payment> paymentOpt = paymentDAO.findById(request.getPaymentId());
            if (paymentOpt.isEmpty()) {
                throw new IllegalArgumentException("Không tìm thấy thanh toán");
            }

            Payment payment = paymentOpt.get();

            // Kiểm tra điều kiện hoàn tiền
            if (!payment.isRefundable()) {
                throw new IllegalArgumentException("Thanh toán này không thể hoàn tiền");
            }

            if (request.getAmount().compareTo(payment.getRefundableAmount()) > 0) {
                throw new IllegalArgumentException("Số tiền hoàn vượt quá số tiền có thể hoàn");
            }

            // Tính phí hoàn tiền
            BigDecimal refundFee = request.getAmount()
                    .multiply(REFUND_FEE_RATE)
                    .setScale(0, RoundingMode.HALF_UP);

            // Tạo Refund entity
            Refund refund = new Refund();
            refund.setPayment(payment);
            refund.setAmount(request.getAmount());
            refund.setRefundFee(refundFee);
            refund.setReason(request.getReason());
            refund.setStatus(RefundStatus.PENDING);
            refund.setRefundNumber(generateRefundNumber());
            refund.setAdminNotes(request.getNotes());

            String currentUser = SessionManager.getInstance().getCurrentUsername();
            refund.setCreatedBy(currentUser);
            refund.setUpdatedBy(currentUser);

            refund = refundDAO.save(refund);
            logger.info("Created refund request {} for payment {}, amount: {}",
                        refund.getId(), payment.getId(), request.getAmount());

            return refund;

        } catch (Exception e) {
            logger.error("Error creating refund request", e);
            throw new RuntimeException("Lỗi tạo yêu cầu hoàn tiền: " + e.getMessage(), e);
        }
    }

    /**
     * Duyệt yêu cầu hoàn tiền
     */
    public boolean approveRefund(Long refundId) {
        try {
            Optional<Refund> refundOpt = refundDAO.findById(refundId);
            if (refundOpt.isEmpty()) {
                return false;
            }

            Refund refund = refundOpt.get();
            String currentUser = SessionManager.getInstance().getCurrentUsername();
            refund.approve(currentUser);
            refund.setUpdatedBy(currentUser);

            // Xử lý hoàn tiền qua gateway nếu cần
            Payment payment = refund.getPayment();
            if (payment.getMethod().isGateway()) {
                PaymentGateway gateway = gateways.get(payment.getMethod());
                if (gateway != null) {
                    refund.markAsProcessing();
                    refundDAO.update(refund);

                    PaymentResponse response = gateway.refund(payment, refund.getAmount(), refund.getReason());
                    if (response.isSuccess()) {
                        refund.markAsCompleted(response.getTransactionId());
                    } else {
                        refund.markAsFailed(response.getMessage());
                    }
                }
            } else {
                // COD hoặc chuyển khoản - đánh dấu hoàn thành ngay
                refund.markAsCompleted(null);
            }

            // Cập nhật payment
            payment.addRefund(refund);
            paymentDAO.update(payment);
            refundDAO.update(refund);

            logger.info("Approved refund: {}", refundId);
            return true;

        } catch (Exception e) {
            logger.error("Error approving refund", e);
            return false;
        }
    }

    /**
     * Từ chối yêu cầu hoàn tiền
     */
    public boolean rejectRefund(Long refundId, String reason) {
        try {
            Optional<Refund> refundOpt = refundDAO.findById(refundId);
            if (refundOpt.isEmpty()) {
                return false;
            }

            Refund refund = refundOpt.get();
            String currentUser = SessionManager.getInstance().getCurrentUsername();
            refund.reject(reason, currentUser);
            refund.setUpdatedBy(currentUser);

            refundDAO.update(refund);
            logger.info("Rejected refund: {}", refundId);
            return true;

        } catch (Exception e) {
            logger.error("Error rejecting refund", e);
            return false;
        }
    }

    /**
     * Lấy danh sách hoàn tiền đang chờ
     */
    public List<Refund> getPendingRefunds() {
        return refundDAO.findPendingRefunds();
    }

    /**
     * Lấy danh sách thanh toán của đơn hàng
     */
    public List<Payment> getPaymentsByOrderId(Long orderId) {
        return paymentDAO.findByOrderId(orderId);
    }

    /**
     * Lấy tất cả thanh toán
     */
    public List<Payment> getAllPayments() {
        return paymentDAO.findAll();
    }

    /**
     * Lấy thanh toán theo ID
     */
    public Payment getPaymentById(Long id) {
        return paymentDAO.findById(id).orElse(null);
    }

    /**
     * Cập nhật thanh toán
     */
    public void updatePayment(Payment payment) {
        paymentDAO.update(payment);
    }

    /**
     * Lấy danh sách hoàn tiền của thanh toán
     */
    public List<Refund> getRefundsByPaymentId(Long paymentId) {
        return refundDAO.findByPaymentId(paymentId);
    }

    /**
     * Tạo mã giao dịch
     */
    private String generateTransactionId() {
        return "PAY" + System.currentTimeMillis();
    }

    /**
     * Tạo mã hoàn tiền
     */
    private String generateRefundNumber() {
        return "REF" + System.currentTimeMillis();
    }
}


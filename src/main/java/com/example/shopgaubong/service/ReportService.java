package com.example.shopgaubong.service;

import com.example.shopgaubong.dao.*;
import com.example.shopgaubong.entity.*;
import com.example.shopgaubong.enums.OrderStatus;
import com.example.shopgaubong.enums.PaymentStatus;
import com.example.shopgaubong.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service để tạo báo cáo và thống kê cho Admin
 */
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);
    private final OrderDAO orderDAO;
    private final PaymentDAO paymentDAO;
    private final ItemDAO itemDAO;
    private final StockItemDAO stockItemDAO;

    public ReportService() {
        this.orderDAO = new OrderDAO();
        this.paymentDAO = new PaymentDAO();
        this.itemDAO = new ItemDAO();
        this.stockItemDAO = new StockItemDAO();
    }

    /**
     * Lấy tổng doanh thu theo khoảng thời gian
     */
    public BigDecimal getTotalRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            List<Order> orders = orderDAO.findByDateRange(startDate, endDate);
            return orders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.CLOSED)
                    .map(Order::getGrandTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            logger.error("Error calculating total revenue: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    /**
     * Lấy doanh thu theo ngày
     */
    public BigDecimal getRevenueByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        return getTotalRevenue(startOfDay, endOfDay);
    }

    /**
     * Lấy doanh thu hôm nay
     */
    public BigDecimal getTodayRevenue() {
        return getRevenueByDate(LocalDate.now());
    }

    /**
     * Lấy doanh thu tuần này
     */
    public BigDecimal getWeekRevenue() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
        return getTotalRevenue(startOfWeek.atStartOfDay(), LocalDateTime.now());
    }

    /**
     * Lấy doanh thu tháng này
     */
    public BigDecimal getMonthRevenue() {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        return getTotalRevenue(startOfMonth.atStartOfDay(), LocalDateTime.now());
    }

    /**
     * Lấy doanh thu năm này
     */
    public BigDecimal getYearRevenue() {
        LocalDate today = LocalDate.now();
        LocalDate startOfYear = today.withDayOfYear(1);
        return getTotalRevenue(startOfYear.atStartOfDay(), LocalDateTime.now());
    }

    /**
     * Lấy số lượng đơn hàng theo trạng thái
     */
    public Map<OrderStatus, Long> getOrderCountByStatus() {
        try {
            List<Order> allOrders = orderDAO.findAll();
            return allOrders.stream()
                    .filter(o -> o.getStatus() != OrderStatus.CART) // Loại bỏ giỏ hàng
                    .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));
        } catch (Exception e) {
            logger.error("Error getting order count by status: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Lấy top sản phẩm bán chạy
     */
    public List<Map<String, Object>> getTopSellingProducts(int limit) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String jpql = "SELECT oi.item.id, oi.item.name, oi.item.sku, SUM(oi.quantity) as totalSold " +
                    "FROM OrderItem oi " +
                    "JOIN oi.order o " +
                    "WHERE o.status = :status " +
                    "GROUP BY oi.item.id, oi.item.name, oi.item.sku " +
                    "ORDER BY totalSold DESC";
            
            Query query = em.createQuery(jpql);
            query.setParameter("status", OrderStatus.CLOSED);
            query.setMaxResults(limit);
            
            List<Object[]> results = query.getResultList();
            List<Map<String, Object>> topProducts = new ArrayList<>();
            
            for (Object[] row : results) {
                Map<String, Object> product = new HashMap<>();
                product.put("itemId", row[0]);
                product.put("name", row[1]);
                product.put("sku", row[2]);
                product.put("totalSold", row[3]);
                topProducts.add(product);
            }
            
            return topProducts;
        } catch (Exception e) {
            logger.error("Error getting top selling products: {}", e.getMessage());
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    /**
     * Lấy doanh thu theo từng ngày trong khoảng thời gian
     */
    public Map<LocalDate, BigDecimal> getDailyRevenue(LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, BigDecimal> dailyRevenue = new LinkedHashMap<>();
        
        // Khởi tạo tất cả các ngày với revenue = 0
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            dailyRevenue.put(currentDate, BigDecimal.ZERO);
            currentDate = currentDate.plusDays(1);
        }
        
        // Tính revenue cho từng ngày
        try {
            List<Order> orders = orderDAO.findByDateRange(
                    startDate.atStartOfDay(), 
                    endDate.atTime(LocalTime.MAX)
            );
            
            orders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.CLOSED)
                    .forEach(order -> {
                        LocalDate orderDate = order.getCreatedAt().toLocalDate();
                        if (!orderDate.isBefore(startDate) && !orderDate.isAfter(endDate)) {
                            dailyRevenue.merge(orderDate, order.getGrandTotal(), BigDecimal::add);
                        }
                    });
        } catch (Exception e) {
            logger.error("Error calculating daily revenue: {}", e.getMessage());
        }
        
        return dailyRevenue;
    }

    /**
     * Lấy thống kê sản phẩm tồn kho thấp
     */
    public List<Map<String, Object>> getLowStockItems() {
        try {
            List<StockItem> lowStockItems = stockItemDAO.findLowStock();
            List<Map<String, Object>> result = new ArrayList<>();
            
            for (StockItem stockItem : lowStockItems) {
                Map<String, Object> itemInfo = new HashMap<>();
                itemInfo.put("itemId", stockItem.getItem().getId());
                itemInfo.put("name", stockItem.getItem().getName());
                itemInfo.put("sku", stockItem.getItem().getSku());
                itemInfo.put("quantity", stockItem.getAvailable());
                itemInfo.put("reorderPoint", stockItem.getLowStockThreshold());
                itemInfo.put("warehouseName", stockItem.getWarehouse().getName());
                result.add(itemInfo);
            }
            
            return result;
        } catch (Exception e) {
            logger.error("Error getting low stock items: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Lấy tổng số đơn hàng theo khoảng thời gian
     */
    public long getTotalOrders(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            List<Order> orders = orderDAO.findByDateRange(startDate, endDate);
            return orders.stream()
                    .filter(o -> o.getStatus() != OrderStatus.CART)
                    .count();
        } catch (Exception e) {
            logger.error("Error getting total orders: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Lấy số đơn hàng hôm nay
     */
    public long getTodayOrders() {
        LocalDate today = LocalDate.now();
        return getTotalOrders(today.atStartOfDay(), LocalDateTime.now());
    }

    /**
     * Lấy giá trị trung bình của đơn hàng
     */
    public BigDecimal getAverageOrderValue(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            List<Order> orders = orderDAO.findByDateRange(startDate, endDate);
            List<Order> completedOrders = orders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.CLOSED)
                    .toList();
            
            if (completedOrders.isEmpty()) {
                return BigDecimal.ZERO;
            }
            
            BigDecimal totalRevenue = completedOrders.stream()
                    .map(Order::getGrandTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            return totalRevenue.divide(
                    BigDecimal.valueOf(completedOrders.size()), 
                    2, 
                    BigDecimal.ROUND_HALF_UP
            );
        } catch (Exception e) {
            logger.error("Error calculating average order value: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    /**
     * Lấy thống kê thanh toán theo phương thức
     */
    public Map<String, Long> getPaymentMethodStats() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String jpql = "SELECT p.method, COUNT(p) FROM Payment p " +
                    "WHERE p.status = :status " +
                    "GROUP BY p.method";
            
            Query query = em.createQuery(jpql);
            query.setParameter("status", PaymentStatus.COMPLETED);
            
            List<Object[]> results = query.getResultList();
            Map<String, Long> stats = new HashMap<>();
            
            for (Object[] row : results) {
                stats.put(row[0].toString(), (Long) row[1]);
            }
            
            return stats;
        } catch (Exception e) {
            logger.error("Error getting payment method stats: {}", e.getMessage());
            return new HashMap<>();
        } finally {
            em.close();
        }
    }

    /**
     * Lấy doanh thu theo tháng trong năm
     */
    public Map<Integer, BigDecimal> getMonthlyRevenueForYear(int year) {
        Map<Integer, BigDecimal> monthlyRevenue = new LinkedHashMap<>();
        
        // Khởi tạo 12 tháng với revenue = 0
        for (int month = 1; month <= 12; month++) {
            monthlyRevenue.put(month, BigDecimal.ZERO);
        }
        
        // Tính revenue cho từng tháng
        try {
            LocalDateTime startOfYear = LocalDate.of(year, 1, 1).atStartOfDay();
            LocalDateTime endOfYear = LocalDate.of(year, 12, 31).atTime(LocalTime.MAX);
            
            List<Order> orders = orderDAO.findByDateRange(startOfYear, endOfYear);
            
            orders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.CLOSED)
                    .forEach(order -> {
                        int month = order.getCreatedAt().getMonthValue();
                        monthlyRevenue.merge(month, order.getGrandTotal(), BigDecimal::add);
                    });
        } catch (Exception e) {
            logger.error("Error calculating monthly revenue: {}", e.getMessage());
        }
        
        return monthlyRevenue;
    }

    /**
     * Lấy thống kê tổng quan cho dashboard
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            stats.put("todayRevenue", getTodayRevenue());
            stats.put("weekRevenue", getWeekRevenue());
            stats.put("monthRevenue", getMonthRevenue());
            stats.put("yearRevenue", getYearRevenue());
            stats.put("todayOrders", getTodayOrders());
            stats.put("ordersByStatus", getOrderCountByStatus());
            stats.put("topSellingProducts", getTopSellingProducts(10));
            stats.put("lowStockItems", getLowStockItems());
            stats.put("averageOrderValue", getAverageOrderValue(
                    LocalDate.now().withDayOfMonth(1).atStartOfDay(), 
                    LocalDateTime.now()
            ));
        } catch (Exception e) {
            logger.error("Error getting dashboard stats: {}", e.getMessage());
        }
        
        return stats;
    }

    /**
     * Lấy số lượng khách hàng mới theo khoảng thời gian
     */
    public long getNewCustomerCount(LocalDateTime startDate, LocalDateTime endDate) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String jpql = "SELECT COUNT(a) FROM Account a " +
                    "WHERE a.role = 'CUSTOMER' " +
                    "AND a.createdAt BETWEEN :startDate AND :endDate";
            
            Query query = em.createQuery(jpql);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            
            return (Long) query.getSingleResult();
        } catch (Exception e) {
            logger.error("Error getting new customer count: {}", e.getMessage());
            return 0;
        } finally {
            em.close();
        }
    }

    /**
     * Lấy tỷ lệ hoàn thành đơn hàng
     */
    public double getOrderCompletionRate(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            List<Order> orders = orderDAO.findByDateRange(startDate, endDate);
            long totalOrders = orders.stream()
                    .filter(o -> o.getStatus() != OrderStatus.CART)
                    .count();
            
            if (totalOrders == 0) {
                return 0.0;
            }
            
            long completedOrders = orders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.CLOSED)
                    .count();
            
            return (double) completedOrders / totalOrders * 100;
        } catch (Exception e) {
            logger.error("Error calculating order completion rate: {}", e.getMessage());
            return 0.0;
        }
    }
}

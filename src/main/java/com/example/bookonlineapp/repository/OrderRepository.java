package com.example.bookonlineapp.repository;

import com.example.bookonlineapp.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {


    // Hàm tìm tất cả các đơn hàng của một người mua (Buyer) cụ thể,
    // sắp xếp theo thời gian đặt hàng mới nhất lên đầu
    List<Order> findByBuyer_IdOrderByOrderDateDesc(Long buyerId);

    List<Order> findByBookSellerIdOrderByOrderDateDesc(Long id);

    List<Order> findByBookSellerIdAndStatusOrderByOrderDateDesc(Long id, String status);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.buyer.id = :buyerId")
    BigDecimal sumTotalAmountByBuyerId(@Param("buyerId") Long buyerId);

    // 1. Tính tổng tiền đã chi (khi mua hàng với vai trò là buyer)
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.buyer.id = :buyerId")
    BigDecimal sumTotalSpentByBuyerId(@Param("buyerId") Long buyerId);

    // 2. Tính tổng tiền đã thu (khi bán được sách thông qua quan hệ Book -> Seller)
    @Query("SELECT SUM(o.totalAmount) FROM Order o JOIN o.book b WHERE b.seller.id = :sellerId")
    BigDecimal sumTotalEarnedBySellerId(@Param("sellerId") Long sellerId);

    // 1. Lấy tất cả đơn hàng sắp xếp mới nhất
    @Query("SELECT o FROM Order o ORDER BY o.orderDate DESC")
    List<Order> findAllByOrderByOrderDateDesc();

    // 2. Tìm kiếm giao dịch (Sử dụng hàm này thay thế hoàn toàn)
    @Query("SELECT o FROM Order o WHERE LOWER(o.buyer.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(o.book.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Order> searchTransactions(@Param("keyword") String keyword);
}
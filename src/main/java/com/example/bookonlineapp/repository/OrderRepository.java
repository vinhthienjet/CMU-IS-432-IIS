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
}
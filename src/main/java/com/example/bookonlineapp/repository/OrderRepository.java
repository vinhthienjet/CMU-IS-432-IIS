package com.example.bookonlineapp.repository;

import com.example.bookonlineapp.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Hàm tìm tất cả các đơn hàng của một người mua (Buyer) cụ thể,
    // sắp xếp theo thời gian đặt hàng mới nhất lên đầu
    List<Order> findByBuyer_IdOrderByOrderDateDesc(Long buyerId);
}
package com.example.bookonlineapp.repository;

import com.example.bookonlineapp.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    // Tìm một sản phẩm cụ thể trong giỏ hàng
    Optional<CartItem> findByCartIdAndBookId(Long cartId, Long bookId);

    // Lấy toàn bộ sản phẩm trong một giỏ hàng (để đếm số lượng)
    List<CartItem> findByCartId(Long cartId);
}
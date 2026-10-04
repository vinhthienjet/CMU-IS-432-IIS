package com.example.bookonlineapp.repository;

import com.example.bookonlineapp.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    // Tìm giỏ hàng theo ID người dùng
    Optional<Cart> findByUserId(Long userId);
}
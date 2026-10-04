package com.example.bookonlineapp.repository;

import com.example.bookonlineapp.entity.Book;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    // Tìm tất cả sách của một người bán, có hỗ trợ sắp xếp
    List<Book> findBySellerId(Long sellerId, Sort sort);

    // Tìm sách của người bán lọc theo trạng thái, có hỗ trợ sắp xếp
    List<Book> findBySellerIdAndStatus(Long sellerId, String status, Sort sort);
}
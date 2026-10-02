package com.example.bookonlineapp.repository;

import com.example.bookonlineapp.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}
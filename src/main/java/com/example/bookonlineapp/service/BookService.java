package com.example.bookonlineapp.service;

import com.example.bookonlineapp.entity.Book;

import java.util.List;

public interface BookService {

    List<Book> getAllBooks();

    Book getBookById(Long id);
}
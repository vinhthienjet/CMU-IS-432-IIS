package com.example.bookonlineapp.controller;

import com.example.bookonlineapp.entity.Book;
import com.example.bookonlineapp.service.BookService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/books/{id}")
    public String bookDetail(@PathVariable Long id, Model model) {

        Book book = bookService.getBookById(id);

        if (book == null) {
            return "redirect:/";
        }

        model.addAttribute("book", book);

        return "book-detail";
    }
}
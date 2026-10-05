package com.example.bookonlineapp.controller;

import com.example.bookonlineapp.entity.Order;
import com.example.bookonlineapp.entity.User;
import com.example.bookonlineapp.repository.OrderRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminTransactionController {

    private final OrderRepository orderRepository;

    public AdminTransactionController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // Hiển thị danh sách tất cả giao dịch và hỗ trợ tìm kiếm
    @GetMapping("/transactions")
    public String manageTransactions(
            @RequestParam(required = false) String keyword,
            HttpSession session,
            Model model) {

        // Kiểm tra quyền Admin
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
            return "redirect:/login";
        }

        List<Order> transactions;
        if (keyword != null && !keyword.trim().isEmpty()) {
            // Gọi đúng tên hàm mới đã viết trong Repository
            transactions = orderRepository.searchTransactions(keyword);
        } else {
            transactions = orderRepository.findAllByOrderByOrderDateDesc();
        }

        model.addAttribute("transactions", transactions);
        model.addAttribute("keyword", keyword);

        return "admin-transactions"; // Trỏ tới file giao diện admin-transactions.html
    }
}
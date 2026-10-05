package com.example.bookonlineapp.controller;

import com.example.bookonlineapp.entity.User;
import com.example.bookonlineapp.repository.OrderRepository;
import com.example.bookonlineapp.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.math.BigDecimal;

@Controller
public class LoginController {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository; // Thêm OrderRepository

    // Cập nhật constructor để inject OrderRepository
    public LoginController(UserRepository userRepository, OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    // Hiển thị giao diện đăng nhập
    @GetMapping("/login")
    public String showLoginForm(HttpSession session) {
        if (session.getAttribute("loggedInUser") != null) {
            return "redirect:/";
        }
        return "login";
    }

    // Xử lý logic đăng nhập
    @PostMapping("/login")
    public String processLogin(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        User user = userRepository.findByUsername(username).orElse(null);

        if (user != null && user.getPassword().equals(password)) {
            // Đăng nhập thành công -> Lưu thông tin vào session
            session.setAttribute("loggedInUser", user);

            // ==========================================
            // TÍNH TOÁN VÀ LƯU DANH HIỆU VÀO SESSION
            // ==========================================
            BigDecimal totalSpent = orderRepository.sumTotalAmountByBuyerId(user.getId());
            double spentValue = (totalSpent != null) ? totalSpent.doubleValue() : 0.0;

            String rank;
            if (spentValue > 20000) {
                rank = "VIP";
            } else if (spentValue >= 15000) {
                rank = "Bạc"; // Hoặc Kim cương tùy bạn phân mốc
            } else if (spentValue >= 10000) {
                rank = "Đồng";
            } else if (spentValue >= 5000) {
                rank = "New"; // Hoặc tùy chỉnh theo yêu cầu mốc của bạn
            } else {
                rank = "New";
            }

            // Lưu danh hiệu vào session để gọi ra ở giao diện HTML
            session.setAttribute("userRank", rank);
            // ==========================================

            return "redirect:/";
        } else {
            model.addAttribute("error", "Tên đăng nhập hoặc mật khẩu không chính xác!");
            return "login";
        }
    }

    // Xử lý đăng xuất
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    // Hiển thị giao diện Đăng ký
    @GetMapping("/register")
    public String showRegisterForm(HttpSession session) {
        if (session.getAttribute("loggedInUser") != null) {
            return "redirect:/";
        }
        return "register";
    }

    // Xử lý logic khi bấm nút Đăng ký
    @PostMapping("/register")
    public String processRegister(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String address,
            Model model) {

        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Tên đăng nhập này đã có người sử dụng!");
            return "register";
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setFullName(fullName);
        newUser.setEmail(email);
        newUser.setPhone(phone);
        newUser.setAddress(address);
        newUser.setRole("USER");

        userRepository.save(newUser);

        model.addAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
        return "login";
    }
}
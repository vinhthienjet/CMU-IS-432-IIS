package com.example.bookonlineapp.controller;

import com.example.bookonlineapp.entity.User;
import com.example.bookonlineapp.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 1. Chỉ hiển thị thông tin cá nhân (Read-only)
    @GetMapping("/profile")
    public String viewProfile(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        User currentUser = userRepository.findById(loggedInUser.getId()).orElse(null);
        model.addAttribute("user", currentUser);
        return "profile";
    }

    // 2. Xác nhận mật khẩu trước khi cho phép chỉnh sửa
    @PostMapping("/profile/verify")
    public String verifyPassword(@RequestParam String password, HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        User currentUser = userRepository.findById(loggedInUser.getId()).orElse(null);

        // Kiểm tra mật khẩu
        if (currentUser != null && currentUser.getPassword().equals(password)) {
            // Đúng mật khẩu -> Trả về trang form chỉnh sửa
            model.addAttribute("user", currentUser);
            return "profile-edit";
        } else {
            // Sai mật khẩu -> Báo lỗi, quay lại trang xem thông tin
            model.addAttribute("user", currentUser);
            model.addAttribute("error", "Mật khẩu không chính xác. Vui lòng thử lại!");
            return "profile";
        }
    }

    // 3. Xử lý lưu thông tin sau khi chỉnh sửa
    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String address,
            @RequestParam(required = false, defaultValue = "") String avatar,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        User user = userRepository.findById(loggedInUser.getId()).orElse(null);
        if (user != null) {
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPhone(phone);
            user.setAddress(address);

            if (!avatar.isEmpty()) {
                user.setAvatar(avatar);
            }

            userRepository.save(user);
            session.setAttribute("loggedInUser", user);

            // Thông báo lưu thành công
            redirectAttributes.addFlashAttribute("success", "Đã sửa thông tin cá nhân thành công!");
        }

        return "redirect:/profile";
    }
}
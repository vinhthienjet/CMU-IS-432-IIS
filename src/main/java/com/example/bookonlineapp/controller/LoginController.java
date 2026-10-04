package com.example.bookonlineapp.controller;

import com.example.bookonlineapp.entity.User;
import com.example.bookonlineapp.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    private final UserRepository userRepository;

    public LoginController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Hiển thị giao diện đăng nhập
    @GetMapping("/login")
    public String showLoginForm(HttpSession session) {
        // Nếu người dùng ĐÃ ĐĂNG NHẬP, chuyển hướng thẳng về trang chủ (không cho hiện form login nữa)
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

        // Gộp chung kiểm tra null và mật khẩu.
        // Ghi chú: Dùng thông báo lỗi chung sẽ an toàn hơn, tránh hacker dò biết username có tồn tại hay không.
        if (user != null && user.getPassword().equals(password)) {

            // Đăng nhập thành công -> Lưu thông tin vào 1 biến duy nhất khớp với home.html
            session.setAttribute("loggedInUser", user);

            return "redirect:/";
        } else {
            // Thất bại
            model.addAttribute("error", "Tên đăng nhập hoặc mật khẩu không chính xác!");
            return "login";
        }
    }

    // Xử lý đăng xuất
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // Hủy bỏ toàn bộ session hiện tại
        session.invalidate();
        return "redirect:/";
    }

    // Hiển thị giao diện Đăng ký
    @GetMapping("/register")
    public String showRegisterForm(HttpSession session) {
        if (session.getAttribute("loggedInUser") != null) {
            return "redirect:/"; // Đã đăng nhập thì không cho vào trang đăng ký
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

        // Kiểm tra xem username đã tồn tại trong database chưa
        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Tên đăng nhập này đã có người sử dụng!");
            return "register";
        }

        // Tạo đối tượng User mới và gán dữ liệu
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setFullName(fullName);
        newUser.setEmail(email);
        newUser.setPhone(phone);
        newUser.setAddress(address);

        // Gán quyền mặc định cho người đăng ký mới là "USER"
        newUser.setRole("USER");

        // Lưu vào database
        userRepository.save(newUser);

        // Chuyển hướng về trang đăng nhập và gửi kèm thông báo thành công
        model.addAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
        return "login";
    }

}
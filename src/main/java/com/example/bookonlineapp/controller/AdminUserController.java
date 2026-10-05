package com.example.bookonlineapp.controller;

import com.example.bookonlineapp.entity.User;
import com.example.bookonlineapp.repository.OrderRepository;
import com.example.bookonlineapp.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminUserController {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public AdminUserController(UserRepository userRepository, OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    // 1. Hiển thị danh sách người dùng (Hỗ trợ tìm kiếm và sắp xếp)
    @GetMapping("/users")
    public String manageUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "id_asc") String sort,
            HttpSession session,
            Model model) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
            return "redirect:/login";
        }

        // Cấu hình sắp xếp
        Sort sortConfig;
        if ("name_asc".equals(sort)) {
            sortConfig = Sort.by(Sort.Direction.ASC, "fullName");
        } else {
            sortConfig = Sort.by(Sort.Direction.ASC, "id");
        }

        // Lấy danh sách user (có tìm kiếm theo từ khóa hoặc lấy tất cả)
        List<User> users;
        if (keyword != null && !keyword.trim().isEmpty()) {
            users = userRepository.findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCase(keyword, keyword);
        } else {
            users = userRepository.findAll(sortConfig);
        }

        // Tính toán số tiền đã chi và đã thu cho mỗi user để hiển thị ra bảng
        Map<Long, BigDecimal> totalSpentMap = new HashMap<>();
        Map<Long, BigDecimal> totalEarnedMap = new HashMap<>();

        for (User u : users) {
            BigDecimal spent = orderRepository.sumTotalSpentByBuyerId(u.getId());
            BigDecimal earned = orderRepository.sumTotalEarnedBySellerId(u.getId());

            totalSpentMap.put(u.getId(), spent != null ? spent : BigDecimal.ZERO);
            totalEarnedMap.put(u.getId(), earned != null ? earned : BigDecimal.ZERO);
        }

        model.addAttribute("users", users);
        model.addAttribute("totalSpentMap", totalSpentMap);
        model.addAttribute("totalEarnedMap", totalEarnedMap);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentSort", sort);

        return "admin-users";
    }

    // 2. Thêm người dùng mới (Yêu cầu mật khẩu Admin xác nhận)
    @PostMapping("/users/add")
    public String addUser(
            @RequestParam String adminPassword,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String address,
            @RequestParam String role,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null || !"ADMIN".equals(admin.getRole())) return "redirect:/login";

        // Kiểm tra mật khẩu Admin
        if (!admin.getPassword().equals(adminPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu Admin không chính xác!");
            return "redirect:/admin/users";
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setFullName(fullName);
        newUser.setEmail(email);
        newUser.setPhone(phone);
        newUser.setAddress(address);
        newUser.setRole(role);

        userRepository.save(newUser);
        redirectAttributes.addFlashAttribute("success", "Đã thêm người dùng mới thành công!");
        return "redirect:/admin/users";
    }

    // 3. Xóa người dùng (Yêu cầu mật khẩu Admin xác nhận)
    @PostMapping("/users/delete")
    public String deleteUser(
            @RequestParam Long userId,
            @RequestParam String adminPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null || !"ADMIN".equals(admin.getRole())) return "redirect:/login";

        if (!admin.getPassword().equals(adminPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu Admin không chính xác!");
            return "redirect:/admin/users";
        }

        userRepository.deleteById(userId);
        redirectAttributes.addFlashAttribute("success", "Đã xóa người dùng thành công!");
        return "redirect:/admin/users";
    }

    // XỬ LÝ SỬA THÔNG TIN NGƯỜI DÙNG TỪ TRANG ADMIN
    @PostMapping("/users/edit")
    public String editUser(
            @RequestParam Long userId,
            @RequestParam String username,
            @RequestParam String fullName,
            @RequestParam(required = false) String password,
            @RequestParam String role,
            @RequestParam String adminPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // 1. Kiểm tra tài khoản Admin đang đăng nhập
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
            return "redirect:/login";
        }

        // 2. Xác thực mật khẩu của Admin hiện tại để đảm bảo an toàn
        if (!loggedInUser.getPassword().equals(adminPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu Admin xác nhận không chính xác!");
            return "redirect:/admin/users";
        }

        // 3. Tìm user cần sửa trong Database
        User userToEdit = userRepository.findById(userId).orElse(null);
        if (userToEdit == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng cần sửa!");
            return "redirect:/admin/users";
        }

        // 4. Cập nhật thông tin mới
        userToEdit.setUsername(username);
        userToEdit.setFullName(fullName);
        userToEdit.setRole(role);

        // Chỉ cập nhật mật khẩu mới nếu Admin có nhập vào ô "Mật khẩu mới"
        if (password != null && !password.trim().isEmpty()) {
            userToEdit.setPassword(password);
        }

        // 5. Lưu lại xuống CSDL
        userRepository.save(userToEdit);

        redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin người dùng thành công!");
        return "redirect:/admin/users";
    }
}
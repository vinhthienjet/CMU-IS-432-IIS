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

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        User user = userRepository.findByUsername(username)
                .orElse(null);

        if (user == null) {
            model.addAttribute("error", "Username không tồn tại");
            return "login";
        }

        if (!user.getPassword().equals(password)) {
            model.addAttribute("error", "Password không đúng");
            return "login";
        }

        session.setAttribute("user", user);

        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {

        session.invalidate();

        return "redirect:/";
    }
}
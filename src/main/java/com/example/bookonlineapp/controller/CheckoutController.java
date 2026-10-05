package com.example.bookonlineapp.controller;

import com.example.bookonlineapp.entity.CartItem;
import com.example.bookonlineapp.entity.Order;
import com.example.bookonlineapp.entity.User;
import com.example.bookonlineapp.repository.CartItemRepository;
import com.example.bookonlineapp.repository.OrderRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class CheckoutController {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;

    public CheckoutController(OrderRepository orderRepository, CartItemRepository cartItemRepository) {
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
    }

    // CHỨC NĂNG 1: Thanh toán tất cả sản phẩm trong giỏ hàng
    @PostMapping("/checkout")
    public String processCheckout(
            @RequestParam String paymentMethod,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        List<CartItem> cartItems = cartItemRepository.findByUser_Id(loggedInUser.getId());
        if (cartItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Giỏ hàng trống!");
            return "redirect:/cart";
        }

        for (CartItem item : cartItems) {
            Order order = new Order();
            order.setBuyer(loggedInUser);
            order.setBook(item.getBook());
            order.setQuantity(item.getQuantity());
            order.setPrice(item.getBook().getPrice() * item.getQuantity());
            order.setPaymentMethod(paymentMethod);
            order.setStatus("Đợi phê duyệt");
            order.setOrderDate(LocalDateTime.now());

            orderRepository.save(order);
        }

        // Xóa giỏ hàng sau khi đặt thành công
        cartItemRepository.deleteAll(cartItems);
        session.setAttribute("cartCount", 0);

        redirectAttributes.addFlashAttribute("success", "Thanh toán giỏ hàng thành công!");
        return "redirect:/my-orders";
    }
}
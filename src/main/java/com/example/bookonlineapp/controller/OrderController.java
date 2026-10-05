package com.example.bookonlineapp.controller;

import com.example.bookonlineapp.entity.Book;
import com.example.bookonlineapp.entity.Order;
import com.example.bookonlineapp.entity.User;
import com.example.bookonlineapp.repository.BookRepository;
import com.example.bookonlineapp.repository.OrderRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class OrderController {

    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;

    public OrderController(OrderRepository orderRepository, BookRepository bookRepository) {
        this.orderRepository = orderRepository;
        this.bookRepository = bookRepository;
    }

    @GetMapping("/my-orders")
    public String myOrders(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        List<Order> myOrders = orderRepository.findByBuyer_IdOrderByOrderDateDesc(loggedInUser.getId());
        model.addAttribute("orders", myOrders);

        return "my-orders";
    }

    @PostMapping("/buy-now")
    public String buyNow(
            @RequestParam Long bookId,
            @RequestParam Integer quantity,
            @RequestParam String paymentMethod,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            User loggedInUser = (User) session.getAttribute("loggedInUser");

            if (loggedInUser == null) {
                return "redirect:/login";
            }

            Book book = bookRepository.findById(bookId).orElse(null);

            if (book == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy sách!");
                return "redirect:/";
            }

            // Kiểm tra số lượng
            if (quantity == null || quantity <= 0) {
                redirectAttributes.addFlashAttribute("error", "Số lượng không hợp lệ!");
                return "redirect:/";
            }

            // ==========================================
            // BỔ SUNG: XỬ LÝ TRỪ KHO HOẶC ĐỔI TRẠNG THÁI SÁCH NGAY KHI MUA
            // ==========================================
            if (book.getQuantity() > quantity) {
                // Nếu kho còn nhiều hơn số lượng khách mua -> Giảm số lượng tồn kho
                book.setQuantity(book.getQuantity() - quantity);
                bookRepository.save(book);

                // (Tùy chọn) Nếu bạn muốn tạo dòng bản ghi mới hiện là SOLD giống ý bạn muốn:
                Book soldBook = new Book();
                soldBook.setTitle(book.getTitle());
                soldBook.setAuthor(book.getAuthor());
                soldBook.setPrice(book.getPrice());
                soldBook.setCategory(book.getCategory());
                soldBook.setDescription(book.getDescription());
                soldBook.setImage(book.getImage());
                soldBook.setSeller(book.getSeller());
                soldBook.setQuantity(quantity);
                soldBook.setStatus("SOLD");
                bookRepository.save(soldBook);

            } else {
                // Nếu khách mua hết sạch số lượng -> Đổi trạng thái sách thành SOLD luôn
                book.setStatus("SOLD");
                book.setQuantity(0); // hoặc giữ nguyên tuỳ bạn
                bookRepository.save(book);
            }
            // ==========================================

            // Giá 1 cuốn
            BigDecimal price = BigDecimal.valueOf(book.getPrice());

            // Tổng tiền = giá 1 cuốn × số lượng
            BigDecimal totalAmount = price.multiply(
                    BigDecimal.valueOf(quantity)
            );

            // Tạo đơn hàng
            Order newOrder = new Order();

            newOrder.setBuyer(loggedInUser);
            newOrder.setBook(book);
            newOrder.setQuantity(quantity);
            newOrder.setPrice(book.getPrice());
            newOrder.setTotalAmount(totalAmount);
            newOrder.setPaymentMethod(paymentMethod);
            newOrder.setStatus("Đợi phê duyệt");
            newOrder.setOrderDate(LocalDateTime.now());

            // Lưu đơn hàng
            orderRepository.save(newOrder);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Đặt hàng thành công!"
            );

            return "redirect:/my-orders";

        } catch (Exception e) {
            System.err.println("=== LỖI KHI MUA NGAY: " + e.getMessage() + " ===");
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống: " + e.getMessage());
            return "redirect:/";
        }
    }

}
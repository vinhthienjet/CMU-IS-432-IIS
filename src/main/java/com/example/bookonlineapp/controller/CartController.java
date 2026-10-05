package com.example.bookonlineapp.controller;

import com.example.bookonlineapp.entity.Book;
import com.example.bookonlineapp.entity.Cart;
import com.example.bookonlineapp.entity.CartItem;
import com.example.bookonlineapp.entity.User;
import com.example.bookonlineapp.repository.BookRepository;
import com.example.bookonlineapp.repository.CartItemRepository;
import com.example.bookonlineapp.repository.CartRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class CartController {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;

    public CartController(CartRepository cartRepository, CartItemRepository cartItemRepository, BookRepository bookRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.bookRepository = bookRepository;
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("bookId") Long bookId,
                            @RequestParam("quantity") int quantity,
                            HttpSession session) {

        // 1. Kiểm tra đăng nhập
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login"; // Chưa đăng nhập thì bắt đi đăng nhập
        }

        // 2. Lấy giỏ hàng của User, nếu chưa có thì tạo mới
        Cart cart = cartRepository.findByUserId(user.getId()).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });

        // 3. Xử lý thêm sản phẩm
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book != null) {
            // Kiểm tra sách đã có trong giỏ chưa
            CartItem cartItem = cartItemRepository.findByCartIdAndBookId(cart.getId(), book.getId()).orElse(null);

            if (cartItem == null) {
                // Sách chưa có: Tạo mới CartItem
                cartItem = new CartItem();
                cartItem.setCart(cart);
                cartItem.setBook(book);
                cartItem.setQuantity(quantity);
            } else {
                // Sách đã có: Cộng dồn số lượng
                cartItem.setQuantity(cartItem.getQuantity() + quantity);
            }
            cartItemRepository.save(cartItem);
        }

        // 4. ĐẾM SỐ LƯỢNG HÀNG TRONG GIỎ VÀ LƯU VÀO SESSION
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        int totalItems = 0;
        for (CartItem item : cartItems) {
            totalItems += item.getQuantity(); // Hoặc dùng totalItems++ nếu bạn chỉ muốn đếm số MẶT HÀNG thay vì TỔNG SỐ LƯỢNG
        }
        session.setAttribute("cartCount", totalItems);

        // Trở về lại trang chi tiết sách vừa xem
        return "redirect:/cart";
    }

    // 1. SỬA LẠI HÀM HIỂN THỊ GIỎ HÀNG (để luôn đếm đúng số lượng)
    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        Cart cart = cartRepository.findByUserId(user.getId()).orElse(null);
        if (cart != null) {
            List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
            model.addAttribute("cartItems", cartItems);

            double totalPrice = 0;
            int totalItems = 0; // Biến đếm số lượng

            for (CartItem item : cartItems) {
                totalPrice += item.getBook().getPrice() * item.getQuantity();
                totalItems += item.getQuantity(); // Đếm tổng số cuốn sách
                // Lưu ý: Nếu bạn chỉ muốn đếm số MẶT HÀNG (ví dụ 2 loại sách thì hiển thị số 2), hãy sửa thành: totalItems = cartItems.size();
            }

            model.addAttribute("totalPrice", totalPrice);
            session.setAttribute("cartCount", totalItems); // Cập nhật lại session mỗi khi vào giỏ hàng
        } else {
            model.addAttribute("cartItems", List.of());
            model.addAttribute("totalPrice", 0);
            session.setAttribute("cartCount", 0);
        }
        return "cart";
    }

    // 2. THÊM HÀM TĂNG SỐ LƯỢNG (+)
    @GetMapping("/cart/increase/{id}")
    public String increaseQuantity(@PathVariable Long id) {
        CartItem item = cartItemRepository.findById(id).orElse(null);
        if (item != null) {
            item.setQuantity(item.getQuantity() + 1);
            cartItemRepository.save(item);
        }
        return "redirect:/cart";
    }

    // 3. THÊM HÀM GIẢM SỐ LƯỢNG (-)
    @GetMapping("/cart/decrease/{id}")
    public String decreaseQuantity(@PathVariable Long id) {
        CartItem item = cartItemRepository.findById(id).orElse(null);
        if (item != null) {
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                cartItemRepository.save(item);
            } else {
                // Nếu số lượng lùi về 0 thì tự động xóa khỏi giỏ
                cartItemRepository.delete(item);
            }
        }
        return "redirect:/cart";
    }

    // 4. THÊM HÀM XÓA HẲN SẢN PHẨM
    @GetMapping("/cart/remove/{id}")
    public String removeItem(@PathVariable Long id) {
        cartItemRepository.findById(id).ifPresent(cartItemRepository::delete);
        return "redirect:/cart";
    }

}
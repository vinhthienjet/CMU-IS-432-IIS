package com.example.bookonlineapp.controller;

import com.example.bookonlineapp.entity.Book;
import com.example.bookonlineapp.entity.User;
import com.example.bookonlineapp.repository.BookRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/seller")
public class SellerController {

    private final BookRepository bookRepository;

    public SellerController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    // TAB 1: SẢN PHẨM CỦA BẠN
    @GetMapping("/products")
    public String manageProducts(
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @RequestParam(required = false, defaultValue = "time_asc") String sort,
            HttpSession session,
            Model model) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        // 1. Cấu hình Sắp xếp
        Sort sortConfig;
        if ("price_asc".equals(sort)) {
            sortConfig = Sort.by(Sort.Direction.ASC, "price"); // Giá tăng dần
        } else if ("price_desc".equals(sort)) {
            sortConfig = Sort.by(Sort.Direction.DESC, "price"); // Giá giảm dần
        } else {
            sortConfig = Sort.by(Sort.Direction.ASC, "id"); // Up trước nằm trước, up sau nằm sau (ID cũ nhất lên đầu)
        }

        // 2. Lấy dữ liệu lọc theo trạng thái
        List<Book> myBooks;
        if ("ALL".equals(status)) {
            myBooks = bookRepository.findBySellerId(loggedInUser.getId(), sortConfig);
        } else {
            myBooks = bookRepository.findBySellerIdAndStatus(loggedInUser.getId(), status, sortConfig);
        }

        model.addAttribute("books", myBooks);
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentSort", sort);

        return "seller-products";
    }

    // TAB 2: QUẢN LÝ DOANH THU (Tạm thời tạo khung)
    @GetMapping("/revenue")
    public String manageRevenue(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        // Logic doanh thu sẽ được code ở bước tiếp theo
        return "seller-revenue";
    }

    // Hàm kiểm tra mật khẩu trước khi Sửa/Xóa
    @PostMapping("/verify-action")
    public String verifyAction(
            @RequestParam String password,
            @RequestParam String action,
            @RequestParam Long bookId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        // Kiểm tra mật khẩu
        if (!loggedInUser.getPassword().equals(password)) {
            // Sai mật khẩu -> Báo lỗi và truyền lại action, bookId để mở lại popup
            redirectAttributes.addFlashAttribute("error", "Mật khẩu không chính xác!");
            redirectAttributes.addFlashAttribute("lastAction", action);
            redirectAttributes.addFlashAttribute("lastBookId", bookId);
            return "redirect:/seller/products";
        }

        // ĐÚNG MẬT KHẨU -> Xử lý theo hành động (action)
        if ("delete".equals(action)) {
            bookRepository.deleteById(bookId); // Xóa sách
            redirectAttributes.addFlashAttribute("success", "Đã xóa sách thành công!");
            return "redirect:/seller/products";

        } else if ("edit".equals(action)) {
            // Chuyển hướng sang trang Form sửa sách (Bạn sẽ tạo giao diện trang này sau)
            return "redirect:/seller/book/edit/" + bookId;
        }

        return "redirect:/seller/products";
    }

    // Hiển thị form sửa sách
    @GetMapping("/book/edit/{id}")
    public String showEditBookForm(@PathVariable Long id, HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        Book book = bookRepository.findById(id).orElse(null);
        if (book == null || !book.getSeller().getId().equals(loggedInUser.getId())) {
            return "redirect:/seller/products"; // Nếu không tìm thấy hoặc không phải sách của mình thì đẩy ra ngoài
        }

        model.addAttribute("book", book);
        // Lưu ý: Nếu bạn có bảng Categories, bạn cần truyền list category vào đây để hiển thị ra thẻ <select>
        return "seller-book-edit";
    }

    // Xử lý lưu thông tin sách sau khi sửa
    @PostMapping("/book/update")
    public String updateBook(
            @RequestParam Long id,
            @RequestParam String title,
            @RequestParam Double price,
            @RequestParam String description,
            @RequestParam String status,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        Book book = bookRepository.findById(id).orElse(null);
        if (book != null && book.getSeller().getId().equals(loggedInUser.getId())) {
            book.setTitle(title);
            book.setPrice(price);
            book.setDescription(description);
            book.setStatus(status); // Cập nhật trạng thái (Đang bán -> Đã bán)

            bookRepository.save(book);
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật thông tin sách thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật!");
        }

        return "redirect:/seller/products";
    }

}
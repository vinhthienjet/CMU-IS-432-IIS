package com.example.bookonlineapp.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.bookonlineapp.entity.Book;
import com.example.bookonlineapp.entity.Category;
import com.example.bookonlineapp.entity.User;
import com.example.bookonlineapp.repository.BookRepository;
import com.example.bookonlineapp.repository.CategoryRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.util.Map;
import com.example.bookonlineapp.repository.OrderRepository;
import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.bookonlineapp.entity.Order;
import java.util.List;
import org.springframework.transaction.annotation.Transactional; // Thêm import này để hỗ trợ xóa dữ liệu

@Controller
@RequestMapping("/seller")
public class SellerController {

    private final BookRepository bookRepository;
    private final OrderRepository orderRepository;
    private final CategoryRepository categoryRepository;

    public SellerController(BookRepository bookRepository, OrderRepository orderRepository, CategoryRepository categoryRepository) {
        this.bookRepository = bookRepository;
        this.orderRepository = orderRepository;
        this.categoryRepository = categoryRepository;
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
            sortConfig = Sort.by(Sort.Direction.ASC, "id"); // ID cũ nhất lên đầu
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

    // Hàm kiểm tra mật khẩu trước khi Sửa/Xóa
    @PostMapping("/verify-action")
    @Transactional // Đảm bảo thực thi giao dịch đồng bộ khi xóa dữ liệu liên quan
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
            // 1. Xóa các đơn hàng (orders) liên quan đến sách này trước để tránh lỗi khóa ngoại Foreign Key
            orderRepository.deleteByBookId(bookId);

            // 2. Tiến hành xóa hẳn sách khỏi cơ sở dữ liệu
            bookRepository.deleteById(bookId);

            redirectAttributes.addFlashAttribute("success", "Đã xóa hẳn sách thành công!");
            return "redirect:/seller/products";

        } else if ("edit".equals(action)) {
            // Chuyển hướng sang trang Form sửa sách
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
            return "redirect:/seller/products";
        }

        model.addAttribute("book", book);
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
            book.setStatus(status);

            bookRepository.save(book);
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật thông tin sách thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật!");
        }

        return "redirect:/seller/products";
    }

    @GetMapping("/revenue")
    public String manageRevenue(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        // 1. Lấy toàn bộ lịch sử giao dịch của người bán này
        List<Order> transactions = orderRepository.findByBookSellerIdOrderByOrderDateDesc(loggedInUser.getId());

        // 2. Tính toán tổng quan
        int totalBooksSold = transactions.size();
        double totalRevenue = 0;
        for (Order order : transactions) {
            totalRevenue += order.getPrice();
        }

        // 3. Truyền dữ liệu ra View
        model.addAttribute("totalBooksSold", totalBooksSold);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("transactions", transactions);

        return "seller-revenue";
    }

    // Hiển thị form thêm sách mới
    @GetMapping("/book/add")
    public String showAddBookForm(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        return "seller-book-add";
    }


    // Xử lý lưu sách mới kèm upload ảnh lên Cloudinary
    @PostMapping("/book/add")
    public String addBook(
            @RequestParam String title,
            @RequestParam String author,
            @RequestParam Double price,
            @RequestParam Integer quantity,
            @RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam String description,
            @RequestParam Long categoryId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        String imageUrl = "";

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                // Khởi tạo trực tiếp đối tượng Cloudinary ngay trong hàm để tránh lỗi nhận diện Bean
                com.cloudinary.Cloudinary cloudinaryInstance = new com.cloudinary.Cloudinary(com.cloudinary.utils.ObjectUtils.asMap(
                        "cloud_name", "pdhtejei",
                        "api_key", "543148714818317",
                        "api_secret", "u3OQhX-C9sVuckz8fUHYb8K9VlU",
                        "secure", true
                ));

                java.util.Map uploadResult = cloudinaryInstance.uploader().upload(imageFile.getBytes(), com.cloudinary.utils.ObjectUtils.emptyMap());
                imageUrl = uploadResult.get("secure_url").toString();

            } catch (Exception e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("error", "Lỗi khi tải ảnh lên Cloudinary: " + e.getMessage());
                return "redirect:/seller/book/add";
            }
        }

        Book newBook = new Book();
        newBook.setTitle(title);
        newBook.setAuthor(author);
        newBook.setPrice(price);
        newBook.setQuantity(quantity);
        newBook.setImage(imageUrl);
        newBook.setDescription(description);
        newBook.setStatus("AVAILABLE");
        newBook.setSeller(loggedInUser);

        Category category = categoryRepository.findById(categoryId).orElse(null);
        newBook.setCategory(category);

        bookRepository.save(newBook);

        redirectAttributes.addFlashAttribute("success", "Đăng bán sách và lưu ảnh thành công!");
        return "redirect:/seller/products";
    }

    // TAB 3: QUẢN LÝ ĐƠN HÀNG
    @GetMapping("/orders")
    public String manageOrders(
            @RequestParam(required = false, defaultValue = "ALL") String status,
            HttpSession session,
            Model model) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        List<Order> orders;
        if ("ALL".equals(status)) {
            orders = orderRepository.findByBookSellerIdOrderByOrderDateDesc(loggedInUser.getId());
        } else {
            orders = orderRepository.findByBookSellerIdAndStatusOrderByOrderDateDesc(loggedInUser.getId(), status);
        }

        model.addAttribute("orders", orders);
        model.addAttribute("currentStatus", status);

        return "seller-orders";
    }

    // Xử lý cập nhật trạng thái đơn hàng (Lên đơn / Từ chối)
    @GetMapping("/order/update-status")
    public String updateOrderStatus(
            @RequestParam("orderId") Long orderId,
            @RequestParam("status") String status,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null) {
            order.setStatus(status);
            orderRepository.save(order);

            if ("Đã lên đơn".equalsIgnoreCase(status) || "SOLD".equalsIgnoreCase(status)) {
                Book book = order.getBook();
                if (book != null) {
                    int orderedQuantity = order.getQuantity();

                    if (book.getQuantity() > orderedQuantity) {
                        book.setQuantity(book.getQuantity() - orderedQuantity);
                        bookRepository.save(book);

                        Book soldBook = new Book();
                        soldBook.setTitle(book.getTitle());
                        soldBook.setAuthor(book.getAuthor());
                        soldBook.setPrice(book.getPrice());
                        soldBook.setCategory(book.getCategory());
                        soldBook.setDescription(book.getDescription());
                        soldBook.setImage(book.getImage());
                        soldBook.setSeller(book.getSeller());
                        soldBook.setQuantity(orderedQuantity);
                        soldBook.setStatus("SOLD");
                        bookRepository.save(soldBook);
                    } else {
                        book.setStatus("SOLD");
                        bookRepository.save(book);
                    }
                }
            }
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật trạng thái đơn hàng thành công!");
        }

        return "redirect:/seller/orders";
    }
}
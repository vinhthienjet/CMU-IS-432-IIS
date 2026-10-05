package com.example.bookonlineapp.controller;

import com.example.bookonlineapp.entity.Book;
import com.example.bookonlineapp.entity.User;
import com.example.bookonlineapp.repository.BookRepository;
import com.example.bookonlineapp.repository.OrderRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.bookonlineapp.entity.Order;
import java.util.List;
import com.example.bookonlineapp.repository.CategoryRepository;
import com.example.bookonlineapp.entity.Category;

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

//    // TAB 2: QUẢN LÝ DOANH THU (Tạm thời tạo khung)
//    @GetMapping("/revenue")
//    public String manageRevenue(HttpSession session, Model model) {
//        User loggedInUser = (User) session.getAttribute("loggedInUser");
//        if (loggedInUser == null) return "redirect:/login";
//
//        // Logic doanh thu sẽ được code ở bước tiếp theo
//        return "seller-revenue";
//    }

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

        // Ghi chú: Nếu bạn có CategoryRepository, hãy mở comment để truyền danh sách thể loại ra form
        // model.addAttribute("categories", categoryRepository.findAll());

        return "seller-book-add";
    }

    // Xử lý lưu sách mới
    @PostMapping("/book/add")
    public String addBook(
            @RequestParam String title,
            @RequestParam String author,
            @RequestParam Double price,
            @RequestParam Integer quantity,
            @RequestParam String image,
            @RequestParam String description,
            @RequestParam Long categoryId, // Lấy ID thể loại từ form
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        Book newBook = new Book();
        newBook.setTitle(title);
        newBook.setAuthor(author);
        newBook.setPrice(price);
        newBook.setQuantity(quantity);
        newBook.setImage(image);
        newBook.setDescription(description);
        newBook.setStatus("AVAILABLE");
        newBook.setSeller(loggedInUser);

        Category category = categoryRepository.findById(categoryId).orElse(null);
        newBook.setCategory(category);

        bookRepository.save(newBook);

        redirectAttributes.addFlashAttribute("success", "Đã đăng bán sách mới thành công!");
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
            // Lấy tất cả
            orders = orderRepository.findByBookSellerIdOrderByOrderDateDesc(loggedInUser.getId());
        } else {
            // Lọc theo trạng thái ("Đợi phê duyệt", "Đã lên đơn", "Từ chối")
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
            // Cập nhật trạng thái đơn hàng
            order.setStatus(status);
            orderRepository.save(order);

            // NẾU đơn hàng được chuyển sang trạng thái "Đã lên đơn"
            if ("Đã lên đơn".equalsIgnoreCase(status) || "SOLD".equalsIgnoreCase(status)) {
                Book book = order.getBook();
                if (book != null) {
                    int orderedQuantity = order.getQuantity(); // Số lượng khách mua

                    if (book.getQuantity() > orderedQuantity) {
                        // Trường hợp kho còn nhiều hơn số lượng mua -> Giảm số lượng dòng gốc và tách dòng SOLD
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
                        soldBook.setStatus("SOLD"); // Đặt trạng thái là SOLD
                        bookRepository.save(soldBook);
                    } else {
                        // Trường hợp kho vừa đủ -> Đổi thẳng dòng hiện tại thành SOLD
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
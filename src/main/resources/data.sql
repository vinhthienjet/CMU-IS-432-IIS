# -- USERS
# # INSERT INTO users
# # (username, password, full_name, email, phone, address, role)
# # VALUES
# #     ('thien', '123456', 'Nguyen Vinh Thien',
# #      'thien@gmail.com', '0905000001', 'Da Nang', 'USER'),
# #
# #     ('hien', '123456', 'Nguyen Ngoc Hien',
# #      'hien@gmail.com', '0905000002', 'Da Nang', 'USER'),
# #
# #     ('trieu', '123456', 'Cao Le Ngoc Trieu',
# #      'trieu@gmail.com', '0905000003', 'Da Nang', 'USER'),
# #
# #     ('huong', '123456', 'Nguyen Van Hoang Huong',
# #      'huong@gmail.com', '0905000004', 'Da Nang', 'USER'),
# #
# #     ('admin', '123456', 'Administrator',
# #      'admin@gmail.com', '0905000005', 'Da Nang', 'ADMIN');
#
#
# -- CATEGORIES
# INSERT INTO categories (name)
# VALUES
#     ('Programming'),
#     ('Database'),
#     ('Web Development'),
#     ('Novel'),
#     ('Japanese');
#
#
# -- BOOKS
# INSERT INTO books
# (title, author, description, price, quantity, image, status, seller_id, category_id)
# VALUES
#     (
#         'Java Programming',
#         'James Gosling',
#         'Basic Java programming book',
#         150000,
#         10,
#         'java.jpg',
#         'AVAILABLE',
#         1,
#         1
#     ),
#
#     (
#         'Spring Boot Beginner',
#         'Craig Walls',
#         'Introduction to Spring Boot',
#         180000,
#         8,
#         'spring.jpg',
#         'AVAILABLE',
#         1,
#         3
#     ),
#
#     (
#         'Database Fundamentals',
#         'Thomas Connolly',
#         'Basic database concepts',
#         200000,
#         5,
#         'database.jpg',
#         'AVAILABLE',
#         2,
#         2
#     ),
#
#     (
#         'Japanese N5',
#         'Minna no Nihongo',
#         'Japanese language book for N5',
#         120000,
#         15,
#         'n5.jpg',
#         'AVAILABLE',
#         3,
#         5
#     ),
#
#     (
#         'Clean Code',
#         'Robert C. Martin',
#         'A handbook of agile software craftsmanship',
#         250000,
#         7,
#         'clean-code.jpg',
#         'AVAILABLE',
#         4,
#         1
#     );
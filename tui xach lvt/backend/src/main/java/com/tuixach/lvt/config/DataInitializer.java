package com.tuixach.lvt.config;

import com.tuixach.lvt.entity.*;
import com.tuixach.lvt.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BannerRepository bannerRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ReviewRepository reviewRepository;
    private final CouponRepository couponRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final BlogPostRepository blogPostRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Starting data seeding process...");

        seedAdminAndUsers();
        seedCategories();
        seedBanners();
        seedProducts();
        seedCoupons();
        seedOrders();
        seedReviews();
        seedBlogPosts();
        
        // Ensure historical data for charts
        randomizeClusteredOrderDates();

        log.info("Data seeding process completed successfully!");
    }

    private void randomizeClusteredOrderDates() {
        List<Order> orders = orderRepository.findAll();
        Random random = new Random();
        boolean changed = false;
        for (Order o : orders) {
            // If the order was created in the last 2 hours (likely just seeded), spread it out
            if (o.getCreatedAt().isAfter(LocalDateTime.now().minusHours(2))) {
                o.setCreatedAt(LocalDateTime.now()
                        .minusMonths(random.nextInt(12))
                        .minusDays(random.nextInt(28))
                        .minusHours(random.nextInt(24)));
                changed = true;
            }
        }
        if (changed) {
            orderRepository.saveAll(orders);
            log.info("Randomized dates for recently seeded orders to populate charts.");
        }
    }

    private void seedAdminAndUsers() {
        // Ensure admin account exists with password '123456'
        User admin = userRepository.findByEmail("admin@tuixach.com").orElse(null);
        if (admin == null) {
            admin = User.builder()
                    .fullName("Admin")
                    .email("admin@tuixach.com")
                    .phone("0123456789")
                    .address("TP. Hồ Chí Minh")
                    .role(User.Role.ADMIN)
                    .active(true)
                    .emailVerified(true)
                    .build();
        }
        admin.setPassword(passwordEncoder.encode("123456"));
        userRepository.save(admin);
        log.info("Ensured admin account: admin@tuixach.com / 123456");

        long userCount = userRepository.count();
        if (userCount < 50) {
            List<User> users = new ArrayList<>();
            String[] firstNames = {"Nguyễn", "Trần", "Lê", "Phạm", "Hoàng", "Phan", "Vũ", "Đặng", "Bùi", "Đỗ"};
            String[] middleNames = {"Văn", "Thị", "Hữu", "Minh", "Quang", "Thành", "Đức", "Anh", "Ngọc", "Thương"};
            String[] lastNames = {"Anh", "Bình", "Chí", "Dũng", "Em", "Giang", "Hương", "Khánh", "Linh", "Minh"};

            for (int i = 0; i < 50; i++) {
                String email = "user" + i + "@example.com";
                if (!userRepository.existsByEmail(email)) {
                    String fullName = firstNames[i % firstNames.length] + " " + 
                                     middleNames[(i / 2) % middleNames.length] + " " + 
                                     lastNames[i % lastNames.length];
                    users.add(User.builder()
                            .fullName(fullName)
                            .email(email)
                            .password(passwordEncoder.encode("password123"))
                            .phone("09" + String.format("%08d", i))
                            .address(i + " Đường ABC, Quận " + (i % 12 + 1) + ", TP. HCM")
                            .role(User.Role.USER)
                            .active(true)
                            .emailVerified(true)
                            .build());
                }
            }
            if (!users.isEmpty()) {
                userRepository.saveAll(users);
                log.info("Seeded {} additional users", users.size());
            }
        }
    }

    private void seedCategories() {
        if (categoryRepository.count() == 0) {
            List<Category> categories = List.of(
                    Category.builder().name("Túi xách nữ").description("Các loại túi xách thời trang cao cấp dành cho phái nữ").active(true).build(),
                    Category.builder().name("Túi xách nam").description("Túi xách, cặp da lịch lãm cho nam giới").active(true).build(),
                    Category.builder().name("Túi đeo chéo").description("Túi đeo chéo tiện lợi, năng động").active(true).build(),
                    Category.builder().name("Balo").description("Balo du lịch, đi học, đi làm đa năng").active(true).build(),
                    Category.builder().name("Ví & Bóp").description("Ví cầm tay, bóp đựng tiền sang trọng").active(true).build(),
                    Category.builder().name("Phụ kiện túi").description("Dây đeo, móc khóa trang trí túi xách").active(true).build(),
                    Category.builder().name("Túi công sở").description("Thiết kế chuyên dụng cho dân văn phòng").active(true).build(),
                    Category.builder().name("Túi du lịch").description("Sức chứa lớn cho những chuyến đi xa").active(true).build()
            );
            categoryRepository.saveAll(categories);
            log.info("Created default categories");
        }
    }

    private void seedBanners() {
        if (bannerRepository.count() == 0) {
            List<Banner> banners = List.of(
                    Banner.builder().title("Bộ Sưu Tập Mùa Hè 2024").imageUrl("https://images.unsplash.com/photo-1584917865442-de89df76afd3?w=1600&q=80").subImageUrl("https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=800&q=80").linkUrl("/products").displayOrder(1).active(true).build(),
                    Banner.builder().title("Siêu Sale Thương Hiệu").imageUrl("https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=1600&q=80").linkUrl("/products").displayOrder(2).active(true).build()
            );
            bannerRepository.saveAll(banners);
            log.info("Created default banners");
        }
    }

    private void seedProducts() {
        long currentCount = productRepository.count();
        if (currentCount < 300) {
            List<Category> categories = categoryRepository.findAll();
            if (categories.isEmpty()) return;

            String[] brands = {"Gucci", "Chanel", "Louis Vuitton", "Dior", "Prada", "Hermes", "Charles & Keith", "Pedro", "Coach", "Lacoste", "Michael Kors", "YSL"};
            String[] models = {"Luxury", "Classic", "Vintage", "Modern", "Street Style", "Elegant", "Casual", "Professional", "Sporty", "Mini", "Tote", "Messenger"};
            String[] colors = {"Đen", "Trắng", "Đỏ", "Hồng", "Nâu", "Xanh Navy", "Be", "Vàng", "Xám", "Xanh Lá", "Tím"};
            String[] images = {
                    "https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=800&q=80",
                    "https://images.unsplash.com/photo-1590874103328-eac38a683ce7?w=800&q=80",
                    "https://images.unsplash.com/photo-1566150905458-1bf1fd143c41?w=800&q=80",
                    "https://images.unsplash.com/photo-1591561954557-26941169b49e?w=800&q=80",
                    "https://images.unsplash.com/photo-1598532163257-ae3c6b2524b6?w=800&q=80",
                    "https://images.unsplash.com/photo-1605733513597-a8183f360700?w=800&q=80",
                    "https://images.unsplash.com/photo-1584677626646-7c8f83690304?w=800&q=80",
                    "https://images.unsplash.com/photo-1614179662391-74480582d974?w=800&q=80",
                    "https://images.unsplash.com/photo-1524498250077-390f9e378fc0?w=800&q=80",
                    "https://images.unsplash.com/photo-1594223274512-ad4803739b7c?w=800&q=80"
            };

            Random random = new Random();
            int toAdd = (int) (300 - currentCount);
            for (int i = 0; i < toAdd; i++) {
                String brand = brands[random.nextInt(brands.length)];
                String model = models[random.nextInt(models.length)];
                String color = colors[random.nextInt(colors.length)];
                Category category = categories.get(random.nextInt(categories.size()));

                String productName = String.format("Túi %s %s %s - Model %d", brand, model, color, i + 101);
                double price = 1000000 + (random.nextDouble() * 19000000); // 1tr - 20tr
                double discount = random.nextDouble() > 0.7 ? price * (0.5 + random.nextDouble() * 0.4) : 0; // 30% sale

                Product product = Product.builder()
                        .name(productName)
                        .description("Sản phẩm túi xách cao cấp từ thương hiệu " + brand + ". Thiết kế đẳng cấp, chất liệu da thật 100%, đường may tinh xảo. Phù hợp cho nhiều dịp khác nhau.")
                        .brand(brand)
                        .price(BigDecimal.valueOf((long) (price / 1000) * 1000))
                        .discountPrice(discount > 0 ? BigDecimal.valueOf((long) (discount / 1000) * 1000) : null)
                        .quantity(20 + random.nextInt(80))
                        .category(category)
                        .featured(random.nextDouble() > 0.9)
                        .active(true)
                        .build();

                product = productRepository.save(product);

                // Add 1-3 images
                int numImages = 1 + random.nextInt(3);
                for (int j = 0; j < numImages; j++) {
                    productImageRepository.save(ProductImage.builder()
                            .imageUrl(images[random.nextInt(images.length)])
                            .isPrimary(j == 0)
                            .displayOrder(j)
                            .product(product)
                            .build());
                }
            }
            log.info("Seeded {} new products", toAdd);
        }
    }

    private void seedCoupons() {
        if (couponRepository.count() == 0) {
            List<Coupon> coupons = List.of(
                    Coupon.builder().code("WELCOME10").description("Giảm 10% cho khách hàng mới").type(Coupon.CouponType.PERCENTAGE).value(BigDecimal.valueOf(10)).minOrderAmount(BigDecimal.valueOf(200000)).usageLimit(1000).startDate(LocalDateTime.now()).endDate(LocalDateTime.now().plusMonths(12)).active(true).build(),
                    Coupon.builder().code("SALE50K").description("Giảm 50,000đ cho đơn hàng từ 500k").type(Coupon.CouponType.FIXED).value(BigDecimal.valueOf(50000)).minOrderAmount(BigDecimal.valueOf(500000)).usageLimit(500).startDate(LocalDateTime.now()).endDate(LocalDateTime.now().plusMonths(3)).active(true).build(),
                    Coupon.builder().code("VIP20").description("Ưu đãi VIP giảm 20%").type(Coupon.CouponType.PERCENTAGE).value(BigDecimal.valueOf(20)).minOrderAmount(BigDecimal.valueOf(2000000)).usageLimit(100).startDate(LocalDateTime.now()).endDate(LocalDateTime.now().plusMonths(6)).active(true).build(),
                    Coupon.builder().code("LVTFEST").description("Lễ hội mua sắm giảm 100k").type(Coupon.CouponType.FIXED).value(BigDecimal.valueOf(100000)).minOrderAmount(BigDecimal.valueOf(1000000)).usageLimit(200).startDate(LocalDateTime.now()).endDate(LocalDateTime.now().plusDays(30)).active(true).build()
            );
            couponRepository.saveAll(coupons);
            log.info("Seeded default coupons");
        }
    }

    private void seedOrders() {
        if (orderRepository.count() < 150) {
            List<User> users = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == User.Role.USER)
                    .collect(Collectors.toList());
            List<Product> products = productRepository.findAll();
            if (users.isEmpty() || products.isEmpty()) return;

            Random random = new Random();
            int toAdd = 150 - (int)orderRepository.count();
            for (int i = 0; i < toAdd; i++) {
                User user = users.get(random.nextInt(users.size()));
                int numItems = 1 + random.nextInt(3);
                List<OrderItem> items = new ArrayList<>();
                BigDecimal total = BigDecimal.ZERO;

                LocalDateTime randomDate = LocalDateTime.now()
                        .minusMonths(random.nextInt(12))
                        .minusDays(random.nextInt(28))
                        .minusHours(random.nextInt(24));

                Order order = Order.builder()
                        .orderCode("ORD-" + System.nanoTime() + "-" + i)
                        .user(user)
                        .receiverName(user.getFullName())
                        .receiverPhone(user.getPhone())
                        .receiverAddress(user.getAddress())
                        .paymentMethod(random.nextDouble() > 0.5 ? Order.PaymentMethod.COD : Order.PaymentMethod.VNPAY)
                        .status(Order.OrderStatus.values()[random.nextInt(Order.OrderStatus.values().length)])
                        .totalAmount(BigDecimal.ZERO)
                        .discountAmount(BigDecimal.ZERO)
                        .createdAt(randomDate)
                        .build();

                order = orderRepository.save(order);

                for (int j = 0; j < numItems; j++) {
                    Product p = products.get(random.nextInt(products.size()));
                    int qty = 1 + random.nextInt(2);
                    BigDecimal price = p.getDiscountPrice() != null ? p.getDiscountPrice() : p.getPrice();
                    BigDecimal subtotal = price.multiply(BigDecimal.valueOf(qty));
                    
                    OrderItem item = OrderItem.builder()
                            .order(order)
                            .product(p)
                            .productName(p.getName())
                            .price(price)
                            .quantity(qty)
                            .subtotal(subtotal)
                            .build();
                    items.add(item);
                    total = total.add(subtotal);
                }
                orderItemRepository.saveAll(items);
                order.setItems(items);
                order.setTotalAmount(total);
                orderRepository.save(order);
            }
            log.info("Seeded {} new orders", toAdd);
        }
    }

    private void seedReviews() {
        if (reviewRepository.count() < 500) {
            List<User> users = userRepository.findAll();
            List<Product> products = productRepository.findAll();
            if (users.isEmpty() || products.isEmpty()) return;

            String[] reviewTexts = {
                    "Sản phẩm rất đẹp, đóng gói cẩn thận.",
                    "Chất lượng da tốt, đúng như mô tả.",
                    "Giao hàng nhanh, nhân viên thân thiện.",
                    "Túi xịn xò, sang trọng lắm ạ.",
                    "Hơi nhỏ so với mình tưởng tượng nhưng vẫn rất xinh.",
                    "Đáng đồng tiền bát gạo.",
                    "Màu sắc bên ngoài đẹp hơn trong ảnh.",
                    "Sẽ quay lại ủng hộ shop tiếp.",
                    "Feedback xịn cho shop, túi quá đẹp luôn.",
                    "Giá hơi cao nhưng chất lượng tương xứng."
            };

            Random random = new Random();
            Set<String> existingPairs = new HashSet<>();
            List<Review> reviewList = new ArrayList<>();
            int currentCount = (int) reviewRepository.count();
            int targetCount = 500;
            int addedCount = 0;
            int attempts = 0;
            int maxAttempts = 2000; // Tránh vòng lặp vô tận

            while (currentCount + addedCount < targetCount && attempts < maxAttempts) {
                attempts++;
                User user = users.get(random.nextInt(users.size()));
                Product product = products.get(random.nextInt(products.size()));
                String pairKey = user.getId() + "-" + product.getId();

                if (!existingPairs.contains(pairKey) && !reviewRepository.existsByUserAndProduct(user, product)) {
                    reviewList.add(Review.builder()
                            .user(user)
                            .product(product)
                            .rating(4 + random.nextInt(2))
                            .comment(reviewTexts[random.nextInt(reviewTexts.length)])
                            .verifiedPurchase(random.nextBoolean())
                            .build());
                    existingPairs.add(pairKey);
                    addedCount++;
                }

                if (reviewList.size() >= 100) {
                    reviewRepository.saveAll(reviewList);
                    reviewList.clear();
                }
            }
            if (!reviewList.isEmpty()) {
                reviewRepository.saveAll(reviewList);
            }
            log.info("Seeded {} new reviews", addedCount);
        }
    }

    private void seedBlogPosts() {
        if (blogPostRepository.count() == 0) {
            User admin = userRepository.findByEmail("admin@tuixach.com").orElse(null);
            if (admin == null) return;

            String[] titles = {
                    "Xu hướng túi xách Thu Đông 2024",
                    "Cách phân biệt túi da thật và giả da",
                    "5 mẫu túi không thể thiếu cho nàng công sở",
                    "Mẹo bảo quản túi xách luôn mới",
                    "Review chi tiết bộ sưu tập Modern Classic",
                    "Tại sao nên đầu tư vào một chiếc túi Luxury?",
                    "Phong cách đường phố cùng túi đeo chéo",
                    "Những gam màu túi xách làm mưa làm gió năm nay"
            };

            List<BlogPost> posts = new ArrayList<>();
            for (int i = 0; i < titles.length; i++) {
                posts.add(BlogPost.builder()
                        .title(titles[i])
                        .slug("bai-viet-" + (i + 1) + "-" + System.currentTimeMillis())
                        .excerpt("Đây là đoạn trích ngắn cho bài viết về " + titles[i].toLowerCase() + "...")
                        .content("<h3>" + titles[i] + "</h3><p>Nội dung chi tiết cho bài viết này sẽ giúp khách hàng có thêm thông tin về thời trang và cách lựa chọn sản phẩm phù hợp...</p>")
                        .coverImageUrl("https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=800&q=80")
                        .author(admin)
                        .status(BlogPost.Status.PUBLISHED)
                        .publishedAt(LocalDateTime.now())
                        .viewCount(100 + new Random().nextInt(900))
                        .build());
            }
            blogPostRepository.saveAll(posts);
            log.info("Seeded blog posts");
        }
    }
}

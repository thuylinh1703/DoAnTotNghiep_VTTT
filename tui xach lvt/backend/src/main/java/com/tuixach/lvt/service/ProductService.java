package com.tuixach.lvt.service;

import com.tuixach.lvt.dto.ProductDTO;
import com.tuixach.lvt.dto.ProductRequest;
import com.tuixach.lvt.entity.Category;
import com.tuixach.lvt.entity.Product;
import com.tuixach.lvt.entity.ProductImage;
import com.tuixach.lvt.exception.ResourceNotFoundException;
import com.tuixach.lvt.repository.CategoryRepository;
import com.tuixach.lvt.repository.ProductImageRepository;
import com.tuixach.lvt.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final FileStorageService fileStorageService;

    public Page<ProductDTO> getProducts(
            Long categoryId, String brand, BigDecimal minPrice, BigDecimal maxPrice,
            String keyword, String sortBy, String period, int page, int size) {
        Sort sort = switch (sortBy != null ? sortBy : "newest") {
            case "priceAsc" -> Sort.by("price").ascending();
            case "priceDesc" -> Sort.by("price").descending();
            default -> Sort.by("createdAt").descending();
        };

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> products = productRepository.findWithFilters(
                categoryId, brand, minPrice, maxPrice, keyword, periodToSince(period), pageable);

        return products.map(this::mapToDTO);
    }

    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));
        return mapToDTO(product);
    }

    public List<ProductDTO> getFeaturedProducts() {
        return productRepository.findByFeaturedTrueAndActiveTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> getNewProducts(int limit) {
        return getNewProducts(limit, null);
    }

    /**
     * @param period null/unknown = no time window, "day" = last 24h, "week" = last 7d,
     *               "month" = last 30d. Dates are rolling (not calendar-aligned) to avoid
     *               Monday-morning empty state.
     */
    public List<ProductDTO> getNewProducts(int limit, String period) {
        Pageable pageable = PageRequest.of(0, limit);
        LocalDateTime since = periodToSince(period);
        List<Product> products = (since == null)
                ? productRepository.findNewProducts(pageable)
                : productRepository.findNewProductsSince(since, pageable);
        return products.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private LocalDateTime periodToSince(String period) {
        if (period == null || period.isBlank()) return null;
        return switch (period.toLowerCase()) {
            case "day" -> LocalDateTime.now().minusDays(1);
            case "week" -> LocalDateTime.now().minusDays(7);
            case "month" -> LocalDateTime.now().minusDays(30);
            default -> null;
        };
    }

    public List<ProductDTO> getDiscountedProducts() {
        return productRepository.findDiscountedProducts().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<String> getAllBrands() {
        return productRepository.findAllBrands();
    }

    @Transactional
    @CacheEvict(value = "relatedProducts", allEntries = true)
    public ProductDTO createProduct(ProductRequest request, List<MultipartFile> images) throws IOException {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục"));

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .discountPrice(request.getDiscountPrice())
                .brand(request.getBrand())
                .quantity(request.getQuantity())
                .featured(request.isFeatured())
                .active(true)
                .category(category)
                .images(new ArrayList<>())
                .build();

        productRepository.save(product);

        if (images != null && !images.isEmpty()) {
            for (int i = 0; i < images.size(); i++) {
                String imageUrl = fileStorageService.storeFile(images.get(i));
                ProductImage productImage = ProductImage.builder()
                        .imageUrl(imageUrl)
                        .isPrimary(i == 0)
                        .displayOrder(i)
                        .product(product)
                        .build();
                productImageRepository.save(productImage);
                product.getImages().add(productImage);
            }
        }

        return mapToDTO(product);
    }

    @Transactional
    @CacheEvict(value = "relatedProducts", allEntries = true)
    public ProductDTO updateProduct(Long id, ProductRequest request, List<MultipartFile> images) throws IOException {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục"));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setDiscountPrice(request.getDiscountPrice());
        product.setBrand(request.getBrand());
        product.setQuantity(request.getQuantity());
        product.setFeatured(request.isFeatured());
        product.setCategory(category);

        if (images != null && !images.isEmpty()) {
            // Delete old images
            for (ProductImage oldImage : product.getImages()) {
                fileStorageService.deleteFile(oldImage.getImageUrl());
            }
            product.getImages().clear();
            productImageRepository.deleteAll(product.getImages());

            // Add new images
            for (int i = 0; i < images.size(); i++) {
                String imageUrl = fileStorageService.storeFile(images.get(i));
                ProductImage productImage = ProductImage.builder()
                        .imageUrl(imageUrl)
                        .isPrimary(i == 0)
                        .displayOrder(i)
                        .product(product)
                        .build();
                productImageRepository.save(productImage);
                product.getImages().add(productImage);
            }
        }

        productRepository.save(product);
        return mapToDTO(product);
    }

    @CacheEvict(value = "relatedProducts", allEntries = true)
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));
        product.setActive(false);
        productRepository.save(product);
    }

    // Admin: get all products including inactive
    public Page<ProductDTO> getAllProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return productRepository.findAll(pageable).map(this::mapToDTO);
    }

    public ProductDTO mapToDTO(Product product) {
        List<String> imageUrls = product.getImages() != null
                ? product.getImages().stream().map(ProductImage::getImageUrl).collect(Collectors.toList())
                : new ArrayList<>();

        String primaryImage = product.getImages() != null
                ? product.getImages().stream()
                        .filter(ProductImage::isPrimary)
                        .map(ProductImage::getImageUrl)
                        .findFirst()
                        .orElse(imageUrls.isEmpty() ? null : imageUrls.get(0))
                : null;

        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .discountPrice(product.getDiscountPrice())
                .brand(product.getBrand())
                .quantity(product.getQuantity())
                .featured(product.isFeatured())
                .active(product.isActive())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .imageUrls(imageUrls)
                .primaryImage(primaryImage)
                .averageRating(product.getAverageRating())
                .reviewCount(product.getReviewCount())
                .createdAt(product.getCreatedAt())
                .build();
    }
}

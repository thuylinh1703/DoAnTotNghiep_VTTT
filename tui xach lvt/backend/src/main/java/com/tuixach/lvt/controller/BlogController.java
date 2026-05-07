package com.tuixach.lvt.controller;

import com.tuixach.lvt.dto.ApiResponse;
import com.tuixach.lvt.dto.BlogPostDTO;
import com.tuixach.lvt.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/blog")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<BlogPostDTO>>> getPublicPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(blogService.getPublicPosts(page, size)));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<BlogPostDTO>> getPublicPostBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(blogService.getPublicPostBySlug(slug)));
    }

    @PostMapping("/{slug}/view")
    public ResponseEntity<ApiResponse<Void>> trackView(@PathVariable String slug) {
        blogService.incrementViewCount(slug);
        return ResponseEntity.ok(ApiResponse.success("Đã ghi nhận lượt xem", null));
    }
}

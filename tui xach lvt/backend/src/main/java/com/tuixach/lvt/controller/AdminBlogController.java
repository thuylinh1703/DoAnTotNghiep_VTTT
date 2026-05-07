package com.tuixach.lvt.controller;

import com.tuixach.lvt.dto.ApiResponse;
import com.tuixach.lvt.dto.BlogPostCreateRequest;
import com.tuixach.lvt.dto.BlogPostDTO;
import com.tuixach.lvt.entity.BlogPost;
import com.tuixach.lvt.entity.User;
import com.tuixach.lvt.service.BlogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/blog")
@RequiredArgsConstructor
public class AdminBlogController {

    private final BlogService blogService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<BlogPostDTO>>> listPosts(
            @RequestParam(required = false) BlogPost.Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(blogService.getAdminPosts(page, size, status)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BlogPostDTO>> getPost(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(blogService.getAdminPostById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BlogPostDTO>> createPost(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody BlogPostCreateRequest request) {
        BlogPostDTO post = blogService.create(request, user);
        return ResponseEntity.ok(ApiResponse.success("Tạo bài viết thành công", post));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BlogPostDTO>> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody BlogPostCreateRequest request) {
        BlogPostDTO post = blogService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật bài viết thành công", post));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long id) {
        blogService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa bài viết thành công", null));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<BlogPostDTO>> publish(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Xuất bản thành công", blogService.publish(id)));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<ApiResponse<BlogPostDTO>> archive(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Lưu trữ thành công", blogService.archive(id)));
    }
}

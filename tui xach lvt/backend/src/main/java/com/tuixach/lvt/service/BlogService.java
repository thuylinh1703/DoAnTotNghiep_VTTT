package com.tuixach.lvt.service;

import com.tuixach.lvt.dto.BlogPostCreateRequest;
import com.tuixach.lvt.dto.BlogPostDTO;
import com.tuixach.lvt.entity.BlogPost;
import com.tuixach.lvt.entity.User;
import com.tuixach.lvt.exception.ResourceNotFoundException;
import com.tuixach.lvt.repository.BlogPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BlogService {

    private final BlogPostRepository blogPostRepository;
    private final SlugService slugService;
    private final HtmlSanitizerService htmlSanitizerService;

    public Page<BlogPostDTO> getPublicPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        return blogPostRepository.findByStatusOrderByPublishedAtDesc(BlogPost.Status.PUBLISHED, pageable)
                .map(this::toDtoWithoutContent);
    }

    public BlogPostDTO getPublicPostBySlug(String slug) {
        BlogPost post = blogPostRepository.findBySlugAndStatus(slug, BlogPost.Status.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết"));
        return toDto(post);
    }

    public Page<BlogPostDTO> getAdminPosts(int page, int size, BlogPost.Status status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        Page<BlogPost> pageData = status == null
                ? blogPostRepository.findAllByOrderByUpdatedAtDesc(pageable)
                : blogPostRepository.findByStatusOrderByUpdatedAtDesc(status, pageable);

        return pageData
                .map(this::toDtoWithoutContent);
    }

    public BlogPostDTO getAdminPostById(Long id) {
        BlogPost post = blogPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết"));
        return toDto(post);
    }

    @Transactional
    public BlogPostDTO create(BlogPostCreateRequest request, User author) {
        BlogPost post = BlogPost.builder()
                .title(request.getTitle().trim())
                .slug(resolveSlug(request, null))
                .excerpt(trimToNull(request.getExcerpt()))
                .content(htmlSanitizerService.sanitize(request.getContent()))
                .coverImageUrl(trimToNull(request.getCoverImageUrl()))
                .author(author)
                .status(request.getStatus() == null ? BlogPost.Status.DRAFT : request.getStatus())
                .publishedAt(request.getStatus() == BlogPost.Status.PUBLISHED ? LocalDateTime.now() : null)
                .build();

        blogPostRepository.save(post);
        return toDto(post);
    }

    @Transactional
    public BlogPostDTO update(Long id, BlogPostCreateRequest request) {
        BlogPost post = blogPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết"));

        post.setTitle(request.getTitle().trim());
        post.setSlug(resolveSlug(request, id));
        post.setExcerpt(trimToNull(request.getExcerpt()));
        post.setContent(htmlSanitizerService.sanitize(request.getContent()));
        post.setCoverImageUrl(trimToNull(request.getCoverImageUrl()));

        if (request.getStatus() != null) {
            setStatus(post, request.getStatus());
        }

        blogPostRepository.save(post);
        return toDto(post);
    }

    @Transactional
    public void delete(Long id) {
        BlogPost post = blogPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết"));
        blogPostRepository.delete(post);
    }

    @Transactional
    public BlogPostDTO publish(Long id) {
        BlogPost post = blogPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết"));
        setStatus(post, BlogPost.Status.PUBLISHED);
        blogPostRepository.save(post);
        return toDto(post);
    }

    @Transactional
    public BlogPostDTO archive(Long id) {
        BlogPost post = blogPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết"));
        setStatus(post, BlogPost.Status.ARCHIVED);
        blogPostRepository.save(post);
        return toDto(post);
    }

    @Transactional
    public void incrementViewCount(String slug) {
        BlogPost post = blogPostRepository.findBySlugAndStatus(slug, BlogPost.Status.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết"));
        post.setViewCount(post.getViewCount() + 1);
        blogPostRepository.save(post);
    }

    private void setStatus(BlogPost post, BlogPost.Status newStatus) {
        post.setStatus(newStatus);
        if (newStatus == BlogPost.Status.PUBLISHED && post.getPublishedAt() == null) {
            post.setPublishedAt(LocalDateTime.now());
        }
    }

    private String resolveSlug(BlogPostCreateRequest request, Long currentId) {
        String raw = request.getSlug();
        if (raw == null || raw.isBlank()) {
            raw = request.getTitle();
        }
        return slugService.generateUniqueSlug(raw, currentId);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private BlogPostDTO toDto(BlogPost post) {
        return BlogPostDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .slug(post.getSlug())
                .excerpt(post.getExcerpt())
                .content(post.getContent())
                .coverImageUrl(post.getCoverImageUrl())
                .authorId(post.getAuthor().getId())
                .authorName(post.getAuthor().getFullName())
                .status(post.getStatus())
                .publishedAt(post.getPublishedAt())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    private BlogPostDTO toDtoWithoutContent(BlogPost post) {
        BlogPostDTO dto = toDto(post);
        dto.setContent(null);
        return dto;
    }
}

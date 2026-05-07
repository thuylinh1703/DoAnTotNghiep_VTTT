package com.tuixach.lvt.dto;

import com.tuixach.lvt.entity.BlogPost;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogPostDTO {
    private Long id;
    private String title;
    private String slug;
    private String excerpt;
    private String content;
    private String coverImageUrl;
    private Long authorId;
    private String authorName;
    private BlogPost.Status status;
    private LocalDateTime publishedAt;
    private long viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

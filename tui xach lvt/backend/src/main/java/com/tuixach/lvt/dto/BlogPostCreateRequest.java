package com.tuixach.lvt.dto;

import com.tuixach.lvt.entity.BlogPost;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BlogPostCreateRequest {

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 200, message = "Tiêu đề tối đa 200 ký tự")
    private String title;

    @Size(max = 220, message = "Slug tối đa 220 ký tự")
    private String slug;

    @Size(max = 500, message = "Tóm tắt tối đa 500 ký tự")
    private String excerpt;

    @NotBlank(message = "Nội dung không được để trống")
    @Size(min = 50, message = "Nội dung tối thiểu 50 ký tự")
    private String content;

    private String coverImageUrl;

    private BlogPost.Status status;
}

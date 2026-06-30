package com.yearis.blog_application.payload.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PostUpdateRequest {

    @Size(min = 10, max = 50, message = "Title must be between 10 and 50 characters")
    private String title;

    @Size(min = 20, max = 5000, message = "Content must be between 20 and 5000 characters")
    private String content;
}

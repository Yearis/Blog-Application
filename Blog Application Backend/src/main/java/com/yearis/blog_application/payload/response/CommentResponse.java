package com.yearis.blog_application.payload.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentResponse {

    private Long id;
    private String body;
    private LocalDateTime createdDate;
    private boolean isEdited;
    private int likes;

    // Flattened Author Info
    private Long authorId;
    private String authorName;

    // Context Info
    private Long postId;
    private Long parentId; // Null if it's a top-level comment
}

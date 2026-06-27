package com.yearis.blog_application.payload.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostResponse {

    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdDate;
    private boolean isEdited;
    private int likes;

    // We flatten the User object here.
    // Instead of returning the whole User entity (security risk),
    // we just return what the UI needs to display.
    private Long authorId;
    private String authorName;
}

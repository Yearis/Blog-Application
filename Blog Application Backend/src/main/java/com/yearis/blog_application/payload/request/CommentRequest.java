package com.yearis.blog_application.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentRequest {

    @NotBlank(message = "Comment cannot be empty")
    @Size(min = 2, max = 1000, message = "Comment must be less than 1000 characters")
    @Schema(
            description = "The comment text.",
            example = """
            This is the first paragraph.
            
            This is the second paragraph after a newline.
            """,
            format = "text"
    )
    private String body;

    // this one can be null in case it's a parent comment
    private Long parentId;

}

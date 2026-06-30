package com.yearis.blog_application.mappers;

import com.yearis.blog_application.entity.Comment;
import com.yearis.blog_application.payload.request.CommentRequest;
import com.yearis.blog_application.payload.response.CommentResponse;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(source = "post.id", target = "postId")
    @Mapping(source = "parent.id", target = "parentId")
    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "authorName", ignore = true)
    CommentResponse mapToResponse(Comment comment);

    Comment mapToEntity(CommentRequest commentRequest);

    @AfterMapping
    default void handleAuthorDetails(Comment comment, @MappingTarget CommentResponse response) {

        // handling author info
        if (comment.getAuthor() != null) {
            response.setAuthorId(comment.getAuthor().getId());
            response.setAuthorName(comment.getAuthor().getUsername());
        } else {

            response.setAuthorId(null);

            // Check body to decide label
            if ("[deleted by user]".equals(comment.getBody()) || "[removed by admin]".equals(comment.getBody())) {
                response.setAuthorName("[removed]"); // User deleted the comment
            } else {
                response.setAuthorName("[deleted]"); // User deleted their account
            }
        }
    }
}

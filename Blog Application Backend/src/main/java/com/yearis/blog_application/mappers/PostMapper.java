package com.yearis.blog_application.mappers;

import com.yearis.blog_application.entity.Post;
import com.yearis.blog_application.payload.request.PostRequest;
import com.yearis.blog_application.payload.response.PostResponse;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PostMapper {

    // Entity -> Response
    @Mapping(source = "author.id", target = "authorId")
    @Mapping(target = "authorName", ignore = true)
    PostResponse mapToResponse(Post post);

    // Response -> Entity
    Post mapToEntity(PostRequest postRequest);

    @AfterMapping
    default void handleAuthorDetails(Post post, @MappingTarget PostResponse response) {
        if (post.getAuthor() != null) {
            response.setAuthorId(post.getAuthor().getId());
            response.setAuthorName(post.getAuthor().getUsername());
        } else {
            // This is Reddit Style Handling where a post or comment/reply can exist w/o a user(i.e a deleted user, someone who created post or comment then deleted the account)
            response.setAuthorId(null);

            // Check content to decide if it was "Post Deleted" or "Account Deleted"
            if ("[deleted by user]".equals(post.getContent()) || "[removed by admin]".equals(post.getContent())) {
                response.setAuthorName("[removed]"); // User deleted the post
            } else {
                response.setAuthorName("[deleted]"); // User deleted their account
            }
        }
    }
}

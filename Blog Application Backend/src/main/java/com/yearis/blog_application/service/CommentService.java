package com.yearis.blog_application.service;

import com.yearis.blog_application.payload.request.CommentRequest;
import com.yearis.blog_application.payload.response.CommentResponse;
import com.yearis.blog_application.payload.response.PostResponse;

import java.util.List;

public interface CommentService {

    /// --- CRUD Operations ---

    /// C: Create/Save
    CommentResponse createComment(Long postId, CommentRequest commentRequest);

    /// R: Read/Find/Get
    CommentResponse getCommentById(Long postId, Long commentId);

    List<CommentResponse> getCommentsByPostId(Long postId, int pageNo, int pageSize);

    List<CommentResponse> getRepliesByCommentId(Long postId, Long commentId, int pageNo, int pageSize);

    /// U: Update
    // we need both id to verify that our comment belong to the right post or not
    CommentResponse updateComment(Long postId, Long commentId, CommentRequest commentRequest);

    /// D: Delete
    // we need both id to verify that our comment belong to the right post or not
    void deleteComment(Long postId, Long commentId);

    /// --- Custom Methods ---

    // we need all the comments made by our user
    // also the postId doesn't matter here as comments would be pretty scrambled
    List<CommentResponse> getCommentsByUserId(Long userId, int pageNo, int pageSize);

    // we need all the comment liked by our user
    List<CommentResponse> findLikedCommentsByUserId(Long userId, int pageNo, int pageSize);
}
package com.yearis.blog_application.service;

public interface LikeService {

    void toggleLikePost(Long postId);

    void toggleLikeComment(Long postId, Long commentId);
}

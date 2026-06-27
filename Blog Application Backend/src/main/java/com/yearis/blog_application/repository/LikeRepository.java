package com.yearis.blog_application.repository;

import com.yearis.blog_application.entity.Comment;
import com.yearis.blog_application.entity.Like;
import com.yearis.blog_application.entity.Post;
import com.yearis.blog_application.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByUserAndPost(User user, Post post);

    Optional<Like> findByUserAndComment(User user, Comment comment);

    // Fetch only the rows where post_id is NOT null
    Page<Like> findByUserIdAndPostIsNotNull(Long userId, Pageable pageable);

    // Fetch only the rows where comment_id is NOT null
    Page<Like> findByUserIdAndCommentIsNotNull(Long userId, Pageable pageable);
}

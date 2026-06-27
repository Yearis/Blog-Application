package com.yearis.blog_application.repository;

import com.yearis.blog_application.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 1. For the Feed (Root Comments only)
    Page<Comment> findByPostIdAndParentIdIsNull(Long postId, Pageable pageable);

    // 2. For the Replies (Children only)
    Page<Comment> findByParentId(Long parentId, Pageable pageable);

    boolean existsByPostId(Long postId);

    Page<Comment> findByAuthorId(Long userId, Pageable pageable);
}

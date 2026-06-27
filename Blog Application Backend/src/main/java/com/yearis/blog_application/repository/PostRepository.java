package com.yearis.blog_application.repository;

import com.yearis.blog_application.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // for our findByTitle method
    Page<Post> findByTitleContaining(String title, Pageable pageable);

    Page<Post> findByAuthorId(Long userId, Pageable pageable);
}

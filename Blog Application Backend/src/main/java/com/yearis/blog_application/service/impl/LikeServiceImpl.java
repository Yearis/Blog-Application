package com.yearis.blog_application.service.impl;

import com.yearis.blog_application.entity.Comment;
import com.yearis.blog_application.entity.Like;
import com.yearis.blog_application.entity.Post;
import com.yearis.blog_application.entity.User;
import com.yearis.blog_application.exception.BlogAPIException;
import com.yearis.blog_application.exception.ResourceNotFoundException;
import com.yearis.blog_application.repository.CommentRepository;
import com.yearis.blog_application.repository.LikeRepository;
import com.yearis.blog_application.repository.PostRepository;
import com.yearis.blog_application.repository.UserRepository;
import com.yearis.blog_application.service.LikeService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public LikeServiceImpl(LikeRepository likeRepository, UserRepository userRepository, PostRepository postRepository, CommentRepository commentRepository) {
        this.likeRepository = likeRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    // to get our current user
    private User currentUser() {

        String usernameOrEmail = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();

        return userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    @Transactional
    public void toggleLikePost(Long postId) {

        // now to like a post 1st our current user should be there
        User currentUser = currentUser();

        // mandatory check that if the post to be liked exists or not
        Post likedPost = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "post Id", postId));

        // then we check if the user has already liked it or not
        Optional<Like> liked = likeRepository.findByUserAndPost(currentUser, likedPost);

        if (liked.isEmpty()) {
            // it's not liked so we like it
            Like like = new Like();
            like.setUser(currentUser);
            like.setPost(likedPost);

            likeRepository.save(like);

            // increment the likes of the post
            likedPost.setLikes(likedPost.getLikes() + 1);
        } else {
            // we unlike it
            likeRepository.delete(liked.get());

            // decrement the likes of the post
            likedPost.setLikes(likedPost.getLikes() - 1);
        }

        // then we save the post
        postRepository.save(likedPost);
    }

    @Override
    @Transactional
    public void toggleLikeComment(Long postId, Long commentId) {

        // now to like a comment 1st current user should be there
        User currentUser = currentUser();

        // mandatory check that if the post which contains comment/reply to be liked exists or not
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "post Id", postId));

        // mandatory check that if the comment to be liked exists or not
        Comment likedComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "comment Id", commentId));

        // we check if our comment exists in the post or not
        if (!likedComment.getPost().getId().equals(postId)) {

            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "Comment doesn't belong to the post");
        }

        // now we check if the comment has been already liked by the user or not
        Optional<Like> liked = likeRepository.findByUserAndComment(currentUser, likedComment);

        if (liked.isEmpty()) {
            // it's not liked so we like it
            Like like = new Like();
            like.setUser(currentUser);
            like.setComment(likedComment);

            likeRepository.save(like);

            // increment the likes of the comment
            likedComment.setLikes(likedComment.getLikes() + 1);
        } else {
            // we unlike it
            likeRepository.delete(liked.get());

            // decrement the likes of the comment
            likedComment.setLikes(likedComment.getLikes() - 1);
        }

        // then we save the comment
        commentRepository.save(likedComment);
    }
}

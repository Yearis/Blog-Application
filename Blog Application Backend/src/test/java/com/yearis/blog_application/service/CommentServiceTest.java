package com.yearis.blog_application.service;

import com.yearis.blog_application.entity.Comment;
import com.yearis.blog_application.entity.Like;
import com.yearis.blog_application.entity.Post;
import com.yearis.blog_application.entity.User;
import com.yearis.blog_application.exception.BlogAPIException;
import com.yearis.blog_application.exception.ResourceNotFoundException;
import com.yearis.blog_application.payload.request.CommentRequest;
import com.yearis.blog_application.payload.response.CommentResponse;
import com.yearis.blog_application.repository.CommentRepository;
import com.yearis.blog_application.repository.LikeRepository;
import com.yearis.blog_application.repository.PostRepository;
import com.yearis.blog_application.repository.UserRepository;
import com.yearis.blog_application.service.impl.CommentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private CommentServiceImpl commentService;

    private User user;
    private Post post;
    private Comment parentComment;
    private Comment replyComment;
    private CommentRequest parentCommentRequest;
    private CommentRequest replyCommentRequest;

    @BeforeEach
    public void setup() {
        user = User.builder()
                .id(1L)
                .username("Test User")
                .email("test@email.com")
                .roles(Collections.emptySet())
                .build();

        post = Post.builder()
                .id(100L)
                .title("Test Post")
                .author(user)
                .build();

        parentCommentRequest = new CommentRequest();
        parentCommentRequest.setBody("Parent comment");

        parentComment = Comment.builder()
                .id(1000L)
                .body("Parent comment")
                .author(user)
                .post(post)
                .build();

        replyCommentRequest = new CommentRequest();
        replyCommentRequest.setBody("Reply comment");
        replyCommentRequest.setParentId(1000L);

        replyComment = Comment.builder()
                .id(1001L)
                .body("Reply comment")
                .author(user)
                .post(post)
                .parent(parentComment)
                .build();

        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("test@email.com");
        SecurityContextHolder.setContext(securityContext);

        lenient().when(userRepository.findByUsernameOrEmail(eq("test@email.com"), eq("test@email.com")))
                .thenReturn(Optional.of(user));
    }

    @Test
    void createParentComment_Success() {
        when(postRepository.findById(100L)).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class))).thenReturn(parentComment);
        when(likeRepository.save(any(Like.class))).thenReturn(new Like());

        CommentResponse response = commentService.createComment(post.getId(), parentCommentRequest);

        assertNotNull(response);
        assertEquals(1000L, response.getId());
        verify(likeRepository, times(1)).save(any(Like.class));
    }

    @Test
    void createReplyComment_Success() {
        when(postRepository.findById(100L)).thenReturn(Optional.of(post));
        when(commentRepository.findById(1000L)).thenReturn(Optional.of(parentComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(replyComment);
        when(likeRepository.save(any(Like.class))).thenReturn(new Like());

        CommentResponse response = commentService.createComment(post.getId(), replyCommentRequest);

        assertNotNull(response);
        assertEquals(1000L, response.getParentId());
    }

    @Test
    void createReplyComment_WrongPost_ShouldThrowException() {
        Post wrongPost = Post.builder().id(999L).build();
        when(postRepository.findById(999L)).thenReturn(Optional.of(wrongPost));

        when(commentRepository.findById(1000L)).thenReturn(Optional.of(parentComment));

        assertThrows(BlogAPIException.class, () -> {
            commentService.createComment(999L, replyCommentRequest);
        });
    }

    @Test
    void createReply_Fail_ParentNotFound() {
        replyCommentRequest.setParentId(9999L);

        when(postRepository.findById(100L)).thenReturn(Optional.of(post));
        when(commentRepository.findById(9999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.createComment(100L, replyCommentRequest);
        });
    }

    @Test
    void getRepliesByCommentId_Success() {
        when(commentRepository.findById(1000L)).thenReturn(Optional.of(parentComment));

        Page<Comment> page = new PageImpl<>(List.of(replyComment));
        when(commentRepository.findByParentId(eq(1000L), any(Pageable.class))).thenReturn(page);

        List<CommentResponse> result = commentService.getRepliesByCommentId(100L, 1000L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Reply comment", result.get(0).getBody());
    }

    @Test
    void getRepliesByCommentId_Fail_ParentBelongsToWrongPost() {
        Post otherPost = Post.builder().id(999L).build();
        Comment wrongParent = Comment.builder().id(1000L).post(otherPost).build();

        when(commentRepository.findById(1000L)).thenReturn(Optional.of(wrongParent));

        assertThrows(BlogAPIException.class, () -> {
            commentService.getRepliesByCommentId(100L, 1000L, 0, 10);
        });
    }

    @Test
    void getCommentById_Success() {
        when(postRepository.findById(100L)).thenReturn(Optional.of(post));
        when(commentRepository.findById(1000L)).thenReturn(Optional.of(parentComment));

        CommentResponse response = commentService.getCommentById(100L, 1000L);

        assertNotNull(response);
        assertEquals(1000L, response.getId());
    }

    @Test
    void getCommentsByPostId_Success() {
        Page<Comment> page = new PageImpl<>(List.of(parentComment));
        when(commentRepository.findByPostIdAndParentIdIsNull(eq(100L), any(Pageable.class))).thenReturn(page);

        List<CommentResponse> result = commentService.getCommentsByPostId(100L, 0, 10);

        assertEquals(1, result.size());
    }

    @Test
    void updateComment_Success() {
        CommentRequest updateRequest = new CommentRequest();
        updateRequest.setBody("Updated content");

        when(postRepository.findById(100L)).thenReturn(Optional.of(post));
        when(commentRepository.findById(1000L)).thenReturn(Optional.of(parentComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(parentComment);

        CommentResponse result = commentService.updateComment(100L, 1000L, updateRequest);

        assertEquals("Updated content", result.getBody());
    }

    @Test
    void updateComment_ReplyComment_Success() {
        CommentRequest updateReq = new CommentRequest();
        updateReq.setBody("Updated Reply Body");

        when(postRepository.findById(100L)).thenReturn(Optional.of(post));
        when(commentRepository.findById(1001L)).thenReturn(Optional.of(replyComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(replyComment);

        CommentResponse response = commentService.updateComment(100L, 1001L, updateReq);

        assertEquals("Updated Reply Body", response.getBody());
        assertEquals(1000L, response.getParentId());
    }

    @Test
    void updateComment_Unauthorized_ShouldThrowException() {
        User hacker = User.builder().id(99L).username("hacker").build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("hacker");
        when(userRepository.findByUsernameOrEmail(eq("hacker"), eq("hacker"))).thenReturn(Optional.of(hacker));

        when(postRepository.findById(100L)).thenReturn(Optional.of(post));
        when(commentRepository.findById(1000L)).thenReturn(Optional.of(parentComment));

        CommentRequest request = new CommentRequest();
        request.setBody("Hack");

        assertThrows(BlogAPIException.class, () -> {
            commentService.updateComment(100L, 1000L, request);
        });
    }

    @Test
    void deleteComment_NoReplies_HardDelete() {
        parentComment.setReplies(Collections.emptyList());

        when(postRepository.findById(100L)).thenReturn(Optional.of(post));
        when(commentRepository.findById(1000L)).thenReturn(Optional.of(parentComment));

        commentService.deleteComment(100L, 1000L);

        verify(commentRepository, times(1)).delete(parentComment);
    }

    @Test
    void deleteComment_WithReplies_SoftDelete() {
        parentComment.setReplies(List.of(replyComment));

        when(postRepository.findById(100L)).thenReturn(Optional.of(post));
        when(commentRepository.findById(1000L)).thenReturn(Optional.of(parentComment));

        commentService.deleteComment(100L, 1000L);

        verify(commentRepository, times(1)).save(parentComment);
        verify(commentRepository, never()).delete(parentComment);
        assertEquals("[deleted by user]", parentComment.getBody());
    }

    @Test
    void deleteComment_ReplyComment_HardDelete() {
        replyComment.setParent(parentComment);
        replyComment.setReplies(Collections.emptyList());

        when(postRepository.findById(100L)).thenReturn(Optional.of(post));
        when(commentRepository.findById(1001L)).thenReturn(Optional.of(replyComment));

        commentService.deleteComment(100L, 1001L);

        verify(commentRepository, times(1)).delete(replyComment);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void getCommentsByUserId_Success() {
        Page<Comment> page = new PageImpl<>(List.of(parentComment));
        when(commentRepository.findByAuthorId(eq(1L), any(Pageable.class))).thenReturn(page);

        List<CommentResponse> result = commentService.getCommentsByUserId(1L, 0, 10);

        assertEquals(1, result.size());
        assertEquals("Parent comment", result.get(0).getBody());
    }

    @Test
    void getCommentsByUserId_Unauthorized_AccessingOthersData() {
        assertThrows(BlogAPIException.class, () -> {
            commentService.getCommentsByUserId(50L, 0, 10);
        });
    }

    @Test
    void findLikedCommentsByUserId_Success() {
        Like like = new Like();
        like.setUser(user);
        like.setComment(parentComment);

        Page<Like> page = new PageImpl<>(List.of(like));
        when(likeRepository.findByUserIdAndCommentIsNotNull(eq(1L), any(Pageable.class))).thenReturn(page);

        List<CommentResponse> result = commentService.findLikedCommentsByUserId(1L, 0, 10);

        assertEquals(1, result.size());
        assertEquals(1000L, result.get(0).getId());
    }

    @Test
    void findLikedCommentsByUserId_Unauthorized() {
        assertThrows(BlogAPIException.class, () -> {
            commentService.findLikedCommentsByUserId(99L, 0, 10);
        });
    }

    @Test
    void getCommentById_CommentNotFound_ShouldThrowException() {
        when(postRepository.findById(100L)).thenReturn(Optional.of(post));
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.getCommentById(100L, 999L);
        });
    }

    @Test
    void getCommentById_WrongPost_ShouldThrowException() {
        Post wrongPost = Post.builder().id(999L).build();
        when(postRepository.findById(999L)).thenReturn(Optional.of(wrongPost));
        when(commentRepository.findById(1000L)).thenReturn(Optional.of(parentComment));

        assertThrows(BlogAPIException.class, () -> {
            commentService.getCommentById(999L, 1000L);
        });
    }

    @Test
    void updateComment_WrongPost_ShouldThrowException() {
        Post wrongPost = Post.builder().id(999L).build();
        when(postRepository.findById(999L)).thenReturn(Optional.of(wrongPost));
        when(commentRepository.findById(1000L)).thenReturn(Optional.of(parentComment));

        CommentRequest request = new CommentRequest();
        request.setBody("Doesn't matter");

        assertThrows(BlogAPIException.class, () -> {
            commentService.updateComment(999L, 1000L, request);
        });
    }

    @Test
    void deleteComment_Admin_Success() {
        User admin = User.builder().id(50L).username("admin").build();
        com.yearis.blog_application.entity.Role adminRole = new com.yearis.blog_application.entity.Role();
        adminRole.setName("ROLE_ADMIN");
        admin.setRoles(java.util.Set.of(adminRole));

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin");
        when(userRepository.findByUsernameOrEmail(eq("admin"), eq("admin"))).thenReturn(Optional.of(admin));

        when(postRepository.findById(100L)).thenReturn(Optional.of(post));
        when(commentRepository.findById(1000L)).thenReturn(Optional.of(parentComment));

        commentService.deleteComment(100L, 1000L);

        verify(commentRepository, times(1)).delete(parentComment);
    }

    @Test
    void createReply_Fail_ParentBelongsToDifferentPost() {

        when(postRepository.findById(100L)).thenReturn(Optional.of(post));

        Post differentPost = Post.builder().id(999L).build();
        Comment alienParent = Comment.builder().id(5000L).post(differentPost).build();

        replyCommentRequest.setParentId(5000L);

        when(commentRepository.findById(5000L)).thenReturn(Optional.of(alienParent));

        assertThrows(BlogAPIException.class, () -> {
            commentService.createComment(100L, replyCommentRequest);
        });
    }

    @Test
    void deleteComment_WrongPost_ShouldThrowException() {

        Post wrongPost = Post.builder().id(999L).build();
        when(postRepository.findById(999L)).thenReturn(Optional.of(wrongPost));

        when(commentRepository.findById(1000L)).thenReturn(Optional.of(parentComment));

        assertThrows(BlogAPIException.class, () -> {
            commentService.deleteComment(999L, 1000L);
        });
    }
}
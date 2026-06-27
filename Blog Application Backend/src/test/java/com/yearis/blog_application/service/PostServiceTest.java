package com.yearis.blog_application.service;

import com.yearis.blog_application.entity.Like;
import com.yearis.blog_application.entity.Post;
import com.yearis.blog_application.entity.User;
import com.yearis.blog_application.exception.BlogAPIException;
import com.yearis.blog_application.exception.ResourceNotFoundException;
import com.yearis.blog_application.payload.request.PostRequest;
import com.yearis.blog_application.payload.response.PostResponse;
import com.yearis.blog_application.repository.CommentRepository;
import com.yearis.blog_application.repository.LikeRepository;
import com.yearis.blog_application.repository.PostRepository;
import com.yearis.blog_application.repository.UserRepository;
import com.yearis.blog_application.service.impl.PostServiceImpl;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PostServiceImpl postService;

    private User user;
    private Post post;
    private PostRequest postRequest;

    @BeforeEach
    public void setup() {

        user = User.builder()
                .id(1L)
                .username("Test User")
                .password("test@123")
                .email("test@email.com")
                .roles(new HashSet<>())
                .build();

        postRequest = new PostRequest();
        postRequest.setTitle("Test Title for Post");
        postRequest.setContent("This is a dummy content for our Post");

        post = Post.builder()
                .id(100L)
                .title("Test Title for Post")
                .content("This is a dummy content for our Post")
                .author(user)
                .likes(1)
                .build();

        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("test@email.com");
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createPost_Success() {
        when(userRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Optional.of(user));
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(likeRepository.save(any(Like.class))).thenReturn(new Like());

        PostResponse result = postService.createPost(postRequest);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("Test User", result.getAuthorName());
        verify(likeRepository, times(1)).save(any(Like.class));
    }

    @Test
    void createPost_UserNotFound_ShouldThrowException() {
        when(userRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            postService.createPost(postRequest);
        });
    }

    @Test
    void findPostById_Success() {
        when(postRepository.findById(100L)).thenReturn(Optional.of(post));

        PostResponse result = postService.findPostById(100L);

        assertNotNull(result);
        assertEquals(100L, post.getId());
        assertEquals("Test User", result.getAuthorName());
    }

    @Test
    void findPostById_PostNotFound_ShouldThrowException() {
        when(postRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            postService.findPostById(100L);
        });
    }

    @Test
    void findPostByTitle_Succeed() {
        List<Post> posts = List.of(post);
        Page<Post> page = new PageImpl<>(posts);

        when(postRepository.findByTitleContaining(eq("Title"), any(Pageable.class))).thenReturn(page);

        List<PostResponse> result = postService.findPostByTitle("Title", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(post.getTitle(), result.get(0).getTitle());
    }

    @Test
    void findPostByTitle_PostNotFound_ShouldThrowException() {
        when(postRepository.findByTitleContaining(eq("Ghost"), any(Pageable.class)))
                .thenReturn(Page.empty());

        List<PostResponse> result = postService.findPostByTitle("Ghost", 0, 10);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void findAllPosts_Success() {
        List<Post> posts = List.of(post);
        Page<Post> page = new PageImpl<>(posts);

        when(postRepository.findAll(any(Pageable.class))).thenReturn(page);

        List<PostResponse> result = postService.findAllPosts(0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void findAllPosts_EmptyDB() {
        when(postRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        List<PostResponse> result = postService.findAllPosts(0, 10);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void updatePost_Success() {
        when(userRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Optional.of(user));

        PostRequest updatedPost = new PostRequest();
        updatedPost.setTitle("Updated Title");
        updatedPost.setContent("Content for the test is now updated");

        when(postRepository.findById(100L)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenReturn(post);

        PostResponse result = postService.updatePost(updatedPost, 100L);

        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
    }

    @Test
    void updatePost_Unauthorized_ShouldThrowException() {

        User hacker = User.builder()
                .id(2L)
                .email("hacker@test.com")
                .roles(new HashSet<>())
                .build();

        when(userRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Optional.of(hacker));
        when(postRepository.findById(100L)).thenReturn(Optional.of(post));

        PostRequest request = new PostRequest();

        assertThrows(BlogAPIException.class, () -> {
            postService.updatePost(request, 100L);
        });
    }

    @Test
    void updatePost_PostNotFound_ShouldThrowException() {
        PostRequest updatedPost = new PostRequest();
        updatedPost.setTitle("Updated Title");
        updatedPost.setContent("Content for the test is now updated");

        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            postService.updatePost(updatedPost, 99L);
        });
    }

    @Test
    void deletePost_NoComments_HardDelete_Success() {
        when(userRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Optional.of(user));
        when(postRepository.findById(100L)).thenReturn(Optional.of(post));
        when(commentRepository.existsByPostId(100L)).thenReturn(false);

        postService.deletePost(100L);

        verify(postRepository, times(1)).delete(post);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void deletePost_WithComments_SoftDelete_Success() {
        when(userRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Optional.of(user));
        when(postRepository.findById(100L)).thenReturn(Optional.of(post));
        when(commentRepository.existsByPostId(100L)).thenReturn(true);
        when(postRepository.save(any(Post.class))).thenReturn(post);

        postService.deletePost(100L);

        verify(postRepository, times(1)).save(post);
        verify(postRepository, never()).delete(post);
        assertEquals("[deleted by user]", post.getContent());
        assertNull(post.getAuthor());
    }

    @Test
    void deletePost_PostNotFound_ShouldThrowException() {
        when(postRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            postService.deletePost(100L);
        });
    }

    @Test
    void deletePost_AdminCanDeleteOthersPost() {

        com.yearis.blog_application.entity.Role adminRole = new com.yearis.blog_application.entity.Role();
        adminRole.setName("ROLE_ADMIN");

        User adminUser = User.builder()
                .id(2L)
                .username("Admin")
                .email("admin@test.com")
                .roles(java.util.Set.of(adminRole))
                .build();

        when(userRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Optional.of(adminUser));

        when(postRepository.findById(100L)).thenReturn(Optional.of(post));
        when(commentRepository.existsByPostId(100L)).thenReturn(false);

        postService.deletePost(100L);

        // Assert
        verify(postRepository, times(1)).delete(post);
    }

    @Test
    void findPostByUserId_Success() {
        Page<Post> page = new PageImpl<>(List.of(post));
        when(postRepository.findByAuthorId(eq(1L), any(Pageable.class))).thenReturn(page);

        List<PostResponse> result = postService.findPostByUserId(1L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void findLikedPostsByUserId_Success() {
        when(userRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Optional.of(user));

        Like like = Like.builder().post(post).user(user).build();
        Page<Like> page = new PageImpl<>(List.of(like));

        when(likeRepository.findByUserIdAndPostIsNotNull(eq(1L), any(Pageable.class))).thenReturn(page);

        List<PostResponse> result = postService.findLikedPostsByUserId(1L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Title for Post", result.get(0).getTitle());
    }

    @Test
    void findLikedPostsByUserId_Unauthorized_ShouldThrowException() {
        User stranger = User.builder().id(2L).build();
        when(userRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Optional.of(user));

        assertThrows(BlogAPIException.class, () -> {
            postService.findLikedPostsByUserId(2L, 0, 10);
        });
    }
}
package com.yearis.blog_application;

import com.yearis.blog_application.entity.Comment;
import com.yearis.blog_application.entity.Like;
import com.yearis.blog_application.entity.Post;
import com.yearis.blog_application.entity.User;
import com.yearis.blog_application.repository.CommentRepository;
import com.yearis.blog_application.repository.LikeRepository;
import com.yearis.blog_application.repository.PostRepository;
import com.yearis.blog_application.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.annotation.Rollback;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(value = false)
class BlogApplicationTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private LikeRepository likeRepository; //

    @Test
    public void treatCreateBlogFlow() {

        // 1. Create User
        User theUser = User.builder()
                .username("Test User")
                .email("test_flow@email.com")
                .password("Password@test1")
                .build();
        User savedUser = userRepository.save(theUser);
        assertNotNull(savedUser.getId());
        System.out.println("1. User Created: " + savedUser.getId());


        // 2. Create Post
        Post thePost = Post.builder()
                .title("Test Blog Flow")
                .content("This is a test post for the full flow")
                .author(savedUser)
                .likes(0) // Start with 0
                .build();
        Post savedPost = postRepository.save(thePost);
        assertNotNull(savedPost.getId());
        System.out.println("2. Post Created: " + savedPost.getId());


        // 3. Create Like (User unlikes the Post)
        Like theLike = Like.builder()
                .user(savedUser)
                .post(savedPost)
                .comment(null) // as it's a post like
                .build();
        Like savedLike = likeRepository.save(theLike);
        assertNotNull(savedLike.getId());
        System.out.println("3. Like Created on Post: " + savedLike.getId());


        // 4. Create Comment
        Comment theComment = Comment.builder()
                .body("This is a parent comment")
                .author(savedUser)
                .post(savedPost)
                .parent(null)
                .build();
        Comment savedComment = commentRepository.save(theComment);
        assertNotNull(savedComment.getId());
        System.out.println("4. Comment Created: " + savedComment.getId());


        // 5. Create Reply
        Comment theReply = Comment.builder()
                .body("This is a reply")
                .author(savedUser)
                .post(savedPost)
                .parent(savedComment)
                .build();
        Comment savedReply = commentRepository.save(theReply);
        assertNotNull(savedReply.getId());
        System.out.println("5. Reply Created: " + savedReply.getId());

        System.out.println("TEST PASSED: Full flow User -> Post -> Like -> Comment -> Reply working!");
    }
}
package com.yearis.blog_application.service.impl;

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
import com.yearis.blog_application.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;

    @Autowired
    public PostServiceImpl(PostRepository postRepository, UserRepository userRepository, CommentRepository commentRepository, LikeRepository likeRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
    }

    // get our current user
    private User currentUser() {

        String usernameOrEmail = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();

        return userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /// --- Mappers ---

    // Converts Entity -> Response DTO
    private PostResponse mapToResponse(Post post) {

        PostResponse response = new PostResponse();

        // we manually map Post to PostDTO
        response.setId(post.getId());
        response.setTitle(post.getTitle());
        response.setContent(post.getContent());
        response.setCreatedDate(post.getCreatedDate());
        response.setEdited(post.isEdited());
        response.setLikes(post.getLikes());

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

        return response;
    }

    // Convert Request DTO -> Entity
    private Post mapToEntity(PostRequest request) {

        Post post = new Post();

        // we manually map PostDTO to Post
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());

        return post;
    }

    /// --- CRUD Operations ---

    /// C: Create/Save

    @Override
    @Transactional
    public PostResponse createPost(PostRequest postRequest) {

        // we map our request DTO to our entity from the request that was received
        Post post = mapToEntity(postRequest);

        // now we link the user to the post
        post.setAuthor(currentUser());

        // we save our post to database
        Post newPost = postRepository.save(post);

        Like firstLike = new Like();
        firstLike.setUser(currentUser());
        firstLike.setPost(newPost);
        // and we don't set the comment as this like is only for post

        likeRepository.save(firstLike);

        return mapToResponse(newPost);
    }

    /// R: Read/Find/Get

    // finding a post by its ID
    @Override
    @Transactional(readOnly = true)
    public PostResponse findPostById(Long id) {

        // we find the post if it doesn't exist, we throw an error
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "post Id", id));

        return mapToResponse(post);
    }

    // finding a post by its Title
    @Override
    @Transactional(readOnly = true)
    public List<PostResponse> findPostByTitle(String title, int pageNo, int pageSize) {

        Sort sort = Sort.by("createdDate").descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Page<Post> posts = postRepository.findByTitleContaining(title, pageable);

        return posts.stream()
                .map(post -> mapToResponse(post))
                .collect(Collectors.toList());
    }

    // we don't directly use postRepository.findAll() here as it would load everything even if we have 1 mil post.
    // That would give OutOfMemoryError so we divide into pages
    @Override
    @Transactional(readOnly = true)
    public List<PostResponse> findAllPosts(int pageNo, int pageSize) {

        Sort sort = Sort.by("createdDate").descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Page<Post> postPage = postRepository.findAll(pageable);

        List<Post> posts = postPage.getContent();

        return posts.stream()
                .map(post -> mapToResponse(post))
                .collect(Collectors.toList());
    }

    /// U: Update

    // update an already existing post
    @Override
    @Transactional
    public PostResponse updatePost(PostRequest postRequest, Long id) {

        // here a new DTO has been sent with the updated info, so we just update that in our og post
        // we find the post if it doesn't exist, we throw an error
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "post Id", id));

        // now we check that does user even has authority to update post
        User currentUser = currentUser();

        User owner = post.getAuthor();

        if (!currentUser.equals(owner)) {

            throw new BlogAPIException(HttpStatus.UNAUTHORIZED, "Unauthorized Access!");
        }

        // we only allow the change for title n content
        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());
        post.setEdited(true);

        // save/update the new post
        Post updatedPost = postRepository.save(post);

        return mapToResponse(updatedPost);
    }

    /// D: Delete

    // delete a post
    @Override
    @Transactional
    public void deletePost(Long id) {

        // 1st, we check if the post to be deleted exists or not
        // if it doesn't exist, we throw an error
        Post post = postRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Post", "post Id", id));

        User currentUser = currentUser();
        User owner = post.getAuthor();

        boolean isOwner = currentUser.equals(owner);
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));

        // as an admin should be allowed to delete inappropriate posts
        if (!isOwner && !isAdmin) {

            throw new BlogAPIException(HttpStatus.UNAUTHORIZED, "Unauthorized Access!");
        }

        // now we need it so that when post is deleted it doesn't collapse the comments
        // check if any comments in the list
        boolean hasComments = commentRepository.existsByPostId(id);
        if (hasComments) {

            if (isAdmin && !isOwner) {
                post.setContent("[removed by admin]"); // Admin removed it
            } else {
                post.setContent("[deleted by user]"); // User deleted it
            }

            post.setAuthor(null);
            post.setEdited(true);
            postRepository.save(post);
        } else {

            postRepository.delete(post);
        }
    }

    /// --- Custom Methods ---

    // to get all the posts created by user
    @Override
    @Transactional(readOnly = true)
    public List<PostResponse> findPostByUserId(Long userId, int pageNo, int pageSize) {

        // in this method we don't check if our current user is same as the userId user as a users posts are public
        // the user doesn't even have to be logged in for this method

        // sorting and breaking the posts into pages
        Sort sort = Sort.by("createdDate").descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        // now we find the posts created by our currentUser
        Page<Post> userPosts = postRepository.findByAuthorId(userId, pageable);

        return userPosts.stream()
                .map(post -> mapToResponse(post))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostResponse> findLikedPostsByUserId(Long userId, int pageNo, int pageSize) {

        // get current user
        User currentUser = currentUser();

        // here we check if our current user's id is same as what's being passed in the methods
        if (!currentUser.getId().equals(userId)) {

            throw new BlogAPIException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
        }

        // sorting and breaking the posts into pages
        Sort sort = Sort.by("createdDate").descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);


        Page<Like> likedPosts = likeRepository.findByUserIdAndPostIsNotNull(userId, pageable);

        return likedPosts.stream()
                .map(like -> mapToResponse(like.getPost()))
                .collect(Collectors.toList());
    }
}

package com.yearis.blog_application.service.impl;

import com.yearis.blog_application.entity.Like;
import com.yearis.blog_application.entity.Post;
import com.yearis.blog_application.entity.User;
import com.yearis.blog_application.exception.BadRequestException;
import com.yearis.blog_application.exception.ResourceNotFoundException;
import com.yearis.blog_application.exception.UnAuthenticatedException;
import com.yearis.blog_application.mappers.PostMapper;
import com.yearis.blog_application.payload.request.PostRequest;
import com.yearis.blog_application.payload.request.PostUpdateRequest;
import com.yearis.blog_application.payload.response.PostResponse;
import com.yearis.blog_application.repository.CommentRepository;
import com.yearis.blog_application.repository.LikeRepository;
import com.yearis.blog_application.repository.PostRepository;
import com.yearis.blog_application.repository.UserRepository;
import com.yearis.blog_application.service.PostService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
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
@AllArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final PostMapper postMapper;

    // get our current user
    private User currentUser() {

        String usernameOrEmail = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();

        return userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /// --- CRUD Operations ---

    /// C: Create/Save

    @Override
    @Transactional
    public PostResponse createPost(PostRequest postRequest) {

        User currentUser = currentUser();

        // we map our request DTO to our entity from the request that was received
        Post post = postMapper.mapToEntity(postRequest);

        // now we link the user to the post
        post.setAuthor(currentUser);

        // we save our post to database
        Post newPost = postRepository.save(post);

        Like firstLike = new Like();
        firstLike.setUser(currentUser);
        firstLike.setPost(newPost);

        // and we don't set the comment as this like is only for post

        likeRepository.save(firstLike);

        return postMapper.mapToResponse(newPost);
    }

    /// R: Read/Find/Get

    // finding a post by its ID
    @Override
    @Transactional(readOnly = true)
    public PostResponse findPostById(Long id) {

        // we find the post if it doesn't exist, we throw an error
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "post Id", id));

        return postMapper.mapToResponse(post);
    }

    // finding a post by its Title
    @Override
    @Transactional(readOnly = true)
    public List<PostResponse> findPostByTitle(String title, int pageNo, int pageSize) {

        Sort sort = Sort.by("createdDate").descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Page<Post> posts = postRepository.findByTitleContaining(title, pageable);

        return posts.stream()
                .map(postMapper::mapToResponse)
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
                .map(postMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    /// U: Update

    // update an already existing post
    @Override
    @Transactional
    public PostResponse updatePost(PostUpdateRequest postUpdateRequest, Long id) {

        // here a new DTO has been sent with the updated info, so we just update that in our og post
        // we find the post if it doesn't exist, we throw an error
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "post Id", id));

        // now we check that does user even has authority to update post
        User currentUser = currentUser();

        User owner = post.getAuthor();

        if (!currentUser.equals(owner)) {

            throw new UnAuthenticatedException("Unauthorized Access!");
        }

        if (postUpdateRequest.getTitle() != null) {

            if (postUpdateRequest.getTitle().trim().isEmpty()) {

                throw new BadRequestException("Post title cannot be empty");
            }

            post.setTitle(postUpdateRequest.getTitle());
        }

        if (postUpdateRequest.getContent() != null) {

            if (postUpdateRequest.getContent().trim().isEmpty()) {

                throw new BadRequestException("Post content cannot be empty");
            }

            post.setContent(postUpdateRequest.getContent());
        }

        post.setEdited(true);

        // save/update the new post
        Post updatedPost = postRepository.save(post);

        return postMapper.mapToResponse(updatedPost);
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

            throw new UnAuthenticatedException("Unauthorized Access!");
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
                .map(postMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostResponse> findLikedPostsByUserId(Long userId, int pageNo, int pageSize) {

        // get current user
        User currentUser = currentUser();

        // here we check if our current user's id is same as what's being passed in the methods
        if (!currentUser.getId().equals(userId)) {

            throw new UnAuthenticatedException("Unauthorized access");
        }

        // sorting and breaking the posts into pages
        Sort sort = Sort.by("createdDate").descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);


        Page<Like> likedPosts = likeRepository.findByUserIdAndPostIsNotNull(userId, pageable);

        return likedPosts.stream()
                .map(like -> postMapper.mapToResponse(like.getPost()))
                .collect(Collectors.toList());
    }
}

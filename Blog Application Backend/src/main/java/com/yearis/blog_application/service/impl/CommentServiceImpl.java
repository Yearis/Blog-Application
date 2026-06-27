package com.yearis.blog_application.service.impl;

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
import com.yearis.blog_application.service.CommentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;

    public CommentServiceImpl(CommentRepository commentRepository, PostRepository postRepository, UserRepository userRepository, LikeRepository likeRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
    }

    // get our current user
    private User currentUser() {

        String usernameOrEmail = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();

        return userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /// --- Mappers ---

    // Converts Entity -> Response DTO
    private CommentResponse mapToResponse(Comment comment) {

        CommentResponse response = new CommentResponse();

        // we manually map Comment to CommentDTO
        response.setId(comment.getId());
        response.setBody(comment.getBody());
        response.setCreatedDate(comment.getCreatedDate());
        response.setEdited(comment.isEdited());
        response.setLikes(comment.getLikes());

        // post info
        response.setPostId(comment.getPost().getId());

        // we also give frontend parent ID for the comment if it exists
        if (comment.getParent() != null) {
            // if comment has a parent meaning its not root comment
            response.setParentId(comment.getParent().getId());
        }

        // handling author info
        if (comment.getAuthor() != null) {
            response.setAuthorId(comment.getAuthor().getId());
            response.setAuthorName(comment.getAuthor().getUsername());
        } else {

            response.setAuthorId(null);

            // Check body to decide label
            if ("[deleted by user]".equals(comment.getBody()) || "[removed by admin]".equals(comment.getBody())) {
                response.setAuthorName("[removed]"); // User deleted the comment
            } else {
                response.setAuthorName("[deleted]"); // User deleted their account
            }
        }

        return response;
    }

    // Convert Request DTO -> Entity
    private Comment mapToEntity(CommentRequest request) {

        Comment comment = new Comment();

        // we manually map CommentDTO to Comment
        comment.setBody(request.getBody());

        return comment;
    }

    /// --- CRUD Operations ---

    /// C: Create/Save

    @Override
    @Transactional
    public CommentResponse createComment(Long postId, CommentRequest commentRequest) {

        // 1st, we map our DTO to our entity
        Comment comment = mapToEntity(commentRequest);

        // our comment doesn't have a post, so we use post repository to get the post if it doesn't exist, we throw an error
        Post post = postRepository.findById(postId)
                        .orElseThrow(() -> new ResourceNotFoundException("Post", "post Id", postId));

        // now we link our comment to a post then to current user
        comment.setPost(post);
        comment.setAuthor(currentUser());

        // now we check if our comment has a parent or not
        // if it has a parent
        if (commentRequest.getParentId() != null) {

            // 1st, we check if that parent exists or not
            Comment parent = commentRepository.findById(commentRequest.getParentId())
                                .orElseThrow(() -> new ResourceNotFoundException("Comment", "comment Id", commentRequest.getParentId()));

            // does our parent comment belong to the same post
            if (!parent.getPost().getId().equals(postId)) {
                throw new BlogAPIException(HttpStatus.BAD_REQUEST, "Comment doesn't belong to the post");
            }

            comment.setParent(parent);
        }

        // now we save our comment to DB
        Comment newComment = commentRepository.save(comment);

        Like firstLike = new Like();
        firstLike.setUser(currentUser());
        firstLike.setComment(newComment);
        // and we don't set the post as this like is only for comment

        likeRepository.save(firstLike);

        return mapToResponse(newComment);
    }

    /// R: Read/Find/Get

    @Override
    @Transactional(readOnly = true)
    public CommentResponse getCommentById(Long postId, Long commentId) {

        Post post = postRepository.findById(postId)
                            .orElseThrow(() -> new ResourceNotFoundException("Post", "post Id", postId));

        // if our comment doesn't exist we throw error
        Comment comment = commentRepository.findById(commentId)
                            .orElseThrow(() -> new ResourceNotFoundException("Comment", "comment Id", commentId));

        if (!comment.getPost().getId().equals(postId)) {
            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "Comment doesn't belong to the post");
        }

        return mapToResponse(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPostId(Long postId, int pageNo, int pageSize) {

        Sort sort = Sort.by("likes").descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Page<Comment> comments = commentRepository.findByPostIdAndParentIdIsNull(postId, pageable);

        return comments.stream()
                .map(comment -> mapToResponse(comment))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getRepliesByCommentId(Long postId, Long commentId, int pageNo, int pageSize) {

        // 1. Find the Parent Comment (The "Thread Starter")
        Comment parentComment = commentRepository.findById(commentId)
                                    .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        // 2. SAFETY CHECK: Does this parent comment belong to the URL's post?
        if (!parentComment.getPost().getId().equals(postId)) {
                throw new BlogAPIException(HttpStatus.BAD_REQUEST, "Comment does not belong to this post");
        }

        Sort sort = Sort.by("createdDate").ascending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Page<Comment> comments = commentRepository.findByParentId(commentId, pageable);

        return comments.stream()
                .map(comment -> mapToResponse(comment))
                .collect(Collectors.toList());
    }

    /// U: Update

    @Override
    @Transactional
    public CommentResponse updateComment(Long postId, Long commentId, CommentRequest commentRequest) {

        // we use post repository to get the post if it doesn't exist, we throw an error
        Post post = postRepository.findById(postId)
                        .orElseThrow(() -> new ResourceNotFoundException("Post", "post Id", postId));

        // we use comment repository to get the comment if it doesn't exist, we throw an error
        Comment comment = commentRepository.findById(commentId)
                            .orElseThrow(() -> new ResourceNotFoundException("Comment", "comment Id", commentId));

        // does our parent comment belong to the same post
        if (!comment.getPost().getId().equals(postId)) {
            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "Comment doesn't belong to the post");
        }

        // now we check that does user even has authority to update comment
        User currentUser = currentUser();

        User owner = comment.getAuthor();

        if (!currentUser.equals(owner)) {

            throw new BlogAPIException(HttpStatus.UNAUTHORIZED, "Unauthorized Access!");
        }

        // if everything is fine we allow the update
        comment.setBody(commentRequest.getBody());
        comment.setEdited(true);

        Comment updatedComment = commentRepository.save(comment);

        return mapToResponse(updatedComment);
    }

    /// D: Delete

    @Override
    @Transactional
    public void deleteComment(Long postId, Long commentId) {

        // we use post repository to get the post if it doesn't exist, we throw an error
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "post Id", postId));

        // we use comment repository to get the comment if it doesn't exist, we throw an error
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "comment Id", commentId));

        // does our parent comment belong to the same post
        if (!comment.getPost().getId().equals(postId)) {
            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "Comment doesn't belong to the post");
        }

        User currentUser = currentUser();
        User postOwner = post.getAuthor();
        User commentOwner = comment.getAuthor();

        boolean isCommentOwner = currentUser.equals(commentOwner);
        boolean isPostOwner = currentUser.equals(postOwner);
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));

        // as an admin should be allowed to delete inappropriate posts
        if (!isCommentOwner && !isPostOwner && !isAdmin) {

            throw new BlogAPIException(HttpStatus.UNAUTHORIZED, "Unauthorized Access!");
        }

        // now we need it so that when parent comment is deleted it doesn't collapse the replies

        // check if any replies in the list
        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {

            if ((isAdmin || isPostOwner) && !isCommentOwner) {
                comment.setBody("[removed by admin]"); // Admin or Post Owner removed it
            } else {
                comment.setBody("[deleted by user]"); // Author deleted it
            }

            comment.setAuthor(null);
            comment.setEdited(true);

            // now we save this rather than deleting in a way it's an update
            commentRepository.save(comment);
        } else {

            // if their no comments we can safely delete it
            commentRepository.delete(comment);
        }
    }

    /// --- Custom Methods ---

    // we need all the comments made by our user
    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByUserId(Long userId, int pageNo, int pageSize) {

        // we get the current user
        User currentUser = currentUser();

        // here we check if our current user's id is same as what's being passed in the methods
        // as comments are not public but private
        if (!currentUser.getId().equals(userId)) {

            throw new BlogAPIException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
        }

        // sorting and breaking the comments into pages
        Sort sort = Sort.by("createdDate").descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        // now we find the comments created by our currentUser
        Page<Comment> userComments = commentRepository.findByAuthorId(userId, pageable);

        return userComments.stream()
                .map(comment -> mapToResponse(comment))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> findLikedCommentsByUserId(Long userId, int pageNo, int pageSize) {

        // get current user
        User currentUser = currentUser();

        // here we check if our current user's id is same as what's being passed in the methods
        if (!currentUser.getId().equals(userId)) {

            throw new BlogAPIException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
        }

        // sorting and breaking the comments into pages
        Sort sort = Sort.by("createdDate").descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Page<Like> likedComments = likeRepository.findByUserIdAndCommentIsNotNull(userId, pageable);

        return likedComments.stream()
                .map(like -> mapToResponse(like.getComment()))
                .collect(Collectors.toList());
    }
}

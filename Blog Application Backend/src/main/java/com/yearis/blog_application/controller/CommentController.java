package com.yearis.blog_application.controller;

import com.yearis.blog_application.payload.request.CommentRequest;
import com.yearis.blog_application.payload.response.CommentResponse;
import com.yearis.blog_application.service.CommentService;
import com.yearis.blog_application.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Comment Rest API Endpoints", description = "Operations related to comments")
@RestController
@RequestMapping("/api/posts/{postId}/comments") // as comments will be created/updated/deleted inside a post
public class CommentController {

    private final CommentService commentService;
    private final LikeService likeService;

    public CommentController(CommentService commentService, LikeService likeService) {
        this.commentService = commentService;
        this.likeService = likeService;
    }

    /// --- CRUD Operations ---

    /// C: Create/Save

    // create a new comment
    @Operation(summary = "Create a new comment", description = "Add a new comment/reply to the Database")
    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @Parameter(description = "ID of the post where you want to create the comment") @PathVariable Long postId,
            @Parameter(description = "payload to create the post") @Valid @RequestBody CommentRequest commentRequest) {

        // as we receive a response n the post that comment will belong to, we sent it to our service class so that it 1st find the post then,
        // creates a new comment object and then maps it to that obj
        CommentResponse savedComment = commentService.createComment(postId, commentRequest);

        // we return the commentDTO so that after clicking the save,
        // the user is redirected to his post
        return new ResponseEntity<>(savedComment, HttpStatus.CREATED);
    }

    /// R: Read/Find/Get

    // find comment by id
    @Operation(summary = "Get a comment by ID", description = "Retrieve a specific comment/reply by ID")
    @GetMapping("/{commentId}")
    public ResponseEntity<CommentResponse> getCommentById(
            @Parameter(description = "ID of the post where parent comment is located") @PathVariable Long postId,
            @Parameter(description = "ID of the parent comment of the reply") @PathVariable Long commentId) {

        // we find our comment
        CommentResponse comment = commentService.getCommentById(postId, commentId);

        return new ResponseEntity<>(comment, HttpStatus.OK);
    }

    // find all comment of a post
    @Operation(summary = "Get all the comments of a post", description = "Retrieve a list of all available comments of a post")
    @GetMapping
    public ResponseEntity<List<CommentResponse>> getCommentsByPostId(
            @Parameter(description = "ID of the post to get its comments") @PathVariable Long postId,
            @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize) {

        List<CommentResponse> comments = commentService.getCommentsByPostId(postId, pageNo, pageSize);

        return new ResponseEntity<>(comments, HttpStatus.OK);
    }

    // find replies of a comment
    @Operation(summary = "Get all the replies of a comment", description = "Retrieve a list of all available replies of a comment")
    @GetMapping("/{commentId}/replies")
    public ResponseEntity<List<CommentResponse>> getRepliesByCommentId(
            @Parameter(description = "ID of the post where parent comment is located") @PathVariable Long postId,
            @Parameter(description = "ID of the parent comment of the reply") @PathVariable Long commentId,
            @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize) {

        List<CommentResponse> replies = commentService.getRepliesByCommentId(postId, commentId, pageNo, pageSize);

        return new ResponseEntity<>(replies, HttpStatus.OK);
    }

    /// U: Update

    // update the comment body
    @Operation(summary = "Update a comment", description = "Update the details of an existing comment/reply")
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @Parameter(description = "ID of the post where parent comment is located") @PathVariable Long postId,
            @Parameter(description = "ID of the comment to update") @PathVariable Long commentId,
            @Parameter(description = "payload for the updated comment") @Valid @RequestBody CommentRequest commentRequest) {

        CommentResponse updatedComment = commentService.updateComment(postId, commentId, commentRequest);

        return new ResponseEntity<>(updatedComment, HttpStatus.OK);
    }

    /// D: Delete

    // delete a comment
    @Operation(summary = "Delete a comment", description = "Remove an existing comment/reply from Database")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(
            @Parameter(description = "ID of the post where parent comment is located") @PathVariable Long postId,
            @Parameter(description = "ID of the parent comment of the reply") @PathVariable Long commentId) {

        commentService.deleteComment(postId, commentId);

        return new ResponseEntity<>("Comment Deleted", HttpStatus.OK);
    }

    /// --- Custom Methods ---

    // to like a comment
    @Operation(summary = "Like/Unlike a comment", description = "Toggle like/unlike a comment for the current user")
    @PostMapping("/{commentId}/like")
    public ResponseEntity<String> toggleLikeComment(
            @Parameter(description = "ID of the post") @PathVariable Long postId,
            @Parameter(description = "ID of the comment") @PathVariable Long commentId) {

        likeService.toggleLikeComment(postId, commentId);

        return new ResponseEntity<>("Success", HttpStatus.OK);
    }
}

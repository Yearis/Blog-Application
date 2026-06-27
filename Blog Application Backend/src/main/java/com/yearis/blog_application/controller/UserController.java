package com.yearis.blog_application.controller;

import com.yearis.blog_application.payload.request.PasswordChangeRequest;
import com.yearis.blog_application.payload.request.UserUpdateRequest;
import com.yearis.blog_application.payload.response.CommentResponse;
import com.yearis.blog_application.payload.response.PostResponse;
import com.yearis.blog_application.payload.response.UserProfileResponse;
import com.yearis.blog_application.service.CommentService;
import com.yearis.blog_application.service.PostService;
import com.yearis.blog_application.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "User Rest API Endpoints", description = "Operations related to user")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final PostService postService;
    private final CommentService commentService;

    public UserController(UserService userService, PostService postService, CommentService commentService) {
        this.userService = userService;
        this.postService = postService;
        this.commentService = commentService;
    }

    /// --- CRUD Operations ---

    /// U: Update

    // update username
    @Operation(summary = "Update username", description = "Update username of an existing user")
    @PutMapping("/username")
    public ResponseEntity<String> updateUsername(
            @Valid @RequestBody UserUpdateRequest userUpdateRequest) {

        userService.updateUsername(userUpdateRequest);

        return new ResponseEntity<>("Success", HttpStatus.OK);
    }

    // update email
    @Operation(summary = "Update email", description = "Update email of an existing user")
    @PutMapping("/email")
    public ResponseEntity<String> updateEmail(
            @Valid @RequestBody UserUpdateRequest userUpdateRequest) {

        userService.updateEmail(userUpdateRequest);

        return new ResponseEntity<>("Success", HttpStatus.OK);
    }

    // update about
    @Operation(summary = "Update about section", description = "Update the about/bio section of the current user")
    @PutMapping("/about")
    public ResponseEntity<String> updateAbout(
            @Valid @RequestBody UserUpdateRequest userUpdateRequest) {

        userService.updateAbout(userUpdateRequest);

        return new ResponseEntity<>("Success", HttpStatus.OK);
    }

    // update password
    @Operation(summary = "Update password", description = "Update password of an existing user")
    @PutMapping("/password")
    public ResponseEntity<String> updatePassword(
            @Valid @RequestBody PasswordChangeRequest passwordChangeRequest) {

        userService.updatePassword(passwordChangeRequest);

        return new ResponseEntity<>("Success", HttpStatus.OK);
    }

    /// --- Custom Methods ---

    // to search for a user. (Public)
    @Operation(summary = "Search users", description = "Search for users by username.")
    @GetMapping("/search")
    public ResponseEntity<List<UserProfileResponse>> searchUsers(
            @Parameter(description = "The username to search for") @RequestParam("username") String username) {

        // logic to search users
        List<UserProfileResponse> result = userService.searchUsers(username);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // to show the posts created by a user. (Public)
    @Operation(summary = "Get posts by user", description = "Get all the posts created by user. This is a public api anyone can use to see a users created posts")
    @GetMapping("/{userId}/posts")
    public ResponseEntity<List<PostResponse>> findPostsCreatedByUser(
            @Parameter(description = "Id of the user whose post we want to view") @PathVariable Long userId,
            @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize) {

        List<PostResponse> posts = postService.findPostByUserId(userId, pageNo, pageSize);

        return new ResponseEntity<>(posts, HttpStatus.OK);
    }

    // to get the profile of a user. (Public)
    @Operation(summary = "Get User Profile", description = "Get the public profile (Username, About, Join Date) of any user by ID")
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getUserProfile(
            @Parameter(description = "ID of the user to fetch") @PathVariable Long id) {

        UserProfileResponse profile = userService.getPublicProfile(id);

        return new ResponseEntity<>(profile, HttpStatus.OK);
    }

    // to get the comments created by user. (Private)
    @Operation(summary = "Get comments by user", description = "Get all the comments created by user. This is a private api only for the current user to see his created comments")
    @GetMapping("/{userId}/comments")
    public ResponseEntity<List<CommentResponse>> findCommentsCreatedByUser(
            @Parameter(description = "Id of the user who created comments") @PathVariable Long userId,
            @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize) {

        List<CommentResponse> comments = commentService.getCommentsByUserId(userId, pageNo, pageSize);

        return new ResponseEntity<>(comments, HttpStatus.OK);
    }

    // to get the posts liked by user. (Private)
    @Operation(summary = "Get posts liked by user", description = "Get all the posts liked by user. This is a private api only for the current user to see his liked posts")
    @GetMapping("/{userId}/liked-posts")
    public ResponseEntity<List<PostResponse>> findLikedPostsByUser(
            @Parameter(description = "Id of the user who liked posts") @PathVariable Long userId,
            @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize) {

        List<PostResponse> posts = postService.findLikedPostsByUserId(userId, pageNo, pageSize);

        return new ResponseEntity<>(posts, HttpStatus.OK);
    }

    // to get the comments liked by user. (Private)
    @Operation(summary = "Get comments liked by user", description = "Get all the comments liked by user. This is a private api only for the current user to see his liked comments")
    @GetMapping("/{userId}/liked-comments")
    public ResponseEntity<List<CommentResponse>> findLikedCommentsByUser(
            @Parameter(description = "Id of the user who liked comments") @PathVariable Long userId,
            @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize) {

        List<CommentResponse> posts = commentService.findLikedCommentsByUserId(userId, pageNo, pageSize);

        return new ResponseEntity<>(posts, HttpStatus.OK);
    }
}

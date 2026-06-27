package com.yearis.blog_application.controller;

import com.yearis.blog_application.payload.request.PostRequest;
import com.yearis.blog_application.payload.response.PostResponse;
import com.yearis.blog_application.service.LikeService;
import com.yearis.blog_application.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Post Rest API Endpoints", description = "Operations related to posts")
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final LikeService likeService;

    @Autowired
    public PostController(PostService postService, LikeService likeService) {
        this.postService = postService;
        this.likeService = likeService;
    }

    /// --- CRUD Operations ---

    /// C: Create/Save

    // create a new post
    @Operation(summary = "Create a new post", description = "Add a new post to the Database")
    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @Parameter(description = "payload to create the post") @Valid @RequestBody PostRequest postRequest) {

        // as we receive a postDTO, we sent it to our service class so that it,
        // creates a new object and then maps it to that obj
        PostResponse savedPost = postService.createPost(postRequest);

        // we return the PostDTO so that after clicking the save,
        // the user is redirected to his post
        return new ResponseEntity<>(savedPost, HttpStatus.CREATED);
    }

    /// R: Read/Find/Get

    // to get post by id
    @Operation(summary = "Get a post by ID", description = "Retrieve a specific post by ID")
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(
            @Parameter(description = "ID of the post to retrieve") @PathVariable Long id) {

        // we find our post using the id
        PostResponse post = postService.findPostById(id);

        return new ResponseEntity<>(post, HttpStatus.OK);
    }

    // to get post by title
    @Operation(summary = "Get a post by Title", description = "Retrieve a specific post by Title")
    @GetMapping("/search")
    public ResponseEntity<List<PostResponse>> getPostByTitle(
            @Parameter(description = "Title of the post to retrieve") @RequestParam String title,
            @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize) {

        List<PostResponse> posts = postService.findPostByTitle(title, pageNo, pageSize);

        return new ResponseEntity<>(posts, HttpStatus.OK);
    }

    // to get all the posts
    @Operation(summary = "Get all the posts", description = "Retrieve a list of all available posts")
    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts(
            @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize) {

        List<PostResponse> posts = postService.findAllPosts(pageNo, pageSize);

        return new ResponseEntity<>(posts, HttpStatus.OK);
    }

    /// U: Update

    // update a post title/content (ig we would need 2 methods)
    @Operation(summary = "Update a post", description = "Update the details of an existing post")
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @Parameter(description = "payload for the updated post") @Valid @RequestBody PostRequest postRequest,
            @Parameter(description = "ID of the post to update") @PathVariable Long id) {

        PostResponse updatedPost = postService.updatePost(postRequest, id);

        return new ResponseEntity<>(updatedPost, HttpStatus.OK);
    }

    /// D: Delete

    // delete a post
    @Operation(summary = "Delete a post", description = "Remove an existing post from Database")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePost(
            @Parameter(description = "ID of the post to delete") @PathVariable Long id) {

        postService.deletePost(id);

        return new ResponseEntity<>("Post successfully deleted!", HttpStatus.OK);
    }

    /// --- Custom Methods ---

    // to like a post
    @Operation(summary = "Like/Unlike a post", description = "Toggle Like/Unlike a post for the current user")
    @PostMapping("/{id}/like")
    public ResponseEntity<String> toggleLikePost (
            @Parameter(description = "ID of the post to like/unlike") @PathVariable Long id) {

        likeService.toggleLikePost(id);

        return new ResponseEntity<>("Success", HttpStatus.OK);
    }
}

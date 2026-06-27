package com.yearis.blog_application.service;

import com.yearis.blog_application.payload.request.PostRequest;
import com.yearis.blog_application.payload.response.PostResponse;

import java.util.List;

public interface PostService {

    /// --- CRUD Operations ---

    /// C: Create/Save
    PostResponse createPost(PostRequest postRequest);

    /// R: Read/Find/Get
    PostResponse findPostById(Long id);

    List<PostResponse> findPostByTitle(String title, int pageNo, int pageSize);

    List<PostResponse> findAllPosts(int pageNo, int pageSize);

    /// U: Update
    PostResponse updatePost(PostRequest postRequest, Long id);

    /// D: Delete
    void deletePost(Long id);

    /// --- Custom Method ---

    // we need all the posts created by our user
    List<PostResponse> findPostByUserId(Long userId, int pageNo, int pageSize);

    // we need all the posts liked by our user
    List<PostResponse> findLikedPostsByUserId(Long userId, int pageNo, int pageSize);
}

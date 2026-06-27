package com.yearis.blog_application.service;

import com.yearis.blog_application.payload.request.PasswordChangeRequest;
import com.yearis.blog_application.payload.request.UserUpdateRequest;
import com.yearis.blog_application.payload.response.UserProfileResponse;

import java.util.List;

public interface UserService {

    String updateUsername(UserUpdateRequest userUpdateRequest);

    String updateEmail(UserUpdateRequest userUpdateRequest);

    String updateAbout(UserUpdateRequest userUpdateRequest);

    String updatePassword(PasswordChangeRequest passwordChangeRequest);

    List<UserProfileResponse> searchUsers(String username);

    UserProfileResponse getPublicProfile(Long id);
}

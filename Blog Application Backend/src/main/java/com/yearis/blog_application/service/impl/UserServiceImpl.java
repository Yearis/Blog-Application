package com.yearis.blog_application.service.impl;

import com.yearis.blog_application.entity.User;
import com.yearis.blog_application.exception.*;
import com.yearis.blog_application.payload.request.PasswordChangeRequest;
import com.yearis.blog_application.payload.request.UserUpdateRequest;
import com.yearis.blog_application.payload.response.UserProfileResponse;
import com.yearis.blog_application.payload.response.UserUpdateResponse;
import com.yearis.blog_application.repository.UserRepository;
import com.yearis.blog_application.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // get our current user
    private User currentUser() {

        String usernameOrEmail = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();

        return userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /// U: Update

    // update user profile
    @Override
    @Transactional
    public UserUpdateResponse updateUserProfile(UserUpdateRequest userUpdateRequest) {

        // first we make sure the user whose details have to be updated exists or not
        // and current user always exists
        User currentUser = currentUser();

        // update username
        if (userUpdateRequest.getUsername() != null) {

            // in case user tries to submit blank username field
            if (userUpdateRequest.getUsername().trim().isEmpty()) {

                throw new BadRequestException("Username cannot be empty");
            }

            // new username shouldn't be his current username
            if (currentUser.getUsername().equals(userUpdateRequest.getUsername())) {

                throw new BadRequestException("New username cannot be same as your current username");
            }

            // now we check if the user's new name already exists or not
            if (userRepository.existsByUsername(userUpdateRequest.getUsername())) {

                throw new BadRequestException("Username already exists.\nTry another name");
            }

            // now we set the name
            currentUser.setUsername(userUpdateRequest.getUsername());
        }

        // update email
        if (userUpdateRequest.getEmail() != null) {
            if (userUpdateRequest.getEmail().trim().isEmpty()) {

                throw new BadRequestException("Email cannot be empty");
            }

            // new email shouldn't be his current email
            if (currentUser.getEmail().equals(userUpdateRequest.getEmail())) {

                throw new BadRequestException("New email cannot be same as your current email");
            }

            // now we check if the user's new email already used or not
            if (userRepository.existsByEmail(userUpdateRequest.getEmail())) {

                throw new BadRequestException("Email already user.\nTry another email");
            }

            // now we set the name
            currentUser.setEmail(userUpdateRequest.getEmail());
        }

        // update about
        if (userUpdateRequest.getAbout() != null) {

            // now we set the about
            currentUser.setAbout(userUpdateRequest.getAbout());
        }

        userRepository.save(currentUser);

        // send response
        UserUpdateResponse userUpdateResponse = new UserUpdateResponse();
        userUpdateResponse.setId(currentUser.getId());
        userUpdateResponse.setUsername(currentUser.getUsername());
        userUpdateResponse.setEmail(currentUser.getEmail());
        userUpdateResponse.setAbout(currentUser.getAbout());
        userUpdateResponse.setJoinedDate(currentUser.getJoinedDate());

        return userUpdateResponse;
    }

    @Override
    @Transactional
    public String updatePassword(PasswordChangeRequest passwordChangeRequest) {

        // first we wanna make sure that user is logged in for this else password cant be changed
        User currentUser = currentUser();

        // now we check if newPassword and confirmedNewPassword match or not
        if (!passwordChangeRequest.getNewPassword().equals(passwordChangeRequest.getConfirmationNewPassword())) {

            throw new ResourceConflictException("New password and Confirmed new password fields should match");
        }

        // now we check if our old password is correct and our new password doesn't match our old password
        if (!passwordEncoder.matches(passwordChangeRequest.getCurrentPassword(), currentUser.getPassword())) {

            throw new UnauthorizedAccessException("Current password is incorrect");
        }

        if (passwordEncoder.matches(passwordChangeRequest.getNewPassword(), currentUser.getPassword())) {

            throw new UnauthorizedAccessException("New password cannot be same as old password");
        }

        // now we set the password
        currentUser.setPassword(passwordEncoder.encode(passwordChangeRequest.getConfirmationNewPassword()));

        // now we save it
        userRepository.save(currentUser);

        return "Password updated!";
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProfileResponse> searchUsers(String username) {

        // this feature doesn't require a user to be logged in
        List<User> users = userRepository.findByUsernameContaining(username);

        return users.stream()
                .map(user -> {
                    UserProfileResponse response = new UserProfileResponse();
                    response.setId(user.getId());
                    response.setUsername(user.getUsername());
                    response.setAbout(user.getAbout());
                    response.setJoinedDate(user.getJoinedDate());
                    return response;
                }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getPublicProfile(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setAbout(user.getAbout());
        response.setJoinedDate(user.getJoinedDate());

        return response;
    }

}

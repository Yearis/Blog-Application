package com.yearis.blog_application.mappers;

import com.yearis.blog_application.entity.User;
import com.yearis.blog_application.payload.request.RegisterRequest;
import com.yearis.blog_application.payload.response.UserProfileResponse;
import com.yearis.blog_application.payload.response.UserUpdateResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserProfileResponse mapToProfileResponse(User user);

    UserUpdateResponse mapToUpdateResponse(User user);

    User mapToEntity(RegisterRequest registerRequest);
}

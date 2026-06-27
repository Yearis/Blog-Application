package com.yearis.blog_application.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserUpdateRequest {

    // we want to update information here such as username, email, password, about
    // any one of these fields can be changed and current password would be required for it
    private String username;

    private String email;

    private String password;

    private String about;
}

package com.yearis.blog_application.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
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
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    @Email(message = "Invalid email format")
    private String email;

    @Size(max = 250, message = "About section must not exceed 250 characters")
    private String about;
}

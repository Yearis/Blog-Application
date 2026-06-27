package com.yearis.blog_application.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "This field is mandatory")
    private String usernameOrEmail;

    @NotBlank(message = "This field is mandatory")
    private String password;
}

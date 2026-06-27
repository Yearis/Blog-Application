package com.yearis.blog_application.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PasswordChangeRequest {

    @NotBlank(message = "This field cannot be blank")
    private String currentPassword;

    @NotBlank(message = "This field cannot be blank")
    @Size(min = 8, max = 30, message = "Password must be at least 8 characters")
    private String newPassword;

    @NotBlank(message = "This field cannot be blank")
    private String confirmationNewPassword;
}

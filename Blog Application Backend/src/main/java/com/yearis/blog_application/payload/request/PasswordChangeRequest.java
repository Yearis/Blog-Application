package com.yearis.blog_application.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}.*$",
            message = "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, and one special character.")
    private String newPassword;

    @NotBlank(message = "This field cannot be blank")
    private String confirmationNewPassword;
}

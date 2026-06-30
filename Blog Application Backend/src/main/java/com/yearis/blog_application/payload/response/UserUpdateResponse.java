package com.yearis.blog_application.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateResponse {

    private Long id;
    private String username;
    private String email;
    private String about;
    private LocalDateTime joinedDate;
}

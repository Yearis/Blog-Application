package com.yearis.blog_application.payload.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
public class JwtAuthResponse {

    @Setter
    private String token;

    private String tokenType = "Bearer";

    public JwtAuthResponse(String token) {
        this.token = token;
    }

}

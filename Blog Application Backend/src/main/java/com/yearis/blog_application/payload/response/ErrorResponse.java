package com.yearis.blog_application.payload.response;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ErrorResponse {

    private int statusCode;
    private String details;
    private LocalDateTime timeStamp;
    private String message;

}

package com.yearis.blog_application.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@NoArgsConstructor // using lombok to generate a no arg constructor
@AllArgsConstructor // using lombok to generate all arg constructor
// @Data // using lombok to generate getter/setter and toString methods (BUT this is dangerous)
@Getter
@Setter
@ToString
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "Title cannot be empty")
    @Size(min = 10, max = 50, message = "Title must be between 10 and 50 characters")
    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @NotBlank(message = "Content cannot be empty")
    @Size(min = 20, max = 5000)
    @Column(name = "content", columnDefinition = "TEXT") // allows long content
    private String content;

    // I can hide the time part in front-end
    @CreationTimestamp
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Builder.Default
    @Column(name = "edited", nullable = false)
    private boolean isEdited = false;

    // we keep default to 1 when a user creates a post as he will like his own post(just like reddit)
    @Builder.Default
    @Column(name = "likes")
    private int likes = 1;

    // this is for relationship to post from user

    // Relationship: Many post -> One user
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            name = "user_id",               // This creates a column named 'user_id' in SQL Table
            referencedColumnName = "id",    // Links to 'id' column in users table
            nullable = true                 // So that the Post can exist even if User is deleted
    )
    @ToString.Exclude // to not accidentally print the Author(User) object in post
    private User author;

}

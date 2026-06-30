package com.yearis.blog_application.entity;

import jakarta.persistence.*;
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

    @Column(name = "title", nullable = false, length = 50)
    private String title;

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

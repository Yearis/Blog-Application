package com.yearis.blog_application.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "likes")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @CreationTimestamp
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    // this is for relationship to like from user, post and comment

    // Relationship: Many likes -> 1 user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            referencedColumnName = "id",
            nullable = false // as a like cant exist w/o a user
    )
    private User user;

    // Relationship: Many likes -> 1 post
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "post_id",
            referencedColumnName = "id",
            nullable = true // as a like can exist w/o a post (maybe comment was liked)
    )
    private Post post;

    // Relationship: Many likes -> 1 comment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "comment_id",
            referencedColumnName = "id",
            nullable = true // as a like can exist w/o a comment (maybe post was liked)
    )
    private Comment comment;

}

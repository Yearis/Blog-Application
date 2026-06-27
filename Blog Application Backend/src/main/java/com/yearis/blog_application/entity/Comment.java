package com.yearis.blog_application.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "comments")
@NoArgsConstructor // using lombok to generate a no arg constructor
@AllArgsConstructor // using lombok to generate all arg constructor
// @Data // using lombok to generate getter/setter and toString methods (BUT this is dangerous)
@Getter
@Setter
@ToString
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "Comment cannot be empty")
    @Size(min = 2, max = 1000, message = "Comment must be less than 1000 characters")
    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    // I can hide the time part in front-end
    @CreationTimestamp
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Builder.Default
    @Column(name = "edited", nullable = false)
    private boolean isEdited = false;

    // we keep default to 1 when a user makes a comment on a past as he will like his own comment(just like reddit)
    @Builder.Default
    @Column(name = "likes")
    private int likes = 1;

    // this is for relationship to comment from post and user

    // Relationship: Many comment -> 1 user
    @ManyToOne
    @JoinColumn(
            name = "user_id",               // This creates a column named 'user_id' in SQL Table
            referencedColumnName = "id",    // Links to 'id' column in users table
            nullable = true                 // So that the Comment can exist even if User is deleted
    )
    @ToString.Exclude
    private User author;

    // Relationship: Many comment -> 1 post
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "post_id",               // This creates a column named 'post_id' in SQL Table
            referencedColumnName = "id",    // Links to 'id' column in users table
            nullable = false                // So that the Comment can't exist if the Post is deleted
    )
    @ToString.Exclude
    private Post post;

    // Self-Referencing relationship

    // Relationship: Many comments/replies -> 1 parent comment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")         // So that the replies cant exists without parent comment
    @ToString.Exclude
    private Comment parent;

    // Relationship: 1 parent comment -> Many comments/replies
    @OneToMany(
            mappedBy = "parent",            // this is mapped to our parent field above
            cascade = CascadeType.ALL       // So that the replies cant exists without its parent comment
    )
    @ToString.Exclude
    private List<Comment> replies;

}

package com.yearis.blog_application.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "users")
@NoArgsConstructor // using lombok to generate a no arg constructor
@AllArgsConstructor // using lombok to generate all arg constructor
// @Data // using lombok to generate getter/setter and toString methods (BUT this is dangerous)
@Getter
@Setter
@ToString
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "Username cannot be empty")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    @Column(name = "username", unique = true, length = 20)
    private String username;

    @NotBlank(message = "Email field cannot be empty")
    @Email
    @Column(name = "email", unique = true)
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}.*$",
             message = "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, and one special character.")
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "about")
    @Builder.Default
    private String about = "Hey there! I am using the Blog App.";

    @CreationTimestamp
    @Column(name = "joined_date", nullable = false, updatable = false)
    private LocalDateTime joinedDate;

    // this is for relationship to user from role

    // Relationship: Many users -> Many roles
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    private Set<Role> roles; // as 1 user can have many roles like a moderator and a user

}

package com.yearis.blog_application.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // I didn't add any @NotNull annotations or @Size annotation as these table would be managed by the admin of system no any user
    @Column(name = "name")
    private String name;
}

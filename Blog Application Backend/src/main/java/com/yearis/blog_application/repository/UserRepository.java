package com.yearis.blog_application.repository;

import com.yearis.blog_application.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // required for login to check if a user exists with this email
    Optional<User> findByEmail(String email);

    // required for login to check if a user exists with this username
    Optional<User> findByUsername(String userName);

    // required for finding a user with username 
    List<User> findByUsernameContaining(String query);

    // required for "Login with Username or Email" feature
    Optional<User> findByUsernameOrEmail(String userName, String email);

    // required during registration to check if an email is already occupied by user as our email field is unique
    Boolean existsByEmail(String email);

    // required during registration to check if a username is already occupied by user as our username field is unique
    Boolean existsByUsername(String userName);

}

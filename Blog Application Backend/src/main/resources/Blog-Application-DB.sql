-- Our ROLES Table
CREATE TABLE IF NOT EXISTS roles (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE
)AUTO_INCREMENT = 1;

-- Our USERS Table
CREATE TABLE IF NOT EXISTS users (
	id BIGINT  AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(20) NOT NULL UNIQUE,
	email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    about VARCHAR(255) DEFAULT 'Hey there! I am using the Blog App.',
    joined_date DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6)
)AUTO_INCREMENT = 10001;

-- Our Connector Table
CREATE TABLE IF NOT EXISTS users_roles (
	user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    
    PRIMARY KEY (user_id, role_id),
    
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE CASCADE,
    
    FOREIGN KEY (role_id)
    REFERENCES roles(id)
    ON DELETE CASCADE
);

-- Our POSTS Table
CREATE TABLE IF NOT EXISTS posts (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    created_date DATETIME(6), -- as we use LocalDateTime
    edited BIT(1) DEFAULT 0,
    likes INT DEFAULT 1,
    user_id BIGINT, -- foreign key for user_id
	
    --  Relationship: Many post -> 1 user
    FOREIGN KEY(user_id)
    REFERENCES users(id)
    ON DELETE SET NULL -- so that when user is deleted, its posts shouldn't be deleted
)AUTO_INCREMENT = 10001;

-- Our Comments Table
CREATE TABLE IF NOT EXISTS comments (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	body TEXT NOT NULL,	
    created_date DATETIME(6),
    edited BIT(1) DEFAULT 0,
    likes INT DEFAULT 1,
    user_id BIGINT, -- foreign key 1 for user_id
    post_id BIGINT NOT NULL, -- foreign key 2 for post_id
    parent_id BIGINT, -- foreign key 3 for parent_id
    
    -- Relationship: Many comment -> 1 user
    FOREIGN KEY(user_id)
    REFERENCES users(id)
    ON DELETE SET NULL, -- so that when user is deleted, its comments shouldn't be deleted
    
    --  Relationship: Many comment -> 1 post
    FOREIGN KEY(post_id)
    REFERENCES posts(id)
    ON DELETE CASCADE, -- so that when the post is deleted, its comments are also deleted
    
    -- Relationship: Many comments/replies -> 1 parent comment
    FOREIGN KEY(parent_id)
    REFERENCES comments(id)
    ON DELETE CASCADE -- so that when the parent comment is deleted, its replies are also deleted
)AUTO_INCREMENT = 10001;

-- Our LIKES Table 
CREATE TABLE IF NOT EXISTS likes (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_date DATETIME(6),
    user_id BIGINT NOT NULL,
    post_id BIGINT, -- this can be null as like can be on comment
    comment_id BIGINT, -- this can be null as like can be on post
    
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE CASCADE,
    
    FOREIGN KEY (post_id)
    REFERENCES posts(id)
    ON DELETE CASCADE,
    
    FOREIGN KEY (comment_id)
    REFERENCES comments(id)
    ON DELETE CASCADE,
    
    CONSTRAINT user_post_like 
    UNIQUE (user_id, post_id),
    
    CONSTRAINT user_comment_like 
    UNIQUE (user_id, comment_id)    
)AUTO_INCREMENT = 10001;

-- Our Roles
INSERT IGNORE INTO roles (id, name) VALUES (1, 'ROLE_ADMIN');
INSERT IGNORE INTO roles (id, name) VALUES (2, 'ROLE_USER');

-- Our Admin User
INSERT IGNORE INTO users (id, username, email, password, about, joined_date)
VALUES (
           10001,
           'Yearis',
           'khush8751@gmail.com',
           '$2a$10$9/3XwPNeHyNBkIwMEibeXOED5bIrAsZStKAKqHdSM0xec8FVO90Ie',
           'I am the Admin User.',
           NOW()
       );

-- Assign admin and user role to me
INSERT IGNORE INTO users_roles (user_id, role_id) VALUES (10001, 1);
INSERT IGNORE INTO users_roles (user_id, role_id) VALUES (10001, 2);
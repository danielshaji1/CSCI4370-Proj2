-- MySQL 8.0.16+; tables use InnoDB transactions and enforced CHECK constraints.
CREATE DATABASE IF NOT EXISTS csx370_mb_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE csx370_mb_platform;

CREATE TABLE IF NOT EXISTS user (
    userId INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    firstName VARCHAR(255) NOT NULL,
    lastName VARCHAR(255) NOT NULL,
    CONSTRAINT uq_user_username UNIQUE (username),
    CONSTRAINT username_min_length CHECK (CHAR_LENGTH(TRIM(username)) >= 2),
    CONSTRAINT firstName_min_length CHECK (CHAR_LENGTH(TRIM(firstName)) >= 2),
    CONSTRAINT lastName_min_length CHECK (CHAR_LENGTH(TRIM(lastName)) >= 2)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS post (
    postId BIGINT AUTO_INCREMENT PRIMARY KEY,
    userId INT NOT NULL,
    content VARCHAR(500) NOT NULL,
    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT post_user_fk FOREIGN KEY (userId) REFERENCES user(userId) ON DELETE CASCADE,
    CONSTRAINT post_not_empty CHECK (CHAR_LENGTH(TRIM(content)) > 0),
    INDEX ix_post_author_date (userId, createdAt, postId),
    INDEX ix_post_date (createdAt, postId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Default RESTRICT for these FKs: MySQL disallows CASCADE on CHECK-constrained columns.
CREATE TABLE IF NOT EXISTS follow (
    followerId INT NOT NULL,
    followedId INT NOT NULL,
    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (followerId, followedId),
    CONSTRAINT follow_follower_fk FOREIGN KEY (followerId) REFERENCES user(userId),
    CONSTRAINT follow_followed_fk FOREIGN KEY (followedId) REFERENCES user(userId),
    CONSTRAINT follow_no_self CHECK (followerId <> followedId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS post_like (
    userId INT NOT NULL,
    postId BIGINT NOT NULL,
    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (userId, postId),
    CONSTRAINT like_user_fk FOREIGN KEY (userId) REFERENCES user(userId) ON DELETE CASCADE,
    CONSTRAINT like_post_fk FOREIGN KEY (postId) REFERENCES post(postId) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS bookmark (
    userId INT NOT NULL,
    postId BIGINT NOT NULL,
    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (userId, postId),
    CONSTRAINT bookmark_user_fk FOREIGN KEY (userId) REFERENCES user(userId) ON DELETE CASCADE,
    CONSTRAINT bookmark_post_fk FOREIGN KEY (postId) REFERENCES post(postId) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS comment (
    commentId BIGINT AUTO_INCREMENT PRIMARY KEY,
    postId BIGINT NOT NULL,
    userId INT NOT NULL,
    content VARCHAR(500) NOT NULL,
    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT comment_post_fk FOREIGN KEY (postId) REFERENCES post(postId) ON DELETE CASCADE,
    CONSTRAINT comment_user_fk FOREIGN KEY (userId) REFERENCES user(userId) ON DELETE CASCADE,
    CONSTRAINT comment_not_empty CHECK (CHAR_LENGTH(TRIM(content)) > 0),
    INDEX ix_comment_post_date (postId, createdAt, commentId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS hashtag (
    hashtagId BIGINT AUTO_INCREMENT PRIMARY KEY,
    tag VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    CONSTRAINT uq_hashtag_tag UNIQUE (tag),
    CONSTRAINT hashtag_not_empty CHECK (CHAR_LENGTH(TRIM(tag)) > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS post_hashtag (
    postId BIGINT NOT NULL,
    hashtagId BIGINT NOT NULL,
    PRIMARY KEY (postId, hashtagId),
    CONSTRAINT tag_post_fk FOREIGN KEY (postId) REFERENCES post(postId) ON DELETE CASCADE,
    CONSTRAINT tag_hashtag_fk FOREIGN KEY (hashtagId) REFERENCES hashtag(hashtagId) ON DELETE CASCADE,
    INDEX ix_tag_posts (hashtagId, postId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Extra feature: a private collection belongs to exactly one user.
CREATE TABLE IF NOT EXISTS saved_collection (
    collectionId BIGINT AUTO_INCREMENT PRIMARY KEY,
    userId INT NOT NULL,
    name VARCHAR(80) NOT NULL,
    description VARCHAR(255) NOT NULL DEFAULT '',
    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT collection_owner_fk FOREIGN KEY (userId) REFERENCES user(userId) ON DELETE CASCADE,
    CONSTRAINT uq_collection_owner_name UNIQUE (userId, name),
    CONSTRAINT collection_not_empty CHECK (CHAR_LENGTH(TRIM(name)) > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS collection_post (
    collectionId BIGINT NOT NULL,
    postId BIGINT NOT NULL,
    savedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (collectionId, postId),
    CONSTRAINT collection_link_collection_fk FOREIGN KEY (collectionId) REFERENCES saved_collection(collectionId) ON DELETE CASCADE,
    CONSTRAINT collection_link_post_fk FOREIGN KEY (postId) REFERENCES post(postId) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Fresh installation only: creates tables and inserts a meaningful demo snapshot.
-- For an existing database, run update_database.sql instead.
-- No DROP statements: existing data is never automatically erased by this file.
-- Export your final demonstrated database with scripts/export_database.sh before submission.
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

-- Fresh demonstration data. All demo passwords are "password", stored as BCrypt hashes.
-- Avery has no posts, demonstrating the People page's Unknown date.
INSERT INTO user (userId,username,password,firstName,lastName) VALUES
(1,'alex','$2a$10$WHPk4V1mOQDVwlDsEiAqyudilDw5pq1lo1rA7U8TzsYYga8nlF89y','Alex','Rivera'),
(2,'jordan','$2a$10$WHPk4V1mOQDVwlDsEiAqyudilDw5pq1lo1rA7U8TzsYYga8nlF89y','Jordan','Lee'),
(3,'taylor','$2a$10$WHPk4V1mOQDVwlDsEiAqyudilDw5pq1lo1rA7U8TzsYYga8nlF89y','Taylor','Morgan'),
(4,'avery','$2a$10$WHPk4V1mOQDVwlDsEiAqyudilDw5pq1lo1rA7U8TzsYYga8nlF89y','Avery','Patel');

INSERT INTO post (postId,userId,content,createdAt) VALUES
(1,1,'Welcome to our campus community! #uga #welcome','2025-03-07 22:54:00'),
(2,2,'Spring is here and Athens is beautiful. #uga #spring','2025-03-08 11:00:00'),
(3,3,'Getting ready for the big game! #uga #football','2025-03-09 09:30:00'),
(4,1,'Planning next year after these amazing #2025 #fireworks','2025-03-10 17:00:00'),
(5,2,'Looking back at our #2025 goals. #uga','2025-03-10 17:00:00'),
(6,3,'The downtown #fireworks were worth the walk. #athens','2025-03-11 20:15:00'),
(7,2,'Study group at the library tomorrow. #uga #study','2025-03-12 12:00:00'),
(8,1,'A picnic on North Campus sounds perfect. #uga #spring','2025-03-13 15:45:00');

-- Alex follows Jordan, but not Taylor: Taylor's posts appear in search/profile, not Alex's home.
INSERT INTO follow (followerId,followedId,createdAt) VALUES
(1,2,'2025-03-07 20:00:00'),(2,1,'2025-03-08 10:00:00'),(3,1,'2025-03-09 08:00:00');
INSERT INTO post_like (userId,postId,createdAt) VALUES
(1,2,'2025-03-08 12:00:00'),(2,1,'2025-03-08 12:00:00'),(3,1,'2025-03-08 13:00:00'),(1,7,'2025-03-12 13:00:00');
INSERT INTO bookmark (userId,postId,createdAt) VALUES
(1,2,'2025-03-14 12:00:00'),(1,7,'2025-03-13 12:00:00'),(2,4,'2025-03-11 12:00:00');
INSERT INTO comment (commentId,postId,userId,content,createdAt) VALUES
(1,1,2,'Glad you are here!','2025-03-07 23:00:00'),
(2,1,3,'Go Dawgs!','2025-03-08 08:00:00'),
(3,7,1,'I will bring my ER modeling notes.','2025-03-12 13:00:00');
INSERT INTO hashtag (hashtagId,tag) VALUES
(1,'uga'),(2,'welcome'),(3,'spring'),(4,'football'),(5,'2025'),(6,'fireworks'),(7,'athens'),(8,'study');
INSERT INTO post_hashtag (postId,hashtagId) VALUES
(1,1),(1,2),(2,1),(2,3),(3,1),(3,4),(4,5),(4,6),(5,5),(5,1),(6,6),(6,7),(7,1),(7,8),(8,1),(8,3);
INSERT INTO saved_collection (collectionId,userId,name,description,createdAt) VALUES
(1,1,'Campus plans','Ideas for the next week around UGA.','2025-03-14 09:00:00'),
(2,1,'Celebrations','Memories from our year together.','2025-03-14 10:00:00'),
(3,2,'My reading list','Private study notes and inspiration.','2025-03-14 11:00:00');
INSERT INTO collection_post (collectionId,postId,savedAt) VALUES
(1,2,'2025-03-14 09:10:00'),(1,7,'2025-03-14 09:20:00'),(2,4,'2025-03-14 10:10:00'),(3,1,'2025-03-14 11:10:00');

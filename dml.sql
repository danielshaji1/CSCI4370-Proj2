-- Exact application statements from SqlQueries.java. ? denotes a bound JDBC parameter.
-- This is a reference catalog, not a setup script. The search entry shows two hashtags;
-- the service generates one placeholder per distinct tag, never interpolating user text.

-- LOGIN: Find a login account; bind username.
-- Page URL: http://localhost:8081/login
SELECT * FROM user WHERE username = ?;

-- REGISTER: Register an account with a BCrypt password hash; bind username, hash, first name, last name.
-- Page URL: http://localhost:8081/register
INSERT INTO user (username, password, firstName, lastName) VALUES (?, ?, ?, ?);

-- USER: Find the requested profile owner; bind user ID.
-- Page URL: http://localhost:8081/profile/1
SELECT userId, firstName, lastName FROM user WHERE userId = ?;

-- HOME: Feed with own and followed users posts; bind viewer twice, own ID, follower ID.
-- Page URL: http://localhost:8081/
SELECT p.postId, p.userId, p.content, p.createdAt, u.firstName, u.lastName,
       (SELECT COUNT(*) FROM post_like l WHERE l.postId = p.postId) AS hearts,
       (SELECT COUNT(*) FROM comment c WHERE c.postId = p.postId) AS comments,
       EXISTS(SELECT 1 FROM post_like l WHERE l.postId = p.postId AND l.userId = ?) AS hearted,
       EXISTS(SELECT 1 FROM bookmark b WHERE b.postId = p.postId AND b.userId = ?) AS bookmarked
FROM post p JOIN user u ON u.userId = p.userId
WHERE p.userId = ? OR EXISTS(SELECT 1 FROM follow f WHERE f.followerId = ? AND f.followedId = p.userId)
ORDER BY p.createdAt DESC, p.postId DESC;

-- PROFILE: Profile posts; bind viewer twice and profile owner ID.
-- Page URL: http://localhost:8081/profile and http://localhost:8081/profile/1
SELECT p.postId, p.userId, p.content, p.createdAt, u.firstName, u.lastName,
       (SELECT COUNT(*) FROM post_like l WHERE l.postId = p.postId) AS hearts,
       (SELECT COUNT(*) FROM comment c WHERE c.postId = p.postId) AS comments,
       EXISTS(SELECT 1 FROM post_like l WHERE l.postId = p.postId AND l.userId = ?) AS hearted,
       EXISTS(SELECT 1 FROM bookmark b WHERE b.postId = p.postId AND b.userId = ?) AS bookmarked
FROM post p JOIN user u ON u.userId = p.userId
WHERE p.userId = ? ORDER BY p.createdAt DESC, p.postId DESC;

-- BOOKMARKS: Bookmarked posts sorted by post creation, not bookmark time; bind viewer twice, bookmark owner.
-- Page URL: http://localhost:8081/bookmarks
SELECT p.postId, p.userId, p.content, p.createdAt, u.firstName, u.lastName,
       (SELECT COUNT(*) FROM post_like l WHERE l.postId = p.postId) AS hearts,
       (SELECT COUNT(*) FROM comment c WHERE c.postId = p.postId) AS comments,
       EXISTS(SELECT 1 FROM post_like l WHERE l.postId = p.postId AND l.userId = ?) AS hearted,
       EXISTS(SELECT 1 FROM bookmark b WHERE b.postId = p.postId AND b.userId = ?) AS bookmarked
FROM post p JOIN user u ON u.userId = p.userId
WHERE EXISTS(SELECT 1 FROM bookmark b WHERE b.userId = ? AND b.postId = p.postId)
ORDER BY p.createdAt DESC, p.postId DESC;

-- SEARCH: All-tags search; bind viewer twice, each unique lowercase tag, then number of tags. %s is replaced ONLY with JDBC question-mark placeholders.
-- Page URL: http://localhost:8081/hashtagsearch?hashtags=%23uga+%23spring
SELECT p.postId, p.userId, p.content, p.createdAt, u.firstName, u.lastName,
       (SELECT COUNT(*) FROM post_like l WHERE l.postId = p.postId) AS hearts,
       (SELECT COUNT(*) FROM comment c WHERE c.postId = p.postId) AS comments,
       EXISTS(SELECT 1 FROM post_like l WHERE l.postId = p.postId AND l.userId = ?) AS hearted,
       EXISTS(SELECT 1 FROM bookmark b WHERE b.postId = p.postId AND b.userId = ?) AS bookmarked
FROM post p JOIN user u ON u.userId = p.userId
WHERE p.postId IN (
    SELECT ph.postId FROM post_hashtag ph JOIN hashtag h ON h.hashtagId = ph.hashtagId
    WHERE h.tag IN (?, ?) GROUP BY ph.postId HAVING COUNT(DISTINCT h.tag) = ?
)
ORDER BY p.createdAt DESC, p.postId DESC;

-- POST: Retrieve one post and viewer-specific counts/flags; bind viewer twice, post ID.
-- Page URL: http://localhost:8081/post/1
SELECT p.postId, p.userId, p.content, p.createdAt, u.firstName, u.lastName,
       (SELECT COUNT(*) FROM post_like l WHERE l.postId = p.postId) AS hearts,
       (SELECT COUNT(*) FROM comment c WHERE c.postId = p.postId) AS comments,
       EXISTS(SELECT 1 FROM post_like l WHERE l.postId = p.postId AND l.userId = ?) AS hearted,
       EXISTS(SELECT 1 FROM bookmark b WHERE b.postId = p.postId AND b.userId = ?) AS bookmarked
FROM post p JOIN user u ON u.userId = p.userId
WHERE p.postId = ?;

-- COMMENTS: Read comments oldest first with ID tie breaker; bind post ID.
-- Page URL: http://localhost:8081/post/1
SELECT c.commentId, c.content, c.createdAt, c.userId, u.firstName, u.lastName
FROM comment c JOIN user u ON u.userId = c.userId
WHERE c.postId = ? ORDER BY c.createdAt ASC, c.commentId ASC;

-- CREATE_POST: Create a post as session user (POST /createpost); bind user ID and content.
-- Page URL: http://localhost:8081/
INSERT INTO post (userId, content) VALUES (?, ?);

-- ENSURE_TAG: Create a normalized hashtag if needed during a post transaction; bind tag.
-- Page URL: http://localhost:8081/ and http://localhost:8081/post/1/edit
INSERT INTO hashtag (tag) VALUES (?) ON DUPLICATE KEY UPDATE tag = tag;

-- LINK_TAG: Link a distinct tag to a post; bind post ID and tag.
-- Page URL: http://localhost:8081/ and http://localhost:8081/post/1/edit
INSERT INTO post_hashtag (postId, hashtagId) SELECT ?, hashtagId FROM hashtag WHERE tag = ?;

-- COMMENT: Add a comment as session user (POST /post/1/comment); bind post ID, user ID, content.
-- Page URL: http://localhost:8081/post/1
INSERT INTO comment (postId, userId, content) VALUES (?, ?, ?);

-- LIKE: Like once (POST /post/1/heart/true); bind user ID, post ID.
-- Page URL: http://localhost:8081/post/1
INSERT INTO post_like (userId, postId) VALUES (?, ?) ON DUPLICATE KEY UPDATE postId = postId;

-- UNLIKE: Remove own like (POST /post/1/heart/false); bind user ID, post ID.
-- Page URL: http://localhost:8081/post/1
DELETE FROM post_like WHERE userId = ? AND postId = ?;

-- BOOKMARK: Bookmark once (POST /post/1/bookmark/true); bind user ID, post ID.
-- Page URL: http://localhost:8081/post/1
INSERT INTO bookmark (userId, postId) VALUES (?, ?) ON DUPLICATE KEY UPDATE postId = postId;

-- UNBOOKMARK: Remove own bookmark (POST /post/1/bookmark/false); bind user ID, post ID.
-- Page URL: http://localhost:8081/post/1
DELETE FROM bookmark WHERE userId = ? AND postId = ?;

-- LOCK_POST: Lock the post for an atomic ownership check and edit; bind post ID.
-- Page URL: http://localhost:8081/post/1/edit
SELECT userId FROM post WHERE postId = ? FOR UPDATE;

-- EDIT: Update owned post content without changing creation date; bind content, post ID, owner ID.
-- Page URL: http://localhost:8081/post/1/edit
UPDATE post SET content = ? WHERE postId = ? AND userId = ?;

-- CLEAR_TAGS: Replace tag links in the same transaction as the content edit; bind post ID.
-- Page URL: http://localhost:8081/post/1/edit
DELETE FROM post_hashtag WHERE postId = ?;

-- PEOPLE: List other users with follow status and last post time; bind viewer twice.
-- Page URL: http://localhost:8081/people
SELECT u.userId, u.firstName, u.lastName,
       EXISTS(SELECT 1 FROM follow f WHERE f.followerId = ? AND f.followedId = u.userId) AS followed,
       (SELECT MAX(p.createdAt) FROM post p WHERE p.userId = u.userId) AS lastPost
FROM user u WHERE u.userId <> ? ORDER BY u.firstName, u.lastName, u.userId;

-- FOLLOW: Follow once (POST /people/2/follow/true); bind follower and followed ID.
-- Page URL: http://localhost:8081/people
INSERT INTO follow (followerId, followedId) VALUES (?, ?) ON DUPLICATE KEY UPDATE followedId = followedId;

-- UNFOLLOW: Unfollow (POST /people/2/follow/false); bind follower and followed ID.
-- Page URL: http://localhost:8081/people
DELETE FROM follow WHERE followerId = ? AND followedId = ?;

-- COLLECTIONS: List only private collections owned by session user, with post counts; bind owner ID.
-- Page URL: http://localhost:8081/collections
SELECT c.collectionId, c.name, c.description, c.createdAt,
       (SELECT COUNT(*) FROM collection_post cp WHERE cp.collectionId = c.collectionId) AS postCount
FROM saved_collection c WHERE c.userId = ? ORDER BY c.createdAt DESC, c.collectionId DESC;

-- COLLECTION: Read one owned collection; bind collection ID, session user ID.
-- Page URL: http://localhost:8081/collections/1
SELECT c.collectionId, c.name, c.description, c.createdAt,
       (SELECT COUNT(*) FROM collection_post cp WHERE cp.collectionId = c.collectionId) AS postCount
FROM saved_collection c WHERE c.collectionId = ? AND c.userId = ?;

-- CREATE_COLLECTION: Create a private named collection; bind owner, name, description.
-- Page URL: http://localhost:8081/collections
INSERT INTO saved_collection (userId, name, description) VALUES (?, ?, ?);

-- RENAME_COLLECTION: Rename/update an owned collection (POST /collections/1/update); bind name, description, collection ID, owner ID.
-- Page URL: http://localhost:8081/collections/1
UPDATE saved_collection SET name = ?, description = ? WHERE collectionId = ? AND userId = ?;

-- DELETE_COLLECTION: Delete an owned collection and cascade its links (POST /collections/1/delete); bind collection ID, owner.
-- Page URL: http://localhost:8081/collections/1
DELETE FROM saved_collection WHERE collectionId = ? AND userId = ?;

-- ADD_COLLECTION_POST: Save a post to an owned collection only (POST /collections/save or /collections/1/posts); bind post ID, collection ID, owner ID.
-- Page URL: http://localhost:8081/post/1
INSERT INTO collection_post (collectionId, postId)
SELECT collectionId, ? FROM saved_collection WHERE collectionId = ? AND userId = ?
ON DUPLICATE KEY UPDATE postId = postId;

-- REMOVE_COLLECTION_POST: Remove one saved post from an owned collection (POST /collections/1/posts/2/remove); bind collection ID, post ID, owner.
-- Page URL: http://localhost:8081/collections/1
DELETE cp FROM collection_post cp JOIN saved_collection c ON c.collectionId = cp.collectionId
WHERE cp.collectionId = ? AND cp.postId = ? AND c.userId = ?;

-- COLLECTION_POSTS: Read posts in an owned collection; bind viewer twice, collection ID, owner ID.
-- Page URL: http://localhost:8081/collections/1
SELECT p.postId, p.userId, p.content, p.createdAt, u.firstName, u.lastName,
       (SELECT COUNT(*) FROM post_like l WHERE l.postId = p.postId) AS hearts,
       (SELECT COUNT(*) FROM comment c WHERE c.postId = p.postId) AS comments,
       EXISTS(SELECT 1 FROM post_like l WHERE l.postId = p.postId AND l.userId = ?) AS hearted,
       EXISTS(SELECT 1 FROM bookmark b WHERE b.postId = p.postId AND b.userId = ?) AS bookmarked
FROM post p JOIN user u ON u.userId = p.userId
JOIN collection_post cp ON cp.postId = p.postId
JOIN saved_collection sc ON sc.collectionId = cp.collectionId
WHERE sc.collectionId = ? AND sc.userId = ?
ORDER BY p.createdAt DESC, p.postId DESC;

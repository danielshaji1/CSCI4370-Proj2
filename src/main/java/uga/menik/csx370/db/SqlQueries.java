package uga.menik.csx370.db;

/** Prepared JDBC SQL. Keep dml.sql synchronized when changing a statement. */
public final class SqlQueries {
    private SqlQueries() {}

    // Find a login account; bind username.
    public static final String LOGIN = """
        SELECT * FROM user WHERE username = ?
        """;

    // Register an account with a BCrypt password hash; bind username, hash, first name, last name.
    public static final String REGISTER = """
        INSERT INTO user (username, password, firstName, lastName) VALUES (?, ?, ?, ?)
        """;

    // Find the requested profile owner; bind user ID.
    public static final String USER = """
        SELECT userId, firstName, lastName FROM user WHERE userId = ?
        """;

    // Feed with own and followed users posts; bind viewer twice, own ID, follower ID.
    public static final String HOME = """
        SELECT p.postId, p.userId, p.content, p.createdAt, u.firstName, u.lastName,
               (SELECT COUNT(*) FROM post_like l WHERE l.postId = p.postId) AS hearts,
               (SELECT COUNT(*) FROM comment c WHERE c.postId = p.postId) AS comments,
               EXISTS(SELECT 1 FROM post_like l WHERE l.postId = p.postId AND l.userId = ?) AS hearted,
               EXISTS(SELECT 1 FROM bookmark b WHERE b.postId = p.postId AND b.userId = ?) AS bookmarked
        FROM post p JOIN user u ON u.userId = p.userId
        WHERE p.userId = ? OR EXISTS(SELECT 1 FROM follow f WHERE f.followerId = ? AND f.followedId = p.userId)
        ORDER BY p.createdAt DESC, p.postId DESC
        """;

    // Profile posts; bind viewer twice and profile owner ID.
    public static final String PROFILE = """
        SELECT p.postId, p.userId, p.content, p.createdAt, u.firstName, u.lastName,
               (SELECT COUNT(*) FROM post_like l WHERE l.postId = p.postId) AS hearts,
               (SELECT COUNT(*) FROM comment c WHERE c.postId = p.postId) AS comments,
               EXISTS(SELECT 1 FROM post_like l WHERE l.postId = p.postId AND l.userId = ?) AS hearted,
               EXISTS(SELECT 1 FROM bookmark b WHERE b.postId = p.postId AND b.userId = ?) AS bookmarked
        FROM post p JOIN user u ON u.userId = p.userId
        WHERE p.userId = ? ORDER BY p.createdAt DESC, p.postId DESC
        """;

    // Bookmarked posts sorted by post creation, not bookmark time; bind viewer twice, bookmark owner.
    public static final String BOOKMARKS = """
        SELECT p.postId, p.userId, p.content, p.createdAt, u.firstName, u.lastName,
               (SELECT COUNT(*) FROM post_like l WHERE l.postId = p.postId) AS hearts,
               (SELECT COUNT(*) FROM comment c WHERE c.postId = p.postId) AS comments,
               EXISTS(SELECT 1 FROM post_like l WHERE l.postId = p.postId AND l.userId = ?) AS hearted,
               EXISTS(SELECT 1 FROM bookmark b WHERE b.postId = p.postId AND b.userId = ?) AS bookmarked
        FROM post p JOIN user u ON u.userId = p.userId
        WHERE EXISTS(SELECT 1 FROM bookmark b WHERE b.userId = ? AND b.postId = p.postId)
        ORDER BY p.createdAt DESC, p.postId DESC
        """;

    // All-tags search; bind viewer twice, each unique lowercase tag, then number of tags. %s is replaced ONLY with JDBC question-mark placeholders.
    public static final String SEARCH = """
        SELECT p.postId, p.userId, p.content, p.createdAt, u.firstName, u.lastName,
               (SELECT COUNT(*) FROM post_like l WHERE l.postId = p.postId) AS hearts,
               (SELECT COUNT(*) FROM comment c WHERE c.postId = p.postId) AS comments,
               EXISTS(SELECT 1 FROM post_like l WHERE l.postId = p.postId AND l.userId = ?) AS hearted,
               EXISTS(SELECT 1 FROM bookmark b WHERE b.postId = p.postId AND b.userId = ?) AS bookmarked
        FROM post p JOIN user u ON u.userId = p.userId
        WHERE p.postId IN (
            SELECT ph.postId FROM post_hashtag ph JOIN hashtag h ON h.hashtagId = ph.hashtagId
            WHERE h.tag IN (%s) GROUP BY ph.postId HAVING COUNT(DISTINCT h.tag) = ?
        )
        ORDER BY p.createdAt DESC, p.postId DESC
        """;

    // Retrieve one post and viewer-specific counts/flags; bind viewer twice, post ID.
    public static final String POST = """
        SELECT p.postId, p.userId, p.content, p.createdAt, u.firstName, u.lastName,
               (SELECT COUNT(*) FROM post_like l WHERE l.postId = p.postId) AS hearts,
               (SELECT COUNT(*) FROM comment c WHERE c.postId = p.postId) AS comments,
               EXISTS(SELECT 1 FROM post_like l WHERE l.postId = p.postId AND l.userId = ?) AS hearted,
               EXISTS(SELECT 1 FROM bookmark b WHERE b.postId = p.postId AND b.userId = ?) AS bookmarked
        FROM post p JOIN user u ON u.userId = p.userId
        WHERE p.postId = ?
        """;

    // Read comments oldest first with ID tie breaker; bind post ID.
    public static final String COMMENTS = """
        SELECT c.commentId, c.content, c.createdAt, c.userId, u.firstName, u.lastName
        FROM comment c JOIN user u ON u.userId = c.userId
        WHERE c.postId = ? ORDER BY c.createdAt ASC, c.commentId ASC
        """;

    // Create a post as session user (POST /createpost); bind user ID and content.
    public static final String CREATE_POST = """
        INSERT INTO post (userId, content) VALUES (?, ?)
        """;

    // Create a normalized hashtag if needed during a post transaction; bind tag.
    public static final String ENSURE_TAG = """
        INSERT INTO hashtag (tag) VALUES (?) ON DUPLICATE KEY UPDATE tag = tag
        """;

    // Link a distinct tag to a post; bind post ID and tag.
    public static final String LINK_TAG = """
        INSERT INTO post_hashtag (postId, hashtagId) SELECT ?, hashtagId FROM hashtag WHERE tag = ?
        """;

    // Add a comment as session user (POST /post/1/comment); bind post ID, user ID, content.
    public static final String COMMENT = """
        INSERT INTO comment (postId, userId, content) VALUES (?, ?, ?)
        """;

    // Like once (POST /post/1/heart/true); bind user ID, post ID.
    public static final String LIKE = """
        INSERT INTO post_like (userId, postId) VALUES (?, ?) ON DUPLICATE KEY UPDATE postId = postId
        """;

    // Remove own like (POST /post/1/heart/false); bind user ID, post ID.
    public static final String UNLIKE = """
        DELETE FROM post_like WHERE userId = ? AND postId = ?
        """;

    // Bookmark once (POST /post/1/bookmark/true); bind user ID, post ID.
    public static final String BOOKMARK = """
        INSERT INTO bookmark (userId, postId) VALUES (?, ?) ON DUPLICATE KEY UPDATE postId = postId
        """;

    // Remove own bookmark (POST /post/1/bookmark/false); bind user ID, post ID.
    public static final String UNBOOKMARK = """
        DELETE FROM bookmark WHERE userId = ? AND postId = ?
        """;

    // Lock the post for an atomic ownership check and edit; bind post ID.
    public static final String LOCK_POST = """
        SELECT userId FROM post WHERE postId = ? FOR UPDATE
        """;

    // Update owned post content without changing creation date; bind content, post ID, owner ID.
    public static final String EDIT = """
        UPDATE post SET content = ? WHERE postId = ? AND userId = ?
        """;

    // Replace tag links in the same transaction as the content edit; bind post ID.
    public static final String CLEAR_TAGS = """
        DELETE FROM post_hashtag WHERE postId = ?
        """;

    // List other users with follow status and last post time; bind viewer twice.
    public static final String PEOPLE = """
        SELECT u.userId, u.firstName, u.lastName,
               EXISTS(SELECT 1 FROM follow f WHERE f.followerId = ? AND f.followedId = u.userId) AS followed,
               (SELECT MAX(p.createdAt) FROM post p WHERE p.userId = u.userId) AS lastPost
        FROM user u WHERE u.userId <> ? ORDER BY u.firstName, u.lastName, u.userId
        """;

    // Follow once (POST /people/2/follow/true); bind follower and followed ID.
    public static final String FOLLOW = """
        INSERT INTO follow (followerId, followedId) VALUES (?, ?) ON DUPLICATE KEY UPDATE followedId = followedId
        """;

    // Unfollow (POST /people/2/follow/false); bind follower and followed ID.
    public static final String UNFOLLOW = """
        DELETE FROM follow WHERE followerId = ? AND followedId = ?
        """;

    // List only private collections owned by session user, with post counts; bind owner ID.
    public static final String COLLECTIONS = """
        SELECT c.collectionId, c.name, c.description, c.createdAt,
               (SELECT COUNT(*) FROM collection_post cp WHERE cp.collectionId = c.collectionId) AS postCount
        FROM saved_collection c WHERE c.userId = ? ORDER BY c.createdAt DESC, c.collectionId DESC
        """;

    // Read one owned collection; bind collection ID, session user ID.
    public static final String COLLECTION = """
        SELECT c.collectionId, c.name, c.description, c.createdAt,
               (SELECT COUNT(*) FROM collection_post cp WHERE cp.collectionId = c.collectionId) AS postCount
        FROM saved_collection c WHERE c.collectionId = ? AND c.userId = ?
        """;

    // Create a private named collection; bind owner, name, description.
    public static final String CREATE_COLLECTION = """
        INSERT INTO saved_collection (userId, name, description) VALUES (?, ?, ?)
        """;

    // Rename/update an owned collection (POST /collections/1/update); bind name, description, collection ID, owner ID.
    public static final String RENAME_COLLECTION = """
        UPDATE saved_collection SET name = ?, description = ? WHERE collectionId = ? AND userId = ?
        """;

    // Delete an owned collection and cascade its links (POST /collections/1/delete); bind collection ID, owner.
    public static final String DELETE_COLLECTION = """
        DELETE FROM saved_collection WHERE collectionId = ? AND userId = ?
        """;

    // Save a post to an owned collection only (POST /collections/save or /collections/1/posts); bind post ID, collection ID, owner ID.
    public static final String ADD_COLLECTION_POST = """
        INSERT INTO collection_post (collectionId, postId)
        SELECT collectionId, ? FROM saved_collection WHERE collectionId = ? AND userId = ?
        ON DUPLICATE KEY UPDATE postId = postId
        """;

    // Remove one saved post from an owned collection (POST /collections/1/posts/2/remove); bind collection ID, post ID, owner.
    public static final String REMOVE_COLLECTION_POST = """
        DELETE cp FROM collection_post cp JOIN saved_collection c ON c.collectionId = cp.collectionId
        WHERE cp.collectionId = ? AND cp.postId = ? AND c.userId = ?
        """;

    // Read posts in an owned collection; bind viewer twice, collection ID, owner ID.
    public static final String COLLECTION_POSTS = """
        SELECT p.postId, p.userId, p.content, p.createdAt, u.firstName, u.lastName,
               (SELECT COUNT(*) FROM post_like l WHERE l.postId = p.postId) AS hearts,
               (SELECT COUNT(*) FROM comment c WHERE c.postId = p.postId) AS comments,
               EXISTS(SELECT 1 FROM post_like l WHERE l.postId = p.postId AND l.userId = ?) AS hearted,
               EXISTS(SELECT 1 FROM bookmark b WHERE b.postId = p.postId AND b.userId = ?) AS bookmarked
        FROM post p JOIN user u ON u.userId = p.userId
        JOIN collection_post cp ON cp.postId = p.postId
        JOIN saved_collection sc ON sc.collectionId = cp.collectionId
        WHERE sc.collectionId = ? AND sc.userId = ?
        ORDER BY p.createdAt DESC, p.postId DESC
        """;

}

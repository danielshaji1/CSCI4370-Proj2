package uga.menik.csx370.services;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import uga.menik.csx370.db.SqlQueries;
import uga.menik.csx370.models.*;
import uga.menik.csx370.utility.PostText;

/** All post lists share one mapping so counts and viewer flags agree on every page. */
@Service
public class PostService {
    private final JdbcTemplate jdbc;

    public PostService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Post readPost(ResultSet row, String viewer) throws SQLException {
        User author = new User(row.getString("userId"), row.getString("firstName"), row.getString("lastName"));
        Post post = new Post(row.getString("postId"), row.getString("content"),
                PostText.date(row.getTimestamp("createdAt")), author, row.getInt("hearts"),
                row.getInt("comments"), row.getBoolean("hearted"), row.getBoolean("bookmarked"));
        post.setEditable(author.getUserId().equals(viewer));
        return post;
    }

    private List<Post> query(String sql, String viewer, Object... parameters) {
        return jdbc.query(sql, (row, index) -> readPost(row, viewer), parameters);
    }

    public List<Post> home(String viewer) { return query(SqlQueries.HOME, viewer, viewer, viewer, viewer, viewer); }
    public List<Post> profile(String owner, String viewer) { return query(SqlQueries.PROFILE, viewer, viewer, viewer, owner); }
    public List<Post> bookmarks(String viewer) { return query(SqlQueries.BOOKMARKS, viewer, viewer, viewer, viewer); }

    public User profileOwner(String id) {
        return jdbc.query(SqlQueries.USER, (row, index) -> new User(row.getString("userId"),
                row.getString("firstName"), row.getString("lastName")), id).stream().findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
    }

    public List<Post> search(String input, String viewer) {
        Set<String> tags = PostText.hashtags(input);
        if (tags.isEmpty()) throw new IllegalArgumentException("Enter one or more hashtags, for example #uga #spring.");
        List<Object> args = new ArrayList<>(List.of(viewer, viewer));
        args.addAll(tags);
        args.add(tags.size());
        String placeholders = String.join(", ", Collections.nCopies(tags.size(), "?"));
        return query(SqlQueries.SEARCH.formatted(placeholders), viewer, args.toArray());
    }

    public ExpandedPost one(String id, String viewer) {
        Post post = query(SqlQueries.POST, viewer, viewer, viewer, id).stream().findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found."));
        List<Comment> comments = jdbc.query(SqlQueries.COMMENTS, (row, index) -> new Comment(
                row.getString("commentId"), row.getString("content"), PostText.date(row.getTimestamp("createdAt")),
                new User(row.getString("userId"), row.getString("firstName"), row.getString("lastName"))), id);
        ExpandedPost expanded = new ExpandedPost(post.getPostId(), post.getContent(), post.getPostDate(),
                post.getUser(), post.getHeartsCount(), post.getCommentsCount(), post.getHearted(), post.isBookmarked(), comments);
        expanded.setEditable(post.isEditable());
        return expanded;
    }

    @Transactional
    public long create(String input, String userId) {
        String content = PostText.required(input, 500, "Post");
        Set<String> tags = PostText.hashtags(content);
        GeneratedKeyHolder key = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            var statement = connection.prepareStatement(SqlQueries.CREATE_POST, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, userId);
            statement.setString(2, content);
            return statement;
        }, key);
        long postId = key.getKey().longValue();
        addTags(postId, tags);
        return postId;
    }

    private void addTags(long postId, Set<String> tags) {
        for (String tag : tags) {
            jdbc.update(SqlQueries.ENSURE_TAG, tag);
            jdbc.update(SqlQueries.LINK_TAG, postId, tag);
        }
    }

    public void comment(String id, String viewer, String input) {
        jdbc.update(SqlQueries.COMMENT, id, viewer, PostText.required(input, 500, "Comment"));
    }

    public void like(String id, String viewer, boolean add) {
        jdbc.update(add ? SqlQueries.LIKE : SqlQueries.UNLIKE, viewer, id);
    }

    public void bookmark(String id, String viewer, boolean add) {
        jdbc.update(add ? SqlQueries.BOOKMARK : SqlQueries.UNBOOKMARK, viewer, id);
    }

    /** Lock, ownership check, content update, and tag replacement are one transaction. */
    @Transactional
    public void edit(String id, String viewer, String input) {
        String content = PostText.required(input, 500, "Post");
        Set<String> tags = PostText.hashtags(content);
        String owner = jdbc.query(SqlQueries.LOCK_POST, (row, index) -> row.getString("userId"), id)
                .stream().findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found."));
        if (!owner.equals(viewer)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the author can edit this post.");
        jdbc.update(SqlQueries.EDIT, content, id, viewer);
        jdbc.update(SqlQueries.CLEAR_TAGS, id);
        addTags(Long.parseLong(id), tags);
    }
}

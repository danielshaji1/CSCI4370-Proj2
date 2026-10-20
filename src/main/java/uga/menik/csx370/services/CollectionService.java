package uga.menik.csx370.services;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uga.menik.csx370.db.SqlQueries;
import uga.menik.csx370.models.Post;
import uga.menik.csx370.models.SavedCollection;
import uga.menik.csx370.utility.PostText;

/** Private named post collections. Every read/write scopes SQL to the session owner. */
@Service
public class CollectionService {
    private final JdbcTemplate jdbc;
    private final PostService posts;
    public CollectionService(JdbcTemplate jdbc, PostService posts) { this.jdbc = jdbc; this.posts = posts; }

    private SavedCollection map(ResultSet row, int index) throws SQLException {
        return new SavedCollection(row.getLong("collectionId"), row.getString("name"),
                row.getString("description"), PostText.date(row.getTimestamp("createdAt")), row.getInt("postCount"));
    }

    public List<SavedCollection> list(String viewer) { return jdbc.query(SqlQueries.COLLECTIONS, this::map, viewer); }
    public SavedCollection get(long id, String viewer) {
        return jdbc.query(SqlQueries.COLLECTION, this::map, id, viewer).stream().findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found."));
    }
    public List<Post> posts(long id, String viewer) {
        get(id, viewer);
        return jdbc.query(SqlQueries.COLLECTION_POSTS, (row, index) -> posts.readPost(row, viewer), viewer, viewer, id, viewer);
    }
    private String description(String input) {
        String value = input == null ? "" : input.strip();
        if (value.codePointCount(0, value.length()) > 255) throw new IllegalArgumentException("Description must be at most 255 characters.");
        return value;
    }
    public long create(String viewer, String name, String description) {
        String title = PostText.required(name, 80, "Collection name");
        String detail = description(description);
        GeneratedKeyHolder key = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            var statement = connection.prepareStatement(SqlQueries.CREATE_COLLECTION, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, viewer);
            statement.setString(2, title);
            statement.setString(3, detail);
            return statement;
        }, key);
        return key.getKey().longValue();
    }
    public void update(long id, String viewer, String name, String description) {
        get(id, viewer);
        jdbc.update(SqlQueries.RENAME_COLLECTION, PostText.required(name, 80, "Collection name"), description(description), id, viewer);
    }
    public void delete(long id, String viewer) {
        get(id, viewer);
        jdbc.update(SqlQueries.DELETE_COLLECTION, id, viewer);
    }
    public void add(long id, String viewer, long postId) {
        get(id, viewer);
        jdbc.update(SqlQueries.ADD_COLLECTION_POST, postId, id, viewer);
    }
    public void remove(long id, String viewer, long postId) {
        get(id, viewer);
        jdbc.update(SqlQueries.REMOVE_COLLECTION_POST, id, postId, viewer);
    }
}

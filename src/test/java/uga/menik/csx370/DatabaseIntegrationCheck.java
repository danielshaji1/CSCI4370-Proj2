package uga.menik.csx370;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.server.ResponseStatusException;
import uga.menik.csx370.models.Post;
import uga.menik.csx370.services.*;

/** Explicit opt-in integration runner. Uses a fresh random database and always drops only that database. */
public class DatabaseIntegrationCheck {
    @Configuration
    @EnableTransactionManagement
    static class Transactions {}

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    private static List<String> ids(List<Post> posts) { return posts.stream().map(Post::getPostId).toList(); }
    private static void denied(Runnable action) {
        try { action.run(); throw new AssertionError("Unauthorized action succeeded"); }
        catch (ResponseStatusException expected) { require(expected.getStatusCode().value() == 403 || expected.getStatusCode().value() == 404, "Wrong access error"); }
    }
    public static void main(String[] args) throws Exception {
        String server = System.getenv().getOrDefault("MB_TEST_SERVER", "jdbc:mysql://127.0.0.1:33306/");
        if (!server.endsWith("/")) throw new IllegalArgumentException("MB_TEST_SERVER must end with / and have no database or query string.");
        String user = System.getenv().getOrDefault("MB_TEST_USER", "root");
        String password = System.getenv().getOrDefault("MB_TEST_PASSWORD", "");
        String database = "csx370_check_" + UUID.randomUUID().toString().replace("-", "");
        try (var admin = DriverManager.getConnection(server, user, password); var statement = admin.createStatement()) {
            statement.execute("CREATE DATABASE " + database + " CHARACTER SET utf8mb4");
            try {
                var source = new DriverManagerDataSource(server + database, user, password);
                String schema = Files.readString(Path.of("schema.sql"))
                        .replaceAll("(?m)^CREATE DATABASE[^;]*;", "")
                        .replaceAll("(?m)^USE csx370_mb_platform;", "");
                new ResourceDatabasePopulator(new ByteArrayResource(schema.getBytes(StandardCharsets.UTF_8))).execute(source);
                try (var context = new AnnotationConfigApplicationContext()) {
                    context.register(Transactions.class);
                    context.registerBean(JdbcTemplate.class, () -> new JdbcTemplate(source));
                    context.registerBean(PlatformTransactionManager.class, () -> new DataSourceTransactionManager(source));
                    context.register(PostService.class, PeopleService.class, CollectionService.class);
                    context.refresh();
                    JdbcTemplate sql = context.getBean(JdbcTemplate.class);
                    sql.update("INSERT INTO user(userId,username,password,firstName,lastName) VALUES(1,'alice','hash','Alice','One'),(2,'bob','hash','Bobby','Two'),(3,'cara','hash','Cara','Three'),(4,'dana','hash','Dana','Four')");
                    PostService posts = context.getBean(PostService.class);
                    PeopleService people = context.getBean(PeopleService.class);
                    CollectionService collections = context.getBean(CollectionService.class);
                    String own = String.valueOf(posts.create("My #2025 #fireworks #FIREWORKS", "1"));
                    String followed = String.valueOf(posts.create("Other #2025 #fireworks", "2"));
                    String hidden = String.valueOf(posts.create("Not followed #2025", "3"));
                    sql.update("UPDATE post SET createdAt='2025-03-07 22:54:00'");
                    people.follow("1", "2", true); people.follow("1", "2", true);
                    require(ids(posts.home("1")).equals(List.of(followed, own)), "Feed scope/order failed");
                    require(ids(posts.profile("3", "1")).equals(List.of(hidden)), "Profile scope failed");
                    require(ids(posts.search("#2025 #FIREWORKS #2025", "1")).equals(List.of(followed, own)), "AND hashtag search failed");
                    require(posts.search("#missing", "1").isEmpty(), "Unknown hashtag matched");
                    posts.like(own, "2", true); posts.like(own, "2", true); posts.like(own, "3", true);
                    posts.comment(own, "2", "First comment"); posts.comment(own, "3", "Second comment");
                    var expanded = posts.one(own, "2");
                    require(expanded.getHeartsCount()==2 && expanded.getCommentsCount()==2 && expanded.getHearted(), "Counts multiply or like state is wrong");
                    require(expanded.getComments().get(0).getContent().equals("First comment"), "Comment order failed");
                    posts.bookmark(followed,"1",true); posts.bookmark(own,"1",true); posts.bookmark(own,"1",true);
                    require(ids(posts.bookmarks("1")).equals(List.of(followed,own)), "Bookmark order/dedup failed");
                    require(posts.bookmarks("2").isEmpty(), "Bookmarks leaked between users");
                    posts.bookmark(own,"1",false); posts.like(own,"2",false);
                    require(posts.one(own,"2").getHeartsCount()==1, "Unlike failed");
                    require(people.getFollowableUsers("1").size()==3, "People includes self or misses users");
                    require(people.getFollowableUsers("1").stream().filter(p -> p.getUserId().equals("4")).findFirst().orElseThrow().getLastActiveDate().equals("Unknown"), "No-post date failed");
                    denied(() -> posts.edit(own,"2","Unauthorized"));
                    posts.edit(own,"1","Changed #uga");
                    require(ids(posts.search("#2025 #fireworks","1")).equals(List.of(followed)), "Edit left stale hashtags");
                    require(posts.one(own,"1").getPostDate().equals("Mar 07, 2025, 10:54 PM"), "Date changed/format incorrect");
                    long list = collections.create("1","Study","Private reading");
                    long other = collections.create("2","Study","Another owner");
                    collections.add(list,"1",Long.parseLong(own)); collections.add(list,"1",Long.parseLong(own));
                    collections.add(list,"1",Long.parseLong(followed));
                    require(collections.get(list,"1").postCount()==2, "Collection count/dedup failed");
                    require(ids(collections.posts(list,"1")).equals(List.of(followed,own)), "Collection post order failed");
                    denied(() -> collections.get(list,"2")); denied(() -> collections.posts(list,"2"));
                    denied(() -> collections.add(list,"2",Long.parseLong(hidden)));
                    denied(() -> collections.update(list,"2","Stolen","")); denied(() -> collections.delete(list,"2"));
                    denied(() -> collections.remove(list,"2",Long.parseLong(own)));
                    require(collections.list("1").size()==1 && collections.list("2").get(0).collectionId()==other, "Collection listing leaked");
                    collections.update(list,"1","Revised","New description");
                    collections.remove(list,"1",Long.parseLong(own));
                    require(collections.get(list,"1").postCount()==1, "Collection removal failed");
                    collections.delete(list,"1");
                    require(sql.queryForObject("SELECT COUNT(*) FROM collection_post WHERE collectionId=?",Integer.class,list)==0, "Collection links did not cascade");
                    require(posts.one(own,"1")!=null,"Deleting collection deleted original post");
                    people.follow("1","2",false);
                    require(ids(posts.home("1")).equals(List.of(own)),"Unfollow failed");
                    // Force a link failure after post insert to prove transactional rollback.
                    int before = sql.queryForObject("SELECT COUNT(*) FROM post",Integer.class);
                    sql.execute("CREATE TRIGGER fail_tag BEFORE INSERT ON post_hashtag FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Rollback probe'");
                    try { posts.create("Must roll back #rollback", "1"); throw new AssertionError("Expected forced link failure"); }
                    catch (org.springframework.dao.DataAccessException expected) { }
                    finally { sql.execute("DROP TRIGGER fail_tag"); }
                    require(sql.queryForObject("SELECT COUNT(*) FROM post",Integer.class)==before,"Post/tag transaction failed to roll back");
                    System.out.println("PASS: feed, profiles, AND search, counts, comments, bookmarks, follow, edits, private collections, and rollback.");
                }
            } finally { statement.execute("DROP DATABASE " + database); }
        }
    }
}

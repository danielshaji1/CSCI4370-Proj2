package uga.menik.csx370;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import com.samskivert.mustache.Mustache;
import junit.framework.TestCase;
import uga.menik.csx370.models.*;

/** Render the actual templates with real model objects, including nested partials. */
public class TemplateRenderingTest extends TestCase {
    private String render(String name, Map<String, Object> model) throws Exception {
        Path templates = Path.of("src/main/resources/templates");
        var compiler = Mustache.compiler().withLoader(partial -> Files.newBufferedReader(templates.resolve(partial + ".mustache")));
        return compiler.compile(Files.readString(templates.resolve(name + ".mustache"))).execute(model);
    }
    public void testExpandedPostEscapesContentAndShowsActions() throws Exception {
        User user = new User("1", "Alex", "Rivera");
        ExpandedPost post = new ExpandedPost("4", "<script>alert(1)</script>", "Mar 07, 2025, 10:54 PM", user,
                2, 1, true, true, List.of(new Comment("1", "First comment", "Mar 08, 2025, 10:00 AM", user)));
        post.setEditable(true);
        Map<String,Object> model = new HashMap<>();
        model.put("posts", List.of(post)); model.put("savePostId", "4");
        model.put("collections", List.of(new SavedCollection(1, "Reading", "Notes", "Mar 07, 2025, 10:54 PM", 2)));
        String page = render("posts_page", model);
        assertTrue(page.contains("&lt;script&gt;")); assertFalse(page.contains("<script>alert"));
        assertTrue(page.contains("First comment")); assertTrue(page.contains("/post/4/heart/false"));
        assertTrue(page.contains("/post/4/bookmark/false")); assertTrue(page.contains("/post/4/edit"));
        assertTrue(page.contains("value=\"1\">Reading"));
    }
    public void testOtherUsersDoNotGetEditAction() throws Exception {
        var post = new Post("4", "Hello", "Today", new User("2", "Jordan", "Lee"), 0, 0, false, false);
        String page = render("home_page", Map.of("posts", List.of(post)));
        assertFalse(page.contains("/post/4/edit"));
        assertTrue(page.contains("/post/4/heart/true"));
    }
    public void testCollectionAndPeoplePages() throws Exception {
        var collection = new SavedCollection(3, "Study", "Exam notes", "Today", 0);
        assertTrue(render("collections_page", Map.of("collections", List.of(collection))).contains("/collections/3"));
        assertTrue(render("collection_page", Map.of("collection", collection, "collectionId", 3, "posts", List.of())).contains("No posts here yet"));
        var person = new FollowableUser("2", "Jordan", "Lee", true, "Unknown");
        String people = render("people_page", Map.of("users", List.of(person)));
        assertTrue(people.contains("Unknown")); assertTrue(people.contains("/people/2/follow/false"));
    }
    public void testEmptyPagesAndAuthForms() throws Exception {
        for (String page : List.of("home_page", "posts_page", "people_page", "collections_page", "login_page", "registration_page")) {
            assertTrue(render(page, Map.of()).contains("<!DOCTYPE html>"));
        }
    }
}

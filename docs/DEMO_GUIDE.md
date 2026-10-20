# Demo recording guide

Use **Chrome**, a populated MySQL database, and the local source checkout. Record your own narration. This guide is a script/checklist, not the video deliverable.

1. **Compile and start.** Show `java -version`, `mvn clean compile`, and the startup command in Readme.txt. Show Docker's `mysql-server-x370` port mapping (3306 → 33306). Explain that MySQL runs in Docker while Spring Boot runs on the host. Wait for the app's Started message.
2. **Log in and register.** Open `http://localhost:8081/` in Chrome. Use alex/password for a fresh seeded installation. Demonstrate registration with a second browser profile or log out and back in. Avoid showing your real passwords.
3. **Home feed.** Explain that it includes your own posts and followed users' posts in descending date order. In the fresh snapshot Alex follows Jordan but not Taylor. Click Taylor's name on People to show Taylor has posts absent from Alex's home feed.
4. **Create a post.** Submit `Preparing for #2025 #fireworks with friends!`. Show it at the top of Home, with your account as author and the specified date format. Try a blank/whitespace post; show the validation error.
5. **Profiles.** Click Profile to see only Alex's posts. Click Jordan's author name on a post or People entry; show only Jordan's posts, newest first.
6. **People.** Show that Alex isn't listed. Point out the formatted last-post timestamps. Avery has no posts and shows Unknown in a fresh installation. Follow Taylor and show Taylor's posts appearing on Home. Unfollow Taylor and show they disappear while Alex's own posts stay.
7. **Likes and comments.** Open a post using its comments icon. Like and unlike, observing the count and filled/empty state. Add two comments and show them oldest first, including the correct author links and comment count. Like/comment from a second account to demonstrate shared counts and per-user flags.
8. **Bookmarks.** Bookmark an older post and a newer post, then visit Bookmarks. Show sorting by the original post date (not save time). Remove a bookmark and confirm its disappearance. Show another account has its own bookmark list.
9. **Hashtag intersection.** Search `#2025 #fireworks`. Explain that each result contains both hashtags. Compare with `#2025` alone and `#FIREWORKS #2025 #2025` (case/duplicate normalization). A missing tag should return no results. Fresh sample post 4 is the initial two-tag match.
10. **Post editing.** Open your own post and edit its text and hashtags. Verify the old hashtag search no longer matches and the new one does. Mention that creation time, comments, likes, and bookmarks stay. Other users' cards should have no Edit link.
11. **New feature: collections.** Open Collections; create “Demo reading list” and a description. Save two posts through their detail pages. Show the count and newest-first order. Save the same post twice to demonstrate no duplication. Rename the collection; remove one post. Switch to another account and try its URL to show it is private. Delete it as the owner and show the original post still exists. Leave at least one populated collection for the final database export.
12. **Code for the new feature.** Briefly show the three UI templates, CollectionController, CollectionService, and the saved_collection/collection_post schema and queries. Describe the many-to-many post membership and owner checks.
13. **Schema/data tour.** Open the MySQL prompt using `docker exec -it mysql-server-x370 mysql -u root -p`. Run `USE csx370_mb_platform;` then `SOURCE /tmp/demo_database.sql;` after copying `docs/demo_database.sql` to that path. Scroll through every table's `SHOW CREATE TABLE` and data output. Explain PKs, FKs, composite keys, unique owner/name, CHECK constraints, cascades, types and NOT NULLs. Show ER_diagram.png at full resolution.
14. **Conclude.** Mention each member's actual work. Save the recording to `deliverables/demo.mp4`.

After recording, export the populated database with `bash scripts/export_database.sh`; inspect that the exported file contains CREATE TABLE and INSERT statements for all ten tables. Complete the signed official contribution form and Readme.txt, then build the final zip.

## Important data preparation

An upgraded existing database retains your own data and may not contain the newer sample accounts/posts. In that case, create equivalent posts and collections before recording; do not reload the fresh seed over existing work. Keep examples containing both tags, only one tag, a user with no posts, and posts from an unfollowed user. Populate every table before the final export.

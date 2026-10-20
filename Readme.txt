CSX370 MICROBLOGGING PLATFORM

TEAM MEMBERS AND CONTRIBUTIONS (required before submission)
PENDING: Replace this section with each team member's full name and actual contributions.
Do not claim work on behalf of another member. Obtain everyone's signature on the official eLC form.

REQUIREMENTS
- Java 17 or newer and Maven.
- Docker Desktop and the MySQL 8.0.16+ container named mysql-server-x370.
- Container port 3306 mapped to host port 33306.
- Chrome for the application and demo recording.
- No Maven dependencies were added to the starter project.

UPGRADE THE EXISTING DATABASE (use this if you can already log in)
1. Open Docker Desktop, then run:
   docker start mysql-server-x370
   docker port mysql-server-x370
   Confirm a mapping such as 3306/tcp -> 0.0.0.0:33306.
2. From the project folder, copy the non-destructive update script:
   docker cp update_database.sql mysql-server-x370:/tmp/update_database.sql
3. Open the MySQL prompt:
   docker exec -it mysql-server-x370 mysql -u root -p
   Enter the MySQL root password configured for your container.
4. At the mysql> prompt:
   SOURCE /tmp/update_database.sql;
   USE csx370_mb_platform;
   SHOW TABLES;
   exit;
   There should be 10 app tables. This update adds missing tables, including the
   new collections tables, without dropping tables or replacing your users/posts.
   Existing base table definitions are retained. schema.sql documents the intended
   full schema; the ER diagram describes a fresh installation of that schema.
   Back up your database before applying a schema update.
5. Stop any old app process with Ctrl+C in its terminal. The current server process
   was started before the CollectionsController route was loaded, so it returns 404
   at /collections even though the refreshed home template shows the new link.
   Restart to load the updated controller. Build/start the updated app:
   mvn clean compile
   mvn spring-boot:run -Dspring-boot.run.jvmArguments='-Dserver.port=8081'
6. Leave both MySQL and the app running. In Chrome open http://localhost:8081/.
   Use your existing login. If you kept the corrected demo accounts: alex / password.
7. Visit Collections to create a reading list, then open any post and save it there.

FRESH INSTALLATION ONLY
Use database_setup.sql instead of update_database.sql in steps 2–4 above:
   docker cp database_setup.sql mysql-server-x370:/tmp/database_setup.sql
   docker exec -it mysql-server-x370 mysql -u root -p
   SOURCE /tmp/database_setup.sql;
It creates all tables and includes meaningful INSERT data in every table. The initial
snapshot contains alex, jordan, taylor, and avery, all with password "password".
Do not import the fresh demo snapshot into a database containing your own data.
If database_setup.sql has been replaced by a final mysqldump export, that export can
contain DROP TABLE statements: import it only when restoring the submitted snapshot.

CONFIGURATION
src/main/resources/application.properties defaults to:
   jdbc:mysql://localhost:33306/csx370_mb_platform
   MySQL username root
Set SPRING_DATASOURCE_PASSWORD in your shell environment. To override the other
settings, use SPRING_DATASOURCE_URL and SPRING_DATASOURCE_USERNAME. Do not commit
database credentials to source control.
Windows command prompt startup:
   mvn spring-boot:run -D"spring-boot.run.arguments=--server.port=8081"
Windows PowerShell startup:
   mvn spring-boot:run --% -Dspring-boot.run.arguments="--server.port=8081"

APPLICATION FEATURES
- Register/login with BCrypt password hashes and a session-scoped current user.
- Create nonempty posts (maximum 500 characters); author always comes from session.
- Home includes own posts and followed users, newest first, with post ID tie breaker.
- Profile navigation opens the current user's posts; every displayed author links to
  that author's profile, including comment authors and People page entries.
- People excludes the current user, supports follow/unfollow, and derives last active
  time solely from posts. No-post users show Unknown.
- Bookmark/unbookmark and like/unlike work wherever a post appears; lists display
  separate comment and like counts without multiplying joined rows.
- Comments are nonempty, session-authored, and ordered oldest first with ID tie breaker.
- Timestamps display as Mar 07, 2025, 10:54 PM using English month names.
- Hashtags consist of letters, digits, or underscores after #. Stored tags are lowercase;
  duplicates are removed. Searches match ALL distinct supplied tags (AND semantics).
  #2025 #fireworks in the fresh snapshot matches only post 4.
- Bookmarks, profiles, search, home and collection posts use newest-first ordering.
- Post editing is an additional convenience. Only the author sees the edit control or
  can access its endpoint. The content and hashtag links change in one transaction;
  the original post date and existing likes/comments/bookmarks stay attached.
- SQL values use prepared parameters. Input limits are checked on the server and forms.

REQUIRED NEW FEATURE: PRIVATE SAVED COLLECTIONS
Access Collections in the top navigation (http://localhost:8081/collections).
Create a named collection with an optional description. Open a post and use Save to
collection to select a collection. Collections show counts and posts newest first.
Rename collections, change descriptions, remove individual posts, or delete collections.
A post can belong to several collections; saving twice does not duplicate it. Collection
names are unique per owner. Another account cannot list, open, rename, add/remove posts
from, or delete your collection. All relevant SQL operations restrict by session user ID.
Deleting a collection cascades its membership rows and preserves the original posts.
This feature adds aggregate queries, a many-to-many relationship, owner-scoped CRUD,
and a multi-page interaction beyond a hashtag lookup.

Code locations (relative to the project root):
  UI:
    src/main/resources/templates/collections_page.mustache
    src/main/resources/templates/collection_page.mustache
    src/main/resources/templates/fragments/save_collection.mustache
    src/main/resources/templates/fragments/top_bar.mustache
    src/main/resources/templates/fragments/post.mustache
  Controller:
    src/main/java/uga/menik/csx370/controllers/CollectionController.java
    PostController supplies available collections on the post detail page.
  New service:
    src/main/java/uga/menik/csx370/services/CollectionService.java
  Model:
    src/main/java/uga/menik/csx370/models/SavedCollection.java
  New database operations / schema:
    saved_collection and collection_post in schema.sql, update_database.sql, and
    database_setup.sql; all collection queries in db/SqlQueries.java and dml.sql.

DATABASE DESIGN AND SQL
ER_diagram.png includes all entities, column types, keys, constraints and the cardinality
of every FK relationship. scripts/RenderErDiagram.java regenerates it using built-in Java:
   java -Djava.awt.headless=true scripts/RenderErDiagram.java
Junction tables resolve user/post likes and bookmarks, self-referencing follows, post/tag
relationships, and collection/post relationships. Composite primary keys prevent duplicates.
The normalized schema stores posts, users, tags and collections once; counts and last-post
times are computed. Non-null FKs require a parent. Most deletions cascade; follow uses the
MySQL default RESTRICT so its no-self-follow CHECK is compatible with MySQL.
The fresh schema uses InnoDB and UTF-8. Existing databases retain their current timezone;
no timezone conversion is hardcoded in display formatting.
dml.sql contains every application retrieval/modification statement, including the complete
post SELECT projections. Each statement has its own purpose and full example page URL.
Question marks represent JDBC parameters. The search entry shows two tags; the service
creates the corresponding number of placeholders for the user's distinct tag count.
SqlQueries.java is the single source of application SQL. The unit check detects missing
or out-of-date catalog statements. The DML catalog is not an initialization script.

VERIFICATION
   mvn test
The existing JUnit dependency is used for 11 checks of validation, tags, rendered templates,
password hashes, SQL catalog coverage, and real Spring dependency/transaction wiring. No new testing libraries were added. If the Maven Surefire provider is unavailable,
run `bash scripts/run_checks.sh` to execute the same JUnit checks directly.
To run the full MySQL integration scenario in a disposable database:
   bash scripts/verify_database.sh
It creates a uniquely named csx370_check_* database and drops ONLY that database, including
on failure. It needs a MySQL account with CREATE/DROP DATABASE privileges. Optional settings:
MB_TEST_SERVER (default jdbc:mysql://127.0.0.1:33306/), MB_TEST_USER (root), and
MB_TEST_PASSWORD (default empty). The scenario covers feed filtering, AND search, dates/counts, follows,
bookmarks, comments, owner-only edits, collection CRUD/privacy, duplicates and rollback.
See docs/VERIFICATION.md for checks actually completed in the development session.
Use docs/DEMO_GUIDE.md for the required Chrome walkthrough and narration checklist.

EXPORT ALL CURRENT DATA BEFORE SUBMISSION
After the final demo data is populated, run:
   bash scripts/export_database.sh
Enter the MySQL root password. This reads mysql-server-x370 with mysqldump and replaces
local database_setup.sql only after a successful export; the old file is backed up as
 database_setup.previous.sql. All current tables, constraints and rows are exported,
with complete INSERT statements. This exports the real data as-is, rather than resetting
it to the initial sample snapshot. The export is not run automatically by the app.
Windows alternative (from an interactive shell inside the container):
   docker exec -it mysql-server-x370 sh
   mysqldump -u root -p --single-transaction --complete-insert --skip-extended-insert --no-tablespaces --set-gtid-purged=OFF --databases csx370_mb_platform > /tmp/database_setup.sql
   exit
   docker cp mysql-server-x370:/tmp/database_setup.sql database_setup.sql

DEMO VIDEO, CONTRIBUTION FORM, AND ZIP
Record a narrated video in Chrome showing compilation/startup, each feature, populated
data, and all tables/types/constraints. Follow docs/DEMO_GUIDE.md. Save the actual recording
as deliverables/demo.mp4. Obtain the official contribution form from eLC, have every team
member sign it, and place the completed file at deliverables/contribution_form.pdf.
Complete the team section above. Export the actual database, then package:
   python3 scripts/package_submission.py
The package script requires the video, signed-form file, and completed team section before
it creates submission.zip. Check the form's signatures and video narration yourself.
A source/artifact draft can be generated at any time with:
   python3 scripts/package_submission.py --draft
This creates submission-draft.zip; it is not a complete eLC submission.

EXTERNAL RESOURCES CONSULTED
- Instructor-provided starter code and assignment instructions.
- Spring transaction implementation and JDBC participation:
  https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/tx-decl-explained.html
  https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html
- MySQL CHECK constraints (including FK action restrictions):
  https://dev.mysql.com/doc/refman/8.0/en/create-table-check-constraints.html
- MySQL FK constraints:
  https://dev.mysql.com/doc/refman/8.0/en/create-table-foreign-keys.html

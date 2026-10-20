# Assignment coverage

The MySQL integration scenario and all 11 Maven tests passed. Live Chrome verification and team deliverables remain as listed in VERIFICATION.md.

| Requirement | Source / artifact | Status |
|---|---|---|
| Session-authored nonempty posts and hashtags | HomeController, PostService, PostText | Implemented |
| Home: own + followed users, newest first | SqlQueries.HOME | Implemented |
| Own/specific profile, author links | ProfileController, post/comment/people templates | Implemented |
| People excludes self, follow/unfollow, last post or Unknown | PeopleService, PeopleController | Implemented |
| Bookmarks scoped to user, newest post first | PostService.bookmarks, BookmarksController | Implemented |
| Multi-hashtag AND search, newest first | PostService.search, SqlQueries.SEARCH | Implemented |
| Likes, bookmarks, comments and separate counts everywhere | Shared post mapper/template, PostController | Implemented |
| Comments oldest first, session author | SqlQueries.COMMENTS and COMMENT | Implemented |
| Required timestamp format | PostText.date | Implemented and unit checked |
| New feature with new UI/controller/service/database operation | Private saved collections; two new tables | Implemented |
| Java Spring Boot, Docker MySQL, existing dependencies only | pom.xml unchanged; Readme.txt | MySQL integration passed; Chrome walkthrough pending |
| Working source | src and pom.xml | Compiles, Maven package and MySQL integration passed |
| Member names and contributions | Readme.txt team section | Team input needed |
| Run instructions, resources, feature code locations | Readme.txt | Written |
| Clear ER PNG | ER_diagram.png | Generated and visually reviewed |
| DDL and meaningful data in every table | database_setup.sql | Fresh snapshot included |
| Final database exported as-is | scripts/export_database.sh | Must run against actual demo database |
| All application DML, purpose per statement, page URL | dml.sql, SqlQueries.java | Written; exact statement coverage checked |
| Narrated video: build/start/features/schema/populated data | docs/DEMO_GUIDE.md | Actual team recording needed |
| Signed group contribution form | deliverables/contribution_form.pdf | Obtain official eLC form and signatures |
| Zip for submission | scripts/package_submission.py | Source/artifact draft built; final package gated on team files |

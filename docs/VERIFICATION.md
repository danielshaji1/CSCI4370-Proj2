# Verification record

## Completed after permission was granted

- `bash scripts/verify_database.sh`: **passed against the running MySQL server** on host port 33306, using the existing MySQL Connector/J dependency. The runner created a random `csx370_check_*` database and removed it afterward. It did not modify the application database.
- The integration scenario verified actual MySQL schema creation, own/followed feed filtering, newest-first ordering and ties, profiles, all-tags search, separate like/comment counts, comment ordering, last-post dates, follow/like/bookmark idempotence, owner-only editing and hashtag replacement, collection CRUD/privacy/cascades, and transaction rollback after a forced SQL error.
- `mvn -q test package`: **passed**. Surefire reported 11 tests, zero failures, zero errors, and zero skipped tests. The Maven JAR was built at `target/csx370-starter-1.0-SNAPSHOT.jar`; the documented app startup remains `mvn spring-boot:run`.
- Tests include four content/hashtag/date checks, four real Mustache template checks, two seed password / exact SQL catalog checks, and one Spring dependency/transaction wiring check.
- The ER diagram was generated at 2780 × 2200 and visually inspected for legibility, types, key roles, all 14 foreign-key relationships, and cardinalities.
- Application dependencies in pom.xml were left unchanged. New application logic uses the starter's existing libraries and Java built-ins.

Run unit/template/wiring checks with `mvn test`. Run MySQL integration checks with `bash scripts/verify_database.sh`.

## Remaining verification and submission work

1. The non-destructive `update_database.sql` migration has now been applied to the live `csx370_mb_platform` database in `mysql-server-x370`. Before applying it, a complete pre-update dump was saved inside the container at `/tmp/csx370_mb_platform_backup_before_update.sql`. Verification confirmed both collection tables now exist and the three existing users remain.
2. Restart the already running Spring Boot process. Its route table predates `CollectionController`, so it still returns 404 for `/collections` even though it serves the refreshed template containing the Collections navigation link. Stop it with Ctrl+C in the terminal running Maven, then run the startup command in Readme.txt. The authenticated HTTP check returned 200 for Home and 404 for Collections before restart.
3. Complete the live Chrome walkthrough in DEMO_GUIDE.md. Service integration and page-template checks do not replace the final browser walkthrough.
4. Export the populated demo database with `scripts/export_database.sh`. The live application database has not been exported here; database_setup.sql is still the meaningful fresh-install snapshot.
5. Supply the actual team names/contributions, narrated recording, and signed official contribution form before creating the final submission archive.

## Earlier environment restrictions resolved

Before approval was enabled, Docker/local database access and Maven cache writes were blocked. The approved reruns above succeeded, superseding those earlier blocked checks.

CSCI 4370 PROJ 2 Readme.txt


TEAM CONTRIBUTIONS:

1. Daniel Shaji - (dks59357): I added the ability to store amount of likes and comments on each individual post.
2. Lucas Patsy - (lsp41296): I helped make the additional demo posts.
3. Dhaval Pucha (dp68186) -  I created the collections page / unique feature. 
4. NAME () - ER Diagram creation. Also developed the saving of time/date with post into the MySQL database.
5. Iliya Shapirov - Created hashtags features and login saves. Bug fixed sql grammar errors.




---------------------------------------
---------------------------------------

RUN INSTRUCTIONS:

1. Make sure the Docker container `mysql-server-x370` is running with MySQL username `root`, password `mysqlpass`, and host port `33306` mapped to container port `3306`. The project database should already be initialized.
2. From the project directory containing `pom.xml`, run:


Mac:
   
   mvn spring-boot:run -Dspring-boot.run.jvmArguments='-Dserver.port=8081'
   

Windows Command Line:
   
   mvn spring-boot:run -D"spring-boot.run.arguments=--server.port=8081"
   

Windows Powershell:
   
   mvn spring-boot:run --% -Dspring-boot.run.arguments="--server.port=8081"
   
   
3. Open [http://localhost:8081/](http://localhost:8081/) in Chrome.
4. Log in with the existing demo account: username `alex`, password `password`.


---------------------------------------
--------------------------------------- 

UNIQUE FEATURE:

The new feature is Private Saved "Collections". Users can organize posts into named reading lists, add descriptions, rename collections, remove saved posts, and delete collections. Each collection displays its post count and posts from newest to oldest. Collections are private to their owner, and deleting one preserves the original posts.

How to access it: Log in, click Collections in the top navigation, and create a collection. Open a post and use Save to collection to choose where to save it.

The code is organized as follows:

- UI: src/main/resources/templates/collections_page.mustache provides collection creation and listing; collection_page.mustache displays and manages a collection; fragments/save_collection.mustache provides the save form.
- Controller: src/main/java/uga/menik/csx370/controllers/CollectionController.java handles the /collections routes for viewing, creating, updating, deleting, and saving/removing posts.
- Service: src/main/java/uga/menik/csx370/services/CollectionService.java performs database operations, validates collection details, and checks ownership.
- SQL/schema additions: saved_collection stores collection details and ownership; collection_post links collections to posts. Their definitions are in schema.sql, database_setup.sql, and update_database.sql. Queries are in src/main/java/uga/menik/csx370/db/SqlQueries.java and documented in dml.sql.

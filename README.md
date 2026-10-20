# CSX370 Microblogging Platform

A Spring Boot microblogging application backed by MySQL. Features include user
registration and login, posts, profiles, follows, likes, bookmarks, hashtag search,
comments, and private saved collections.

## Requirements

- Java 17 or newer
- Maven
- MySQL 8.0.16 or newer (Docker setup instructions are in [Readme.txt](./Readme.txt))

## Run locally

Follow [Readme.txt](./Readme.txt) to prepare a fresh or existing database, then set
the MySQL connection environment variables and start the application:

```sh
export SPRING_DATASOURCE_URL='jdbc:mysql://localhost:33306/csx370_mb_platform'
export SPRING_DATASOURCE_USERNAME='root'
export SPRING_DATASOURCE_PASSWORD='<your MySQL password>'
mvn spring-boot:run
```

The application is available at `http://localhost:8080/` by default. Use the
instructions in `Readme.txt` for the alternate port and Windows commands.

## Checks

```sh
mvn test
```

The optional MySQL integration scenario in `scripts/verify_database.sh` creates
and removes a uniquely named test database. Configure `MB_TEST_SERVER`,
`MB_TEST_USER`, and `MB_TEST_PASSWORD` for your local MySQL instance before running
it.

See [docs/DEMO_GUIDE.md](./docs/DEMO_GUIDE.md) for the feature walkthrough and
[docs/REQUIREMENTS_CHECKLIST.md](./docs/REQUIREMENTS_CHECKLIST.md) for project
requirements.

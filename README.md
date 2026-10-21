# CSCI 4370 Project 2

1. Make sure the Docker container `mysql-server-x370` is running with MySQL username `root`, password `mysqlpass`, and host port `33306` mapped to container port `3306`. The project database should already be initialized.
2. From the project directory containing `pom.xml`, run:


Mac:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.jvmArguments='-Dserver.port=8081'
   ```

Windows Command Line:
   ```bash
   mvn spring-boot:run -D"spring-boot.run.arguments=--server.port=8081"
   ```

Windows Powershell:
   ```bash
   mvn spring-boot:run --% -Dspring-boot.run.arguments="--server.port=8081"
   ```
   
3. Open [http://localhost:8081/](http://localhost:8081/) in Chrome.
4. Log in with the existing demo account: username `alex`, password `password`.

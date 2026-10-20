#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mvn -q -DskipTests test-compile dependency:build-classpath -Dmdep.outputFile=target/check-classpath.txt -Dmdep.includeScope=test
# This runner creates a randomly named test database and drops only that database.
# It requires CREATE/DROP DATABASE privilege; it never uses the app database.
java -cp "target/test-classes:target/classes:$(cat target/check-classpath.txt)" uga.menik.csx370.DatabaseIntegrationCheck

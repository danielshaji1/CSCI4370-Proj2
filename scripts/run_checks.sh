#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
# Uses the JUnit 3 dependency already included by the starter.
mvn -q -DskipTests test-compile dependency:build-classpath -Dmdep.outputFile=target/check-classpath.txt -Dmdep.includeScope=test
check_classpath="target/test-classes:target/classes:$(cat target/check-classpath.txt)"
for check in PostTextTest TemplateRenderingTest SqlAndSeedTest ApplicationContextTest; do
    java -cp "$check_classpath" junit.textui.TestRunner "uga.menik.csx370.$check"
done

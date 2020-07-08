#! /usr/bin/env bash

./gradlew build jacocoTestReport publishToMavenLocal

export CODACY_PROJECT_TOKEN=$1
curl -LS -o codacy-coverage-reporter-assembly.jar "$(curl -LSs https://api.github.com/repos/codacy/codacy-coverage-reporter/releases/latest | jq -r '.assets | map({name, browser_download_url} | select(.name | endswith(".jar"))) | .[0].browser_download_url')"
java -jar codacy-coverage-reporter-assembly.jar report --language Kotlin --force-language -r ./core/build/jacoco/jacocoTestReport.xml

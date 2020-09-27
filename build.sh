#! /usr/bin/env bash

./gradlew --no-daemon build jacocoTestReport publishToMavenLocal

cp -r ./build/publications ./
cp -r ./android/build/javadoc ./publications/android
cp -r ./android-testing/build/javadoc ./publications/androidd-testing
cp -r ./core/build/javadoc ./publications/core
cp -r ./core-testing/build/javadoc ./publications/core-testing
cp -r ./jdk/build/javadoc ./publications/jdk
cp -r ./jdk-testing/build/javadoc ./publications/jdk-testing

if [ -z ${1+x} ]; then
  echo "CODACY project token is unset";
else
  export CODACY_PROJECT_TOKEN=$1
  curl -LS -o codacy-coverage-reporter-assembly.jar "$(curl -LSs https://api.github.com/repos/codacy/codacy-coverage-reporter/releases/latest | jq -r '.assets | map({name, browser_download_url} | select(.name | endswith(".jar"))) | .[0].browser_download_url')"
  java -jar codacy-coverage-reporter-assembly.jar report --language Kotlin --force-language -r ./core/build/jacoco/jacocoTestReport.xml
  rm -f codacy-coverage-reporter-assembly.jar
fi

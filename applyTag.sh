#!/bin/bash

# $1 = tag
# $2 = username
# $3 = password

echo "applying tag $1"

sed -i -E 's/const val version = \"[0-9]+\.[0-9]+\.[0-9]+\"/const val version = \"'${1:1}'\"/g' ./buildSrc/src/main/kotlin/dev/zieger/utils/Globals.kt

sed -i -E 's/mavenUser=/mavenUser='$2'/g' ./gradle.properties
sed -i -E 's/mavenPass=/mavenPass='$3'/g' ./gradle.properties
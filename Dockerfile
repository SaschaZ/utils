FROM openjdk:8

# Support Gradle
ENV TERM dumb
ENV JAVA_OPTS "-Xms2g -Xmx5g"
ENV GRADLE_OPTS "-Xms2g -Xmx5g -XX:MaxPermSize=512m -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8"

# Add Project
COPY . /project
WORKDIR /project

# Remove possible temporary build files
RUN rm -f ./local.properties && \
    find . -name build -print0 | xargs -0 rm -rf && \
    rm -rf .gradle && \
    rm -rf ~/.m2 && \
    rm -rf ~/.gradle

CMD ["./gradlew", "clean", "publishToMavenLocal"]
FROM openjdk:8

# Use the version number of the android sdk tools from here: https://developer.android.com/studio/index.html#downloads.
ENV ANDROID_SDK_TOOLS_VERSION="4333796"
ENV ANDROID_PLATFORM_VERSION="29"
ENV ANDROID_BUILD_TOOLS_VERSION="29.0.3"

ENV ANDROID_HOME /opt/android-sdk

WORKDIR /tmp

# Installing packages
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        curl \
        wget \
        unzip \
        zip \
        python-pip && \
    rm -rf /var/lib/apt/lists/ && \
    apt-get clean

# Install Android SDK
RUN wget -q -O tools.zip https://dl.google.com/android/repository/sdk-tools-linux-${ANDROID_SDK_TOOLS_VERSION}.zip && \
    unzip -q tools.zip && \
    rm -fr $ANDROID_HOME tools.zip && \
    mkdir -p $ANDROID_HOME && \
    mv tools $ANDROID_HOME/tools && \
    cd $ANDROID_HOME && \
    # Install Android components
    yes | tools/bin/sdkmanager --licenses && \
    echo "Install android-${ANDROID_PLATFORM_VERSION}" && \
    tools/bin/sdkmanager "platforms;android-${ANDROID_PLATFORM_VERSION}" && \
    echo "Install platform-tools" && \
    tools/bin/sdkmanager "platform-tools" && \
    echo "Install build-tools-${ANDROID_BUILD_TOOLS_VERSION}" && \
    tools/bin/sdkmanager "build-tools;${ANDROID_BUILD_TOOLS_VERSION}" && \
    echo "Install tools" && \
    tools/bin/sdkmanager "tools"

# Add android commands to PATH
ENV ANDROID_SDK_HOME $ANDROID_HOME
ENV PATH $PATH:$ANDROID_SDK_HOME/tools:$ANDROID_SDK_HOME/platform-tools

# Support Gradle
ENV TERM dumb
ENV JAVA_OPTS "-Xms512m -Xmx1536m"
ENV GRADLE_OPTS "-XX:+UseG1GC -XX:MaxGCPauseMillis=1000"

# Add Project
COPY . /project
WORKDIR /project

# Remove possible temporary build files
RUN rm ./local.properties && \
    find . -name build -print0 | xargs -0 rm -rf

CMD ["./gradlew", "publishToMavenLocal"]
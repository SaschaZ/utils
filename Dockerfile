FROM openjdk:8

# Use the version number of the android sdk tools from here: https://developer.android.com/studio/index.html#downloads.
ENV ANDROID_SDK_TOOLS_VERSION="6609375"
ENV ANDROID_PLATFORM_VERSION="30"
ENV ANDROID_BUILD_TOOLS_VERSION="30.0.2"

ENV ANDROID_HOME /opt/android-sdk

# Add android commands to PATH
ENV ANDROID_SDK_HOME ${ANDROID_HOME}
ENV PATH $PATH:$ANDROID_SDK_HOME/tools:$ANDROID_SDK_HOME/platform-tools:$ANDROID_SDK_HOME/cmdline-tools/version/bin

WORKDIR /tmp

# Installing packages
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        curl \
        wget \
        unzip \
        zip \
        jq \
        python-pip && \
    rm -rf /var/lib/apt/lists/ && \
    apt-get clean

# Install Android SDK
RUN wget -q -O tools.zip https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_SDK_TOOLS_VERSION}_latest.zip && \
    unzip -q tools.zip && \
    rm -fr ${ANDROID_HOME} tools.zip && \
    mkdir -p ${ANDROID_HOME}/cmdline-tools/version && \
    mv tools/* ${ANDROID_HOME}/cmdline-tools/version/ && \
    # Install Android components
    yes | sdkmanager --licenses && \
    echo "Install android-${ANDROID_PLATFORM_VERSION}" && \
    sdkmanager --install "platforms;android-${ANDROID_PLATFORM_VERSION}" && \
    echo "Install platform-tools" && \
    sdkmanager --install "platform-tools" && \
    echo "Install build-tools-${ANDROID_BUILD_TOOLS_VERSION}" && \
    sdkmanager --install "build-tools;${ANDROID_BUILD_TOOLS_VERSION}" && \
    echo "Install tools" && \
    sdkmanager --install "tools"

# Support Gradle
ENV TERM dumb
ENV JAVA_OPTS "-Xms512m -Xmx1536m"
ENV GRADLE_OPTS "-XX:+UseG1GC -XX:MaxGCPauseMillis=1000"

# Add Project
COPY . /project
WORKDIR /project

# Remove possible temporary build files
RUN rm ./local.properties && \
    find . -name build | xargs rm -rf

CMD ["./build.sh"]

plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-android-extensions")
    id("maven-publish")
    id("idea")
}

group = "dev.zieger.utils"
version = "1.1.38"

android {
    compileSdkVersion(29)
    buildToolsVersion("29.0.2")
    defaultConfig {
        minSdkVersion(14)
        targetSdkVersion(29)
        multiDexEnabled = true
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    sourceSets {
        getByName("androidTest").java.setSrcDirs(listOf("src/main/kotlin", "src/androidTest/kotlin"))
        getByName("androidTest").assets.setSrcDirs(listOf("src/main/assets"))

        getByName("main").java.setSrcDirs(listOf("src/main/kotlin"))
        getByName("main").assets.setSrcDirs(listOf("src/main/assets"))
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

//    publishing {
//        publications {
//            create<MavenPublication>("fooLib") {
//                val binaryJar = components["java"]
//
//                val sourcesJar by tasks.creating(Jar::class) {
//                    archiveClassifier.set("sources")
//                    from(sourceSets["main"].java.srcDirs)
//                }
//
//                val javadocJar by tasks.creating(Jar::class) {
//                    archiveClassifier.set("javadoc")
//                    from("$buildDir/javadoc")
//                }
//
//                from(binaryJar)
//                artifact(sourcesJar)
//                artifact(javadocJar)
//            }
//        }
//    }
}

dependencies {
    implementation(project(":core"))
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.61")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.3")
    implementation("androidx.appcompat:appcompat:1.1.0")
    implementation("androidx.core:core-ktx:1.1.0")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")

    implementation(project(":testing"))
    implementation("androidx.test.espresso:espresso-core:3.2.0")
    implementation("androidx.test:runner:1.2.0")
    implementation("androidx.test:core:1.2.0")
    implementation("androidx.test:rules:1.2.0")
//    implementation("androidx.test.ext:junit:1.1.1")

    androidTestImplementation(project(":testing"))
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
    androidTestImplementation("androidx.test:runner:1.2.0")
    androidTestImplementation("androidx.test:core:1.2.0")
    androidTestImplementation("androidx.test:rules:1.2.0")
//    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("com.squareup.okhttp3:mockwebserver:4.3.1")
    androidTestImplementation("org.bouncycastle:bcprov-jdk16:1.46")
}

//publishing {
//    (publications) {
//
//        // Publish the release aar artifact
//        register("mavenAar", MavenPublication::class) {
//            from(components["android"])
//            groupId = "digital.wup.android-maven-publish"
//            version = "${project.version}"
//
//        }
//    }
//}
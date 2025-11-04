// Top-level build file where you can add configuration options common to all sub-projects/modules.
apply(from = "publish-local-aars.gradle.kts")
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("org.sonarqube") version "4.4.1.3373"
    alias(libs.plugins.android.library) apply false
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("${rootDir}/local-maven-repo") }  // 추가
    }
}

sonar {
    properties {
        property("sonar.projectKey", "android-ongil")
        property("sonar.projectName", "Ongil Android")
        property("sonar.projectVersion", "1.0")
        
        property("sonar.modules", "app,wear")
        
        property("sonar.sourceEncoding", "UTF-8")
    }
}

project(":app") {
    sonar {
        properties {
            property("sonar.sources", "src/main/java,src/main/kotlin")
            property("sonar.tests", "src/test/java,src/test/kotlin")
            property("sonar.java.binaries", "build/intermediates/javac/debug/classes")
            property("sonar.kotlin.binaries", "build/tmp/kotlin-classes/debug")
            property("sonar.exclusions", "**/R.java,**/BuildConfig.java")
        }
    }
}

project(":wear") {
    sonar {
        properties {
            property("sonar.sources", "src/main/java,src/main/kotlin")
            property("sonar.tests", "src/test/java,src/test/kotlin")
            property("sonar.java.binaries", "build/intermediates/javac/debug/classes")
            property("sonar.kotlin.binaries", "build/tmp/kotlin-classes/debug")
            property("sonar.exclusions", "**/R.java,**/BuildConfig.java")
        }
    }
}
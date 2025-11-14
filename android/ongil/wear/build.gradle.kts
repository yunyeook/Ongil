plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    alias(libs.plugins.dagger.hilt.android)  // Hilt 플러그인
    kotlin("kapt")
}

android {
    namespace = "kr.co.ongil.wear"
    compileSdk = 36  // Wear OS 6 기준

    defaultConfig {
        applicationId = "kr.co.ongil"  // 모바일과 동일
        minSdk = 30     // Wear OS 3.0 (Watch 4 이상 호환)
        targetSdk = 36  // Wear OS 6 (One UI Watch 8)
        versionCode = 1
        versionName = "1.0"

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    // useLibrary("wear-sdk")
    buildFeatures {
        compose = true
    }
}

dependencies {
    // implementation("com.google.android.wearable:wear:2.9.0")
    compileOnly("com.google.android.wearable:wearable:2.9.0")
    implementation(libs.play.services.wearable)

    // Coroutines Play Services (await() 사용을 위해)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.wear.tooling.preview)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.tiles)
    implementation(libs.androidx.tiles.material)
    implementation(libs.androidx.tiles.tooling.preview)
    implementation(libs.horologist.compose.tools)
    implementation(libs.horologist.tiles)
    implementation(libs.androidx.watchface.complications.data.source.ktx)

    // Hilt (의존성 주입)
    implementation(libs.dagger.hilt.android)              // Hilt 핵심
    kapt(libs.dagger.hilt.compiler)                       // Hilt 컴파일러
    implementation(libs.androidx.hilt.navigation.compose) // Compose에서 Hilt ViewModel 사용
    kapt(libs.androidx.hilt.compiler)                     // AndroidX Hilt 컴파일러

    //DataStore (로그인 정보 등 저장)
    implementation(libs.androidx.datastore.preferences)  // Key-Value 저장소

    //Common 모듈 : 워치에서도 앱과 공통 코드 사용
    implementation(project(":common"))  // 공통 모듈 연결

    // Lifecycle & ViewModel (화면 로직 관리)
    implementation(libs.androidx.lifecycle.viewmodel.compose)  // ViewModel for Compose
    implementation(libs.androidx.lifecycle.runtime.compose)    // Lifecycle utilities
    implementation(libs.androidx.lifecycle.runtime.ktx)        // Coroutine 지원


    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.tiles.tooling)

}
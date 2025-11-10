import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.dagger.hilt.android)
    kotlin("kapt")
    id("com.google.gms.google-services")
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
    }
}

android {
    namespace = "kr.co.ongil"
    compileSdk = 35  // Android 15 기준

    defaultConfig {
        applicationId = "kr.co.ongil"
        minSdk = 30     // Android 11 (S21 출시 버전)
        targetSdk = 35  // Android 15
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // local.properties의 BASE_URL을 BuildConfig 필드로 추가
        buildConfigField("String", "BASE_URL", "\"${localProperties.getProperty("BASE_URL") ?: ""}\"")
        buildConfigField("String", "SSE_URL", "\"${localProperties.getProperty("SSE_URL") ?: ""}\"")
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.coil.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.androidx.navigation.compose)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.gson)

    // Kotlinx Serialization
    implementation(libs.kotlinx.serialization.json)

    // Common Module
    implementation(project(":common"))

    // Hilt
    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    kapt(libs.androidx.hilt.compiler)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Firebase BOM (버전 통합)
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))

    // FCM Messaging
    implementation("com.google.firebase:firebase-messaging")

    // HTTP 통신용
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // JSON 파싱
    implementation("org.json:json:20240303")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0")
    // viewModelScope 지원
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")
    // lifecycleScope 및 flow 지원

    // WebRTC
    implementation("com.github.webrtc-sdk:android:104.5112.09")
}
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt") // Kotlin Kapt 플러그인 추가
}

android {

    namespace = "com.example.ai_macrofy"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.ai_macrofy"
        minSdk = 33
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}// build.gradle (Module-level)



dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // OpenAI GPT-4 API (Retrofit 또는 OkHttp로 요청 구현)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)

    // OCR (ML Kit)
    implementation(libs.mlkit)

    // Room Database
    implementation(libs.room.runtime)
    kapt(libs.room.compiler)
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")


}
plugins {
    id("com.android.application")
}

android {
    namespace = "com.openai.pocketcat"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.openai.pocketcat"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "0.2.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

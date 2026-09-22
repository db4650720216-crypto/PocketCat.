plugins {
    id("com.android.application")
}

android {
    namespace = "com.openai.pocketcat"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.openai.pocketcat"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

# Build the Pocket Cat APK

The repository includes a GitHub Actions workflow at `.github/workflows/build-apk.yml`.

It installs JDK 17, Android API 37, Build Tools 36.0.0, and Gradle 9.6.0, then runs:

    gradle --no-daemon :app:assembleDebug

The resulting installable debug APK is:

    app/build/outputs/apk/debug/app-debug.apk

GitHub Actions uploads it as the artifact `PocketCat-debug-apk`.

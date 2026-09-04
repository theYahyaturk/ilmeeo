# Ilmeeo Android WebView

This repository contains the Android WebView app for [Ilmeeo](https://ilmeeo.alifeo.com/). It wraps the Ilmeeo website in a native Android application and includes location support for website features such as prayer times.

## Import and build

1. Open this folder in Android Studio.
2. Allow Gradle to sync and install any suggested Android SDK components.
3. Select the `app` configuration and run it on an Android device or emulator.

The project uses `compileSdk 35`, requires Android 7.0 or newer (`minSdk 24`), and targets Android 15 (`targetSdk 35`). A working internet connection is required by the app.

To build from a terminal:

```powershell
.\gradlew.bat assembleDebug
```

## Customize the app

- Website URLs and bottom navigation destinations: `app/src/main/java/com/ilmeeo/app/MainActivity.java`
- App name: `app/src/main/res/values/strings.xml`
- App colors and theme: `app/src/main/res/values/colors.xml` and `app/src/main/res/values/themes.xml`
- Launcher and round launcher icons: `app/src/main/res/mipmap-*` and `app/src/main/AndroidManifest.xml`
- Layout and bottom navigation: `app/src/main/res/layout/activity_main.xml` and `app/src/main/res/menu/bottom_nav_menu.xml`

Replace the image assets in `app/src/main/res/drawable*` or the launcher icon assets in `app/src/main/res/mipmap-*` with your own branding as needed. Keep the resource names unchanged, or update their references in the manifest and layouts.

## Notes

The app requests internet, network-state, location, and notification permissions. Links to YouTube, Amazon, and Google Forms are opened outside the WebView. Generated build files and local Android Studio settings are excluded by `.gitignore`.
# Deployment and Setup Guide - NHAI Auth Biometric Gateway

This deployment guide walks you through setting up Android Studio, building, signing, and deploying the **NHAI Auth** secure gateway application.

---

## 1. System Requirements and Prerequisites

Before beginning, ensure your system has:
* **Operating System**: Windows 10/11, macOS, or Linux.
* **Java Development Kit**: JDK 17 or JDK 21 (Eclipse Temurin is recommended).
* **Android Studio**: Android Studio Koala (2024.1.1) or newer.
* **Android SDK**: SDK Platform Level 34 (Android 14) or newer installed.
* **Hardware Device/Emulator**:
  - A physical Android device with Android 10+ (API level 29+) and a working front-facing camera.
  - Or an Android Emulator with camera emulation activated (Webcam redirection) and biometric support.

---

## 2. Model Asset Placement

The application relies on offline TFLite neural network models to run face detection, liveness, and embedding extraction. You must place these models in the assets directory:

> [!IMPORTANT]
> Verify that the files are named exactly as shown and are located under `app/src/main/assets/`:
> 1. **Face Embedding Model**: `mobilefacenet_05x_widened_int8_final.tflite` (6.8 MB)
> 2. **Liveness Validation Model**: `minifasnet_v2_widened_int8_final.tflite` (25 KB)

* Path: [assets directory](file:///c:/Users/kasiv/Downloads/nhai-auth/app/src/main/assets/)

---

## 3. Project Configuration

### A. Environment Configuration (`.env`)
Create a file named `.env` in the root folder of the project. This configures keys and configurations:
```ini
GEMINI_API_KEY=your_gemini_api_key_here
```
Refer to the [.env.example](file:///c:/Users/kasiv/Downloads/nhai-auth/.env.example) in the root.

### B. Local SDK Path Configuration (`local.properties`)
Android Studio automatically creates `local.properties`. If building from the terminal, ensure that the path to your Android SDK is defined:
* On Windows: `sdk.dir=C\:\\Users\\<YourUsername>\\AppData\\Local\\Android\\Sdk`
* On macOS/Linux: `sdk.dir=/Users/<YourUsername>/Library/Android/sdk`

* Path: [local.properties](file:///c:/Users/kasiv/Downloads/nhai-auth/local.properties)

---

## 4. Building and Packaging the App

### A. Importing to Android Studio
1. Launch Android Studio.
2. Click **File > Open** and select the [nhai-auth](file:///c:/Users/kasiv/Downloads/nhai-auth/) root directory.
3. Wait for the Gradle project sync to complete. Android Studio will resolve and download the dependencies listed in [build.gradle.kts](file:///c:/Users/kasiv/Downloads/nhai-auth/app/build.gradle.kts).

### B. Building via Gradle Command Line
From the project root directory, run the following Gradle commands:

* **Verify and Run Unit Tests**:
  ```powershell
  .\gradlew test
  ```
* **Assemble Debug APK**:
  ```powershell
  .\gradlew assembleDebug
  ```
  This creates the debug APK at: `app/build/outputs/apk/debug/app-debug.apk`.
* **Assemble Production Release APK**:
  ```powershell
  .\gradlew assembleRelease
  ```
  This creates the release APK at: `app/build/outputs/apk/release/app-release-unsigned.apk` (which requires manual signing via `apksigner`).

---

## 5. Device Deployment and Launch

### A. Physical Device Configuration
1. On your Android device, go to **Settings > About Phone** and tap **Build Number** 7 times to enable Developer Options.
2. Go to **Settings > Developer Options** and enable **USB Debugging**.
3. Connect the device to your development computer via a USB cable.

### B. Deploying via Android Studio
1. Select your device from the target device drop-down menu in the top toolbar.
2. Click the green **Run** button (or press `Shift + F10`).
3. The app will build, install, and launch on your device.

---

## 6. Troubleshooting Common Deployment Failures

### 1. `ModelNotAvailableException` or `LivenessModelUnavailableException`
* **Symptom**: The app crashes or displays a warning during enrollment prep or camera verification.
* **Resolution**: Double-check that `mobilefacenet_05x_widened_int8_final.tflite` and `minifasnet_v2_widened_int8_final.tflite` are placed under `app/src/main/assets/`. Ensure there are no typos in the file names.

### 2. Biometric Prompt Crash on Emulators
* **Symptom**: Clicking fingerprint authentication freezes the screen or triggers a crash.
* **Resolution**: The app has been patched to handle this. If it still fails, go to the Emulator Extended Controls, choose **Fingerprint**, and enroll a fingerprint profile in the Android Settings before launching the biometric check, or click the **MANUAL SECURE LOGIN** bypass button to log in using standard credentials:
  - **Operator Code**: `772`
  - **Secure Access Key**: `123456`

### 3. Gradle Sync Failures (missing dependency versions)
* **Symptom**: Gradle fails to resolve packages during project import.
* **Resolution**: Go to **File > Invalidate Caches / Restart**, then rebuild. Make sure your network is connected and can access Maven repositories.

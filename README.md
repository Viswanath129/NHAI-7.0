# NHAI Auth - Secure Biometric Edge Gateway

An offline-first, secure biometric gateway for highway infrastructure node operators, utilizing edge AI face recognition and anti-spoofing liveness detection.

---

## 📖 Table of Contents
1. [Introduction](#introduction)
2. [Project Structure](#project-structure)
3. [Key Features](#key-features)
4. [Architecture Overview](#architecture-overview)
5. [TFLite Model Assets Configuration](#tflite-model-assets-configuration)
6. [Quick Deployment Setup](#quick-deployment-setup)
7. [Testing and Verification Manual](#testing-and-verification-manual)
8. [Technical Deep Dives](#technical-deep-dives)
9. [Developer Contribution guidelines](#developer-contribution-guidelines)
10. [License](#license)

---

## 1. Introduction

In critical logistics and national transportation infrastructure, verifying operator identity securely at local highway nodes is essential. **NHAI Auth** implements an offline-first facial authentication gateway on Android edge devices. By deploying local neural networks for face recognition (MobileFaceNet) and texture liveness checking (MiniFASNet), the app remains functional in remote areas with poor or absent network connectivity while securing biometric templates inside encrypted local storage.

---

## 2. Project Structure

The project repository is structured as follows:

* 📂 **Root Directory**:
  - [LICENSE](file:///c:/Users/kasiv/Downloads/nhai-auth/LICENSE) - MIT Open-source License terms.
  - [CONTRIBUTING.md](file:///c:/Users/kasiv/Downloads/nhai-auth/CONTRIBUTING.md) - Collaboration standards, coding conventions, and setup guides.
  - [CHANGELOG.md](file:///c:/Users/kasiv/Downloads/nhai-auth/CHANGELOG.md) - Change tracking from baseline project configuration to the audited release.
  - [build.gradle.kts](file:///c:/Users/kasiv/Downloads/nhai-auth/build.gradle.kts) & [settings.gradle.kts](file:///c:/Users/kasiv/Downloads/nhai-auth/settings.gradle.kts) - Gradle configuration files.
* 📂 **Architecture Documentation**:
  - [Architecture.md](file:///c:/Users/kasiv/Downloads/nhai-auth/architecture/Architecture.md) - Deep-dive explanations of the biometric pipeline and thread security model.
* 📂 **Operational Documentation (`docs/`)**:
  - [PROJECT_REPORT.md](file:///c:/Users/kasiv/Downloads/nhai-auth/docs/PROJECT_REPORT.md) - Complete hackathon report details and development resolutions.
  - [DEPLOYMENT_GUIDE.md](file:///c:/Users/kasiv/Downloads/nhai-auth/docs/DEPLOYMENT_GUIDE.md) - Detailed compilation, packaging, and setup instructions.
  - [TESTING_GUIDE.md](file:///c:/Users/kasiv/Downloads/nhai-auth/docs/TESTING_GUIDE.md) - Step-by-step test matrix for real devices and virtual testing.
* 📂 **Source Code (`app/src/main/`)**:
  - [Assets Folder](file:///c:/Users/kasiv/Downloads/nhai-auth/app/src/main/assets/) - Directory for AI model placement.
  - [MobileFaceNetEngine.kt](file:///c:/Users/kasiv/Downloads/nhai-auth/app/src/main/java/com/example/ai/tflite/MobileFaceNetEngine.kt) - Face embedding model wrapper.
  - [SilentFaceEngine.kt](file:///c:/Users/kasiv/Downloads/nhai-auth/app/src/main/java/com/example/ai/tflite/SilentFaceEngine.kt) - Liveness analysis model wrapper.
  - [FaceAnalyzer.kt](file:///c:/Users/kasiv/Downloads/nhai-auth/app/src/main/java/com/example/camera/FaceAnalyzer.kt) - Thread-safe CameraX frame analyzer.
  - [LoginScreen.kt](file:///c:/Users/kasiv/Downloads/nhai-auth/app/src/main/java/com/example/ui/screens/LoginScreen.kt) - Central login portal with fallback checks.
  - [ScanScreen.kt](file:///c:/Users/kasiv/Downloads/nhai-auth/app/src/main/java/com/example/ui/screens/ScanScreen.kt) - Biometric scan and matching loop.
  - [AppDatabase.kt](file:///c:/Users/kasiv/Downloads/nhai-auth/app/src/main/java/com/example/data/AppDatabase.kt) - Room database configured with SQLCipher.

---

## 3. Key Features

* **Local Machine Learning**: Runs MobileFaceNet and MiniFASNet v2 locally on edge neural network accelerators via TensorFlow Lite hardware delegates.
* **Anti-Spoofing & Liveness Protection**: Real-time evaluation of skin texture, eye-blink sequence tracking, and alignment heuristics prevents unauthorized spoofing entries.
* **SQLCipher Encrypted Database**: All employee profiles, biometric signatures, and access logs are protected locally using SQLCipher with Keystore-derived passphrases.
* **Thread-Safe Camera Analytics**: Uses coroutine dispatching to separate frame analysis and database comparisons, maintaining smooth UI rendering without ANRs.
* **Seamless Biometric Fallbacks**: Integrates fingerprint authorization prompts with manual password entries to prevent lockout conditions.

---

## 4. Architecture Overview

The application utilizes a camera stream processed through Google ML Kit Face Detection. When a face is aligned:
1. A texture crop is sent to `SilentFaceEngine` for spoof verification.
2. A crop is sent to `MobileFaceNetEngine` to calculate a 512-dimensional signature.
3. Similarity indices are compared against stored profiles on background coroutine pools.

```
[Camera Input] ──> [ML Kit Alignment] ──> [Liveness (MiniFASNet)] ──> [Recognition (MobileFaceNet)] ──> [Room Encrypted DB Match]
```
For a detailed sequence trace, see the [Architecture Manual](file:///c:/Users/kasiv/Downloads/nhai-auth/architecture/Architecture.md).

---

## 5. TFLite Model Assets Configuration

To build the application, place the following models under `app/src/main/assets/`:
1. **mobilefacenet_05x_widened_int8_final.tflite** (6.8 MB) - Face recognition engine.
2. **minifasnet_v2_widened_int8_final.tflite** (25 KB) - Anti-spoofing engine.

* Folder Reference: [app/src/main/assets](file:///c:/Users/kasiv/Downloads/nhai-auth/app/src/main/assets/)

---

## 6. Quick Deployment Setup

1. Open the project in Android Studio.
2. Add a `.env` file containing your `GEMINI_API_KEY`.
3. Put the model files into the [assets directory](file:///c:/Users/kasiv/Downloads/nhai-auth/app/src/main/assets/).
4. Rebuild the project in Android Studio.
5. Deploy to your physical device or emulator. For emulator fallback credentials:
   - **Operator Code**: `772`
   - **Secure Access Key**: `123456`

See the [Deployment Guide](file:///c:/Users/kasiv/Downloads/nhai-auth/docs/DEPLOYMENT_GUIDE.md) for step-by-step instructions.

---

## 7. Testing and Verification Manual

Verify all security pipelines using our [Testing Manual](file:///c:/Users/kasiv/Downloads/nhai-auth/docs/TESTING_GUIDE.md). It outlines step-by-step test matrix configurations for:
- Standard agent enrollment
- Secure identification matching
- Spoofing validation checks
- Duplication warning audits

---

## 8. Technical Deep Dives

* **Hackathon Project Details**: [PROJECT_REPORT.md](file:///c:/Users/kasiv/Downloads/nhai-auth/docs/PROJECT_REPORT.md)
* **Android Architecture**: [Architecture.md](file:///c:/Users/kasiv/Downloads/nhai-auth/architecture/Architecture.md)

---

## 9. Developer Contribution Guidelines

Help us improve the biometric secure gateway by reviewing the [Contributing Guidelines](file:///c:/Users/kasiv/Downloads/nhai-auth/CONTRIBUTING.md).

---

## 10. License

This project is licensed under the MIT License. See [LICENSE](file:///c:/Users/kasiv/Downloads/nhai-auth/LICENSE) for details.

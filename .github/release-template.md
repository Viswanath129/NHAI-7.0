# Release v1.0.0 - Production Biometric Gateway

This release marks the secure, offline-first production-ready version of the **NHAI Auth Secure Gateway** app.

## APK & Build Outputs
* **Production Secure APK**: `nhai-auth-v1.0.0-release.apk` (Signed, SQLCipher active)
* **Debug Secure APK**: `nhai-auth-v1.0.0-debug.apk` (Supports local logcat tracing)

> [!IMPORTANT]
> The APK relies on bundled TensorFlow Lite models. If you are rebuilding the app, make sure to place:
> - `mobilefacenet_05x_widened_int8_final.tflite` (6.8 MB)
> - `minifasnet_v2_widened_int8_final.tflite` (25 KB)
> inside the `app/src/main/assets/` directory before building the APK.

## Release Features
1. **Edge AI Facial Recognition**: MobileFaceNet INT8 model integration with dynamic ByteBuffer scaling.
2. **Interactive Liveness Detection**: MiniFASNet v2 texture liveness checks combined with landmark-based blink detection.
3. **Hardware Biometric Fallback**: Seamless integration with Android `BiometricPrompt` for fingerprint fallbacks.
4. **Offline Secure Database**: Room DB encrypted using SQLCipher with KeyStore security constraints.
5. **Thread-Safe Architecture**: Asynchronous image analysis and matching loops offloaded to background threads.

## Installation Instructions
1. Download `nhai-auth-v1.0.0-release.apk`.
2. Enable "Install from Unknown Sources" on your Android device.
3. Open the APK file to install the application.
4. Open the app. For emulator testing, use the manual credentials:
   - **Operator Code**: `772` (or any code $\ge 3$ digits)
   - **Secure Access Key**: `123456`

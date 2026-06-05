# Project Report: NHAI Auth Secure Gateway

An offline-first, edge AI-powered biometric secure gateway designed for highway infrastructure node operators.

---

## 1. Executive Summary

In critical national infrastructure, such as national highway tolling grids and regional monitoring nodes, secure operator identity verification is paramount. Conventional password-based protocols are vulnerable to credential sharing, theft, and social engineering.

**NHAI Auth** resolves these challenges by introducing a localized, secure, biometric gateway on Android edge devices. By deploying local neural networks for face recognition and texture-based liveness verification, the gateway operates without cloud dependencies. This design guarantees resilience against network connectivity dropouts and prevents the interception of biometric templates over public networks.

---

## 2. Core Features & Capabilities

* **Edge AI Facial Identification**: Integrates MobileFaceNet to extract a compact 512-dimensional vector representation of the operator's face, ensuring fast and accurate matches.
* **Dual-Stage Liveness System**:
  - **Landmark-Based Biometrics**: Monitors eye aspect ratios to verify natural blinking.
  - **MiniFASNet Texture Analysis**: Examines skin texture features to identify and reject spoofing attempts (e.g. photos, screens, paper masks).
* **Guided Multi-Pose Enrollment**: Requires the operator to perform structured head turns (front, left, right, up, blink) to collect multi-angle embeddings.
* **SQLCipher Storage Layer**: Protects all operator profiles, logs, and database records using SQLCipher encryption, with cryptographic key management linked to the Android KeyStore.
* **Real-time Logging & Auditability**: Writes cryptographic transaction audit logs for every success, failure, and security alert.

---

## 3. Technology Stack

* **Platform**: Native Android (Kotlin & Jetpack Compose).
* **Inference Library**: TensorFlow Lite (TFLite) with GPU / NNAPI delegate fallback routing via [DelegateManager.kt](file:///c:/Users/kasiv/Downloads/nhai-auth/app/src/main/java/com/example/ai/tflite/DelegateManager.kt).
* **Face Detection**: Google ML Kit Face Detection API.
* **Database**: Room Database with SQLCipher support in [AppDatabase.kt](file:///c:/Users/kasiv/Downloads/nhai-auth/app/src/main/java/com/example/data/AppDatabase.kt).
* **Asynchronous Concurrency**: Kotlin Coroutines and Flow APIs.

---

## 4. Key Engineering Resolutions

During our development and audit, we resolved several critical issues:

### A. TFLite DataType Output Size Mismatch
* **Problem**: Quantized INT8 neural networks sometimes output float arrays. Allocating static-sized output buffers (e.g., 512 bytes for MobileFaceNet) triggers JVM memory buffer size mismatch crashes if the model outputs Float32 values.
* **Resolution**: We rewrote the model loader in [MobileFaceNetEngine.kt](file:///c:/Users/kasiv/Downloads/nhai-auth/app/src/main/java/com/example/ai/tflite/MobileFaceNetEngine.kt) and [SilentFaceEngine.kt](file:///c:/Users/kasiv/Downloads/nhai-auth/app/src/main/java/com/example/ai/tflite/SilentFaceEngine.kt) to dynamically probe the model's datatype at runtime:
  ```kotlin
  val outputTensor = newInterpreter.getOutputTensor(0)
  isOutputFloat = outputTensor.dataType() != org.tensorflow.lite.DataType.INT8
  val size = if (isOutputFloat) dimension * 4 else dimension
  outputBuffer = ByteBuffer.allocateDirect(size)
  ```

### B. Biometric Prompt Emulator Lockout
* **Problem**: Calling standard fingerprint prompts on devices lacking biometric sensors (like standard development emulators) causes freezes or crashes.
* **Resolution**: We added availability checks in [LoginScreen.kt](file:///c:/Users/kasiv/Downloads/nhai-auth/app/src/main/java/com/example/ui/screens/LoginScreen.kt), [AuthScreens.kt](file:///c:/Users/kasiv/Downloads/nhai-auth/app/src/main/java/com/example/ui/screens/AuthScreens.kt), and [ScanScreen.kt](file:///c:/Users/kasiv/Downloads/nhai-auth/app/src/main/java/com/example/ui/screens/ScanScreen.kt). The system now falls back to manual secure credential inputs or screen redirection when biometric hardware is absent.

### C. Concurrency Discard Race in Camera Pipelines
* **Problem**: Disposing a Compose screen shuts down the camera and TFLite engines. However, background frame analysis calls might fire just as the engines close, triggering a Native `SIGSEGV` crash.
* **Resolution**: In [FaceAnalyzer.kt](file:///c:/Users/kasiv/Downloads/nhai-auth/app/src/main/java/com/example/camera/FaceAnalyzer.kt), we introduced a thread-safe lock block. All asynchronous model inferences and engine resource closes are serialized under the same `engineLock` mutex.

### D. Main Thread ANR Blocking
* **Problem**: Sequentially matching a face vector against hundreds of database profiles blocks Jetpack Compose's main loop, triggering Application Not Responding (ANR) warnings.
* **Resolution**: We offloaded database retrieval and similarity calculations to the background using Kotlin Coroutines in [ScanScreen.kt](file:///c:/Users/kasiv/Downloads/nhai-auth/app/src/main/java/com/example/ui/screens/ScanScreen.kt):
  ```kotlin
  coroutineScope.launch(Dispatchers.Default) {
      // Background comparison loop
      withContext(Dispatchers.Main) {
          // Update Compose UI state
      }
  }
  ```

---

## 5. Future Roadmap

1. **Multi-Spectral IR Support**: Utilize infra-red camera sensors to make anti-spoofing/liveness verification bulletproof.
2. **Dynamic Challenge-Response**: Introduce dynamic facial movement prompts (e.g. smile, nod, frown) based on random challenge generators.
3. **Decentralized Audit Log Syncing**: Securely replicate audit logs across nodes using zero-trust syncing mechanisms to guarantee tamper-proof infrastructure logs.

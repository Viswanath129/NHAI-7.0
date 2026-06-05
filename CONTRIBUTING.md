# Contributing to NHAI Auth Secure Gateway

Thank you for your interest in contributing to the **NHAI Auth Secure Gateway**! This project provides offline-first, secure biometric authentication for critical infrastructure operators. 

By contributing, you help make secure identity verification more robust, reliable, and accessible.

---

## Code of Conduct

We expect all contributors to maintain a respectful, welcoming, and professional environment. Please be constructive in your feedback and collaborative in your interactions.

## How Can I Contribute?

### 1. Reporting Bugs
* Check the existing issues or the [CHANGELOG.md](file:///c:/Users/kasiv/Downloads/nhai-auth/CHANGELOG.md) to see if the bug has already been reported or fixed.
* Open an issue with a clear title and description.
* Include steps to reproduce the issue, your hardware environment (emulator, device model), Android OS version, and any relevant logcats or crash logs.

### 2. Requesting Features
* Open a new feature request issue.
* Describe the feature, its utility, and how it aligns with the secure biometric gateway framework.
* Provide mockups or flow diagram suggestions if possible.

### 3. Submitting Code Changes (Pull Requests)
* Fork the repository and create your branch from `main`.
* Keep changes focused. If you want to make multiple unrelated changes, submit them as separate pull requests.
* Ensure code compiles and all unit tests pass before submitting.
* Reference the issue number that your PR resolves in the description.

---

## Coding Standards

### Kotlin & Jetpack Compose
* Follow the official Kotlin coding style guidelines.
* Ensure Compose UI components use descriptive, unique IDs for UI test components.
* Composable functions should be kept small, modular, and performance-minded.
* Offload heavy processing or disk operations to background dispatchers (`Dispatchers.Default` or `Dispatchers.IO`). Do NOT block the Main thread.

### AI/TFLite Engines
* All updates to AI inference engines (such as [MobileFaceNetEngine.kt](file:///c:/Users/kasiv/Downloads/nhai-auth/app/src/main/java/com/example/ai/tflite/MobileFaceNetEngine.kt) or [SilentFaceEngine.kt](file:///c:/Users/kasiv/Downloads/nhai-auth/app/src/main/java/com/example/ai/tflite/SilentFaceEngine.kt)) must check the interpreter status and protect native memory against concurrent disposal races.
* Always query output tensors dynamically for output datatype size to ensure models run properly on both quantized and floating-point setups without throwing memory allocation exceptions.

### Security Guidelines
* Database modifications must support encryption via SQLCipher.
* Use Android KeyStore for managing cryptographic keys. Do not hardcode raw keys or store passwords in plain text.
* Ensure clear text logs do not print biometric embeddings or personal identifiable information (PII).

---

## Development Setup

1. Clone the repository.
2. Open the project in Android Studio.
3. Make sure to download and place the required TFLite model files in the `app/src/main/assets` directory (see the [DEPLOYMENT_GUIDE.md](file:///c:/Users/kasiv/Downloads/nhai-auth/docs/DEPLOYMENT_GUIDE.md)).
4. Run tests with `.\gradlew.bat test`.

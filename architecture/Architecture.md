# Architecture Reference Guide - NHAI Auth Biometric Gateway

This document details the high-performance, secure biometric pipeline of the **NHAI Auth** secure gateway. The system runs offline-first edge AI models for facial identification and liveness check.

---

## 1. System Architecture Overview

NHAI Auth is built upon a layered Android Architecture using Jetpack Compose for the UI layer, Kotlin Coroutines for asynchronous work, Room (encrypted via SQLCipher) for the storage layer, and TensorFlow Lite for edge inference.

```mermaid
graph TD
    A[CameraX PreviewView] -->|ImageProxy Frames| B(FaceAnalyzer)
    B -->|1. ML Kit Face Detection| C{Face Detected?}
    C -->|No| A
    C -->|Yes| D[2. Aligned Pose Check]
    D -->|Invalid Pose| A
    D -->|Valid Alignment| E[3. Liveness Check]
    E -->|Spoof Detected| F[Access Denied Screen]
    E -->|Live & Blink Met| G[4. MobileFaceNet Embedding]
    G -->|Extract FloatArray| H[5. Verification Matcher]
    H -->|Dispatchers.Default Cosine Loop| I[(SQLCipher Encrypted Room DB)]
    I -->|Best Match > 0.60| J[Access Granted Screen]
    I -->|Best Match < 0.60| F
```

---

## 2. Dynamic Sequence Trace

The following sequence diagram details the frame analysis lifecycle within [FaceAnalyzer.kt](file:///c:/Users/kasiv/Downloads/nhai-auth/app/src/main/java/com/example/camera/FaceAnalyzer.kt) and its interactions with the AI engines and database matching loops.

```mermaid
sequenceDiagram
    autonumber
    participant Camera as CameraX Source
    participant Analyzer as FaceAnalyzer
    participant MLKit as ML Kit Face Detector
    participant Liveness as SilentFaceEngine (MiniFASNet)
    participant FaceNet as MobileFaceNetEngine
    participant Database as Encrypted Room DB (SQLCipher)
    
    Camera->>Analyzer: analyze(ImageProxy)
    Note over Analyzer: Guarded by engineLock
    Analyzer->>MLKit: process(InputImage)
    MLKit-->>Analyzer: onSuccess(List<Face>)
    
    rect rgb[Thread-Safe Execution (engineLock)]
        Analyzer->>Analyzer: Check alignment & rotation angles
        Analyzer->>FaceNet: extractEmbedding(Bitmap)
        Note over FaceNet: Run mobilefacenet_05x_widened_int8_final.tflite
        FaceNet-->>Analyzer: FloatArray (512-d)
        
        Analyzer->>Liveness: analyzeLiveness(Bitmap, landmarks)
        Note over Liveness: Run minifasnet_v2_widened_int8_final.tflite
        Liveness-->>Analyzer: LivenessResult (spoofScore)
    end
    
    Analyzer-->>Camera: close ImageProxy & reset throttle
    
    Note over Analyzer: Switch to Dispatchers.Default (Background)
    Analyzer->>Database: Query enrolled profiles
    Database-->>Analyzer: List<EmployeeProfile>
    Note over Analyzer: Compute Cosine Similarity (threshold >= 0.60)
    Analyzer->>Analyzer: Update AuthStep state (SUCCESS/FAILED)
```

---

## 3. Core Pipelines & Implementation Components

### A. AI Inference Layer
1. **Face Recognition Engine**: Implemented in [MobileFaceNetEngine.kt](file:///c:/Users/kasiv/Downloads/nhai-auth/app/src/main/java/com/example/ai/tflite/MobileFaceNetEngine.kt). It processes a cropped $112 \times 112$ RGB face image, normalized and loaded as a direct `ByteBuffer`. It outputs a 512-dimensional embedding vector.
2. **Liveness Validation Engine**: Implemented in [SilentFaceEngine.kt](file:///c:/Users/kasiv/Downloads/nhai-auth/app/src/main/java/com/example/ai/tflite/SilentFaceEngine.kt). It uses a MiniFASNet v2 model that processes a cropped $80 \times 80$ face texture to generate a liveness score (spoof threshold = `0.85f`).
3. **Dynamic Buffer Dimensioning**: To prevent size mismatch crashes between INT8 quantized and Float32 baseline TFLite model variants, both engines dynamically calculate input and output byte arrays during `initialize()` by querying the tensor metadata:
   ```kotlin
   val outputTensor = newInterpreter.getOutputTensor(0)
   isOutputFloat = outputTensor.dataType() != org.tensorflow.lite.DataType.INT8
   val size = if (isOutputFloat) dimension * 4 else dimension
   outputBuffer = ByteBuffer.allocateDirect(size)
   ```

### B. Concurrency & Lifecycle Security
- **Memory Safety and Mutex**: Image processing callbacks from ML Kit and CameraX are asynchronous. When a Jetpack Compose screen is disposed, the local CameraX resources and engines are closed. If an asynchronous frame callback fires after the interpreters are closed, it triggers a native memory segmentation fault (`SIGSEGV`).
- To prevent this, [FaceAnalyzer.kt](file:///c:/Users/kasiv/Downloads/nhai-auth/app/src/main/java/com/example/camera/FaceAnalyzer.kt) wraps the inference calls and the `close()` execution under a synchronized `engineLock` mutex block. If `isClosed` becomes true, frame processing halts immediately.

### C. Threading Model
* **Main Thread Protection**: Neural network similarity calculations and SQLite queries are blocking operations. Performing them on the Main thread causes UI stutter and Android System ANR (Application Not Responding) dialogs.
* **Background Dispatchers**: In [ScanScreen.kt](file:///c:/Users/kasiv/Downloads/nhai-auth/app/src/main/java/com/example/ui/screens/ScanScreen.kt), the similarity verification loops are wrapped in a background coroutine context:
  ```kotlin
  coroutineScope.launch(Dispatchers.Default) {
      val db = AppDatabase.getDatabase(context)
      val profiles = db.employeeDao().getAllProfiles()
      // ... compute cosine similarities ...
      withContext(Dispatchers.Main) {
          // ... update UI state (AuthStep) ...
      }
  }
  ```

### D. Offline Database Security
* **SQLCipher Encryption**: The local SQLite database, managed in [AppDatabase.kt](file:///c:/Users/kasiv/Downloads/nhai-auth/app/src/main/java/com/example/data/AppDatabase.kt), is encrypted transparently using SQLCipher. 
* **Key Derivation**: The database passphrase is dynamically derived at runtime using keys generated in the [DatabaseKeyManager.kt](file:///c:/Users/kasiv/Downloads/nhai-auth/app/src/main/java/com/example/data/security/DatabaseKeyManager.kt) using the hardware-backed Android KeyStore.

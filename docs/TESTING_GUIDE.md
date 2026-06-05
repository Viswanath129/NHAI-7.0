# Testing and Verification Guide - NHAI Auth Biometric Gateway

This testing manual contains step-by-step procedures to verify the biometric pipeline, security parameters, liveness checks, and offline database mechanisms of the **NHAI Auth** secure gateway.

---

## 1. Emulator-Specific Bypass Testing

For testing on standard x86/ARM emulators without physical biometric sensors or front-facing cameras:

1. **Initial Login**:
   - Navigate to the secure portal login.
   - Select **MANUAL SECURE LOGIN**.
   - Input the standard test Operator Code: `772` (or any operator code $\ge 3$ digits) and Secure Access Key: `123456`.
   - Click **AUTHENTICATE MANUALLY**. The app detects the lack of hardware biometric registers and permits secure bypass to the Home Dashboard.
2. **Virtual Camera Redirection**:
   - Go to emulator settings (three dots) > **Camera**.
   - Set the Front Camera option to `VirtualScene` or redirect your laptop/desktop physical webcam. This allows the emulator to run the ML Kit and CameraX feeds.

---

## 2. Test Cases and Validation Matrix

Perform the following test sequences to confirm biometric integrity:

### Test Case 1: First-Time Agent Enrollment
* **Prerequisites**: Clear existing data or use a clean install.
* **Steps**:
  1. Open the application. On the login screen, click **FIRST TIME ENROLLMENT**.
  2. Input enrollment particulars in [EnrollDetailsScreen.kt](file:///c:/Users/kasiv/Downloads/nhai-auth/app/src/main/java/com/example/ui/screens/EnrollDetailsScreen.kt):
     - **Agent ID**: `EMP-2026`
     - **Name**: `John Doe`
     - **Department**: `National Highway Operations`
     - **Role**: `Operator`
  3. Click **CONTINUE TO READINESS**.
  4. The system validates resources on the readiness screen: Front Camera, Face Detection, and neural networks. Click **START ENROLLMENT**.
  5. Position your face in front of the camera. The app guides you through 5 active head gestures:
     - **FRONT**: Look straight. Ensure your face is centered.
     - **LEFT**: Turn head slightly left (Euler Y > 15°).
     - **RIGHT**: Turn head slightly right (Euler Y < -15°).
     - **UPWARD**: Tilt head slightly upward (Euler X > 12°).
     - **BLINK**: Close both eyes slowly (Blink probability < 0.35).
  6. On successful blink, the app automatically transitions to the [EnrollProcessingScreen](file:///c:/Users/kasiv/Downloads/nhai-auth/app/src/main/java/com/example/ui/screens/EnrollmentFlowScreens.kt).
  7. Confirm that the embedding, encryption, liveness, and database saves complete with a green **SUCCESS** indicator.

### Test Case 2: Verification and Identification
* **Prerequisites**: John Doe must be enrolled from Test Case 1.
* **Steps**:
  1. On the login page, select **FACE AUTHENTICATION** to open [ScanScreen.kt](file:///c:/Users/kasiv/Downloads/nhai-auth/app/src/main/java/com/example/ui/screens/ScanScreen.kt).
  2. Position the registered user's face in the camera frame.
  3. Hold steady until alignment is complete, and perform a blink when prompted.
  4. The system runs matching against the database on a background thread.
  5. Check that the UI updates to show **Welcome, John Doe** and navigates to the Home Dashboard.

### Test Case 3: Anti-Spoofing / Liveness Check
* **Goal**: Confirm that 2D static photos or digital screens are rejected.
* **Steps**:
  1. Navigate to **FACE AUTHENTICATION** on the login page.
  2. Present a high-resolution printout photo or digital display image of John Doe's face to the camera.
  3. Try to complete the alignment and verify if the liveness system blocks the attempt or if it fails the verification matching due to static texturing.
  4. Verify that the app displays **ACCESS DENIED** or holds in the validation loop without authorizing.

### Test Case 4: Multiple User and Duplicate Biometric Check
* **Goal**: Validate that one person cannot enroll under two separate IDs.
* **Steps**:
  1. Complete Enrollment for John Doe (Agent ID: `EMP-2026`).
  2. Click **FIRST TIME ENROLLMENT** again.
  3. Input details for a new Agent: ID `EMP-9999`, Name `Jane Smith`.
  4. Scan John Doe's face during the active gesture screen.
  5. Check that the processing screen fails during **Saving Agent Profile** and reports **Duplicate Biometrics Detected. Agent already enrolled.**

---

## 3. Reviewing System Logs

For auditing the biometric pipelines, look at the Logcat console in Android Studio. Filter by the tag `MobileFaceNet` or `SilentFace` to watch trace checkpoints:

* `VERIFY_STEP_1`: Start facial embedding extraction.
* `VERIFY_STEP_2`: ExtractEmbedding successful (512 float values).
* `VERIFY_STEP_3`: Starting liveness checking.
* `VERIFY_STEP_4`: Liveness evaluation complete.
* `VERIFY_STEP_ERROR`: Logs failures, including null bitmaps or network execution glitches.

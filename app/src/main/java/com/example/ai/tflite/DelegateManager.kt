package com.example.ai.tflite

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.nnapi.NnApiDelegate

object DelegateManager {
    private const val TAG = "DelegateManager"

    fun createInterpreterOptions(context: Context, useNnApi: Boolean = true): Interpreter.Options {
        val options = Interpreter.Options()
        
        try {
            if (useNnApi) {
                // Priority 1: NNAPI (Hardware acceleration for NPU/DSP)
                val nnApiDelegate = NnApiDelegate()
                options.addDelegate(nnApiDelegate)
                Log.d(TAG, "NNAPI Delegate added.")
                return options
            }
        } catch (e: Exception) {
            Log.w(TAG, "NNAPI failed to initialize. Falling back to GPU.", e)
        }

        try {
            // Priority 2: GPU
            val compatList = CompatibilityList()
            if (compatList.isDelegateSupportedOnThisDevice) {
                val gpuOptions = GpuDelegate.Options()
                gpuOptions.setQuantizedModelsAllowed(true)
                val gpuDelegate = GpuDelegate(gpuOptions)
                options.addDelegate(gpuDelegate)
                Log.d(TAG, "GPU Delegate added.")
                return options
            }
        } catch (e: Exception) {
            Log.w(TAG, "GPU Delegate failed to initialize. Falling back to CPU.", e)
        }

        // Priority 3: CPU (Fallback)
        Log.d(TAG, "Using CPU fallback with 4 threads.")
        options.setNumThreads(4)
        return options
    }
}

package com.example.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.NHAIApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val appContainer = (applicationContext as NHAIApplication).container
            val auditLogDao = appContainer.database.auditLogDao()
            
            // 1. Fetch encrypted logs/records from DB
            val unsyncedLogs = auditLogDao.getUnsyncedLogs()
            if (unsyncedLogs.isEmpty()) {
                return@withContext Result.success()
            }

            // 2. Upload to central NHAI server (simulated external network call)
            // e.g. apiService.syncLogs(unsyncedLogs)
            
            // 3. Mark as synced in DB
            val ids = unsyncedLogs.map { it.id }
            auditLogDao.markAsSynced(ids)

            Result.success()
        } catch (e: Exception) {
            // If offline, return Retry
            Result.retry()
        }
    }
}

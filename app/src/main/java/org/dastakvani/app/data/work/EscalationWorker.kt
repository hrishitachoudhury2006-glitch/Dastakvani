package org.dastakvani.app.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.dastakvani.app.data.local.ComplaintRepository

class EscalationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Check for complaints exceeding 15 days without action and escalate to RTI
            val escalatedCount = ComplaintRepository.runEscalationCheck()
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}

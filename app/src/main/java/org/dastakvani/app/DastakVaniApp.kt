package org.dastakvani.app

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import org.dastakvani.app.data.local.ComplaintRepository
import org.dastakvani.app.data.ml.GrievanceClassifier
import org.dastakvani.app.data.work.EscalationWorker
import java.util.concurrent.TimeUnit

class DastakVaniApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // 1. Initialize local persistent repository
        ComplaintRepository.initialize(this)

        // 2. Initialize on-device machine learning classifier
        GrievanceClassifier.initialize(this)

        // 3. Schedule periodic WorkManager for escalation & legal RTI monitoring
        scheduleEscalationMonitoring()
    }

    private fun scheduleEscalationMonitoring() {
        try {
            val escalationRequest = PeriodicWorkRequestBuilder<EscalationWorker>(
                repeatInterval = 12,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            ).build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "dastak_vani_escalation_worker",
                ExistingPeriodicWorkPolicy.KEEP,
                escalationRequest
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

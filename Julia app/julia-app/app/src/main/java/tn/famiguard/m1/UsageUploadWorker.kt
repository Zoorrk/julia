package tn.famiguard.m1

import android.content.Context
import android.os.Build
import androidx.work.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/** Envoie périodiquement le temps d'écran de l'enfant vers Firebase. */
class UsageUploadWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val code = Prefs.familyCode(applicationContext) ?: return Result.success()
        if (!UsageRepository.hasUsageAccess(applicationContext)) return Result.retry()
        val list = UsageRepository.todayUsage(applicationContext)
        val apps = list.take(30).map {
            mapOf<String, Any>("pkg" to it.packageName, "label" to it.label, "ms" to it.millisToday)
        }
        val total = list.sumOf { it.millisToday }
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        Fb.uploadUsage(code, date, total, apps, "${Build.MANUFACTURER} ${Build.MODEL}")
        return Result.success()
    }

    companion object {
        fun schedule(ctx: Context) {
            val req = PeriodicWorkRequestBuilder<UsageUploadWorker>(15, TimeUnit.MINUTES)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build()
            WorkManager.getInstance(ctx).enqueueUniquePeriodicWork("usage-upload", ExistingPeriodicWorkPolicy.UPDATE, req)
        }
        fun runNow(ctx: Context) {
            val req = OneTimeWorkRequestBuilder<UsageUploadWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build()
            WorkManager.getInstance(ctx).enqueue(req)
        }
    }
}

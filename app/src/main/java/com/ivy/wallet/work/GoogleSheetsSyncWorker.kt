package com.ivy.wallet.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class GoogleSheetsSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val httpClient: HttpClient 
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val action = inputData.getString("action") ?: return Result.failure()
        val transactionId = inputData.getString("id") ?: return Result.failure()
        
        val date = inputData.getString("date") ?: ""
        val amount = inputData.getDouble("amount", 0.0)
        val category = inputData.getString("category") ?: ""
        val type = inputData.getString("type") ?: ""
        val description = inputData.getString("description") ?: ""

        // Paste your deployed script url token here
        val url = "https://script.google.com/macros/s/AKfycbzsA509JYB7H905Ub47Gww93k_wjRtwh3WBWgMLt7nwkSafHunoRl_u3riNWTFdtjC7iA/exec"

        return try {
            httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("id", transactionId)
                    put("action", action)
                    put("date", date)
                    put("amount", amount)
                    put("category", category)
                    put("type", type)
                    put("description", description)
                })
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            // Tell WorkManager to back off and try again when the network recovers
            Result.retry()
        }
    }
}
package com.ptzcontroller

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Utility class for checking for app updates from GitHub releases
 */
class UpdateChecker(
    private val activity: Activity,
    private val currentVersion: String,
    private val repositoryOwner: String,
    private val repositoryName: String
) {
    companion object {
        private const val TAG = "UpdateChecker"
    }
    
    /**
     * Check for updates and show a dialog if a newer version is available
     */
    fun checkForUpdates() {
        UpdateCheckTask().execute()
    }
    
    /**
     * AsyncTask to check for updates in the background
     */
    private inner class UpdateCheckTask : AsyncTask<Void, Void, String?>() {
        override fun doInBackground(vararg params: Void): String? {
            try {
                // Get latest release info from GitHub API
                val apiUrl = "https://api.github.com/repos/$repositoryOwner/$repositoryName/releases/latest"
                val url = URL(apiUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()
                    return response.toString()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking for updates", e)
            }
            return null
        }
        
        override fun onPostExecute(result: String?) {
            result?.let {
                try {
                    val json = JSONObject(it)
                    val latestVersion = json.getString("tag_name").removePrefix("v")
                    val releaseNotes = json.getString("body")
                    val downloadUrl = json.getString("html_url")

                    if (isNewerVersion(latestVersion)) {
                        showUpdateDialog(latestVersion, releaseNotes, downloadUrl)
                    } else {
                        // No update needed
                        Log.d(TAG, "App is up to date (current: $currentVersion, latest: $latestVersion)")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing update response", e)
                }
            }
        }
    }
    
    /**
     * Compare version strings to check if the latest is newer than current
     */
    private fun isNewerVersion(latestVersion: String): Boolean {
        try {
            val current = currentVersion.split(".").map { it.toInt() }
            val latest = latestVersion.split(".").map { it.toInt() }

            for (i in 0 until minOf(current.size, latest.size)) {
                if (latest[i] > current[i]) {
                    return true
                }
                if (latest[i] < current[i]) {
                    return false
                }
            }
            
            return latest.size > current.size
        } catch (e: Exception) {
            Log.e(TAG, "Error comparing versions", e)
            return false
        }
    }
    
    /**
     * Show an update dialog with release notes
     */
    private fun showUpdateDialog(newVersion: String, releaseNotes: String, downloadUrl: String) {
        val dialog = AlertDialog.Builder(activity)
            .setTitle("Update Available")
            .setMessage(
                "A new version ($newVersion) is available. Would you like to update?\n\n" +
                "What's new:\n$releaseNotes"
            )
            .setPositiveButton("Update Now") { _, _ ->
                // Open browser to download page
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                activity.startActivity(intent)
            }
            .setNegativeButton("Later", null)
            .create()
        
        dialog.show()
    }
}
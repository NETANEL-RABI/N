package com.offlinestore.app

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val apps = mutableListOf<AppInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerApps)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadAppsFromAssets()
    }

    private fun loadAppsFromAssets() {
        apps.clear()
        val fileList = try {
            assets.list("apks") ?: emptyArray()
        } catch (e: Exception) {
            emptyArray()
        }

        val pm = packageManager
        for (fileName in fileList) {
            if (!fileName.endsWith(".apk", ignoreCase = true)) continue

            val tempFile = copyAssetToCache(fileName) ?: continue
            val packageInfo = pm.getPackageArchiveInfo(tempFile.absolutePath, PackageManager.GET_ACTIVITIES)

            if (packageInfo != null) {
                packageInfo.applicationInfo.sourceDir = tempFile.absolutePath
                packageInfo.applicationInfo.publicSourceDir = tempFile.absolutePath

                val label = pm.getApplicationLabel(packageInfo.applicationInfo).toString()
                val icon = try {
                    pm.getApplicationIcon(packageInfo.applicationInfo)
                } catch (e: Exception) {
                    null
                }

                apps.add(
                    AppInfo(
                        assetFileName = fileName,
                        label = label,
                        packageName = packageInfo.packageName,
                        versionName = packageInfo.versionName,
                        icon = icon
                    )
                )
            }
        }

        recyclerView.adapter = AppListAdapter(apps) { app -> confirmInstall(app) }

        if (apps.isEmpty()) {
            Toast.makeText(this, "לא נמצאו קבצי APK בתיקיית assets/apks", Toast.LENGTH_LONG).show()
        }
    }

    private fun copyAssetToCache(fileName: String): File? {
        return try {
            val outDir = File(cacheDir, "apks").apply { mkdirs() }
            val outFile = File(outDir, fileName)
            assets.open("apks/$fileName").use { input ->
                FileOutputStream(outFile).use { output ->
                    input.copyTo(output)
                }
            }
            outFile
        } catch (e: Exception) {
            null
        }
    }

    private fun confirmInstall(app: AppInfo) {
        AlertDialog.Builder(this)
            .setTitle(app.label)
            .setMessage("גרסה: ${app.versionName ?: "לא ידוע"}\nPackage: ${app.packageName}\n\nלהתקין את האפליקציה הזו?")
            .setPositiveButton("התקן") { _, _ -> installApp(app) }
            .setNegativeButton("ביטול", null)
            .show()
    }

    private fun installApp(app: AppInfo) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!packageManager.canRequestPackageInstalls()) {
                Toast.makeText(
                    this,
                    "יש לאשר הרשאת 'התקנת אפליקציות לא ידועות' למוצר הזה ולנסות שוב",
                    Toast.LENGTH_LONG
                ).show()
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
                return
            }
        }

        val apkFile = File(File(cacheDir, "apks"), app.assetFileName)
        val apkUri: Uri = FileProvider.getUriForFile(
            this,
            "$packageName.fileprovider",
            apkFile
        )

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        startActivity(installIntent)
    }
}

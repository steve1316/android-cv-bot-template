package com.example.cv_bot_template.utils

import android.content.Context
import android.os.Build
import android.util.Log
import com.example.cv_bot_template.MainActivity.loggerTag
import com.example.cv_bot_template.StartModule
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * This class is in charge of holding the Message Log to which all logging messages from the bot goes to and also saves it all into a file when the bot has finished.
 */
class MessageLog {
	companion object {
		private const val TAG: String = "${loggerTag}MessageLog"
		var messageLog = arrayListOf<String>()

		var saveCheck = false

		/**
		 * Save the current Message Log into a new file inside internal storage's /logs/ folder.
		 *
		 * @param context The context for the application.
		 */
		fun saveLogToFile(context: Context) {
			cleanLogsFolder(context)

			if (!saveCheck) {
				Log.d(TAG, "Now beginning process to save current Message Log to internal storage...")

				// Generate file path to save to. All message logs will be saved to the /logs/ folder inside internal storage. Create the /logs/ folder if needed.
				val path = File(context.getExternalFilesDir(null)?.absolutePath + "/logs/")
				if (!path.exists()) {
					path.mkdirs()
				}

				// Generate the file name.
				val fileName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					val current = LocalDateTime.now()
					val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
					"log @ ${current.format(formatter)}"
				} else {
					val current = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
					val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
					"log @ ${current.format(sdf)}"
				}

				// Now save the Message Log to the new text file.
				Log.d(TAG, "Now saving Message Log to file named \"$fileName\" at $path")
				messageLog.add("\nNow saving Message Log to file named \"$fileName\" at $path")

				// Send a event to the React Native frontend.
				StartModule.sendEvent("MessageLog", "Now saving Message Log to file named \"$fileName\" at $path")

				val file = File(path, "$fileName.txt")

				if (!file.exists()) {
					file.createNewFile()
					file.printWriter().use { out ->
						messageLog.forEach {
							out.println(it)
						}
					}

					saveCheck = true
				}
			}
		}

		/**
		 * Clean up the logs folder if the amount of logs inside is greater than the specified amount.
		 *
		 * @param context The context for the application.
		 */
		private fun cleanLogsFolder(context: Context) {
			val directory = File(context.getExternalFilesDir(null)?.absolutePath + "/logs/")

			// Delete all logs if the amount inside is greater than 50.
			val files = directory.listFiles()
			if (files != null && files.size > 50) {
				files.forEach { file ->
					file.delete()
				}
			}
		}
	}
}
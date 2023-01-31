package com.example.cv_bot_template.bot

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import com.example.cv_bot_template.MainActivity.loggerTag
import com.example.cv_bot_template.data.ConfigData
import com.steve1316.automation_library.data.SharedData
import com.steve1316.automation_library.utils.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.opencv.core.Point
import com.steve1316.automation_library.utils.MediaProjectionService as MPS

/**
 * Main driver for bot activity and navigation.
 */
class EntryPoint(private val myContext: Context) {
	private val tag: String = "${loggerTag}Game"

	val configData: ConfigData = ConfigData(myContext)
	val imageUtils: ImageUtils = ImageUtils(myContext)
	val gestureUtils: MyAccessibilityService = MyAccessibilityService.getInstance()

	/**
	 * Wait the specified seconds to account for ping or loading.
	 *
	 * @param seconds Number of seconds to pause execution.
	 */
	fun wait(seconds: Double) {
		runBlocking {
			delay((seconds * 1000).toLong())
		}
	}

	/**
	 * Finds and presses the image's location.
	 *
	 * @param imageName Name of the button image file.
	 * @param tries Number of tries to find the specified image. Defaults to 0 which will use ImageUtil's default.
	 * @param suppressError Whether or not to suppress saving error messages to the log in failing to find the image.
	 * @return True if the image was found and clicked. False otherwise.
	 */
	fun findAndPress(imageName: String, tries: Int = 0, suppressError: Boolean = false): Boolean {
		if (configData.debugMode) {
			MessageLog.printToLog("[DEBUG] Now attempting to find and click the \"$imageName\" button.", tag)
		}

		val tempLocation: Point? = imageUtils.findImage(imageName, tries = tries, suppressError = suppressError)
		return if (tempLocation != null) {
			if (configData.enableDelayTap) {
				val newDelay: Double = ((configData.delayTapMilliseconds - 100)..(configData.delayTapMilliseconds + 100)).random().toDouble() / 1000
				if (configData.debugMode) MessageLog.printToLog("[DEBUG] Adding an additional delay of ${newDelay}s...", tag)
				wait(newDelay)
			}
			gestureUtils.tap(tempLocation.x, tempLocation.y, imageName)
			wait(1.0)
			true
		} else {
			false
		}
	}

	/**
	 * Check rotation of the Virtual Display and if it is stuck in Portrait Mode, destroy and remake it.
	 *
	 */
	private fun landscapeCheck() {
		if (SharedData.displayHeight > SharedData.displayWidth) {
			Log.d(tag, "Virtual Display is not correct. Recreating it now...")
			MPS.forceGenerateVirtualDisplay(myContext)
		} else {
			Log.d(tag, "Skipping recreation of Virtual Display as it is correct.")
		}
	}

	/**
	 * Bot will begin automation here.
	 *
	 * @return True if all automation goals have been met. False otherwise.
	 */
	fun start(): Boolean {
		val startTime: Long = System.currentTimeMillis()

		if (configData.debugMode) {
			MessageLog.printToLog("\n[DEBUG] I am starting here but as a debugging message!", tag)
		} else {
			MessageLog.printToLog("\n[INFO] I am starting here!", tag)
		}

		landscapeCheck()

		wait(0.5)

		MessageLog.printToLog("[INFO] Device dimensions: ${SharedData.displayHeight}x${SharedData.displayWidth}\n", tag)

		gestureUtils.tap(550.0, 2060.0)

		MessageLog.printToLog("\n[INFO] I am ending here!", tag)

		val endTime: Long = System.currentTimeMillis()
		val runTime: Long = endTime - startTime
		MessageLog.printToLog("\nTotal Runtime: ${runTime}ms", tag)

		val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(myContext)
		if (sharedPreferences.getBoolean("enableDiscordNotifications", false)) {
			wait(1.0)
			DiscordUtils.queue.add("Total Runtime: ${runTime}ms")
			wait(1.0)
		}

		return true
	}
}
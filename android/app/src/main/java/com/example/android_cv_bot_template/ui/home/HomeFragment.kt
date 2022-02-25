package com.example.android_cv_bot_template.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.example.android_cv_bot_template.MainActivity
import com.example.android_cv_bot_template.R
import com.example.android_cv_bot_template.data.ConfigData
import com.example.android_cv_bot_template.utils.JSONParser
import com.example.android_cv_bot_template.utils.MediaProjectionService
import com.example.android_cv_bot_template.utils.MessageLog
import com.example.android_cv_bot_template.utils.MyAccessibilityService
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.UpdateFrom
import java.io.File

class HomeFragment : Fragment() {
	private val loggerTag: String = "[${MainActivity.loggerTag}]HomeFragment"
	private val SCREENSHOT_PERMISSION_REQUEST_CODE: Int = 100
	private var firstBoot = false
	
	private lateinit var myContext: Context
	private lateinit var homeFragmentView: View
	private lateinit var startButton: Button
	
	@SuppressLint("SetTextI18n")
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		myContext = requireContext()
		
		homeFragmentView = inflater.inflate(R.layout.fragment_home, container, false)
		
		val jsonParser = JSONParser(myContext)
		
		// Start or stop the MediaProjection service via this button.
		startButton = homeFragmentView.findViewById(R.id.start_button)
		startButton.setOnClickListener {
			val readyCheck = startReadyCheck()
			if (readyCheck && !MediaProjectionService.isRunning) {
				startProjection()
				startButton.text = getString(R.string.stop)
				
				// This is needed because onResume() is immediately called right after accepting the MediaProjection and it has not been properly
				// initialized yet so it would cause the button's text to revert back to "Start".
				firstBoot = true
			} else if (MediaProjectionService.isRunning) {
				stopProjection()
				startButton.text = getString(R.string.start)
			}
		}
		
		// Check if the application created the config.json file yet and if not, create it.
		val file = File(myContext.getExternalFilesDir(null), "config.json")
		if (!file.exists()) {
			file.createNewFile()
			
			val content = "{\n" +
					"    \"discord\": {\n" +
					"        \"discordToken\": \"\",\n" +
					"        \"userID\": \"\"\n" +
					"    },\n" +
					"    \"twitter\": {\n" +
					"        \"apiKey\": \"\",\n" +
					"        \"apiKeySecret\": \"\",\n" +
					"        \"accessToken\": \"\",\n" +
					"        \"accessTokenSecret\": \"\"\n" +
					"    }\n" +
					"}\n"
			
			file.writeText(content)
			
			Log.d(loggerTag, "Created config.json in internal storage.")
		} else {
			Log.d(loggerTag, "config.json already exists.")
			
			val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
			
			// Save the Twitter API keys and tokens and every other settings in the config.json to SharedPreferences.
			try {
				if (file.exists()) {
					jsonParser.constructConfigClass()
					
					sharedPreferences.edit {
						putString("discordToken", ConfigData.discordToken)
						putString("userID", ConfigData.userID)
						commit()
					}
					
					Log.d(loggerTag, "Saved config.json settings to SharedPreferences.")
				}
			} catch (e: Exception) {
				Log.e(loggerTag, "Encountered error while saving Twitter API credentials to SharedPreferences from config: ${e.stackTraceToString()}")
				Log.e(loggerTag, "Clearing any existing Twitter API credentials from SharedPreferences...")
				
				sharedPreferences.edit {
					remove("apiKey")
					remove("apiKeySecret")
					remove("accessToken")
					remove("accessTokenSecret")
					commit()
				}
			}
		}
		
		////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////////////
		
		val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
		val item = sharedPreferences.getString("item", "")
		val value = sharedPreferences.getInt("value", 1)
		val savedOptions = sharedPreferences.getString("savedOptions", "")?.split("|")
		val nestedSettings = sharedPreferences.getBoolean("nestedSettings", false)
		val confidence: Int = sharedPreferences.getInt("confidence", 80)
		val confidenceAll: Int = sharedPreferences.getInt("confidenceAll", 80)
		val customScale: Double = sharedPreferences.getString("customScale", "1.0")!!.toDouble()
		val enableDiscord: Boolean = sharedPreferences.getBoolean("enableDiscord", false)
		val debugMode = sharedPreferences.getBoolean("debugMode", false)
		
		////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////////////
		// Now construct the strings to print them.
		
		val nestedSettingsString: String = if (nestedSettings) {
			"Checked"
		} else {
			"Not Checked"
		}
		
		val customScaleString: String = if (customScale == 1.0) {
			"1.0 (Default)"
		} else {
			"$customScale"
		}
		
		val enableDiscordString: String = if (enableDiscord) {
			"Enabled"
		} else {
			"Disabled"
		}
		
		val debugModeString: String = if (debugMode) {
			"Enabled"
		} else {
			"Disabled"
		}
		
		////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////////////
		// Update the TextView here based on the information of the SharedPreferences.
		
		val settingsStatusTextView: TextView = homeFragmentView.findViewById(R.id.settings_status)
		settingsStatusTextView.setTextColor(Color.WHITE)
		settingsStatusTextView.text = "---------- Category 1 ----------\n" +
				"Example Picker 1: $item\n" +
				"Example SeekBar: $value\n" +
				"Saved Option(s): $savedOptions\n" +
				"Nested Preference: $nestedSettingsString\n" +
				"---------- Category 2 ----------\n" +
				"Confidence for Single Image Matching: $confidence%\n" +
				"Confidence for Multiple Image Matching: $confidenceAll%\n" +
				"Scale: $customScaleString\n" +
				"Discord Notifications: $enableDiscordString\n" +
				"Debug Mode: $debugModeString"
		
		// Enable the start button if the required settings have been set.
		startButton.isEnabled = (item != null && item.isNotEmpty())
		
		return homeFragmentView
	}
	
	override fun onResume() {
		super.onResume()
		
		// Update the button's text depending on if the MediaProjection service is running.
		if (!firstBoot) {
			if (MediaProjectionService.isRunning) {
				startButton.text = getString(R.string.stop)
			} else {
				startButton.text = getString(R.string.start)
			}
		}
		
		// Setting this false here will ensure that stopping the MediaProjection Service outside of this application will update this button's text.
		firstBoot = false
		
		// Now update the Message Log inside the ScrollView with the latest logging messages from the bot.
		Log.d(loggerTag, "Now updating the Message Log TextView...")
		val messageLogTextView = homeFragmentView.findViewById<TextView>(R.id.message_log)
		messageLogTextView.text = ""
		var index = 0
		
		// Get local copies of the message log.
		val messageLog = MessageLog.messageLog
		val messageLogSize = MessageLog.messageLog.size
		while (index < messageLogSize) {
			messageLogTextView.append("\n" + messageLog[index])
			index += 1
		}
		
		////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////////////
		// Set up the app updater to check for the latest update from GitHub. Point it to your update.xml.
		AppUpdater(myContext)
			.setUpdateFrom(UpdateFrom.XML)
			.setUpdateXML("https://raw.githubusercontent.com/steve1316/android-cv-bot-template/master/app/update.xml")
			.start()
	}
	
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		if (requestCode == SCREENSHOT_PERMISSION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
			// Start up the MediaProjection service after the user accepts the onscreen prompt.
			myContext.startService(data?.let { MediaProjectionService.getStartIntent(myContext, resultCode, data) })
		}
	}
	
	/**
	 * Checks to see if the application is ready to start.
	 *
	 * @return True if the application has overlay permission and has enabled the Accessibility Service for it. Otherwise, return False.
	 */
	private fun startReadyCheck(): Boolean {
		if (!checkForOverlayPermission() || !checkForAccessibilityPermission()) {
			return false
		}
		
		return true
	}
	
	/**
	 * Starts the MediaProjection Service.
	 */
	private fun startProjection() {
		val mediaProjectionManager = context?.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
		startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), SCREENSHOT_PERMISSION_REQUEST_CODE)
	}
	
	/**
	 * Stops the MediaProjection Service.
	 */
	private fun stopProjection() {
		context?.startService(MediaProjectionService.getStopIntent(requireContext()))
	}
	
	/**
	 * Checks if the application has permission to draw overlays. If not, it will direct the user to enable it.
	 *
	 * Source is from https://github.com/Fate-Grand-Automata/FGA/blob/master/app/src/main/java/com/mathewsachin/fategrandautomata/ui/MainFragment.kt
	 *
	 * @return True if it has permission. False otherwise.
	 */
	private fun checkForOverlayPermission(): Boolean {
		if (!Settings.canDrawOverlays(requireContext())) {
			Log.d(loggerTag, "Application is missing overlay permission.")
			
			AlertDialog.Builder(requireContext()).apply {
				setTitle(R.string.overlay_disabled)
				setMessage(R.string.overlay_disabled_message)
				setPositiveButton(R.string.go_to_settings) { _, _ ->
					// Send the user to the Overlay Settings.
					val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${requireContext().packageName}"))
					startActivity(intent)
				}
				setNegativeButton(android.R.string.cancel, null)
			}.show()
			
			return false
		}
		
		Log.d(loggerTag, "Application has permission to draw overlay.")
		return true
	}
	
	/**
	 * Checks if the Accessibility Service for this application is enabled. If not, it will direct the user to enable it.
	 *
	 * Source is from https://stackoverflow.com/questions/18094982/detect-if-my-accessibility-service-is-enabled/18095283#18095283
	 *
	 * @return True if it is enabled. False otherwise.
	 */
	private fun checkForAccessibilityPermission(): Boolean {
		val prefString = Settings.Secure.getString(myContext.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
		
		if (prefString != null && prefString.isNotEmpty()) {
			// Check the string of enabled accessibility services to see if this application's accessibility service is there.
			val enabled = prefString.contains(myContext.packageName.toString() + "/" + MyAccessibilityService::class.java.name)
			
			if (enabled) {
				Log.d(loggerTag, "This application's Accessibility Service is currently turned on.")
				return true
			}
		}
		
		// Moves the user to the Accessibility Settings if the service is not detected.
		AlertDialog.Builder(myContext).apply {
			setTitle(R.string.accessibility_disabled)
			setMessage(R.string.accessibility_disabled_message)
			setPositiveButton(R.string.go_to_settings) { _, _ ->
				Log.d(loggerTag, "Accessibility Service is not detected. Moving user to Accessibility Settings.")
				val accessibilitySettingsIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
				myContext.startActivity(accessibilitySettingsIntent)
			}
			setNegativeButton(android.R.string.cancel, null)
			show()
		}
		
		return false
	}
}

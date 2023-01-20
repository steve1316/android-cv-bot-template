package com.example.cv_bot_template.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.example.cv_bot_template.MainActivity
import com.steve1316.automation_library.utils.JSONParser
import org.json.JSONObject
import java.io.File

/**
 * Custom JSONParser implementation to suit whatever settings the developer needs to pull from the settings.json file.
 *
 * Available helper methods are toStringArrayList() and toIntArrayList().
 *
 */
class CustomJSONParser : JSONParser() {
	private val tag = "${MainActivity.loggerTag}CustomJSONParser"

	override fun initializeSettings(myContext: Context) {
		Log.d(tag, "Loading settings from JSON file to SharedPreferences...")

		// Grab the JSON object from the file.
		val jString = File(myContext.getExternalFilesDir(null), "settings.json").bufferedReader().use { it.readText() }
		val jObj = JSONObject(jString)

		//////////////////////////////////////////////////////////////////////////
		//////////////////////////////////////////////////////////////////////////

		// Here you can parse out each property from the JSONObject via key iteration. You can create a static class
		// elsewhere to hold the JSON data. Or you can save them all into SharedPreferences.

		val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(myContext)

		try {
			val twitterObj = jObj.getJSONObject("twitter")
			sharedPreferences.edit {
				putString("twitterAPIKey", twitterObj.getString("twitterAPIKey"))
				putString("twitterAPIKeySecret", twitterObj.getString("twitterAPIKeySecret"))
				putString("twitterAccessToken", twitterObj.getString("twitterAccessToken"))
				putString("twitterAccessTokenSecret", twitterObj.getString("twitterAccessTokenSecret"))
				commit()
			}
		} catch (_: Exception) {
		}

		try {
			val discordObj = jObj.getJSONObject("discord")
			sharedPreferences.edit {
				putBoolean("enableDiscordNotifications", discordObj.getBoolean("enableDiscordNotifications"))
				putString("discordToken", discordObj.getString("discordToken"))
				putString("discordUserID", discordObj.getString("discordUserID"))
				commit()
			}
		} catch (_: Exception) {
		}

		try {
			val androidObj = jObj.getJSONObject("android")
			sharedPreferences.edit {
				putBoolean("enableDelayTap", androidObj.getBoolean("enableDelayTap"))
				putInt("delayTapMilliseconds", androidObj.getInt("delayTapMilliseconds"))
				putFloat("confidence", androidObj.getDouble("confidence").toFloat())
				putFloat("confidenceAll", androidObj.getDouble("confidenceAll").toFloat())
				putFloat("customScale", androidObj.getDouble("customScale").toFloat())
				putBoolean("enableTestForHomeScreen", androidObj.getBoolean("enableTestForHomeScreen"))
				commit()
			}
		} catch (_: Exception) {
		}

		//////////////////////////////////////////////////////////////////////////
		//////////////////////////////////////////////////////////////////////////

		Log.d(tag, "Finished loading settings from JSON file to SharedPreferences.")
	}
}
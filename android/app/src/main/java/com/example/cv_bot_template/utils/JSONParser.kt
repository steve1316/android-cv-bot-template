package com.example.cv_bot_template.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.example.cv_bot_template.MainActivity.loggerTag
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class JSONParser {
	/**
	 * Initialize settings into SharedPreferences from the JSON file.
	 *
	 * @param myContext The application context.
	 */
	fun initializeSettings(myContext: Context) {
		Log.d(loggerTag, "Loading settings from JSON file to SharedPreferences...")

		// Grab the JSON object from the file.
		val jString = File(myContext.getExternalFilesDir(null), "settings.json").bufferedReader().use { it.readText() }
		val jObj = JSONObject(jString)

		//////////////////////////////////////////////////////////////////////////
		//////////////////////////////////////////////////////////////////////////
		// Manually save all key-value pairs from JSON object to SharedPreferences.
		//
		// Add more try-catch blocks to cover each JSONArray object as you need.

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
		} catch (e: Exception) {
		}

		try {
			val discordObj = jObj.getJSONObject("discord")
			sharedPreferences.edit {
				putBoolean("enableDiscordNotifications", discordObj.getBoolean("enableDiscordNotifications"))
				putString("discordToken", discordObj.getString("discordToken"))
				putString("discordUserID", discordObj.getString("discordUserID"))
				commit()
			}
		} catch (e: Exception) {
		}
	}

	/**
	 * Convert JSONArray to ArrayList object.
	 *
	 * @param jsonArray The JSONArray object to be converted.
	 * @return The converted ArrayList object.
	 */
	private fun toArrayList(jsonArray: JSONArray): ArrayList<String> {
		val newArrayList: ArrayList<String> = arrayListOf()

		var i = 0
		while (i < jsonArray.length()) {
			newArrayList.add(jsonArray.get(i) as String)
			i++
		}

		return newArrayList
	}
}
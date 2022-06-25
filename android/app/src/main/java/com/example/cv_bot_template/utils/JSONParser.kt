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
	 * Initialize settings from the JSON file.
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

		// Here you can parse out each property from the JSONObject via key iteration. You can create a static class
		// elsewhere to hold the JSON data. Or you can save them all into SharedPreferences.

		val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(myContext)

		try {
			val discordObj = jObj.getJSONObject("discord")

			sharedPreferences.edit {
				discordObj.keys().forEach { key ->
					when (key) {
						"enableDiscordNotifications" -> {
							putBoolean(key, discordObj[key] as Boolean)
						}
						else -> {
							putString(key, discordObj[key] as String)
						}
					}
				}

				commit()
			}
		} catch(e: Exception) {}

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
		} catch (e: Exception) {
		}
	}

	/**
	 * Convert JSONArray to ArrayList<String> object.
	 *
	 * @param jsonArray The JSONArray object to be converted.
	 * @return The converted ArrayList<String> object.
	 */
	private fun toStringArrayList(jsonArray: JSONArray): ArrayList<String> {
		val newArrayList: ArrayList<String> = arrayListOf()

		var i = 0
		while (i < jsonArray.length()) {
			newArrayList.add(jsonArray.get(i) as String)
			i++
		}

		return newArrayList
	}
}
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
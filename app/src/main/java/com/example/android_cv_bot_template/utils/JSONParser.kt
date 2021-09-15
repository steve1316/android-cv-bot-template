package com.example.android_cv_bot_template.utils

import android.content.Context
import com.beust.klaxon.JsonReader
import com.example.android_cv_bot_template.data.ConfigData
import java.io.File
import java.io.StringReader

class JSONParser(private val myContext: Context) {
	/**
	 * Construct the ConfigData class associated with the config.json file.
	 */
	fun constructConfigClass() {
		// Now construct the data class for config.
		val objectString = File(myContext.getExternalFilesDir(null), "config.json").bufferedReader().use { it.readText() }
		JsonReader(StringReader(objectString)).use { reader ->
			reader.beginObject {
				while (reader.hasNext()) {
					// Grab setting category name.
					when (reader.nextName()) {
						"discord" -> {
							reader.beginObject {
								while (reader.hasNext()) {
									val key = reader.nextString()
									val value = reader.nextString()
									
									if (key == "discordToken") {
										ConfigData.discordToken = value
									} else if (key == "userID") {
										ConfigData.userID = value
									}
								}
							}
						}
						"twitter" -> {
							reader.beginObject {
								while (reader.hasNext()) {
									val key = reader.nextString()
									val value = reader.nextString()
									
									if (key == "apiKey") {
										ConfigData.apiKey = value
									} else if (key == "apiKeySecret") {
										ConfigData.apiKeySecret = value
									} else if (key == "accessToken") {
										ConfigData.accessToken = value
									} else if (key == "accessTokenSecret") {
										ConfigData.accessTokenSecret = value
									}
								}
							}
						}
					}
				}
			}
		}
	}
}
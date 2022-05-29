package com.example.cv_bot_template.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import com.example.cv_bot_template.MainActivity.loggerTag

class ConfigData(myContext: Context) {
	private val tag = "${loggerTag}ConfigData"

	val debugMode: Boolean

	// Twitter
	val twitterAPIKey: String
	val twitterAPIKeySecret: String
	val twitterAccessToken: String
	val twitterAccessTokenSecret: String

	// Discord
	val enableDiscordNotifications: Boolean
	val discordToken: String
	val discordUserID: String

	// Android
	val enableDelayTap: Boolean
	val delayTapMilliseconds: Int
	val confidence: Double
	val confidenceAll: Double
	val customScale: Double

	init {
		Log.d(tag, "Loading settings from SharedPreferences to memory...")

		val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(myContext)

		debugMode = sharedPreferences.getBoolean("debugMode", false)

		// Consumer keys and access tokens required to use the Twitter API.
		twitterAPIKey = sharedPreferences.getString("twitterAPIKey", "")!!
		twitterAPIKeySecret = sharedPreferences.getString("twitterAPIKeySecret", "")!!
		twitterAccessToken = sharedPreferences.getString("twitterAccessToken", "")!!
		twitterAccessTokenSecret = sharedPreferences.getString("twitterAccessTokenSecret", "")!!

		// Token and user ID for use with the Discord API.
		enableDiscordNotifications = sharedPreferences.getBoolean("enableDiscordNotifications", false)
		discordToken = sharedPreferences.getString("discordToken", "")!!
		discordUserID = sharedPreferences.getString("discordUserID", "")!!

		// Android-specific settings.
		enableDelayTap = sharedPreferences.getBoolean("enableDelayTap", false)
		delayTapMilliseconds = sharedPreferences.getInt("delayTapMilliseconds", 1000)
		confidence = sharedPreferences.getFloat("confidence", 0.8f).toDouble() / 100.0
		confidenceAll = sharedPreferences.getFloat("confidenceAll", 0.8f).toDouble() / 100.0
		customScale = sharedPreferences.getFloat("customScale", 1.0f).toDouble()

		Log.d(tag, "Successfully loaded settings from SharedPreferences to memory.")
	}
}
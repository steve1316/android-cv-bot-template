package com.example.cv_bot_template.utils

import android.os.StrictMode
import com.example.cv_bot_template.MainActivity.loggerTag
import com.example.cv_bot_template.bot.Game
import twitter4j.Twitter
import twitter4j.v1.Query
import twitter4j.v1.TwitterV1

/**
 * Provides the functions needed to perform Twitter API-related tasks such as searching tweets for room codes.
 */
class TwitterUtils(private val game: Game, private val test: Boolean = false) {
	// For Twitter API v1.1
	private lateinit var oldTwitterClient: TwitterV1

	companion object {
		private const val tag: String = "${loggerTag}TwitterRoomFinder"
	}

	/**
	 * Connect to Twitter API V1.1
	 *
	 */
	fun connect() {
		if (!test) {
			game.printToLog("\n[TWITTER] Authenticating provided consumer keys and access tokens with the Twitter API V1.1...", tag)
			val result = testConnection()
			if (result == "Test successfully completed.") {
				game.printToLog("[TWITTER] Successfully connected to the Twitter API V1.1.", tag)
			} else {
				throw Exception(result)
			}
		}
	}

	/**
	 * Test connection to the API using the consumer keys/tokens for V1.1.
	 *
	 * @return Either a success message or an error message depending on the connection to the API.
	 */
	fun testConnection(): String {
		// Allow Network IO to be run on the main thread without throwing the NetworkOnMainThreadException.
		val policy: StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build()
		StrictMode.setThreadPolicy(policy)

		val size = try {
			// Create the Twitter client object to use the Twitter API V1.1.
			oldTwitterClient = Twitter.newBuilder().apply {
				oAuthConsumer(game.configData.twitterAPIKey, game.configData.twitterAPIKeySecret)
				oAuthAccessToken(game.configData.twitterAccessToken, game.configData.twitterAccessTokenSecret)
			}.build().v1()

			val queryResult = oldTwitterClient.search().search(Query.of("Hello World"))
			queryResult.count
		} catch (e: Exception) {
			game.printToLog("[ERROR] Cannot connect to Twitter API v1.1 due to keys and access tokens being incorrect.", tag, isError = true)
			return "[ERROR] Cannot connect to Twitter API v1.1 due to keys and access tokens being incorrect."
		}

		return if (size > 0) {
			"Test successfully completed."
		} else {
			"[ERROR] Connection was successful but test search came up empty."
		}
	}
}
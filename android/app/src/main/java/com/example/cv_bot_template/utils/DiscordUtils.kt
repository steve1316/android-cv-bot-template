package com.example.cv_bot_template.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import com.example.cv_bot_template.MainActivity.loggerTag
import com.example.cv_bot_template.R
import org.javacord.api.DiscordApi
import org.javacord.api.DiscordApiBuilder
import org.javacord.api.entity.channel.PrivateChannel
import org.javacord.api.entity.user.User
import org.javacord.api.entity.user.UserStatus
import java.util.*


/**
 * This class takes care of notifying users of status updates via Discord private DMs.
 */
class DiscordUtils(myContext: Context) {
	private val tag: String = "${loggerTag}DiscordUtils"
	private var appName: String = myContext.getString(R.string.app_name)

	private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(myContext)
	private val discordToken: String = sharedPreferences.getString("discordToken", "")!!
	private val discordUserID: String = sharedPreferences.getString("discordUserID", "")!!

	companion object {
		val queue: Queue<String> = LinkedList()
		lateinit var client: DiscordApi
		lateinit var privateChannel: PrivateChannel

		fun disconnectClient() {
			if (this::client.isInitialized && client.status == UserStatus.ONLINE) {
				client.disconnect()
			}
		}
	}

	private fun sendMessage(message: String) {
		privateChannel.sendMessage(message).join()
	}

	fun main() {
		try {
			Log.d(tag, "Starting Discord process now...")

			client = DiscordApiBuilder().setToken(discordToken).login().join()
			val user: User = client.getUserById(discordUserID).join()
			privateChannel = user.openPrivateChannel().join()

			Log.d(tag, "Successfully fetched reference to user and their private channel.")

			queue.add("```diff\n+ Successful mobile connection to Discord API for $appName\n```")

			// Loop and send any messages inside the Queue.
			while (true) {
				if (queue.isNotEmpty()) {
					val message = queue.remove()
					Log.d(tag, "Sending the following message to Discord DM: $message")
					sendMessage(message)

					if (message.contains("Terminated connection to Discord API")) {
						break
					}
				}
			}

			Log.d(tag, "Terminated connection to Discord API.")
			disconnectClient()
		} catch (e: Exception) {
			Log.e(tag, "Failed to initialize JDA client: ${e.stackTraceToString()}")
			disconnectClient()
		}
	}
}
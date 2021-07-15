package com.example.android_cv_bot_template.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.core.content.edit
import androidx.preference.*
import com.example.android_cv_bot_template.MainActivity
import com.example.android_cv_bot_template.R

class NestedSettingsFragment : PreferenceFragmentCompat() {
	private val TAG: String = "[${MainActivity.loggerTag}]NestedSettingsFragment"
	
	private lateinit var sharedPreferences: SharedPreferences
	
	// This listener is triggered whenever the user changes a Preference setting in the Settings Page.
	private val onSharedPreferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
		val nestedCheckBox: CheckBoxPreference = findPreference("nestedCheckBox")!!
		
		if (key != null) {
			// Note that is no need to handle the Preference that allows multiple selection here as it is already handled in its own function.
			when (key) {
				"nestedCheckBox" -> {
					sharedPreferences.edit {
						putBoolean("nestedSettings", nestedCheckBox.isChecked)
						commit()
					}
				}
			}
		}
	}
	
	override fun onResume() {
		super.onResume()
		
		// Makes sure that OnSharedPreferenceChangeListener works properly and avoids the situation where the app suddenly stops triggering the listener.
		preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
	}
	
	override fun onPause() {
		super.onPause()
		preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
	}
	
	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
		// Display the layout using the preferences xml.
		setPreferencesFromResource(R.xml.preferences_nested, rootKey)
		
		// Get the SharedPreferences.
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
		
		// Grab the saved preferences from the previous time the user used the app.
		val nestedSettings = sharedPreferences.getBoolean("nestedSettings", false)
		
		// Get references to the Preference components.
		val nestedCheckBox: CheckBoxPreference = findPreference("nestedCheckBox")!!
		
		nestedCheckBox.isChecked = nestedSettings
		
		Log.d(TAG, "Nested Settings Preferences created successfully.")
	}
}
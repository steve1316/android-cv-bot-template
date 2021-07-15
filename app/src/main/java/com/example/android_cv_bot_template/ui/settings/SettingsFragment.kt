package com.example.android_cv_bot_template.ui.settings

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.core.content.edit
import androidx.navigation.fragment.findNavController
import androidx.preference.*
import com.example.android_cv_bot_template.MainActivity
import com.example.android_cv_bot_template.R

class SettingsFragment : PreferenceFragmentCompat() {
	private val TAG: String = "[${MainActivity.loggerTag}]SettingsFragment"
	
	private lateinit var sharedPreferences: SharedPreferences
	
	private lateinit var builder: AlertDialog.Builder
	private lateinit var multipleOptionItems: Array<String>
	private lateinit var multipleOptionCheckedItems: BooleanArray
	private var userSelectedOptions: ArrayList<Int> = arrayListOf()
	
	// This listener is triggered whenever the user changes a Preference setting in the Settings Page.
	private val onSharedPreferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
		val exampleListPreference: ListPreference = findPreference("exampleListPreference")!!
		val exampleSeekBarPreference: SeekBarPreference = findPreference("exampleSeekBarPreference")!!
		val debugModeCheckBox: CheckBoxPreference = findPreference("debugModeCheckBox")!!
		
		if (key != null) {
			// Note that is no need to handle the Preference that allows multiple selection here as it is already handled in its own function.
			when (key) {
				"exampleListPreference" -> {
					sharedPreferences.edit {
						putString("item", exampleListPreference.value)
						commit()
					}
				}
				"exampleSeekBarPreference" -> {
					sharedPreferences.edit {
						putInt("value", exampleSeekBarPreference.value)
						commit()
					}
				}
				"debugModeCheckBox" -> {
					sharedPreferences.edit {
						putBoolean("debugMode", debugModeCheckBox.isChecked)
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
	
	// This function is called right after the user navigates to the SettingsFragment.
	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
		// Display the layout using the preferences xml.
		setPreferencesFromResource(R.xml.preferences, rootKey)
		
		// Get the SharedPreferences.
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
		
		// Grab the saved preferences from the previous time the user used the app.
		val item1 = sharedPreferences.getString("item", "")
		val value = sharedPreferences.getInt("value", 1)
		val debugMode = sharedPreferences.getBoolean("debugMode", false)
		
		// Get references to the Preference components.
		val exampleListPreference: ListPreference = findPreference("exampleListPreference")!!
		val exampleSeekBarPreference: SeekBarPreference = findPreference("exampleSeekBarPreference")!!
		val debugModeCheckBox: CheckBoxPreference = findPreference("debugModeCheckBox")!!
		
		// Now set the following values from the shared preferences.
		
		if (item1 != null && item1.isNotEmpty()) {
			exampleListPreference.value = item1
			exampleSeekBarPreference.isEnabled = true
		}
		
		exampleSeekBarPreference.value = value
		debugModeCheckBox.isChecked = debugMode
		
		createMultiplePickerAlertDialog()
		
		// Solution courtesy of https://stackoverflow.com/a/63368599
		// In short, Fragments via the mobile_navigation.xml are children of NavHostFragment, not MainActivity's supportFragmentManager.
		// This is why using the method described in official Google docs via OnPreferenceStartFragmentCallback and using the supportFragmentManager is not correct for this instance.
		findPreference<Preference>("nestedSettings")?.setOnPreferenceClickListener {
			// Navigate to the TrainingFragment.
			findNavController().navigate(R.id.nav_settings_nested)
			true
		}
		
		Log.d(TAG, "Preferences created successfully.")
	}
	
	/**
	 * Example function of how to build and display the AlertDialog for multiple option selection.
	 */
	private fun createMultiplePickerAlertDialog() {
		val multiplePreference: Preference = findPreference("multiplePreference")!!
		val savedOptions = sharedPreferences.getString("savedOptions", "")!!.split("|")
		
		// Update the Preference's summary to reflect the order of options selected if the user did it before.
		if (savedOptions.toList().isEmpty() || savedOptions.toList()[0] == "") {
			multiplePreference.summary = "Select the Options(s) in order from highest to lowest priority."
		} else {
			multiplePreference.summary = "${savedOptions.toList()}"
		}
		
		multiplePreference.setOnPreferenceClickListener {
			// Create the AlertDialog that pops up after clicking on this Preference.
			builder = AlertDialog.Builder(context)
			builder.setTitle("Select Option(s)")
			
			// Grab the array of multiple options.
			multipleOptionItems = resources.getStringArray(R.array.multiple_list)
			
			// Populate the list for multiple options if this is the first time.
			if (savedOptions.isEmpty()) {
				multipleOptionCheckedItems = BooleanArray(multipleOptionItems.size)
				var index = 0
				multipleOptionItems.forEach { _ ->
					multipleOptionCheckedItems[index] = false
					index++
				}
			} else {
				multipleOptionCheckedItems = BooleanArray(multipleOptionItems.size)
				var index = 0
				multipleOptionItems.forEach {
					// Populate the checked items BooleanArray with true or false depending on what the user selected before.
					multipleOptionCheckedItems[index] = savedOptions.contains(it)
					index++
				}
			}
			
			// Set the selectable items for this AlertDialog.
			builder.setMultiChoiceItems(multipleOptionItems, multipleOptionCheckedItems) { _, position, isChecked ->
				if (isChecked) {
					userSelectedOptions.add(position)
				} else {
					userSelectedOptions.remove(position)
				}
			}
			
			// Set the AlertDialog's PositiveButton.
			builder.setPositiveButton("OK") { _, _ ->
				// Grab the options using the acquired indexes. This will put them in order from the user's highest to lowest priority.
				val values: ArrayList<String> = arrayListOf()
				userSelectedOptions.forEach {
					values.add(multipleOptionItems[it])
				}
				
				// Join the elements together into a String with the "|" delimiter in order to keep its order when storing into SharedPreferences.
				val newValues = values.joinToString("|")
				
				// Note: putStringSet does not support ordering or duplicate values. If you need ordering/duplicate values, either concatenate the values together as a String separated by a
				// delimiter or think of another way.
				sharedPreferences.edit {
					putString("savedOptions", newValues)
					apply()
				}
				
				// Recreate the AlertDialog again to update it with the newly selected items.
				createMultiplePickerAlertDialog()
				
				if (values.toList().isEmpty()) {
					multiplePreference.summary = "Select the Options(s) in order from highest to lowest priority."
				} else {
					multiplePreference.summary = "${values.toList()}"
				}
			}
			
			// Set the AlertDialog's NegativeButton.
			builder.setNegativeButton("Dismiss") { dialog, _ -> dialog?.dismiss() }
			
			// Set the AlertDialog's NeutralButton.
			builder.setNeutralButton("Clear all") { _, _ ->
				// Go through every checked item and set them to false.
				for (i in multipleOptionCheckedItems.indices) {
					multipleOptionCheckedItems[i] = false
				}
				
				// After that, clear the list of user-selected options and the one in SharedPreferences.
				userSelectedOptions.clear()
				sharedPreferences.edit {
					remove("savedOptions")
					apply()
				}
				
				// Recreate the AlertDialog again to update it with the newly selected items and reset its summary.
				createMultiplePickerAlertDialog()
				multiplePreference.summary = "Select the Options(s) in order from highest to lowest priority."
			}
			
			// Finally, show the AlertDialog to the user.
			builder.create().show()
			
			true
		}
	}
}
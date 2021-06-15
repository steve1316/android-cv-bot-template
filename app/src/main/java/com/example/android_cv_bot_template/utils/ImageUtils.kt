package com.example.android_cv_bot_template.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import com.example.android_cv_bot_template.bot.Game
import com.example.android_cv_bot_template.ui.settings.SettingsFragment
import com.google.mlkit.vision.text.TextRecognition
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

/**
 * Utility functions for image processing via CV like OpenCV.
 */
class ImageUtils(context: Context, private val game: Game) {
	private val TAG: String = "Example_ImageUtils"
	private var myContext = context
	
	// Initialize Google's ML OCR.
	private val textRecognizer = TextRecognition.getClient()
	
	private val matchMethod: Int = Imgproc.TM_CCOEFF_NORMED
	
	private var debugMode: Boolean = false
	
	companion object {
		private var matchFilePath: String = ""
		private lateinit var matchLocation: Point
		private var matchLocations: ArrayList<Point> = arrayListOf()
		
		/**
		 * Saves the file path to the saved match image file for debugging purposes.
		 *
		 * @param filePath File path to where to store the image containing the location of where the match was found.
		 */
		private fun updateMatchFilePath(filePath: String) {
			matchFilePath = filePath
		}
	}
	
	init {
		// Set the file path to the /files/temp/ folder.
		val matchFilePath: String = myContext.getExternalFilesDir(null)?.absolutePath + "/temp"
		updateMatchFilePath(matchFilePath)
		
		// Now determine if Debug Mode is turned on for more informational logging messages.
		debugMode = SettingsFragment.getBooleanSharedPreference(myContext, "debugMode")
	}
	
	/**
	 * Match between the source Bitmap from /files/temp/ and the template Bitmap from the assets folder.
	 *
	 * @param sourceBitmap Bitmap from the /files/temp/ folder.
	 * @param templateBitmap Bitmap from the assets folder.
	 * @param useCannyAlgorithm Check whether or not to use Canny edge detection algorithm. Defaults to false.
	 */
	private fun match(sourceBitmap: Bitmap, templateBitmap: Bitmap, useCannyAlgorithm: Boolean = false): Boolean {
		// Create the Mats of both source and template images.
		val sourceMat = Mat()
		val templateMat = Mat()
		Utils.bitmapToMat(sourceBitmap, sourceMat)
		Utils.bitmapToMat(templateBitmap, templateMat)
		
		// Make the Mats grayscale for the source and the template.
		Imgproc.cvtColor(sourceMat, sourceMat, Imgproc.COLOR_BGR2GRAY)
		Imgproc.cvtColor(templateMat, templateMat, Imgproc.COLOR_BGR2GRAY)
		
		if (useCannyAlgorithm) {
			// Blur the source and template.
			Imgproc.blur(sourceMat, sourceMat, Size(3.0, 3.0))
			Imgproc.blur(templateMat, templateMat, Size(3.0, 3.0))
			
			// Apply Canny edge detection algorithm in both source and template. Generally recommended for threshold2 to be 3 times threshold1.
			Imgproc.Canny(sourceMat, sourceMat, 100.0, 300.0)
			Imgproc.Canny(templateMat, templateMat, 100.0, 300.0)
		}
		
		// Create the result matrix.
		val resultColumns: Int = sourceMat.cols() - templateMat.cols() + 1
		val resultRows: Int = sourceMat.rows() - templateMat.rows() + 1
		val resultMat = Mat(resultRows, resultColumns, CvType.CV_32FC1)
		
		// Now perform the matching and localize the result.
		Imgproc.matchTemplate(sourceMat, templateMat, resultMat, matchMethod)
		val mmr: Core.MinMaxLocResult = Core.minMaxLoc(resultMat)
		
		matchLocation = Point()
		var matchCheck = false
		
		// Depending on which matching method was used, the algorithms determine which location was the best.
		if ((matchMethod == Imgproc.TM_SQDIFF || matchMethod == Imgproc.TM_SQDIFF_NORMED) && mmr.minVal <= 0.2) {
			matchLocation = mmr.minLoc
			matchCheck = true
			
			if (debugMode) {
				game.printToLog("[DEBUG] Match found with similarity <= 0.2 at Point $matchLocation with minVal = ${mmr.minVal}.", MESSAGE_TAG = TAG)
			}
		} else if ((matchMethod != Imgproc.TM_SQDIFF && matchMethod != Imgproc.TM_SQDIFF_NORMED) && mmr.maxVal >= 0.8) {
			matchLocation = mmr.maxLoc
			matchCheck = true
			
			if (debugMode) {
				game.printToLog("[DEBUG] Match found with similarity >= 0.8 at Point $matchLocation with maxVal = ${mmr.maxVal}.", MESSAGE_TAG = TAG)
			}
		} else {
			if (debugMode) {
				game.printToLog("[DEBUG] Match not found.", MESSAGE_TAG = TAG)
			}
		}
		
		if (matchCheck) {
			// Draw a rectangle around the supposed best matching location and then save the match into a file in /files/temp/ directory. This is for
			// debugging purposes to see if this algorithm found the match accurately or not.
			if (matchFilePath != "") {
				Imgproc.rectangle(sourceMat, matchLocation, Point(matchLocation.x + templateMat.cols(), matchLocation.y + templateMat.rows()), Scalar(0.0, 128.0, 0.0), 5)
				Imgcodecs.imwrite("$matchFilePath/match.png", sourceMat)
			}
			
			// Center the coordinates so that any tap gesture would be directed at the center of that match location instead of the default
			// position of the top left corner of the match location.
			matchLocation.x += (templateMat.cols() / 2)
			matchLocation.y += (templateMat.rows() / 2)
			
			return true
		} else {
			return false
		}
	}
	
	private fun matchAll(sourceBitmap: Bitmap, templateBitmap: Bitmap): ArrayList<Point> {
		// Create the Mats of both source and template images.
		val sourceMat = Mat()
		val templateMat = Mat()
		Utils.bitmapToMat(sourceBitmap, sourceMat)
		Utils.bitmapToMat(templateBitmap, templateMat)
		
		// Make the Mats grayscale for the source and the template.
		Imgproc.cvtColor(sourceMat, sourceMat, Imgproc.COLOR_BGR2GRAY)
		Imgproc.cvtColor(templateMat, templateMat, Imgproc.COLOR_BGR2GRAY)
		
		// Create the result matrix.
		val resultColumns: Int = sourceMat.cols() - templateMat.cols() + 1
		val resultRows: Int = sourceMat.rows() - templateMat.rows() + 1
		val resultMat = Mat(resultRows, resultColumns, CvType.CV_32FC1)
		
		if (debugMode) {
			game.printToLog("[DEBUG] Now beginning search for all matches...", MESSAGE_TAG = TAG)
		}
		
		// Loop until all matches are found.
		while (true) {
			// Now perform the matching and localize the result.
			Imgproc.matchTemplate(sourceMat, templateMat, resultMat, matchMethod)
			val mmr: Core.MinMaxLocResult = Core.minMaxLoc(resultMat)
			
			if ((matchMethod == Imgproc.TM_SQDIFF || matchMethod == Imgproc.TM_SQDIFF_NORMED) && mmr.minVal <= 0.2) {
				val tempMatchLocation: Point = mmr.minLoc
				
				if (debugMode) {
					game.printToLog("[DEBUG] Match found with similarity <= 0.2 at Point $matchLocation with minVal = ${mmr.minVal}.", MESSAGE_TAG = TAG)
				}
				
				// Draw a rectangle around the match and then save it to the specified file.
				Imgproc.rectangle(sourceMat, tempMatchLocation, Point(tempMatchLocation.x + templateMat.cols(), tempMatchLocation.y + templateMat.rows()), Scalar(255.0, 255.0, 255.0), 5)
				Imgcodecs.imwrite("$matchFilePath/matchAll.png", sourceMat)
				
				// Center the location coordinates and then save it to the arrayList.
				tempMatchLocation.x += (templateMat.cols() / 2)
				tempMatchLocation.y += (templateMat.rows() / 2)
				matchLocations.add(tempMatchLocation)
			} else if ((matchMethod != Imgproc.TM_SQDIFF && matchMethod != Imgproc.TM_SQDIFF_NORMED) && mmr.maxVal >= 0.8) {
				val tempMatchLocation: Point = mmr.maxLoc
				
				if (debugMode) {
					game.printToLog("[DEBUG] Match found with similarity >= 0.8 at Point $matchLocation with maxVal = ${mmr.maxVal}.", MESSAGE_TAG = TAG)
				}
				
				// Draw a rectangle around the match and then save it to the specified file.
				Imgproc.rectangle(sourceMat, tempMatchLocation, Point(tempMatchLocation.x + templateMat.cols(), tempMatchLocation.y + templateMat.rows()), Scalar(255.0, 255.0, 255.0), 5)
				Imgcodecs.imwrite("$matchFilePath/matchAll.png", sourceMat)
				
				// Center the location coordinates and then save it to the arrayList.
				tempMatchLocation.x += (templateMat.cols() / 2)
				tempMatchLocation.y += (templateMat.rows() / 2)
				matchLocations.add(tempMatchLocation)
			} else {
				break
			}
		}
		
		return matchLocations
	}
	
	/**
	 * Open the source and template image files and return Bitmaps for them.
	 *
	 * @param templateName File name of the template image.
	 * @param templateFolderName Name of the subfolder in /assets/ that the template image is in.
	 * @return A Pair of source and template Bitmaps.
	 */
	private fun getBitmaps(templateName: String, templateFolderName: String): Pair<Bitmap?, Bitmap?> {
		var sourceBitmap: Bitmap? = null
		
		// Keep swiping a little bit up and down to trigger a new image for ImageReader to grab.
		while (sourceBitmap == null) {
			sourceBitmap = MediaProjectionService.takeScreenshotNow()
			
			if (sourceBitmap == null) {
				game.gestureUtils.swipe(500f, 1000f, 500f, 900f, 100L)
				game.gestureUtils.swipe(500f, 900f, 500f, 1000f, 100L)
				game.wait(0.5)
			}
		}
		
		var templateBitmap: Bitmap?
		
		// Get the Bitmap from the template image file inside the specified folder.
		myContext.assets?.open("$templateFolderName/$templateName.webp").use { inputStream ->
			// Get the Bitmap from the template image file and then start matching.
			templateBitmap = BitmapFactory.decodeStream(inputStream)
		}
		
		return if (templateBitmap != null) {
			Pair(sourceBitmap, templateBitmap)
		} else {
			if (debugMode) {
				game.printToLog("[ERROR] One or more of the Bitmaps are null.", MESSAGE_TAG = TAG, isError = true)
			}
			
			Pair(sourceBitmap, templateBitmap)
		}
	}
	
	/**
	 * Acquire a Bitmap from the URL's image file.
	 *
	 * @return A new Bitmap.
	 */
	fun getBitmapFromURL(url: URL): Bitmap {
		// Open up a HTTP connection to the URL.
		val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
		connection.doInput = true
		connection.connect()
		
		// Download the image from the URL.
		val input: InputStream = connection.inputStream
		return BitmapFactory.decodeStream(input)
	}
	
	/**
	 * Pixel search by its RGB value.
	 *
	 * @param bitmap Bitmap of the image to search for the specific pixel.
	 * @param red The pixel's Red value.
	 * @param blue The pixel's Blue value.
	 * @param green The pixel's Green value.
	 * @return A Pair object of the (x,y) coordinates on the Bitmap for the matched pixel.
	 */
	fun pixelSearch(bitmap: Bitmap, red: Int, blue: Int, green: Int): Pair<Int, Int> {
		var x = 0
		var y = 0
		
		// Iterate through each pixel in the Bitmap and compare RGB values.
		while (x < bitmap.width) {
			while (y < bitmap.height) {
				val pixel = bitmap.getPixel(x, y)
				
				if (Color.red(pixel) == red && Color.blue(pixel) == blue && Color.green(pixel) == green) {
					game.printToLog("Found matching pixel at ($x, $y).", MESSAGE_TAG = TAG)
					return Pair(x, y)
				}
				
				y++
			}
			
			x++
			y = 0
		}
		
		return Pair(-1, -1)
	}
	
	/**
	 * Finds the location of the specified button.
	 *
	 * @param templateName File name of the template image.
	 * @param tries Number of tries before failing. Defaults to 3.
	 * @param suppressError Whether or not to suppress saving error messages to the log.
	 * @return Point object containing the location of the match or null if not found.
	 */
	fun findButton(templateName: String, tries: Int = 3, suppressError: Boolean = false): Point? {
		val folderName = "buttons"
		var numberOfTries = tries
		
		while (numberOfTries > 0) {
			val (sourceBitmap, templateBitmap) = getBitmaps(templateName, folderName)
			
			if (sourceBitmap != null && templateBitmap != null) {
				val resultFlag: Boolean = match(sourceBitmap, templateBitmap)
				if (!resultFlag) {
					numberOfTries -= 1
					if (numberOfTries <= 0) {
						if (!suppressError) {
							game.printToLog("[WARNING] Failed to find the ${templateName.toUpperCase(Locale.ROOT)} button.", MESSAGE_TAG = TAG)
						}
						
						return null
					}
					
					Log.d(TAG, "Failed to find the ${templateName.toUpperCase(Locale.ROOT)} button. Trying again...")
					game.wait(1.0)
				} else {
					game.printToLog("[SUCCESS] Found the ${templateName.toUpperCase(Locale.ROOT)} at $matchLocation.", MESSAGE_TAG = TAG)
					return matchLocation
				}
			}
		}
		
		return null
	}
	
	/**
	 * Confirms whether or not the bot is at the specified location.
	 *
	 * @param templateName File name of the template image.
	 * @param tries Number of tries before failing. Defaults to 3.
	 * @param suppressError Whether or not to suppress saving error messages to the log.
	 * @return True if the current location is at the specified location. False otherwise.
	 */
	fun confirmLocation(templateName: String, tries: Int = 3, suppressError: Boolean = false): Boolean {
		val folderName = "headers"
		var numberOfTries = tries
		while (numberOfTries > 0) {
			val (sourceBitmap, templateBitmap) = getBitmaps(templateName + "_header", folderName)
			
			if (sourceBitmap != null && templateBitmap != null) {
				val resultFlag: Boolean = match(sourceBitmap, templateBitmap)
				if (!resultFlag) {
					numberOfTries -= 1
					if (numberOfTries <= 0) {
						break
					}
					
					game.wait(1.0)
				} else {
					game.printToLog("[SUCCESS] Current location confirmed to be at ${templateName.toUpperCase(Locale.ROOT)}.", MESSAGE_TAG = TAG)
					return true
				}
			} else {
				break
			}
		}
		
		if (!suppressError) {
			game.printToLog("[WARNING] Failed to confirm the bot's location at ${templateName.toUpperCase(Locale.ROOT)}.", MESSAGE_TAG = TAG)
		}
		
		return false
	}
	
	/**
	 * Finds all occurrences of the specified image in the buttons folder. Has an optional parameter to specify looking in the items folder instead.
	 *
	 * @param templateName File name of the template image.
	 * @param isItem Whether or not the user wants to search for items instead of buttons.
	 * @return An ArrayList of Point objects containing all the occurrences of the specified image or null if not found.
	 */
	fun findAll(templateName: String, isItem: Boolean = false): ArrayList<Point> {
		val folderName = if (!isItem) {
			"buttons"
		} else {
			"items"
		}
		
		val (sourceBitmap, templateBitmap) = getBitmaps(templateName, folderName)
		
		// Clear the ArrayList first before attempting to find all matches.
		matchLocations.clear()
		
		if (sourceBitmap != null && templateBitmap != null) {
			matchAll(sourceBitmap, templateBitmap)
		}
		
		// Sort the match locations by ascending y coordinates.
		matchLocations.sortBy { it.y }
		
		if (debugMode) {
			game.printToLog("[DEBUG] Found match locations for $templateName: $matchLocations.", MESSAGE_TAG = TAG)
		}
		
		return matchLocations
	}
	
	/**
	 * Perform OCR text detection.
	 */
	fun findText(templateName: String) {
		// Read up on Google's ML Kit at https://developers.google.com/ml-kit/vision/text-recognition/android and my
		// usage of it at https://github.com/steve1316/granblue-automation-android/blob/main/app/src/main/java/com/steve1316/granblueautomation_android/utils/ImageUtils.kt for a better
		// understanding of how to do OCR detection.
	}
	
	/**
	 * Waits for the specified image to vanish from the screen.
	 *
	 * @param templateName File name of the template image.
	 * @param timeout Amount of time to wait before timing out. Default is 5 seconds.
	 * @param suppressError Whether or not to suppress saving error messages to the log.
	 * @return True if the specified image vanished from the screen. False otherwise.
	 */
	fun waitVanish(templateName: String, timeout: Int = 5, suppressError: Boolean = false): Boolean {
		game.printToLog("[INFO] Now waiting for $templateName to vanish from the screen...", MESSAGE_TAG = TAG)
		
		var remaining = timeout
		if (findButton(templateName, tries = 1, suppressError = suppressError) == null) {
			return true
		} else {
			while (findButton(templateName, tries = 1, suppressError = suppressError) == null) {
				game.wait(1.0)
				remaining -= 1
				if (remaining <= 0) {
					return false
				}
			}
			
			return true
		}
	}
}
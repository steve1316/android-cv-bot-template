package com.example.android_cv_bot_template.utils

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import androidx.preference.PreferenceManager
import com.example.android_cv_bot_template.MainActivity
import com.example.android_cv_bot_template.bot.Game
import com.example.android_cv_bot_template.ui.settings.SettingsFragment
import com.google.mlkit.vision.text.TextRecognition
import com.googlecode.tesseract.android.TessBaseAPI
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

/**
 * Utility functions for image processing via CV like OpenCV.
 */
class ImageUtils(context: Context, private val game: Game) {
	private val TAG: String = "[${MainActivity.loggerTag}]ImageUtils"
	private var myContext = context
	private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(myContext)
	
	// Initialize Google's ML OCR.
	private val textRecognizer = TextRecognition.getClient()
	
	private val matchMethod: Int = Imgproc.TM_CCOEFF_NORMED
	
	private val tessBaseAPI: TessBaseAPI
	
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
		debugMode = sharedPreferences.getBoolean("debugMode", false)
		
		// Uncomment the below line to initialize Tesseract for the purposes of OCR text recognition.
		// initTesseract("SET FILE NAME OF .TRAINEDDATA FOR TESSERACT INITIALIZATION HERE")
		tessBaseAPI = TessBaseAPI()
	}
	
	/**
	 * Match between the source Bitmap from /files/temp/ and the template Bitmap from the assets folder.
	 *
	 * @param sourceBitmap Bitmap from the /files/temp/ folder.
	 * @param templateBitmap Bitmap from the assets folder.
	 * @param region Specify the region consisting of (x, y, width, height) of the source screenshot to template match. Defaults to (0, 0, 0, 0) which is equivalent to searching the full image.
	 * @param useCannyAlgorithm Check whether or not to use Canny edge detection algorithm. Defaults to false.
	 * @return True if a match was found. False otherwise.
	 */
	private fun match(sourceBitmap: Bitmap, templateBitmap: Bitmap, region: IntArray = intArrayOf(0, 0, 0, 0), useCannyAlgorithm: Boolean = false): Boolean {
		// If a custom region was specified, crop the source screenshot.
		val srcBitmap = if (!region.contentEquals(intArrayOf(0, 0, 0, 0))) {
			Bitmap.createBitmap(sourceBitmap, region[0], region[1], region[2], region[3])
		} else {
			sourceBitmap
		}
		
		// Create the Mats of both source and template images.
		val sourceMat = Mat()
		val templateMat = Mat()
		Utils.bitmapToMat(srcBitmap, sourceMat)
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
			
			// If a custom region was specified, readjust the coordinates to reflect the fullscreen source screenshot.
			if (!region.contentEquals(intArrayOf(0, 0, 0, 0))) {
				matchLocation.x = sourceBitmap.width - (region[0] + matchLocation.x)
				matchLocation.y = sourceBitmap.height - (region[1] + matchLocation.y)
			}
			
			return true
		} else {
			return false
		}
	}
	
	/**
	 * Search through the whole source screenshot for all matches to the template image.
	 *
	 * @param sourceBitmap Bitmap from the /files/temp/ folder.
	 * @param templateBitmap Bitmap from the assets folder.
	 * @return ArrayList of Point objects that represents the matches found on the source screenshot.
	 */
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
	 * Finds the location of the specified image from the /images/ folder inside assets.
	 *
	 * @param templateName File name of the template image.
	 * @param tries Number of tries before failing. Defaults to 3.
	 * @param region Specify the region consisting of (x, y, width, height) of the source screenshot to template match. Defaults to (0, 0, 0, 0) which is equivalent to searching the full image.
	 * @param suppressError Whether or not to suppress saving error messages to the log. Defaults to false.
	 * @return Point object containing the location of the match or null if not found.
	 */
	fun findImage(templateName: String, tries: Int = 3, region: IntArray = intArrayOf(0, 0, 0, 0), suppressError: Boolean = false): Point? {
		val folderName = "images"
		var numberOfTries = tries
		
		while (numberOfTries > 0) {
			val (sourceBitmap, templateBitmap) = getBitmaps(templateName, folderName)
			
			if (sourceBitmap != null && templateBitmap != null) {
				val resultFlag: Boolean = match(sourceBitmap, templateBitmap, region)
				if (!resultFlag) {
					numberOfTries -= 1
					if (numberOfTries <= 0) {
						if (!suppressError) {
							game.printToLog("[WARNING] Failed to find the ${templateName.uppercase()} button.", MESSAGE_TAG = TAG)
						}
						
						return null
					}
					
					Log.d(TAG, "Failed to find the ${templateName.uppercase()} button. Trying again...")
					game.wait(1.0)
				} else {
					game.printToLog("[SUCCESS] Found the ${templateName.uppercase()} at $matchLocation.", MESSAGE_TAG = TAG)
					return matchLocation
				}
			}
		}
		
		return null
	}
	
	/**
	 * Confirms whether or not the bot is at the specified location from the /headers/ folder inside assets.
	 *
	 * @param templateName File name of the template image.
	 * @param tries Number of tries before failing. Defaults to 3.
	 * @param region Specify the region consisting of (x, y, width, height) of the source screenshot to template match. Defaults to (0, 0, 0, 0) which is equivalent to searching the full image.
	 * @param suppressError Whether or not to suppress saving error messages to the log.
	 * @return True if the current location is at the specified location. False otherwise.
	 */
	fun confirmLocation(templateName: String, tries: Int = 3, region: IntArray = intArrayOf(0, 0, 0, 0), suppressError: Boolean = false): Boolean {
		val folderName = "headers"
		var numberOfTries = tries
		while (numberOfTries > 0) {
			val (sourceBitmap, templateBitmap) = getBitmaps(templateName + "_header", folderName)
			
			if (sourceBitmap != null && templateBitmap != null) {
				val resultFlag: Boolean = match(sourceBitmap, templateBitmap, region)
				if (!resultFlag) {
					numberOfTries -= 1
					if (numberOfTries <= 0) {
						break
					}
					
					game.wait(1.0)
				} else {
					game.printToLog("[SUCCESS] Current location confirmed to be at ${templateName.uppercase()}.", MESSAGE_TAG = TAG)
					return true
				}
			} else {
				break
			}
		}
		
		if (!suppressError) {
			game.printToLog("[WARNING] Failed to confirm the bot location at ${templateName.uppercase()}.", MESSAGE_TAG = TAG)
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
			"images"
		} else {
			"items"
		}
		
		val (sourceBitmap, templateBitmap) = getBitmaps(templateName, folderName)
		
		// Clear the ArrayList first before attempting to find all matches.
		matchLocations.clear()
		
		if (sourceBitmap != null && templateBitmap != null) {
			matchAll(sourceBitmap, templateBitmap)
		}
		
		// Sort the match locations by ascending x and y coordinates.
		matchLocations.sortBy { it.x }
		matchLocations.sortBy { it.y }
		
		if (debugMode) {
			game.printToLog("[DEBUG] Found match locations for $templateName: $matchLocations.", MESSAGE_TAG = TAG)
		}
		
		return matchLocations
	}
	
	/**
	 * Perform OCR text detection.
	 */
	fun findTextGoogleMLKit(templateName: String) {
		// Read up on Google's ML Kit at https://developers.google.com/ml-kit/vision/text-recognition/android and my
		// usage of it at https://github.com/steve1316/granblue-automation-android/blob/main/app/src/main/java/com/steve1316/granblueautomation_android/utils/ImageUtils.kt for a better
		// understanding of how to do OCR detection.
	}
	
	/**
	 * Waits for the specified image to vanish from the screen.
	 *
	 * @param templateName File name of the template image.
	 * @param timeout Amount of time to wait before timing out. Default is 5 seconds.
	 * @param region Specify the region consisting of (x, y, width, height) of the source screenshot to template match. Defaults to (0, 0, 0, 0) which is equivalent to searching the full image.
	 * @param suppressError Whether or not to suppress saving error messages to the log.
	 * @return True if the specified image vanished from the screen. False otherwise.
	 */
	fun waitVanish(templateName: String, timeout: Int = 5, region: IntArray = intArrayOf(0, 0, 0, 0), suppressError: Boolean = false): Boolean {
		game.printToLog("[INFO] Now waiting for $templateName to vanish from the screen...", MESSAGE_TAG = TAG)
		
		var remaining = timeout
		if (findImage(templateName, tries = 1, region = region, suppressError = suppressError) == null) {
			return true
		} else {
			while (findImage(templateName, tries = 1, region = region, suppressError = suppressError) == null) {
				game.wait(1.0)
				remaining -= 1
				if (remaining <= 0) {
					return false
				}
			}
			
			return true
		}
	}
	
	/**
	 * Initialize Tesseract for future OCR operations. Make sure to put your .traineddata inside the root of the /assets/ folder.
	 *
	 * @param traineddataFileName The file name including its extension for the .traineddata of Tesseract.
	 */
	private fun initTesseract(traineddataFileName: String) {
		val externalFilesDir: File? = myContext.getExternalFilesDir(null)
		val tempDirectory: String = externalFilesDir?.absolutePath + "/tesseract/tessdata/"
		val newTempDirectory = File(tempDirectory)
		
		// If the /files/temp/ folder does not exist, create it.
		if (!newTempDirectory.exists()) {
			val successfullyCreated: Boolean = newTempDirectory.mkdirs()
			
			// If the folder was not able to be created for some reason, log the error and stop the MediaProjection Service.
			if (!successfullyCreated) {
				game.printToLog("[ERROR] Failed to create the /files/tesseract/tessdata/ folder.", MESSAGE_TAG = TAG, isError = true)
			} else {
				game.printToLog("[INFO] Successfully created /files/tesseract/tessdata/ folder.", MESSAGE_TAG = TAG)
			}
		} else {
			game.printToLog("[INFO] /files/tesseract/tessdata/ folder already exists.", MESSAGE_TAG = TAG)
		}
		
		// If the .traineddata is not in the application folder, copy it there from assets.
		val trainedDataPath = File(tempDirectory, traineddataFileName)
		if (!trainedDataPath.exists()) {
			try {
				game.printToLog("[INFO] Starting Tesseract initialization.", MESSAGE_TAG = TAG)
				val input = myContext.assets.open(traineddataFileName)
				
				val output = FileOutputStream("$tempDirectory/$traineddataFileName")
				
				val buffer = ByteArray(1024)
				var read: Int
				while (input.read(buffer).also { read = it } != -1) {
					output.write(buffer, 0, read)
				}
				
				input.close()
				output.flush()
				output.close()
				game.printToLog("[INFO] Finished Tesseract initialization.", MESSAGE_TAG = TAG)
			} catch (e: IOException) {
				game.printToLog("[ERROR] IO EXCEPTION: ${e.stackTraceToString()}", MESSAGE_TAG = TAG, isError = true)
			}
		}
	}
	
	/**
	 * Perform OCR text detection using Tesseract along with some image manipulation via thresholding to make the cropped screenshot black and white using OpenCV.
	 *
	 * @return The detected String in the cropped region.
	 */
	fun findTextTesseract(): String {
		val (_, _) = getBitmaps("", "")
		
		tessBaseAPI.init(myContext.getExternalFilesDir(null)?.absolutePath + "/tesseract/", "jpn")
		game.printToLog("[INFO] Training file loaded.\n", MESSAGE_TAG = TAG)
		
		// Read in the new screenshot and crop it.
		var cvImage = Imgcodecs.imread("${matchFilePath}/source.png", Imgcodecs.IMREAD_GRAYSCALE)
		cvImage = cvImage.submat(0, 500, 0, 500)
		
		// Save the cropped image before converting it to black and white in order to troubleshoot issues related to differing device sizes and cropping.
		Imgcodecs.imwrite("$matchFilePath/pre_tesseract_result.png", cvImage)
		
		// Thresh the grayscale cropped image to make black and white.
		val bwImage = Mat()
		Imgproc.threshold(cvImage, bwImage, 200.0, 255.0, Imgproc.THRESH_BINARY)
		Imgcodecs.imwrite("$matchFilePath/tesseract_result.png", bwImage)
		
		val resultBitmap = BitmapFactory.decodeFile("$matchFilePath/tesseract_result.png")
		tessBaseAPI.setImage(resultBitmap)
		
		// Set the Page Segmentation Mode to '--psm 7' or "Treat the image as a single text line" according to https://tesseract-ocr.github.io/tessdoc/ImproveQuality.html#page-segmentation-method
		tessBaseAPI.pageSegMode = TessBaseAPI.PageSegMode.PSM_SINGLE_LINE
		
		var result = "empty!"
		try {
			// Finally, detect text on the cropped region.
			result = tessBaseAPI.utF8Text
		} catch (e: Exception) {
			game.printToLog("[ERROR] Cannot perform OCR: ${e.stackTraceToString()}", MESSAGE_TAG = TAG, isError = true)
		}
		
		tessBaseAPI.end()
		
		return result
	}
}
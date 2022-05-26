# Android CV Bot Template

![GitHub commit activity](https://img.shields.io/github/commit-activity/m/steve1316/android-cv-bot-template?logo=GitHub) ![GitHub last commit](https://img.shields.io/github/last-commit/steve1316/android-cv-bot-template?logo=GitHub) ![GitHub issues](https://img.shields.io/github/issues/steve1316/android-cv-bot-template?logo=GitHub) ![GitHub pull requests](https://img.shields.io/github/issues-pr/steve1316/android-cv-bot-template?logo=GitHub) ![GitHub](https://img.shields.io/github/license/steve1316/android-cv-bot-template?logo=GitHub)

> You can visit my Android bot of Granblue Fantasy for more usage details and examples of how to use this framework: https://github.com/steve1316/granblue-automation-android

> Some additional usage examples at [Uma Android Training Helper](https://github.com/steve1316/uma-android-training-helper) and [Uma Android Automation](https://github.com/steve1316/uma-android-automation)

This template project serves as a starter point for Android botting or as a general framework that relies on Computer Vision template matching via OpenCV and executing gestures like tapping and scrolling to accomplish a automation goal. It uses MediaProjection Service to programmatically take screenshots and the Accessibility Service to execute gestures. The framework is well annotated with documentation to serve as explanations and usage hints.

https://user-images.githubusercontent.com/18709555/118407909-c933be00-b637-11eb-92c2-3c4acd355aff.mp4

# Provided Features

-   A Home page that also houses the Message Log to allow the user to see informational logging messages.
-   A Settings page that utilizes SharedPreferences to share data across the application and its associated Services.
-   Floating overlay button to issue START/STOP signals to the bot process.
-   Fleshed out template matching functions via OpenCV
-   Notifications to alert users of various changes during workflow.
-   Accessibility Service that will allow the bot process to execute gestures on the screen like tapping a specific point.
-   Automatically checking for new app updates from your designated GitHub repository.
-   Ability for sending you messages via Discord private DMs.

# Requirements

1. [Android Device or Emulator (Nougat 7.0+)](https://developer.android.com/about/versions)
    1. (Experimental) Tablets supported with minimum 1600 pixel width like the Galaxy Tab S7. If oriented portrait, browsers like Chrome needs to have Desktop Mode turned off and situated on the left half of the tablet. If landscape, browsers like Chrome needs to have Desktop Mode turned on and situated on the left half of the tablet.
    2. Tested emulator was Bluestacks 5 with the following settings:
        - P64 (Beta)
        - 1080x1920 (Portrait Mode as emulators do not have a way to tell the bot that it rotated.)
        - 240 DPI
        - 4+ GB of Memory

# Instructions

1. Download the project and extract.
2. Go to `https://opencv.org/releases/` and download OpenCV 4.5.1 (make sure to download the Android version of OpenCV) and extract it.
3. Create a new folder inside the root of the `android` folder named `opencv` and copy the extracted files in `/OpenCV-android-sdk/sdk/` from Step 2 into it.
4. Build the Javascript portion of the project by running `yarn install` in the root of the project folder as well.
5. You can now build and run on your Android Device or create your own .apk file.
6. `Clean Project` and then `Rebuild Project` under the Build menu.
7. After building is complete, you can test the capability of this framework in the Android Studio's emulator.
8. After you familiarized yourself with what the framework can do, you can refactor the application's package name in various parts of the project and in `settings.gradle`.
9. If you want your application automatically check for the latest updates from your GitHub repo using `AppUpdater`, do the following:
    1. Upload a .xml file to your Github repo using the provided example `app/update.xml` with your updated version number, release notes, and link to the `Releases` page of your GitHub repo.
    2. Update the `setUpdateXML()` with the `RAW` link to your new update.xml.
    3. Now when a user has a lower version number compared to the latest version in your `Releases` page in your GitHub repo, they will be prompted with a dialog window like this:

> ![i_view32_cyUHsXlLFG](https://user-images.githubusercontent.com/18709555/125871637-0a803f09-fbc3-49b9-ae39-1a77cf64bbf3.png)

### Some things to note while developing

1. `ImageUtils` class reads in images in `.webp` format to reduce apk file size. You can change this if you wish.
2. All images are recommended to be categorized in separate folders inside the /assets/ folder. Be sure to update the `folderName` variables inside the various functions in `ImageUtils`. Or you could remove the need to organize them and just put all image assets into one place. Just make sure to update the code references to the `folderName` variables.
3. When working on a horizontal screen, the coordinate axis gets flipped as well. So if your vertical orientation dimensions is 1080x2400, then the horizontal orientation dimensions gets flipped to 2400x1080.
4. (on the `old` branch) If you want to create nested Fragment Preference settings, there is an example provided to showcase how to do that in SettingsFragment.kt and mobile_navigation.xml.

# Important Classes to be familiar with

## BotService

-   Facilitates the display and moving of the floating overlay button.
-   Able to start/stop the bot process on a new Thread and notify users of bot state changes like Success or Exception encountered.

## ImageUtils

-   Able to template match for a single image or multiple image locations on the screen.
-   Able to detect text via Google's ML Kit (Read up on the Google's documentation and my usage of it at `https://github.com/steve1316/granblue-automation-android/blob/main/app/src/main/java/com/steve1316/granblueautomation_android/utils/ImageUtils.kt` for a better understanding of it)

## MediaProjectionService

-   Starts up the MediaProjection Service to allow the `ImageUtils` class to programmatically grab screenshots to perform template matching on it.

## MessageLog

-   Sends informational logging messages from the `Game` class to the Home page of the application to quickly view what is going on.
-   Automatically saves logs into text files when the bot stops.

## MyAccessibilityService

-   Starts up the Accessibility Service to allow the `Game` class to execute gestures at specified (x,y) coordinates on the screen.
    -   Supported gestures are: tap, swipe, and scroll via AccessibilityAction.
-   Note: If you encounter this Exception: `kotlin.UninitializedPropertyAccessException: lateinit property instance has not been initialized`, it means that you must have terminated the application via Android Studio. This causes the Accessibility Service to bug out. This does not happen in regular use without interference from Android Studio. To fix this, you can toggle on/off the Accessibility Service until the Toast message pops back up signalling that the Service is now running properly again.

## NotificationUtils

-   Allows the bot process to create and update Notifications to notify users of the status of the bot and whether or not the bot encounters an Exception.
-   Sends a STOP signal to the bot process from the Notification's button via the `StopServiceReceiver` class.

# Technologies used

1. [MediaProjection - Used to obtain full screenshots](https://developer.android.com/reference/android/media/projection/MediaProjection)
2. [AccessibilityService - Used to dispatch gestures like tapping and scrolling](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService)
3. [OpenCV Android 4.5.1 - Used to template match](https://opencv.org/releases/)
4. [Tesseract4Android 2.1.1 - For performing OCR on the screen](https://github.com/adaptech-cz/Tesseract4Android)
5. [AppUpdater 2.7 - For automatically checking and notifying the user for new app updates](https://github.com/javiersantos/AppUpdater)

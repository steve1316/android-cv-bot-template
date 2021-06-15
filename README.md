# Android CV Bot Template

![GitHub commit activity](https://img.shields.io/github/commit-activity/m/steve1316/android-cv-bot-template?logo=GitHub) ![GitHub last commit](https://img.shields.io/github/last-commit/steve1316/android-cv-bot-template?logo=GitHub) ![GitHub issues](https://img.shields.io/github/issues/steve1316/android-cv-bot-template?logo=GitHub) ![GitHub pull requests](https://img.shields.io/github/issues-pr/steve1316/android-cv-bot-template?logo=GitHub) ![GitHub](https://img.shields.io/github/license/steve1316/android-cv-bot-template?logo=GitHub)

> You can visit my Android bot of Granblue Fantasy for more usage details and examples of how to use this framework: https://github.com/steve1316/granblue-automation-android

This template project serves as a starter point for Android botting that relies on Computer Vision template matching via OpenCV and executing gestures like tapping and scrolling to accomplish a automation goal. It uses MediaProjection Service to programmatically take screenshots and the Accessibility Service to execute gestures. The framework is well annotated with documentation to serve as explanations and usage hints.

https://user-images.githubusercontent.com/18709555/118407909-c933be00-b637-11eb-92c2-3c4acd355aff.mp4

# Provided Features
- A Home page that also houses the Message Log to allow the user to see informational logging messages.
- A Settings page that utilizes SharedPreferences to share data across the application and its associated Services.
- Floating overlay button to issue START/STOP signals to the bot process.
- Fleshed out template matching functions via OpenCV
- Notifications to alert users of various changes during workflow.
- Accessibility Service that will allow the bot process to execute gestures on the screen like tapping a specific point.

# Instructions
1. Download the project and extract.
2. Download the Android version of OpenCV and extract its ```sdk``` folder contents into a newly created folder named ```opencv``` in the root of the project folder.
3. ```Clean Project``` and then ```Rebuild Project``` under the Build menu.
4. After building is complete, you can test the capability of this framework in the Android Studio's emulator.
5. After you familiarized yourself with what the framework can do, you can refactor the application's package name in various parts of the project and in ```settings.gradle```.

### Some things to note while developing
1. ```ImageUtils``` class reads in images in ```.webp``` format to reduce apk file size. You can change this if you wish.
2. All images are recommended to be categorized in separate folders inside the /assets/ folder. Be sure to update the ```folderName``` variables inside the various functions in ```ImageUtils```. Or you could remove the need to organize them and just put all image assets into one place. Just make sure to update the code references to the ```folderName``` variables.
3. When working on a horizontal screen, the coordinate axis gets flipped as well. So if your vertical orientation dimensions is 1080x2400, then the horizontal orientation dimensions gets flipped to 2400x1080.

# Important Classes to be familiar with
## BotService
- Facilitates the display and moving of the floating overlay button.
- Able to start/stop the bot process on a new Thread and notify users of bot state changes like Success or Exception encountered.

## ImageUtils
- Able to template match for a single image or multiple image locations on the screen.
- Able to detect text via Google's ML Kit (Read up on the Google's documentation and my usage of it at ```https://github.com/steve1316/granblue-automation-android/blob/main/app/src/main/java/com/steve1316/granblueautomation_android/utils/ImageUtils.kt``` for a better understanding of it)

## MediaProjectionService
- Starts up the MediaProjection Service to allow the ```ImageUtils``` class to programmatically grab screenshots to perform template matching on it.

## MessageLog
- Sends informational logging messages from the ```Game``` class to the Home page of the application to quickly view what is going on.
- Automatically saves logs into text files when the bot stops.

## MyAccessibilityService
- Starts up the Accessibility Service to allow the ```Game``` class to execute gestures at specified (x,y) coordinates on the screen.
  - Supported gestures are: tap, swipe, and scroll via AccessibilityAction.
- Note: If you encounter this Exception: ```kotlin.UninitializedPropertyAccessException: lateinit property instance has not been initialized```, it means that you must have terminated the application via Android Studio. This causes the Accessibility Service to bug out. This does not happen in regular use without interference from Android Studio. To fix this, you can toggle on/off the Accessibility Service until the Toast message pops back up signalling that the Service is now running properly again.

## NotificationUtils
- Allows the bot process to create and update Notifications to notify users of the status of the bot and whether or not the bot encounters an Exception.
- Sends a STOP signal to the bot process from the Notification's button via the ```StopServiceReceiver``` class.
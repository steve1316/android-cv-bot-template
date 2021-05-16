# Android CV Bot Template

![GitHub commit activity](https://img.shields.io/github/commit-activity/m/steve1316/android-cv-bot-template?logo=GitHub) ![GitHub last commit](https://img.shields.io/github/last-commit/steve1316/android-cv-bot-template?logo=GitHub) ![GitHub issues](https://img.shields.io/github/issues/steve1316/android-cv-bot-template?logo=GitHub) ![GitHub pull requests](https://img.shields.io/github/issues-pr/steve1316/android-cv-bot-template?logo=GitHub) ![GitHub](https://img.shields.io/github/license/steve1316/android-cv-bot-template?logo=GitHub)

> You can visit my Android bot of Granblue Fantasy for more usage details and examples of how to use this framework: https://github.com/steve1316/granblue-automation-android

This template project serves as a starter point for Android botting that relies on Computer Vision template matching via OpenCV and executing gestures like tapping and scrolling to accomplish a automation goal. It uses MediaProjection Service to programmatically take screenshots and the Accessibility Service to execute gestures. The framework is well annotated with documentation to serve as explanations and usage hints.

<iframe src="https://player.vimeo.com/video/550041665?badge=0&amp;autopause=0&amp;player_id=0&amp;app_id=58479" width="420" height="886" frameborder="0" allow="fullscreen" allowfullscreen title="Application Demo"></iframe>

# Provided Features
- A Home page that also houses the Message Log to allow the user to see informational logging messages.
- A Settings page that utilizes SharedPreferences to share data across the application and its associated Services.
- Floating overlay button to issue START/STOP signals to the bot process.
- Fleshed out template matching functions via OpenCV
- Notifications to alert users of various changes during workflow.
- Accessibility Service that will allow the bot process to execute gestures on the screen like tapping a specific point.

# Instructions
1. Download the project and extract.
2. Download the Android version of OpenCV and extract it into a newly created folder named ```opencv``` in the root of the project folder.
3. ```Clean Project``` and then ```Rebuild Project``` under the Build menu.
4. After building is complete, you can test the capability of this framework in the Android Studio's emulator.
5. After you familiarized yourself with what the framework can do, you can refactor the application's package name in various parts of the project and in ```settings.gradle```.

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
- Starts up the Accessibility Service to allow the ```Game``` class to execute taps, scrolls, and swipes at specified (x,y) coordinates on the screen.
- Note: If you suddenly encounter an error while trying to execute a gesture, it means that you must have terminated the application via Android Studio. This causes the Accessibility Service to bug out. This does not happen in regular use without interference from Android Studio or you can toggle on/off the Accessibility Service until the Toast message pops up signalling that the Service is now running.

## NotificationUtils
- Allows the bot process to create and update Notifications to notify users of the status of the bot and whether or not the bot encounters an Exception.
- Sends a STOP signal to the bot process from the Notification's button via the ```StopServiceReceiver``` class.
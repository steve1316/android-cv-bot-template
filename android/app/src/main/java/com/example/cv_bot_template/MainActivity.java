package com.example.cv_bot_template;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import com.facebook.react.ReactActivity;
import com.facebook.react.ReactActivityDelegate;
import com.facebook.react.ReactRootView;
import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.zoontek.rnbootsplash.RNBootSplash;

import org.opencv.android.OpenCVLoader;

import java.util.Locale;

public class MainActivity extends ReactActivity {
    public static final String loggerTag = "[TAG]";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RNBootSplash.init(this); // initialize the splash screen for react-native-bootsplash
        super.onCreate(null); // null for react-native-screens

        // Set application locale to combat cases where user's language uses commas instead of decimal points for floating numbers.
        Configuration config = this.getResources().getConfiguration();
        Locale locale = new Locale("en");
        Locale.setDefault(locale);
        this.getResources().updateConfiguration(config, this.getResources().getDisplayMetrics());

        // Set up the app updater to check for the latest update from GitHub.
        new AppUpdater(this)
                .setUpdateFrom(UpdateFrom.XML)
                .setUpdateXML("https://raw.githubusercontent.com/steve1316/android-cv-bot-template/main/android/app/update.xml")
                .start();

        // Load OpenCV native library. This will throw a "E/OpenCV/StaticHelper: OpenCV error: Cannot load info library for OpenCV". It is safe to
        // ignore this error. OpenCV functionality is not impacted by this error.
        OpenCVLoader.initDebug();
    }

    /**
     * Returns the name of the main component registered from JavaScript. This is used to schedule
     * rendering of the component.
     */
    @Override
    protected String getMainComponentName() {
        return "CV Bot Template";
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Returns the instance of the {@link ReactActivityDelegate}. There the RootView is created and
     * you can specify the renderer you wish to use - the new renderer (Fabric) or the old renderer
     * (Paper).
     */
    @Override
    protected ReactActivityDelegate createReactActivityDelegate() {
        return new MainActivityDelegate(this, getMainComponentName());
    }

    public static class MainActivityDelegate extends ReactActivityDelegate {
        public MainActivityDelegate(ReactActivity activity, String mainComponentName) {
            super(activity, mainComponentName);
        }

        @Override
        protected ReactRootView createRootView() {
            ReactRootView reactRootView = new ReactRootView(getContext());
            // If you opted-in for the New Architecture, we enable the Fabric Renderer.
            reactRootView.setIsFabric(BuildConfig.IS_NEW_ARCHITECTURE_ENABLED);
            return reactRootView;
        }

        @Override
        protected boolean isConcurrentRootEnabled() {
            // If you opted-in for the New Architecture, we enable Concurrent Root (i.e. React 18).
            // More on this on https://reactjs.org/blog/2022/03/29/react-v18.html
            return BuildConfig.IS_NEW_ARCHITECTURE_ENABLED;
        }
    }
}

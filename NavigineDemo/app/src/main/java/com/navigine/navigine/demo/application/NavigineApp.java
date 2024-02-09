package com.navigine.navigine.demo.application;

import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.navigine.navigine.demo.utils.DimensionUtils;
import com.navigine.sdk.Navigine;

public class NavigineApp extends Application implements LifecycleObserver {

    public static Context AppContext = null;

    @Override
    public void onCreate() {
        super.onCreate();

        AppContext = getApplicationContext();

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        DimensionUtils.setDisplayMetrics(displayMetrics);

        Navigine.initialize(getApplicationContext());

        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private void onEnterForeground() {
        try {
            Navigine.setMode(Navigine.Mode.NORMAL);
        } catch (Throwable e) {
            Log.e("NavigineSDK", "Navigine SDK is not initialized yet");
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private void onResume() {
        try {
            Navigine.setMode(Navigine.Mode.NORMAL);
        } catch (Throwable e) {
            Log.e("NavigineSDK", "Navigine SDK is not initialized yet");
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private void onPause() {
        try {
            Navigine.setMode(Navigine.Mode.BACKGROUND);
        } catch (Throwable e) {
            Log.e("NavigineSDK", "Navigine SDK is not initialized yet");
        }
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private void onEnterBackground() {
        try {
            Navigine.setMode(Navigine.Mode.BACKGROUND);
        } catch (Throwable e) {
            Log.e("NavigineSDK", "Navigine SDK is not initialized yet");
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private void onDestroy() {
        try {
            Navigine.setMode(Navigine.Mode.BACKGROUND);
        } catch (Throwable e) {
            Log.e("NavigineSDK", "Navigine SDK is not initialized yet");
        }
    }
}

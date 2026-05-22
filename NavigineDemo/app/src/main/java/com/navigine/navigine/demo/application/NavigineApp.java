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

        Navigine.initialize(getApplicationContext(), true);

        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }
}

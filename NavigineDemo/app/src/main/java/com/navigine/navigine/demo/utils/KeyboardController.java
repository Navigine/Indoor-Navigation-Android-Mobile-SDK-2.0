package com.navigine.navigine.demo.utils;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.app.Activity;
import android.view.inputmethod.InputMethodManager;

public class KeyboardController {

    public static void hideSoftKeyboard(Activity activity) {
        if (activity.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }
}

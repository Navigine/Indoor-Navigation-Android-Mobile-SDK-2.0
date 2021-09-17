package com.navigine.navigine.demo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.widget.AppCompatEditText;

import java.util.Objects;

public class CustomEditText extends AppCompatEditText
{

    public CustomEditText(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.setOnEditorActionListener((v, actionId, event) ->
        {
            if (actionId == KeyEvent.KEYCODE_ENDCALL)
            {
                InputMethodManager imm = (InputMethodManager) v.getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                Objects.requireNonNull(imm).hideSoftInputFromWindow(CustomEditText.this.getWindowToken(), 0);
                CustomEditText.this.clearFocus();
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP)
            this.clearFocus();
        return super.onKeyPreIme(keyCode, event);
    }

}


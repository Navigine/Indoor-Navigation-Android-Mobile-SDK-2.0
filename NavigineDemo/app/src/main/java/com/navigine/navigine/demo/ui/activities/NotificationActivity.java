package com.navigine.navigine.demo.ui.activities;

import static com.navigine.navigine.demo.utils.Constants.NOTIFICATION_IMAGE;
import static com.navigine.navigine.demo.utils.Constants.NOTIFICATION_TEXT;
import static com.navigine.navigine.demo.utils.Constants.NOTIFICATION_TITLE;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.navigine.navigine.demo.R;

public class NotificationActivity extends AppCompatActivity {

    private ImageView mBackBtn = null;
    private ImageView mImg     = null;
    private TextView  mTitle   = null;
    private TextView  mText    = null;

    private Bundle extras = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        extras = getIntent().getExtras();
        if (extras == null) finish();

        setContentView(R.layout.activity_notification);

        initViews();
        setViewsParams();
        setViewsListeners();
    }


    private void initViews() {
        mBackBtn = findViewById(R.id.activity_notification_back);
        mTitle   = findViewById(R.id.activity_notification_title);
        mImg     = findViewById(R.id.activity_notification_img);
        mText    = findViewById(R.id.activity_notification_text);
    }

    private void setViewsParams() {
        mTitle.setText(extras.getString(NOTIFICATION_TITLE));
        mText. setText(extras.getString(NOTIFICATION_TEXT));

        Glide.with(this).load(extras.getString(NOTIFICATION_IMAGE)).onlyRetrieveFromCache(true).centerInside().into(mImg);
    }

    private void setViewsListeners() {
        mBackBtn.setOnClickListener(v -> onBackPressed());
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
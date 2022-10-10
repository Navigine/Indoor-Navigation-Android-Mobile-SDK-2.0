package com.navigine.navigine.demo.ui.activities;

import static com.navigine.navigine.demo.utils.Constants.DL_QUERY_LOCATION_ID;
import static com.navigine.navigine.demo.utils.Constants.DL_QUERY_SERVER;
import static com.navigine.navigine.demo.utils.Constants.DL_QUERY_SUBLOCATION_ID;
import static com.navigine.navigine.demo.utils.Constants.DL_QUERY_USERHASH;
import static com.navigine.navigine.demo.utils.Constants.DL_QUERY_VENUE_ID;
import static com.navigine.navigine.demo.utils.Constants.ENDPOINT_GET_USER;
import static com.navigine.navigine.demo.utils.Constants.TAG;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavDeepLinkBuilder;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;
import com.navigine.camera.ui.activity.BarcodeScannerActivity;
import com.navigine.navigine.demo.R;
import com.navigine.navigine.demo.models.UserSession;
import com.navigine.navigine.demo.ui.dialogs.sheets.BottomSheetHost;
import com.navigine.navigine.demo.utils.NavigineSdkManager;
import com.navigine.navigine.demo.utils.NetworkUtils;
import com.navigine.navigine.demo.utils.PermissionUtils;

import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private final int WARNING_DURATION = 1500;

    private TextInputLayout           mLoginField        = null;
    private EditText                  mUserHash          = null;
    private Button                    mLoginButton       = null;
    private BottomSheetHost mDialog            = null;
    private TextView                  mEditHost          = null;
    private TextView                  mWarningTv         = null;
    private CircularProgressIndicator mProgressIndicator = null;

    private final View.OnClickListener mLoginClickListener = v -> onHandleLoginAction();

    private final ActivityResultLauncher<String[]> permissionLauncher
            = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), this::onRequestPermissions);

    private final ActivityResultLauncher<String> permissionLauncherCamera
            = registerForActivityResult(new ActivityResultContracts.RequestPermission(), this::onRequestCamera);

    private final ActivityResultLauncher<Intent> qrScannerLauncher
            = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::onHandleQrScanResult);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setViewsParams();
        setViewsListeners();
        permissionsRequest();
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    private void permissionsRequest() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH_SCAN});
        } else {
            permissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION});
        }
    }

    private void setViewsListeners() {

        mLoginField.setEndIconOnClickListener(v -> permissionLauncherCamera.launch(Manifest.permission.CAMERA));

        mLoginButton.setOnClickListener(mLoginClickListener);

        mUserHash.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mLoginButton.setEnabled(charSequence.length() != 0);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                updateUserHash(editable.toString().trim());
            }
        });

        mEditHost.setOnClickListener(view -> showBottomSheet());
    }

    private void initViews() {
        mWarningTv         = findViewById(R.id.login__warning_tv);
        mLoginField        = findViewById(R.id.login__login_layout);
        mUserHash          = findViewById(R.id.login__login_edit);
        mLoginButton       = findViewById(R.id.login__done_button);
        mProgressIndicator = findViewById(R.id.login__progress);
        mEditHost          = findViewById(R.id.login__change_host);
        mDialog            = new BottomSheetHost();
    }

    private void setViewsParams() {
        mUserHash.setText(UserSession.USER_HASH.trim());
    }

    private void showBottomSheet() {
        mDialog.show(getSupportFragmentManager(), null);
    }

    private void onHandleLoginAction() {
        showLoginProgress();
        if (NetworkUtils.isNetworkActive(this)) tryLogin();
        else {
            showTempWarningMessage(getString(R.string.err_network_no_connection));
            hideLoginProgress();
        }
    }

    private void tryLogin() {
        String url = UserSession.LOCATION_SERVER + ENDPOINT_GET_USER + UserSession.USER_HASH;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> onHandleCheckUserHashResponse(),
                error -> onHandleCheckUserHashError(error));
        Volley.newRequestQueue(this).add(request);
    }

    private void onHandleCheckUserHashError(VolleyError error) {
        if (error instanceof AuthFailureError) {
            showTempWarningMessage(getString(R.string.err_network_auth));
        }
        hideLoginProgress();
    }

    private void onHandleCheckUserHashResponse() {
        if (sdkInit()) openMainScreen();
        else {
            showTempWarningMessage(getString(R.string.err_sdk_not_init));
            hideLoginProgress();
        }
    }

    private void openMainScreen() {
        startActivity(new Intent(this, MainActivity.class));
        finishAffinity();
    }

    private void showTempWarningMessage(String message) {
        showWarningMessage(message);
        mWarningTv.postDelayed(this::hideWarningMessage, WARNING_DURATION);
    }

    private void showWarningMessage(String message) {
        mWarningTv.setText(message);
        mWarningTv.setVisibility(View.VISIBLE);
    }

    private void hideWarningMessage() {
        mWarningTv.setVisibility(View.INVISIBLE);
    }

    private boolean sdkInit() {
        return NavigineSdkManager.initializeSdk();
    }

    private void showLoginProgress() {
        mLoginButton.setText(null);
        mLoginButton.setOnClickListener(null);
        mProgressIndicator.show();
    }

    private void hideLoginProgress() {
        mProgressIndicator.hide();
        mLoginButton.setText(getString(R.string.login_btn_title));
        mLoginButton.setOnClickListener(mLoginClickListener);
    }

    private void updateUserHash(String value) {
        UserSession.USER_HASH = value;
    }

    private void onRequestPermissions(Map<String, Boolean> result) {
        for (Map.Entry<String, Boolean> permissionEntry : result.entrySet()) {
            switch (permissionEntry.getKey()) {
                case Manifest.permission.ACCESS_COARSE_LOCATION:
                case Manifest.permission.ACCESS_FINE_LOCATION:
                    if (permissionEntry.getValue()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                            if (!PermissionUtils.hasLocationBackgroundPermission(this))
                                PermissionUtils.showBackgroundPermissionRationale(this);
                    }
                    break;
                case Manifest.permission.BLUETOOTH_SCAN:
                    if (!permissionEntry.getValue()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                            PermissionUtils.showBluetoothPermissionRationale(this);
                    }
                    break;
            }
        }
    }

    private void onRequestCamera(Boolean result) {
        if (result)
            qrScannerLauncher.launch(new Intent(LoginActivity.this, BarcodeScannerActivity.class));
        else
            showTempWarningMessage(getString(R.string.err_permission_camera));
    }

    private void onHandleQrScanResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            Uri data = result.getData().getData();
            if (data != null) {
                try {
                    String locationServer = data.getQueryParameter(DL_QUERY_SERVER);
                    String userHash       = data.getQueryParameter(DL_QUERY_USERHASH);
                    String venueId        = data.getQueryParameter(DL_QUERY_VENUE_ID);

                    UserSession.LOCATION_SERVER = locationServer;
                    UserSession.USER_HASH       = userHash;

                    Bundle bundle = new Bundle();
                    bundle.putString(DL_QUERY_LOCATION_ID, data.getQueryParameter(DL_QUERY_LOCATION_ID));
                    bundle.putString(DL_QUERY_SUBLOCATION_ID, data.getQueryParameter(DL_QUERY_SUBLOCATION_ID));
                    bundle.putString(DL_QUERY_VENUE_ID, data.getQueryParameter(DL_QUERY_VENUE_ID));

                    if (NavigineSdkManager.initializeSdk()) {
                        try {
                            createDeepLink(bundle).send();
                            finishAffinity();
                        } catch (PendingIntent.CanceledException e) {
                            Log.e(TAG, getString(R.string.err_deep_link_send));
                        }
                    }
                } catch (UnsupportedOperationException | NullPointerException e) {
                    Log.e(TAG, getString(R.string.err_deep_link_parse));
                }
            }
        }
    }

    private PendingIntent createDeepLink(Bundle bundle) {
        return new NavDeepLinkBuilder(this)
                .setGraph(R.navigation.navigation_navigation)
                .setDestination(R.id.navigation_navigation)
                .setArguments(bundle)
                .setComponentName(MainActivity.class)
                .createPendingIntent();
    }

}
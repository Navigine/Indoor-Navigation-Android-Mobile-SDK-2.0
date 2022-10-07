package com.navigine.navigine.demo.ui.activities;

import static com.navigine.navigine.demo.utils.Constants.DL_QUERY_SERVER;
import static com.navigine.navigine.demo.utils.Constants.DL_QUERY_USERHASH;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.navigine.camera.ui.activity.BarcodeScannerActivity;
import com.navigine.navigine.demo.R;
import com.navigine.navigine.demo.models.UserSession;
import com.navigine.navigine.demo.ui.dialogs.sheets.HostBottomSheet;
import com.navigine.navigine.demo.utils.NavigineSdkManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout mLoginField    = null;
    private EditText        mUserHash      = null;
    private Button          mLoginButton   = null;
    private HostBottomSheet mDialog        = null;
    private TextView        mEditHost      = null;

    private List<String> PERMISSIONS = new ArrayList<>(Arrays.asList
            (Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION));

    private ActivityResultLauncher<Intent> qrScannerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            Intent dlIntent = new Intent(result.getData());
            dlIntent.setClass(this, MainActivity.class);
            Uri qrData = dlIntent.getData();
            if (qrData != null) {
                UserSession.LOCATION_SERVER = qrData.getQueryParameter(DL_QUERY_SERVER);
                UserSession.USER_HASH       = qrData.getQueryParameter(DL_QUERY_USERHASH);
            }

            if (NavigineSdkManager.initializeSdk())
                startActivity(dlIntent);
        }
    });

    private ActivityResultLauncher<String[]> requestPermissionLauncher
            = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {

        for (Map.Entry<String, Boolean> permissionEntry : result.entrySet()) {

            String permission = permissionEntry.getKey();
            boolean granted = permissionEntry.getValue();

            switch (permission) {
                case Manifest.permission.ACCESS_COARSE_LOCATION:
                case Manifest.permission.ACCESS_FINE_LOCATION:
                    if (granted) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 1000);
                        }
                    } else {
                        new MaterialAlertDialogBuilder(this).setTitle(R.string.dialog_permission_title_location)
                                .setMessage(R.string.dialog_permission_body_location)
                                .setPositiveButton(R.string.dialog_permission_ok, null)
                                .show();
                    }
                    break;
                case Manifest.permission.ACCESS_BACKGROUND_LOCATION:
                    if (!granted) {
                        new MaterialAlertDialogBuilder(this).setTitle(R.string.dialog_permission_title_location_bg)
                                .setMessage(R.string.dialog_permission_body_location_bg)
                                .setPositiveButton(R.string.dialog_permission_ok, null)
                                .show();
                    }
                    break;
                case Manifest.permission.BLUETOOTH_SCAN:
                case Manifest.permission.BLUETOOTH_CONNECT:
                    if (!granted) {
                        new MaterialAlertDialogBuilder(this).setTitle(R.string.dialog_permission_title_bluetooth)
                                .setMessage(R.string.dialog_permission_body_bluetooth)
                                .setPositiveButton(R.string.dialog_permission_ok, null)
                                .show();
                    }
                    break;
                case Manifest.permission.CAMERA:
                    if (granted) qrScannerLauncher.launch(new Intent(LoginActivity.this, BarcodeScannerActivity.class));
                    break;
            }
        }
    });

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            PERMISSIONS.addAll(Arrays.asList(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT));
        requestPermissionLauncher.launch(PERMISSIONS.toArray(new String[]{}));
    }

    private void setViewsListeners() {

        mLoginField.setEndIconOnClickListener(v -> requestPermissionLauncher.launch(new String[]{Manifest.permission.CAMERA}));

        mLoginButton.setOnClickListener(view -> {
            if (!NavigineSdkManager.initializeSdk()) {
                Toast.makeText(this, R.string.err_sdk_not_init, Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        mUserHash.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mLoginButton.setEnabled(charSequence.length() != 0);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                UserSession.USER_HASH = editable.toString().trim();
            }
        });

        mEditHost.setOnClickListener(view -> showBottomSheet());
    }

    private void initViews() {
        mLoginField  = findViewById(R.id.login__login_layout);
        mUserHash    = findViewById(R.id.login__login_edit);
        mLoginButton = findViewById(R.id.login__done_button);
        mEditHost    = findViewById(R.id.login__change_host);
        mDialog      = new HostBottomSheet();
    }

    private void setViewsParams() {
        mUserHash.setText(UserSession.USER_HASH.trim());
    }

    private void showBottomSheet() {
        mDialog.show(getSupportFragmentManager(), null);
    }
}
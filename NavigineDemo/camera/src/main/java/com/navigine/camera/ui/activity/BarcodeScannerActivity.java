package com.navigine.camera.ui.activity;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.common.internal.Objects;
import com.google.android.material.chip.Chip;
import com.navigine.camera.R;
import com.navigine.camera.camera.BarcodeProcessor;
import com.navigine.camera.camera.CameraSource;
import com.navigine.camera.ui.custom.CameraSourcePreview;
import com.navigine.camera.ui.custom.GraphicOverlay;
import com.navigine.camera.viewmodel.CameraViewModel;

import java.io.IOException;

public class BarcodeScannerActivity extends AppCompatActivity {

    private CameraViewModel cameraViewModel = null;
    private CameraViewModel.CameraState currentCameraState = null;

    private CameraSourcePreview preview           = null;
    private GraphicOverlay      graphicOverlay    = null;
    private Chip                promtChip         = null;
    private ImageView           closeBtn          = null;
    private ImageView           flashBtn          = null;
    private AnimatorSet         promtChipAnimator = null;
    private CameraSource        cameraSource      =  null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.activity_barcode_scanner);
        initcameraViewModels();
        initViews();
        setViewsParams();
        setViewsListeners();
        setObservers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraViewModel.markCameraFrozen();
        currentCameraState = CameraViewModel.CameraState.NOT_STARTED;
        cameraSource.   setFrameProcessor(new BarcodeProcessor(graphicOverlay, cameraViewModel));
        cameraViewModel.setCameraState(CameraViewModel.CameraState.DETECTING);
    }

    @Override
    protected void onPause() {
        super.onPause();
        currentCameraState = CameraViewModel.CameraState.NOT_STARTED;
        stopCameraPreview();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) cameraSource.release();
        cameraSource = null;
    }

    private void initcameraViewModels() {
        cameraViewModel = new ViewModelProvider(this).get(CameraViewModel.class);
    }

    private void initViews() {
        preview        = findViewById(R.id.camera_preview);
        graphicOverlay = findViewById(R.id.camera_preview_graphic_overlay);
        promtChip      = findViewById(R.id.bottom_prompt_chip);
        closeBtn       = findViewById(R.id.camera_close_button);
        flashBtn       = findViewById(R.id.camera_flash_button);

        cameraSource = new CameraSource(graphicOverlay);
        promtChipAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.camera_bottom_prompt_chip_enter);
    }

    private void setViewsParams() {
        promtChipAnimator.setTarget(promtChip);
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            flashBtn.setSelected(false);
            flashBtn.setEnabled(false);
        }
    }

    private void setViewsListeners() {
        closeBtn.setOnClickListener(view -> onBackPressed());
        flashBtn.setOnClickListener(view -> {
            flashBtn.setSelected(!flashBtn.isSelected());
            cameraSource.updateFlashMode(flashBtn.isSelected() ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
        });
    }

    private void setObservers() {
        cameraViewModel.cameraState.observe(this, cameraState -> {
            if (cameraState == null || Objects.equal(currentCameraState, cameraState)) return;
            currentCameraState = cameraState;

            boolean wasPromptChipGone = promtChip.getVisibility() == View.GONE;

            switch (cameraState) {
                case DETECTING: {
                    promtChip.setVisibility(View.VISIBLE);
                    promtChip.setText(R.string.camera_barcode_detecting);
                    startCameraPreview();
                    break;
                }
                case CONFIRMING: {
                    promtChip.setVisibility(View.VISIBLE);
                    promtChip.setText(R.string.camera_barcode_confirming);
                    startCameraPreview();
                    break;
                }
                case SEARCHING: {
                    promtChip.setVisibility(View.VISIBLE);
                    promtChip.setText(R.string.camera_barcode_searching);
                    stopCameraPreview();
                    break;
                }
                case DETECTED:
                case SEARCHED:
                    promtChip.setVisibility(View.GONE);
                    stopCameraPreview();
                    break;
                default:
                    promtChip.setVisibility(View.GONE);
                    break;
            }

            boolean playChipAnimation = wasPromptChipGone && promtChip.getVisibility() == View.VISIBLE;
            if (playChipAnimation && !promtChipAnimator.isRunning()) promtChipAnimator.start();
        });

        cameraViewModel.detectedBarcode.observe(this, barcode -> {
            if (barcode != null) {
                Intent intent = new Intent();
                intent.setData(Uri.parse(barcode.getRawValue()));
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }


    private void startCameraPreview() {
        if (cameraSource != null) {
            if (!cameraViewModel.isCameraLive()) {
                try {
                    cameraViewModel.markCameraLive();
                    if (preview != null) {
                        preview.start(cameraSource);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    cameraSource.release();
                    cameraSource = null;
                }
            }
        }
    }

    private void stopCameraPreview() {
        if (cameraViewModel.isCameraLive()) {
            cameraViewModel.markCameraFrozen();
            flashBtn.setSelected(false);
            if (preview != null) preview.stop();
        }
    }
}
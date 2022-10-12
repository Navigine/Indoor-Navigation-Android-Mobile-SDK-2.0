package com.navigine.navigine.demo.ui.dialogs.sheets;

import static com.navigine.navigine.demo.utils.Constants.ENDPOINT_HEALTH_CHECK;
import static com.navigine.navigine.demo.utils.Constants.HOST_VERIFY_TAG;
import static com.navigine.navigine.demo.utils.Constants.SIZE_FAILED;
import static com.navigine.navigine.demo.utils.Constants.SIZE_SUCCESS;
import static com.navigine.navigine.demo.utils.Constants.TAG;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.transition.ChangeBounds;
import androidx.transition.TransitionManager;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.navigine.navigine.demo.R;
import com.navigine.navigine.demo.models.UserSession;
import com.navigine.navigine.demo.utils.DimensionUtils;
import com.navigine.navigine.demo.utils.NetworkUtils;

public class BottomSheetHost extends BottomSheetDialogFragment {

    private EditText                  mHostEdit         = null;
    private MaterialButton            mCloseButton      = null;
    private TextView                  mSubtitle         = null;
    private MaterialButton            mChangeButton     = null;
    private FrameLayout               animContainer     = null;
    private CircularProgressIndicator progressIndicator = null;
    private LottieAnimationView       statusAnim        = null;
    private GradientDrawable          mHostEditStroke   = null;

    private int colorSuccess;
    private int colorFailed;

    private RequestQueue requestQueue = null;

    private TextWatcher textWatcherHost  = null;

    private final int ANIM_DURATION = 500;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_bottom_host, container, false);
        initViews(view);
        initViewsListeners();
        setViewsListeners();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        setViewsParams();
    }

    private void initViews(View view) {
        mSubtitle         = view.findViewById(R.id.host__sheet_subtitle);
        mCloseButton      = view.findViewById(R.id.host__sheet_close_button);
        mHostEdit         = view.findViewById(R.id.host__sheet_edit);
        mChangeButton     = view.findViewById(R.id.host__sheet_change_button);
        animContainer     = view.findViewById(R.id.host__anim_container);
        progressIndicator = view.findViewById(R.id.host__progress_circular_indicator);
        statusAnim        = view.findViewById(R.id.host__anim_status);
        mHostEditStroke   = (GradientDrawable) mHostEdit.getBackground();
    }

    private void initViewsListeners() {
        textWatcherHost = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mChangeButton.setEnabled(charSequence.length() >= 1);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        };
    }

    private void setViewsListeners() {
        statusAnim.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                hideStatusAnimation();
                dismiss();
            }
        });
        mCloseButton.setOnClickListener((view) -> {
            if (requestQueue != null) requestQueue.cancelAll(HOST_VERIFY_TAG);
            dismiss();
        });

        mHostEdit.addTextChangedListener(textWatcherHost);

        mChangeButton.setOnClickListener((view) ->
        {
            cancelHealthCheckRequest();
            showLoadingAnimation();
            disableChangeHostButton();

            if (NetworkUtils.isNetworkActive(view.getContext())) {
                sendHealthCheckRequest(createHealthCheckRequest());
            } else {
                hideLoadingAnimation();
                updateHostField(false, getString(R.string.err_network_no_connection));
            }
        });
    }

    private void setViewsParams() {
        colorSuccess = ContextCompat.getColor(requireActivity(), R.color.colorSuccess);
        colorFailed = ContextCompat.getColor(requireActivity(), R.color.colorError);
        mHostEdit.setText(UserSession.LOCATION_SERVER);
    }

    private void hideStatusAnimation() {
        TransitionManager.beginDelayedTransition((ViewGroup) getView(), new ChangeBounds());
        statusAnim.clearAnimation();
        statusAnim.setVisibility(View.GONE);
        animContainer.setVisibility(View.GONE);
    }

    private void showLoadingAnimation() {
        TransitionManager.beginDelayedTransition((ViewGroup) getView(), new ChangeBounds());
        animContainer.setVisibility(View.VISIBLE);
        progressIndicator.show();
    }

    private void hideLoadingAnimation() {
        progressIndicator.hide();
    }

    private void cancelHealthCheckRequest() {
        if (requestQueue != null) requestQueue.cancelAll(HOST_VERIFY_TAG);
    }

    private Request<String> createHealthCheckRequest() {
        if (requestQueue == null) requestQueue = Volley.newRequestQueue(requireActivity());

        StringRequest stringRequest = new StringRequest(Request.Method.HEAD, getHealthCheckUrl(),
                response -> onHealthCheckSuccess(),
                error -> onHealthCheckFail(error));

        stringRequest.setTag(HOST_VERIFY_TAG);

        return stringRequest;
    }

    private void sendHealthCheckRequest(Request<String> request) {
        if (requestQueue != null) requestQueue.add(request);
    }

    private void onHealthCheckFail(VolleyError error) {
        hideLoadingAnimation();
        updateHostField(false, getErrorMessage(error));
        Log.e(TAG, error.toString());
    }

    private void onHealthCheckSuccess() {
        updateLocationServer();
        hideLoadingAnimation();
        updateHostField(true, getString(R.string.server_correct));
    }

    private void updateLocationServer() {
        UserSession.LOCATION_SERVER = mHostEdit.getText().toString();
    }

    private void disableChangeHostButton() {
        mChangeButton.setEnabled(false);
    }

    @NonNull
    private String getHealthCheckUrl() {
        return mHostEdit.getText().toString() + ENDPOINT_HEALTH_CHECK;
    }

    private void updateHostField(boolean isOperationSuccess, String infoMessage) {

        int animRes, animSize, animColor;
        float animStartProgress;

        if (isOperationSuccess) {
            animRes = R.raw.verify;
            animSize = (int) DimensionUtils.pxFromDp(SIZE_SUCCESS);
            animColor = colorSuccess;
            animStartProgress = 0f;
        } else {
            animRes = R.raw.failed;
            animSize = (int) DimensionUtils.pxFromDp(SIZE_FAILED);
            animColor = colorFailed;
            animStartProgress = .2f;
        }

        animContainer.postDelayed(() -> {
            statusAnim.setAnimation(animRes);
            statusAnim.getLayoutParams().height = animSize;
            statusAnim.setMinProgress(animStartProgress);
            statusAnim.setVisibility(View.VISIBLE);
            statusAnim.playAnimation();
            mSubtitle.setText(infoMessage);
            mSubtitle.setTextColor(animColor);
            mHostEditStroke.setStroke(4, animColor);
        }, ANIM_DURATION);
    }

    @NonNull
    private String getErrorMessage(VolleyError error) {
        String message;

        if (error instanceof NetworkError) {
            message = getString(R.string.err_network_server_incorrect);
        } else if (error instanceof ServerError) {
            message = getString(R.string.err_network_server);
        } else if (error instanceof AuthFailureError) {
            message = getString(R.string.err_network_auth);
        } else if (error instanceof ParseError) {
            message = getString(R.string.err_network_parse);
        } else if (error instanceof NoConnectionError) {
            message = getString(R.string.err_network_no_connection);
        } else if (error instanceof TimeoutError) {
            message = getString(R.string.err_network_timeout);
        } else message = getString(R.string.err_network_server_incorrect);

        return message;
    }
}

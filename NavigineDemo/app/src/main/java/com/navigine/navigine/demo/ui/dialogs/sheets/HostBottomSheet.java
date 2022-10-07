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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.navigine.navigine.demo.R;
import com.navigine.navigine.demo.models.UserSession;
import com.navigine.navigine.demo.utils.DimensionUtils;

public class HostBottomSheet extends BottomSheetDialogFragment {

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestQueue = Volley.newRequestQueue(requireActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_host, container, false);
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
                if (charSequence.length() >= 1) {
                    mChangeButton.setEnabled(true);
                } else {
                    mChangeButton.setEnabled(false);
                }
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
                super.onAnimationEnd(animation);
                TransitionManager.beginDelayedTransition((ViewGroup) getView(), new ChangeBounds());
                statusAnim.clearAnimation();
                statusAnim.setVisibility(View.GONE);
                animContainer.setVisibility(View.GONE);
                dismiss();
            }
        });
        mCloseButton.setOnClickListener((view) -> {
            requestQueue.cancelAll(HOST_VERIFY_TAG);
            dismiss();
        });

        mHostEdit.addTextChangedListener(textWatcherHost);

        mChangeButton.setOnClickListener((view) ->
        {

            requestQueue.cancelAll(HOST_VERIFY_TAG);
            String url = mHostEdit.getText().toString() + ENDPOINT_HEALTH_CHECK;

            StringRequest stringRequest = new StringRequest(Request.Method.HEAD, url,
                    response -> {
                        progressIndicator.hide();
                        animContainer.postDelayed(() -> {
                            statusAnim.setAnimation(R.raw.verify);
                            float pixels = DimensionUtils.pxFromDp(SIZE_SUCCESS);
                            statusAnim.getLayoutParams().height = (int) pixels;
                            statusAnim.setMinProgress(0);
                            statusAnim.setVisibility(View.VISIBLE);
                            statusAnim.playAnimation();
                            mSubtitle.setText(R.string.server_correct);
                            mSubtitle.setTextColor(colorSuccess);
                            mHostEditStroke.setStroke(4, colorSuccess);

                            UserSession.LOCATION_SERVER = mHostEdit.getText().toString();
                        }, 500);
                    },
                    error -> {
                        progressIndicator.hide();
                        animContainer.postDelayed(() -> {
                            statusAnim.setAnimation(R.raw.failed);
                            float pixels = DimensionUtils.pxFromDp(SIZE_FAILED);
                            statusAnim.getLayoutParams().height = (int) pixels;
                            statusAnim.setMinProgress(.2f);
                            statusAnim.setVisibility(View.VISIBLE);
                            statusAnim.playAnimation();
                            mSubtitle.setText(R.string.server_incorrect);
                            mSubtitle.setTextColor(colorFailed);
                            mHostEditStroke.setStroke(4, colorFailed);
                        }, 500);

                        String message = null;

                        if (error instanceof NetworkError) {
                            message = "Cannot connect to host...Please check your connection!";
                        } else if (error instanceof ServerError) {
                            message = "The server could not be found. Please try again after some time";
                        } else if (error instanceof AuthFailureError) {
                            message = "Authentication error";
                        } else if (error instanceof ParseError) {
                            message = "Parsing error. Please try again after some time";
                        } else if (error instanceof NoConnectionError) {
                            message = "Cannot connect to Internet...Please check your connection";
                        } else if (error instanceof TimeoutError) {
                            message = "Connection TimeOut. Please check your internet connection";
                        } else message = "Cannot connect to host";

                        Log.e(TAG, message);
                    });

            stringRequest.setTag(HOST_VERIFY_TAG);
            TransitionManager.beginDelayedTransition((ViewGroup) getView(), new ChangeBounds());
            animContainer.setVisibility(View.VISIBLE);
            progressIndicator.show();
            mChangeButton.setEnabled(false);
            requestQueue.add(stringRequest);
        });
    }

    private void setViewsParams() {
        colorSuccess = ContextCompat.getColor(requireActivity(), R.color.colorSuccess);
        colorFailed = ContextCompat.getColor(requireActivity(), R.color.colorError);
        mHostEdit.setText(UserSession.LOCATION_SERVER);
    }
}

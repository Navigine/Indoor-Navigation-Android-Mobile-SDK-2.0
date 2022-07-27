package com.navigine.navigine.demo.ui.fragments;

import static com.navigine.navigine.demo.utils.Constants.ENDPOINT_GET_USER;
import static com.navigine.navigine.demo.utils.Constants.RESPONSE_KEY_AVATAR;
import static com.navigine.navigine.demo.utils.Constants.RESPONSE_KEY_COMPANY;
import static com.navigine.navigine.demo.utils.Constants.RESPONSE_KEY_EMAIl;
import static com.navigine.navigine.demo.utils.Constants.RESPONSE_KEY_HASH;
import static com.navigine.navigine.demo.utils.Constants.RESPONSE_KEY_NAME;
import static com.navigine.navigine.demo.utils.Constants.TAG;

import android.animation.LayoutTransition;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.navigine.navigine.demo.BuildConfig;
import com.navigine.navigine.demo.R;
import com.navigine.navigine.demo.models.UserSession;

public class ProfileFragment extends Fragment
{
    private ClipboardManager clipboardManager = null;

    private Window             window          = null;
    private ShapeableImageView mUserImage      = null;
    private TextView           mUserName       = null;
    private TextView           mUserCompany    = null;
    private TextView           mUserHash       = null;
    private TextView           mUserEmail      = null;
    private ImageButton        mCopy           = null;

    private static final int AVATAR_PADDING = 16;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clipboardManager = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initViews(view);
        setViewsParams(view);
        setViewsListeners();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateUserInfo();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden)
            window.setStatusBarColor(ContextCompat.getColor(requireActivity(), R.color.colorBackground));
    }

    private void initViews(View view) {
        window          = requireActivity().getWindow();
        mUserHash       = view.findViewById(R.id.profile__user_id);
        mUserEmail      = view.findViewById(R.id.profile__user_email);
        mCopy           = view.findViewById(R.id.profile__copy);
        mUserCompany    = view.findViewById(R.id.profile__user_company);
        mUserName       = view.findViewById(R.id.profile__user_name);
        mUserImage      = view.findViewById(R.id.profile__user_image);
    }

    private void setViewsParams(View view) {
        ((ConstraintLayout) view.findViewById(R.id.profile__main_view)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            ViewCompat.setElevation(mUserImage, 4f);
        }
    }

    private void setViewsListeners() {
        mCopy.setOnClickListener(v -> {
            ClipData data = ClipData.newPlainText("user hash", mUserHash.getText());
            clipboardManager.setPrimaryClip(data);
            Snackbar.make(v, R.string.copied, 500)
                    .setBackgroundTint(ContextCompat.getColor(requireActivity(), R.color.colorPrimary))
                    .setTextColor(Color.WHITE)
                    .setAnchorView(R.id.main__bottom_navigation)
                    .show();
        });
    }

    private void updateUserInfo() {
        String url = UserSession.LOCATION_SERVER + ENDPOINT_GET_USER + UserSession.USER_HASH;

        JsonObjectRequest getUserRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, response ->
                {
                    String avatarUrl = response.optString(RESPONSE_KEY_AVATAR, "");

                    Glide
                            .with(requireActivity())
                            .load(avatarUrl)
                            .apply(RequestOptions.circleCropTransform().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true))
                            .placeholder(R.drawable.ic_avatar_mock)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    mUserImage.setContentPadding(AVATAR_PADDING, AVATAR_PADDING, AVATAR_PADDING, AVATAR_PADDING);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    return false;
                                }
                            })
                            .into(mUserImage);

                    mUserHash.   setText(response.optString(RESPONSE_KEY_HASH, BuildConfig.DEFAULT_USER_HASH));
                    mUserName.   setText(response.optString(RESPONSE_KEY_NAME, ""));
                    mUserCompany.setText(response.optString(RESPONSE_KEY_COMPANY, ""));
                    mUserEmail.  setText(response.optString(RESPONSE_KEY_EMAIl, ""));


                }, error -> {
                    if (error instanceof AuthFailureError) {
                        Log.e(TAG, getString(R.string.err_profile_update_auth));
                        UserSession.USER_HASH = BuildConfig.DEFAULT_USER_HASH;
                    }
                    mUserHash.setText(String.valueOf(UserSession.USER_HASH));
                });

        Volley.newRequestQueue(requireActivity()).add(getUserRequest);
    }
}
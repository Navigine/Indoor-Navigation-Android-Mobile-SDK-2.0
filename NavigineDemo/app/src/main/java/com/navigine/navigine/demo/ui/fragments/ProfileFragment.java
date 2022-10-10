package com.navigine.navigine.demo.ui.fragments;

import static com.navigine.navigine.demo.utils.Constants.ENDPOINT_GET_USER;
import static com.navigine.navigine.demo.utils.Constants.TAG;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.VolleyError;
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
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.navigine.navigine.demo.R;
import com.navigine.navigine.demo.models.UserSession;
import com.navigine.navigine.demo.utils.DimensionUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class ProfileFragment extends Fragment
{
    private ClipboardManager mClipboardManager = null;

    private static final int AVATAR_PADDING = (int) DimensionUtils.pxFromDp(16);
    private static final int SNACK_DURATION = 500;

    private Window             mWindow         = null;
    private ShapeableImageView mUserImage      = null;
    private MaterialTextView   mUserNameTv     = null;
    private TextView           mUserHashTv     = null;
    private TextView           mUserCompanyTv  = null;
    private TextView           mUserEmailTv    = null;
    private ImageView          mCopyIv         = null;
    private ShimmerFrameLayout mShimmerAvatar  = null;
    private ShimmerFrameLayout mShimmerName    = null;
    private ShimmerFrameLayout mShimmerInfo    = null;

    private JsonObjectRequest mUserInfoRequest = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mClipboardManager = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        createUserInfoRequest();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initViews(view);
        setViewsParams();
        setViewsListeners();

        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            updateStatusBar();
            sendUserInfoRequest();
        }
    }

    private void initViews(View view) {
        mWindow        = requireActivity().getWindow();
        mUserImage     = view.findViewById(R.id.profile__user_image);
        mUserNameTv    = view.findViewById(R.id.profile__user_name);
        mUserHashTv    = view.findViewById(R.id.profile__user_hash);
        mUserCompanyTv = view.findViewById(R.id.profile__user_company);
        mUserEmailTv   = view.findViewById(R.id.profile__user_email);
        mCopyIv        = view.findViewById(R.id.profile__copy);
        mShimmerAvatar = view.findViewById(R.id.profile__user_image_shimmer);
        mShimmerName   = view.findViewById(R.id.profile__user_name_shimmer);
        mShimmerInfo   = view.findViewById(R.id.profile__info_shimmer);
    }

    private void setViewsParams() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            ViewCompat.setElevation(mUserImage, 4f);
        }
    }

    private void setViewsListeners() {
        mCopyIv.setOnClickListener(v -> {
            onCopyData();
            showMessage(v, v, getString(R.string.profile_copy), SNACK_DURATION, R.color.colorPrimary, android.R.color.transparent);
        });
    }

    private void updateStatusBar() {
        mWindow.setStatusBarColor(ContextCompat.getColor(requireActivity(), R.color.colorBackground));
    }

    private void createUserInfoRequest() {
        String url = UserSession.LOCATION_SERVER + ENDPOINT_GET_USER + UserSession.USER_HASH;
        mUserInfoRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                this::onHandleResponseProfileInfo,
                this::onHandleResponseErrorProfileInfo);
        mUserInfoRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private void sendUserInfoRequest() {
        Volley.newRequestQueue(requireActivity()).add(mUserInfoRequest);
    }

    private void onCopyData() {
        ClipData data = ClipData.newPlainText("user hash", mUserHashTv.getText());
        mClipboardManager.setPrimaryClip(data);
    }

    private void showMessage(View v, @Nullable View anchorView, String msg, int duration, int textColorRes, int bgColorRes) {
        Snackbar snackCopy = Snackbar.make(v, msg, duration);
        View snackView = snackCopy.getView();
        TextView snackTv = snackView.findViewById(com.google.android.material.R.id.snackbar_text);
        snackTv.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        snackTv.setTextColor(ContextCompat.getColor(requireActivity(), textColorRes));
        snackTv.setTextSize(16);
        snackCopy.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE);
        snackCopy.setAnchorView(anchorView);
        snackView.setBackgroundColor(ContextCompat.getColor(requireActivity(), bgColorRes));
        snackCopy.show();
    }

    private void onHandleResponseProfileInfo(JSONObject response) {
        try
        {
            String mUserHash    = response.getString(getString(R.string.net_get_key_hash));
            String mUserName    = response.getString(getString(R.string.net_get_key_name));
            String mUserCompany = response.getString(getString(R.string.net_get_key_company));
            String mUserEmail   = response.getString(getString(R.string.net_get_key_email));
            String mAvatarUrl   = response.getString(getString(R.string.net_get_key_avatar));

            if (!mUserHash.isEmpty())    UserSession.USER_HASH       = mUserHash;
            if (!mUserName.isEmpty())    UserSession.USER_NAME       = mUserName;
            if (!mUserCompany.isEmpty()) UserSession.USER_COMPANY    = mUserCompany;
            if (!mUserEmail.isEmpty())   UserSession.USER_EMAIL      = mUserEmail;
            if (!mAvatarUrl.isEmpty())   UserSession.USER_AVATAR_URL = mAvatarUrl;

        }
        catch (JSONException e) {
            Log.e(TAG, String.format(Locale.ENGLISH, "%s  %s", getString(R.string.err_network_response_parse), e));
        }
        finally {
            updateAvatar();
            updateProfileInfo();

            mShimmerName.hideShimmer();
            mShimmerInfo.hideShimmer();
        }
    }

    private void updateProfileInfo() {
        mUserNameTv.   setText(UserSession.USER_NAME);
        mUserHashTv.   setText(UserSession.USER_HASH);
        mUserCompanyTv.setText(UserSession.USER_COMPANY);
        mUserEmailTv.  setText(UserSession.USER_EMAIL);
    }

    private void updateAvatar() {
        Glide
                .with(requireActivity())
                .load(UserSession.USER_AVATAR_URL)
                .apply(RequestOptions.circleCropTransform().diskCacheStrategy(DiskCacheStrategy.DATA).skipMemoryCache(false))
                .placeholder(R.drawable.ic_avatar_mock)
                .transition(DrawableTransitionOptions.withCrossFade())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        mUserImage.setContentPadding(AVATAR_PADDING, AVATAR_PADDING, AVATAR_PADDING, AVATAR_PADDING);
                        mShimmerAvatar.hideShimmer();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        mUserImage.setContentPadding(0, 0, 0, 0);
                        mShimmerAvatar.hideShimmer();
                        return false;
                    }
                })
                .into(mUserImage);
    }

    private void onHandleResponseErrorProfileInfo(VolleyError error) {
        if (error instanceof NoConnectionError)Log.e(TAG, getString(R.string.err_network_no_connection));
        if (error instanceof AuthFailureError) Log.e(TAG, getString(R.string.err_network_auth));

        updateAvatar();
        updateProfileInfo();

        mShimmerName.hideShimmer();
        mShimmerInfo.hideShimmer();
    }
}
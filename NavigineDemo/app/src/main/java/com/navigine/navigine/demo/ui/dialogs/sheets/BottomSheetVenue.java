package com.navigine.navigine.demo.ui.dialogs.sheets;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.navigine.navigine.demo.R;


public class BottomSheetVenue extends BottomSheetDialogFragment {

    private TextView         mSheetTitle        = null;
    private TextView         mVenueDescription  = null;
    private ImageView        mVenueImage        = null;
    private MaterialButton   mCloseButton       = null;
    private MaterialButton   mRouteButton       = null;

    private String mTitle       = null;
    private String mDescription = null;
    private String mImageRef    = null;

    private View.OnClickListener onClickListener = null;
    private static int VISIBILITY = View.VISIBLE;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_bottom_venue, container, false);
        initViews(view);
        setViewsParams();
        setViewsListeners();

        return view;
    }

    private void initViews(View view) {
        mSheetTitle       = view.findViewById(R.id.venue_dialog__title);
        mVenueDescription = view.findViewById(R.id.venue_dialog__description);
        mVenueImage       = view.findViewById(R.id.venue_dialog__image);
        mCloseButton      = view.findViewById(R.id.venue_dialog__search_btn_close);
        mRouteButton      = view.findViewById(R.id.venue_dialog__route_button);
    }

    private void setViewsParams() {
        mSheetTitle.      setText(mTitle);
        mVenueDescription.setText(mDescription);

        if (mImageRef != null && !mImageRef.equals("")) {
            Glide
                    .with(requireActivity())
                    .load(mImageRef)
                    .apply(new RequestOptions().fitCenter())
                    .into(mVenueImage);
        } else {
            mVenueImage.setImageResource(R.drawable.elm_loading_venue_photo);
        }

        mRouteButton.setVisibility(VISIBILITY);
        mRouteButton.setOnClickListener(onClickListener);
    }

    private void setViewsListeners() {
        mCloseButton.setOnClickListener(v -> dismiss());
    }

    public void setSheetTitle(String title) {
        mTitle = title;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public void setImageRef(String reference) {
        mImageRef = reference;
    }

    public void setRouteButtonClick(View.OnClickListener listener) {
        onClickListener = listener;
    }

    public void setRouteButtonVisibility(int visibility) {
        VISIBILITY = visibility;
    }
}
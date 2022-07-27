package com.navigine.navigine.demo.adapters.locations;

import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.textview.MaterialTextView;
import com.navigine.navigine.demo.R;

public class LocationViewHolder extends RecyclerView.ViewHolder {

    protected LinearLayout        itemContainer;
    protected MaterialTextView    titleTextView;
    protected LottieAnimationView check;

    public LocationViewHolder(@NonNull View itemView) {
        super(itemView);

        itemContainer = itemView.findViewById(R.id.locations_list_item__container);
        titleTextView = itemView.findViewById(R.id.locations_list_item__title_text_view);
        check         = itemView.findViewById(R.id.locations_list_item__check);
    }
}

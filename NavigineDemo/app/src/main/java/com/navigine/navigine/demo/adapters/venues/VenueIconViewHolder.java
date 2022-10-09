package com.navigine.navigine.demo.adapters.venues;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.navigine.navigine.demo.R;

public class VenueIconViewHolder extends RecyclerView.ViewHolder {

    protected ImageView icon  = null;
    protected TextView  title = null;

    public VenueIconViewHolder(@NonNull View itemView) {
        super(itemView);

        icon  = itemView.findViewById(R.id.li_venue_icon);
        title = itemView.findViewById(R.id.li_venue_title);
    }
}

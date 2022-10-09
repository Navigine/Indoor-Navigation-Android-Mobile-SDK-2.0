package com.navigine.navigine.demo.adapters.debug;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.navigine.navigine.demo.R;


public class DebugViewHolderBaseInfo extends RecyclerView.ViewHolder {

    protected TextView name  = null;
    protected TextView value = null;

    public DebugViewHolderBaseInfo(@NonNull View itemView) {
        super(itemView);

        name  = itemView.findViewById(R.id.li_debug_info__name);
        value = itemView.findViewById(R.id.li_debug_info__value);
    }
}

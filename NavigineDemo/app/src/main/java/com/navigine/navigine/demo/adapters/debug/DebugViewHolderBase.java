package com.navigine.navigine.demo.adapters.debug;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.navigine.navigine.demo.R;


public class DebugViewHolderBase extends RecyclerView.ViewHolder {

    protected TextView title = null;
    protected TextView uuid  = null;
    protected TextView rssi  = null;

    public DebugViewHolderBase(@NonNull View itemView) {
        super(itemView);

        title = itemView.findViewById(R.id.li_debug__title);
        uuid  = itemView.findViewById(R.id.li_debug__uuid);
        rssi  = itemView.findViewById(R.id.li_debug__rssi);
    }
}

package com.navigine.navigine.demo.adapters.debug;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.navigine.navigine.demo.R;


public class DebugViewHolderBaseBeacons extends DebugViewHolderBase {

    protected TextView distance = null;

    public DebugViewHolderBaseBeacons(@NonNull View itemView) {
        super(itemView);

        title    = itemView.findViewById(R.id.li_debug_beacons__title);
        uuid     = itemView.findViewById(R.id.li_debug_beacons__uuid);
        rssi     = itemView.findViewById(R.id.li_debug_beacons__rssi);
        distance = itemView.findViewById(R.id.li_debug_beacons__distance);
    }
}

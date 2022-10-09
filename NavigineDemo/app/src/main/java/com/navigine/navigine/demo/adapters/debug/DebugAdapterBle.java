package com.navigine.navigine.demo.adapters.debug;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.navigine.idl.java.SignalMeasurement;
import com.navigine.navigine.demo.R;

import java.util.Locale;

public class DebugAdapterBle extends DebugAdapterBaseExpanded<DebugViewHolderBase, SignalMeasurement> {

    @NonNull
    @Override
    public DebugViewHolderBase onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType) {
            case 0:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_round_top_debug, parent, false);
                break;
            case 1:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_round_bottom_debug, parent, false);
                break;
            default:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_debug, parent, false);
                break;
        }
        return new DebugViewHolderBase(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DebugViewHolderBase holder, int position) {
        if (position == 0) {
            holder.title.setText(String.format(Locale.ENGLISH, "BLE devices (%d), entries/sec: %.1f", mCurrentList.size(), (float) mCurrentList.size()));
            super.onBindViewHolder(holder, position);
        }
        else {
            try {
                SignalMeasurement result = mCurrentList.get(position - 1);

                holder.uuid.setText(result.getId());
                holder.rssi.setText(String.format(Locale.ENGLISH, "%.1f", result.getRssi()));
            } catch (IndexOutOfBoundsException e) {
                holder.uuid.setText("---");
                holder.rssi.setText(null);
            }
        }
    }
}

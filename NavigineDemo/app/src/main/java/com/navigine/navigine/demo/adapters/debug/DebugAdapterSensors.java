package com.navigine.navigine.demo.adapters.debug;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.navigine.navigine.demo.R;

public class DebugAdapterSensors extends DebugAdapterBase<DebugViewHolderBaseInfo, String[]> {

    @NonNull
    @Override
    public DebugViewHolderBaseInfo onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType) {
            case 0:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_round_top_debug_sensors, parent, false);
                break;
            case 1:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_round_bottom_debug_info, parent, false);
                break;
            default:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_debug_info, parent, false);
                break;
        }
        return new DebugViewHolderBaseInfo(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DebugViewHolderBaseInfo holder, int position) {
        if (position > 0) {
            try {
                holder.name. setText(mCurrentList.get(position - 1)[0]);
                holder.value.setText(mCurrentList.get(position - 1)[1]);
            } catch (ArrayIndexOutOfBoundsException e) {
                holder.name. setText("---");
                holder.value.setText(null);
            }

        }
    }

    @Override
    public int getItemCount() {
        return mCurrentList.size() + 1;
    }

    @Override
    void onCopyContent() {}
}

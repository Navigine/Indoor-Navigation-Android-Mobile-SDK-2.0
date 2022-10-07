package com.navigine.navigine.demo.adapters.debug;

import static com.navigine.navigine.demo.utils.Constants.LIST_SIZE_DEFAULT;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.navigine.idl.java.SignalMeasurement;
import com.navigine.navigine.demo.R;

import java.util.Locale;

public class DebugAdapterEddystone extends DebugAdapterBase<DebugViewHolderBaseBeacons, SignalMeasurement> {


    @NonNull
    @Override
    public DebugViewHolderBaseBeacons onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType) {
            case 0:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_round_top_debug_beacons, parent, false);
                break;
            case 1:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_round_bottom_debug_beacons, parent, false);
                break;
            default:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_debug_beacons, parent, false);
                break;
        }
        return new DebugViewHolderBaseBeacons(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DebugViewHolderBaseBeacons holder, int position) {
        if (position == 0) {
            holder.title.setText(String.format(Locale.ENGLISH, "EDDYSTONE (%d), entries/sec: %.1f", mCurrentList.size(), (float) mCurrentList.size()));
            if (expand) holder.title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_circle_up, 0);
            else        holder.title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_circle_down, 0);
            holder.title.setOnTouchListener((v, event) -> {
                v.performClick();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getX() >= holder.title.getWidth() - holder.title.getTotalPaddingEnd()) {
                        if (mCurrentList.size() <= LIST_SIZE_DEFAULT)
                            Toast.makeText(mRecyclerView.getContext(), R.string.debug_expand_list_nothing, Toast.LENGTH_SHORT).show();
                        else {
                            expand = !expand;
                            notifyDataSetChanged();
                            mRecyclerView.scheduleLayoutAnimation();
                        }
                    }
                }
                return true;
            });
        }
        else {
            try {
                SignalMeasurement result = mCurrentList.get(position - 1);
                String[] ids = result.getId().split(",");
                String address = ids[0].substring(1, 15 - ids[1].length()) + "…, " + ids[1].substring(0, ids[1].length() - 1);

                holder.uuid.    setText(address);
                holder.rssi.    setText(String.format(Locale.ENGLISH, "%.1f", result.getRssi()));
                holder.distance.setText(String.format(Locale.ENGLISH, "%.1fm", result.getDistance()));
            } catch (IndexOutOfBoundsException e) {
                holder.uuid.    setText("---");
                holder.rssi.    setText(null);
                holder.distance.setText(null);
            }
        }
    }
}

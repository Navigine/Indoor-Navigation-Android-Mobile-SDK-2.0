package com.navigine.navigine.demo.adapters.debug;

import static com.navigine.navigine.demo.utils.Constants.LIST_SIZE_DEFAULT;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.navigine.idl.java.SignalMeasurement;
import com.navigine.navigine.demo.R;

import java.util.List;
import java.util.Locale;

public class DebugAdapterWifi extends DebugAdapterBase<DebugViewHolderBase, SignalMeasurement> {

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
            holder.title.setText(String.format(Locale.ENGLISH, "Wi-Fi network (%d)", mCurrentList.size()));
            if (expand) holder.title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_circle_up, 0);
            else        holder.title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_circle_down, 0);
            holder.title.setOnTouchListener((v, event) -> {
                v.performClick();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getX() >= holder.title.getWidth() - holder.title.getTotalPaddingEnd()) {
                        if (mCurrentList.size() <= LIST_SIZE_DEFAULT)
                            Toast.makeText(mRecyclerView.getContext(), R.string.nothing_expand, Toast.LENGTH_SHORT).show();
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

                holder.uuid. setText(result.getId());
                holder.rssi. setText(String.format(Locale.ENGLISH, "%.1f", result.getRssi()));
            } catch (IndexOutOfBoundsException e) {
                holder.uuid.setText("---");
            }
        }
    }
}

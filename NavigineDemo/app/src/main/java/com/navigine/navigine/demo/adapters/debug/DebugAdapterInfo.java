package com.navigine.navigine.demo.adapters.debug;

import android.content.ClipData;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.navigine.navigine.demo.R;

import java.util.List;


public class DebugAdapterInfo extends DebugAdapterBase<DebugViewHolderBaseInfo, String[]> {

    private boolean isPressed  = false;

    private String copyContent = null;

    @NonNull
    @Override
    public DebugViewHolderBaseInfo onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType) {
            case 0:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_round_top_debug_info, parent, false);
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
        try {
            String name = mCurrentList.get(position)[0];
            String value = mCurrentList.get(position)[1];
            if (name.contains(mContext.getString(R.string.debug_info_field_5_1)) && value.contains(mContext.getString(R.string.debug_info_field_5_2))) {
                Spannable spannableBl  = new SpannableString(name);
                Spannable spannableGeo = new SpannableString(value);

                holder.name. setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorTextSecondaryGray));
                holder.value.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorTextSecondaryGray));

                spannableBl. setSpan(new ForegroundColorSpan(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorTextPrimary)), name.indexOf(':') + 1, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableGeo.setSpan(new ForegroundColorSpan(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorTextPrimary)), value.indexOf(':') + 1, value.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                holder.name.setText(spannableBl);
                holder.value.setText(spannableGeo);
            }
            else {
                holder.name.setText(name);
                holder.value.setText(value);

                if (name.contains(mContext.getString(R.string.debug_info_field_2))) {
                    holder.value.setOnTouchListener((v, event) -> {
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            isPressed = false;
                            v.performClick();
                        }
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            isPressed = true;
                            copyContent = value;
                            mGestureDetector.onTouchEvent(event);
                        }
                        return true;
                    });
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            holder.name.setText("---");
            holder.value.setText("---");
        }
    }

    @Override
    public void submit(List<String[]> list) {
        if (!isPressed)
            super.submit(list);
    }

    @Override
    void onCopyContent() {
        ClipData clip = ClipData.newPlainText("Device ID", copyContent);
        mClipboardManager.setPrimaryClip(clip);
        Toast.makeText(mContext, R.string.debug_copy_device_id, Toast.LENGTH_SHORT).show();
    }
}

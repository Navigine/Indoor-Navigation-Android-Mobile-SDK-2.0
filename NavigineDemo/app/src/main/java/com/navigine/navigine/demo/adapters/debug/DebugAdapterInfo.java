package com.navigine.navigine.demo.adapters.debug;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.navigine.navigine.demo.R;

public class DebugAdapterInfo extends DebugAdapterBase<DebugViewHolderBaseInfo, String[]> {

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
            String var1 = mCurrentList.get(position)[0];
            String var2 = mCurrentList.get(position)[1];
            if (var1.contains("Bluetooth") && var2.contains("Geolocation")) {
                Spannable spannableBl = new SpannableString(var1);
                Spannable spannableGeo = new SpannableString(var2);

                holder.name.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorTextSecondary));
                holder.value.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorTextSecondary));

                spannableBl.setSpan(new ForegroundColorSpan(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorTextPrimary)), var1.indexOf(':') + 1, var1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableGeo.setSpan(new ForegroundColorSpan(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorTextPrimary)), var2.indexOf(':') + 1, var2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                holder.name.setText(spannableBl);
                holder.value.setText(spannableGeo);
            }
            else {
                holder.name.setText(mCurrentList.get(position)[0]);
                holder.value.setText(mCurrentList.get(position)[1]);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            holder.name.setText("---");
            holder.value.setText("---");
        }
    }

    @Override
    public int getItemCount() {
        return mCurrentList.size();
    }
}

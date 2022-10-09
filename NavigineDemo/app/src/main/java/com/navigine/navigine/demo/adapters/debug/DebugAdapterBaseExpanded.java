package com.navigine.navigine.demo.adapters.debug;

import android.content.ClipData;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.navigine.idl.java.SignalMeasurement;
import com.navigine.navigine.demo.R;

import java.util.List;
import java.util.Locale;

public abstract class DebugAdapterBaseExpanded<T extends DebugViewHolderBase, V extends SignalMeasurement> extends DebugAdapterBase<T, V> {

    public static final int LIST_SIZE_DEFAULT = 6;

    protected boolean expand = false;

    private final StringBuilder copyTextBuilder = new StringBuilder();

    @Override
    public void onBindViewHolder(@NonNull T holder, int position) {
        if (mCurrentList.size() <= LIST_SIZE_DEFAULT)
            holder.title.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        else {
            if (expand)
                holder.title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_circle_up, 0);
            else
                holder.title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_circle_down, 0);
        }
        holder.title.setOnTouchListener((v, event) -> {
            if (event.getX() >= holder.title.getWidth() - holder.title.getTotalPaddingEnd()) {
                if (holder.title.getCompoundDrawables()[2] != null) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        isPressed = false;
                        expand = !expand;
                        mRecyclerView.scheduleLayoutAnimation();
                        v.performClick();
                        notifyDataSetChanged();
                    }
                }
            } else {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.postDelayed(() -> {
                        isPressed = !isRootScrolling;
                        if (isPressed) mGestureDetector.onTouchEvent(event);
                    }, 300);
                }
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        if (!expand) return LIST_SIZE_DEFAULT;
        else return mCurrentList.size() + 1;
    }

    public void submit(List<V> list) {
        if (!isPressed) {
            mCurrentList.clear();
            mCurrentList.addAll(list);
            if (mCurrentList.size() <= LIST_SIZE_DEFAULT) expand = false;
            notifyDataSetChanged();
        }
    }

    @Override
    void onCopyContent() {
        copyTextBuilder.setLength(0);
        for (SignalMeasurement signalMeasurement : mCurrentList) {
            copyTextBuilder.append(signalMeasurement.getId());
            copyTextBuilder.append(" ");
            copyTextBuilder.append(String.format(Locale.ENGLISH, "%.1f", signalMeasurement.getRssi()));
            copyTextBuilder.append("  ");
            copyTextBuilder.append(String.format(Locale.ENGLISH, "%.1fm", signalMeasurement.getDistance()));
            copyTextBuilder.append('\n');
        }
        ClipData clip = ClipData.newPlainText("list content", copyTextBuilder.toString());
        mClipboardManager.setPrimaryClip(clip);
        Toast.makeText(mContext, R.string.debug_copy_list_content, Toast.LENGTH_SHORT).show();
    }
}

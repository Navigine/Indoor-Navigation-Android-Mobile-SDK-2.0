package com.navigine.navigine.demo.adapters.debug;

import static com.navigine.navigine.demo.utils.Constants.LIST_SIZE_DEFAULT;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public abstract class DebugAdapterBase<T extends RecyclerView.ViewHolder, V> extends RecyclerView.Adapter<T> {

    protected RecyclerView mRecyclerView = null;

    private static final int TYPE_ROUNDED_TOP    = 0;
    private static final int TYPE_ROUNDED_BOTTOM = 1;
    private static final int TYPE_RECT           = 2;

    protected List<V> mCurrentList = new ArrayList<>();

    protected boolean expand = false;

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return TYPE_ROUNDED_TOP;
        if (position == mCurrentList.size() - 1) return TYPE_ROUNDED_BOTTOM;
        return TYPE_RECT;
    }

    @Override
    public int getItemCount() {
        if (!expand) return LIST_SIZE_DEFAULT;
        else return mCurrentList.size() + 1;
    }

    public void submit(List<V> list) {
        mCurrentList.clear();
        mCurrentList.addAll(list);
        if (mCurrentList.size() <= LIST_SIZE_DEFAULT) expand = false;
        notifyDataSetChanged();
    }
}

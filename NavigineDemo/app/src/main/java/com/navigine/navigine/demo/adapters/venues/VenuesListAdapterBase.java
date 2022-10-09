package com.navigine.navigine.demo.adapters.venues;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class VenuesListAdapterBase<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> {

    protected RecyclerView mRecyclerView = null;
    protected Context mContext = null;

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
        mContext = recyclerView.getContext();
    }
}

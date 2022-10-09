package com.navigine.navigine.demo.adapters.sublocations;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textview.MaterialTextView;
import com.navigine.idl.java.Sublocation;
import com.navigine.navigine.demo.R;

import java.util.ArrayList;
import java.util.List;

public class SublocationsAdapter<T extends Sublocation> extends ArrayAdapter<T> {

    private List<T> mCurrentList = new ArrayList<>();

    private final int TYPE_TOP = 0;
    private final int TYPE_MID = 1;
    private final int TYPE_BOTTOM = 2;

    public SublocationsAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    @Override
    public int getCount() {
        return mCurrentList.size();
    }

    @Nullable
    @Override
    public T getItem(int position) {
        return mCurrentList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return TYPE_TOP;
        if (position == mCurrentList.size() - 1) return TYPE_BOTTOM;
        return TYPE_MID;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_sublocation, parent, false);
        ((MaterialTextView) convertView).setText(mCurrentList.get(position).getName());
        return convertView;
    }

    public void submit(ArrayList<T> sublocations) {
        mCurrentList.clear();
        mCurrentList.addAll(sublocations);
        notifyDataSetChanged();
    }
}

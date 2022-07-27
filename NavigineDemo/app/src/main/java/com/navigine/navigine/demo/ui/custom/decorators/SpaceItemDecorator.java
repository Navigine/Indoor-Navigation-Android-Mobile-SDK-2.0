package com.navigine.navigine.demo.ui.custom.decorators;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SpaceItemDecorator extends RecyclerView.ItemDecoration {

    private final int spanCount = 5;
    private int space;

    public SpaceItemDecorator(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        outRect.bottom = space;

        if (parent.getChildLayoutPosition(view) < spanCount) {
            outRect.top = space * 2;
        } else {
            outRect.top = 0;
        }
    }
}

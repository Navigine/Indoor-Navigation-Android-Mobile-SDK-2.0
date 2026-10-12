package com.navigine.navigine.demo.adapters.route;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.navigine.idl.java.RouteAnnotation;
import com.navigine.navigine.demo.R;
import com.navigine.navigine.demo.utils.RouteFormatter;

import java.util.ArrayList;
import java.util.List;

public class RouteAnnotationAdapter extends BaseAdapter {

    private static final float MIN_DISPLAY_DISTANCE = 1f;

    private final List<RouteAnnotation> mAnnotations = new ArrayList<>();
    private float mAdvance = 0f;

    @Override
    public int getCount() {
        return mAnnotations.size();
    }

    @Override
    public Object getItem(int i) {
        return mAnnotations.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_cancel_route, parent, false);
        }

        RouteAnnotation annotation = mAnnotations.get(i);
        float distance = Math.max(annotation.getAdvance() - mAdvance, MIN_DISPLAY_DISTANCE);

        ImageView directionImage = view.findViewById(R.id.cancel_route_item__direction_image);
        TextView distanceText = view.findViewById(R.id.cancel_route_item__distance_text);
        TextView timeText = view.findViewById(R.id.cancel_route_item__time_text);

        int icon = RouteFormatter.icon(annotation.getType());
        if (icon == RouteFormatter.NO_ICON) {
            directionImage.setVisibility(View.INVISIBLE);
        } else {
            directionImage.setImageResource(icon);
            directionImage.setVisibility(View.VISIBLE);
        }

        distanceText.setText(RouteFormatter.instruction(annotation, distance));
        timeText.setText(RouteFormatter.duration(RouteFormatter.estimateSeconds(distance)));

        return view;
    }

    public void submit(List<RouteAnnotation> annotations, float advance) {
        mAnnotations.clear();
        mAnnotations.addAll(annotations);
        mAdvance = advance;
        notifyDataSetChanged();
    }

    public void clear() {
        submit(new ArrayList<>(), 0f);
    }
}

package com.navigine.navigine.demo.adapters.route;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.navigine.idl.java.RouteEvent;
import com.navigine.idl.java.RouteEventType;
import com.navigine.idl.java.RouteNode;
import com.navigine.idl.java.Sublocation;
import com.navigine.idl.java.TurnType;
import com.navigine.navigine.demo.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class RouteEventAdapter extends BaseAdapter {
    private List<RouteNode> mRouteNodes = new ArrayList<>();
    private Sublocation     mSublocation = null;

    @Override
    public int getCount() { return mRouteNodes.size(); }

    @Override
    public Object getItem(int i) { return mRouteNodes.get(i); }

    @Override
    public long getItemId(int i) { return i; }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) Objects.requireNonNull(viewGroup.getContext())
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item_cancel_route, null);
        }

        RouteNode node = mRouteNodes.get(i);
        RouteEvent event = (node.getEvents() != null && !node.getEvents().isEmpty()) ? node.getEvents().get(0) : null;
        float distance = Math.max(node.getDistance(), 1.0f);

        ImageView directionImage = view.findViewById(R.id.cancel_route_item__direction_image);
        TextView leftDistance    = view.findViewById(R.id.cancel_route_item__distance_text);
        TextView leftTimeText    = view.findViewById(R.id.cancel_route_item__time_text);

        if (i == mRouteNodes.size() - 1) {
            directionImage.setImageResource(R.drawable.ic_to_point);
            leftDistance.setText(String.format(Locale.ENGLISH, "In %.0f m finish!", distance));
        } else if (event != null) {
            directionImage.setImageResource(resolveDirectionIcon(event));
            leftDistance.setText(buildDistanceText(event, distance));
        }

        double time = (node.getDistance() / 1.43) / 60;
        leftTimeText.setText(time < 1 ? "< 1 min" : String.format(Locale.ENGLISH, "%.0f min", time));

        return view;
    }

    private int resolveDirectionIcon(RouteEvent event) {
        if (event.getType() == RouteEventType.TURN_EVENT && event.getTurnEvent() != null) {
            TurnType turn = event.getTurnEvent().getType();
            if (turn == TurnType.LEFT_SLIGHT || turn == TurnType.LEFT_NORMAL || turn == TurnType.LEFT_SHARP)
                return R.drawable.ic_left;
            if (turn == TurnType.RIGHT_SLIGHT || turn == TurnType.RIGHT_NORMAL || turn == TurnType.RIGHT_SHARP)
                return R.drawable.ic_right;
        }
        return R.drawable.ic_escalator;
    }

    private String buildDistanceText(RouteEvent event, float distance) {
        String prefix = String.format(Locale.ENGLISH, "In %.0f m ", distance);
        if (event.getType() == RouteEventType.TRANSITION_ENTRY_EVENT) {
            String subName = mSublocation.getName();
            return prefix + "go to " + (subName.length() > 15 ? subName.substring(0, 13) + "..." : subName);
        }
        if (event.getType() == RouteEventType.TURN_EVENT && event.getTurnEvent() != null) {
            TurnType turn = event.getTurnEvent().getType();
            if (turn == TurnType.LEFT_SLIGHT || turn == TurnType.LEFT_NORMAL || turn == TurnType.LEFT_SHARP)
                return prefix + "turn left";
            if (turn == TurnType.RIGHT_SLIGHT || turn == TurnType.RIGHT_NORMAL || turn == TurnType.RIGHT_SHARP)
                return prefix + "turn right";
        }
        return prefix;
    }

    public void submit(List<RouteNode> nodes, Sublocation sublocation) {
        mRouteNodes.clear();
        mRouteNodes.addAll(nodes);
        mSublocation = sublocation;
        notifyDataSetChanged();
    }
}
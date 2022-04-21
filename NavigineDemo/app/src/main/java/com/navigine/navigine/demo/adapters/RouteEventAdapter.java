package com.navigine.navigine.demo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.navigine.idl.java.RouteEvent;
import com.navigine.idl.java.RouteEventType;
import com.navigine.navigine.demo.R;
import com.navigine.navigine.demo.application.NavigineApp;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class RouteEventAdapter extends BaseAdapter
{
    private List<RouteEvent> cancelRouteList;

    public RouteEventAdapter(List<RouteEvent> eventsList)
    {
        this.cancelRouteList = eventsList;
    }

    @Override
    public int getCount()
    {
        return cancelRouteList.size();
    }

    @Override
    public Object getItem(int i)
    {
        return cancelRouteList.get(i);
    }

    @Override
    public long getItemId(int i)
    {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
    {
        if (view == null)
        {
            LayoutInflater inflater = (LayoutInflater) Objects.requireNonNull(viewGroup.getContext()).getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.cancel_route_item, null);
        }

        RouteEvent event = cancelRouteList.get(i);
        ImageView directionImage = view.findViewById(R.id.cancel_route_item__direction_image);
        TextView leftDistance   = view.findViewById(R.id.cancel_route_item__distance_text);
        TextView  leftTimeText   = view.findViewById(R.id.cancel_route_item__time_text);

        if (i == cancelRouteList.size() - 1)
        {
            directionImage.setImageResource(R.drawable.ic_to_point);
            leftDistance.setText(String.format(Locale.ENGLISH, "In %.0f m finish!", Math.max(event.getDistance(), 1.0f)));
        }
        else
        {
            directionImage.setImageResource(event.getType() == RouteEventType.TURN_LEFT ? R.drawable.ic_left : event.getType() == RouteEventType.TURN_RIGHT ? R.drawable.ic_right : R.drawable.ic_escalator);

            String distanceText = String.format(Locale.ENGLISH, "In %.0f m ", Math.max(event.getDistance(), 1.0f));
            if (event.getType() == RouteEventType.TRANSITION)
            {

                String subName = (NavigineApp.CurrentLocation != null ? NavigineApp.CurrentSublocation.getName() : "");

                distanceText += "go to " + (subName.length() > 15 ? subName.substring(0, 13) + "..." : subName);
            }
            else
                distanceText += event.getType() == RouteEventType.TURN_RIGHT ? "turn right" : "turn left";

            leftDistance.setText(distanceText);
        }

        double time = (event.getDistance() / 1.43) / 60;
        leftTimeText.setText(time < 1 ? "< 1 min" : String.format(Locale.ENGLISH, "%.0f min", time));

        return view;
    }
}

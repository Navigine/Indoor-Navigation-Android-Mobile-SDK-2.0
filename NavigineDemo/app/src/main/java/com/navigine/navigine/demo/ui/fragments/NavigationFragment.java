package com.navigine.navigine.demo.ui.fragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.navigine.idl.java.AnimationType;
import com.navigine.idl.java.IconMapObject;
import com.navigine.idl.java.Location;
import com.navigine.idl.java.LocationListener;
import com.navigine.idl.java.LocationPoint;
import com.navigine.idl.java.LocationPolyline;
import com.navigine.idl.java.MapObjectPickResult;
import com.navigine.idl.java.Notification;
import com.navigine.idl.java.NotificationListener;
import com.navigine.idl.java.PickListener;
import com.navigine.idl.java.Point;
import com.navigine.idl.java.Polyline;
import com.navigine.idl.java.PolylineMapObject;
import com.navigine.idl.java.Position;
import com.navigine.idl.java.PositionListener;
import com.navigine.idl.java.RouteEvent;
import com.navigine.idl.java.RouteEventType;
import com.navigine.idl.java.RouteListener;
import com.navigine.idl.java.RoutePath;
import com.navigine.idl.java.Sublocation;
import com.navigine.idl.java.Venue;
import com.navigine.idl.java.Zone;
import com.navigine.idl.java.ZoneListener;
import com.navigine.navigine.demo.R;
import com.navigine.navigine.demo.adapters.RouteEventAdapter;
import com.navigine.navigine.demo.application.NavigineApp;
import com.navigine.navigine.demo.ui.custom.BottomSheetListView;
import com.navigine.view.LocationView;
import com.navigine.view.TouchInput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class NavigationFragment extends Fragment {

    private Button               swap                    = null;
    private Button               record                  = null;
    private GridView             gvMain                  = null;
    private LocationView         locationView            = null;
    private ConstraintLayout     makeRouteSheet          = null;
    private ConstraintLayout     cancelRouteSheet        = null;
    private ConstraintLayout     venueBottomSheet        = null;
    private LinearLayout         venuesListSheet         = null;
    private RelativeLayout       mDirectionLayout        = null;
    private LinearLayout         mVenueRouteLayout       = null;
    private LinearLayout         mRouteChooseFromLayout  = null;
    private NestedScrollView     venueScrollView         = null;
    private View                 mRouteDivider           = null;
    private BottomSheetBehavior  mMakeRouteBehavior      = null;
    private BottomSheetBehavior  mSheetBehavior          = null;
    private BottomSheetBehavior  mCancelRouteBehaviour   = null;
    private BottomSheetBehavior  mVenuesBehavior         = null;
    private BottomSheetListView  cancelRouteListView     = null;
    private TextView             mToText                 = null;
    private TextView             mFromCurrentText        = null;
    private TextView             mCancelRouteDistance    = null;
    private TextView             mCancelRouteTime        = null;
    private TextView             mDirectionTextView      = null;
    private TextView             mSheetDescription       = null;
    private TextView             mSheetTitle             = null;
    private TextView             mVenuePhone             = null;
    private TextView             mVenueCategory          = null;
    private Button               mStartRouteButton       = null;
    private Button               mRouteSheetCancelButton = null;
    private Button               mVenueCancelButton      = null;
    private Button               mCallButton             = null;
    private Button               mMakeVenueRouteButton   = null;
    private AppCompatImageView   mFromImageView          = null;
    private ImageView            mDirectionImageView     = null;
    private ImageView            mSheetVenueImage        = null;
    private IconMapObject        mPosition               = null; // draw user position
    private PolylineMapObject    polylineMapObject       = null; // draw route path
    private IconMapObject        targetPoint             = null; // draw target point

    Location    mLocation    = null; // current location
    Sublocation mSublocation = null; // current sublocation

    private int mCurrentSubLocationIndex = -1;

    SublocationsAdapter mAdapter      = null;
    List<Sublocation>   mSublocations = new ArrayList<>();

    boolean isRecording = false;

    private ArrayList<Point>  points            = new ArrayList<>(); // points of route path
    private List<RouteEvent>  mCancelRouteList  = new ArrayList<>();

    private RouteEventAdapter mRouteEventAdapter = null;

    private RoutePath mLastActiveRoutePath = null;

    private LocationPoint mTargetPoint = null; // destination of user defined point
    private LocationPoint mFromPoint   = null; // starting point
    private LocationPoint mPinPoint    = null; // point of destination pin
    private Venue         mTargetVenue = null; // destination of picked venue
    private Venue         mFromVenue   = null; // venue as starting point
    private Venue         mToVenue     = null; // venue as destination
    private Venue         mPinVenue    = null; // picked venue as destination

    private boolean mSelectMapPoint    = false;

    private PositionListener     positionListener     = null;
    private ZoneListener         zoneListener         = null;
    private LocationListener     locationListener     = null;
    private NotificationListener notificationListener = null;
    private RouteListener        routeListener        = null;

    private boolean locationLoaded = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initListeners();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navigation, container, false);

        initViews(view);
        setViewsParams();
        setViewsListeners();
        initAdapters();
        setAdapters();
        addListeners();
        initLocationViewObjects();

        /**
         * Attention, this is not a ready-to-use method!
         * This is just a small tutorial how to make route
         */
        //TODO:        makeRoute();

        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (locationLoaded) {
                locationLoaded = false;
                if (hasTarget()) {
                    onCancelRoute(null);
                    mCancelRouteBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
                gvMain.setVisibility(View.VISIBLE);
                mAdapter = new SublocationsAdapter();
                gvMain.setAdapter(mAdapter);
                mAdapter.updateList();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //To remove any listener invoke removeListeners on the required SDK manager
        NavigineApp.NavigationManager.removePositionListener(positionListener);
        NavigineApp.ZoneManager.removeZoneListener(zoneListener);
        NavigineApp.LocationManager.removeLocationListener(locationListener);
        NavigineApp.NotificationManager.removeNotificationListener(notificationListener);
        NavigineApp.RouteManager.removeRouteListener(routeListener);
    }

    private void initListeners() {

        /*
         * SHOW USER CURRENT POSITION ON LOCATION
         *
         * To show the user current position on location view add PositionListener to NavigationManager.
         * If real beacons are at the location or the beacon generator is running then method
         * onPositionUpdated will be called periodically.
         * params:
         *        position - object, contains info about current position
         * To display the user position on the location view add IconMapObject to LocationViewController
         * and call setPositionAnimated or setPosition when position updates.
         *
         * If an error occurred while determining the position (e.g. no beacons found), onPositionError
         * will be called.
         * params:
         *        error - an object containing information about the error
         */
        positionListener = new PositionListener() {
            @Override
            public void onPositionUpdated(Position position) {
                mFromPoint = new LocationPoint(position.getPoint(), position.getLocationId(), position.getSublocationId());
                mPosition.setPositionAnimated(mFromPoint, 1.0f, AnimationType.CUBIC);
            }

            @Override
            public void onPositionError(Error error) {
                Log.d("NAVIGINE_LOG", error.getMessage());
            }
        };

        /*
         * ZONE ENTRY OR EXIT NOTIFYING
         *
         * To find out if a user is in or out of a specific zone add ZoneListener to ZoneManager.
         * If the user has entered a certain zone, onEnterZone will be called, otherwise -
         * If the user has left a certain zone, onLeaveZone will be called.
         * params:
         *        zone - an object containing information about the certain zone
         */
        zoneListener = new ZoneListener() {
            @Override
            public void onEnterZone(Zone zone) {
                Log.d("NAVIGINE_ZONES", "Enter zone" + zone.getName());
            }

            @Override
            public void onLeaveZone(Zone zone) {
                Log.d("NAVIGINE_ZONES", "Leave zone" + zone.getName());
            }
        };

        /*
         * UPDATE LOCATION ON LOCATION VIEW
         *
         * If the location selected by the user has loaded or has already loaded, the method
         * onLocationLoaded is called. Here you can update location view by setting to it loaded location.
         */
        locationListener = new LocationListener() {
            @Override
            public void onLocationLoaded(Location location) {
                mLocation = location;
                mSublocation = mLocation.getSublocations().get(0);

                mSublocations.clear();
                mSublocations.addAll(location.getSublocations());

                if (isVisible()) {
                    if (hasTarget()) {
                        onCancelRoute(null);
                        mCancelRouteBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
                    }
                    gvMain.setVisibility(View.VISIBLE);
                    mAdapter = new SublocationsAdapter();
                    gvMain.setAdapter(mAdapter);
                    mAdapter.updateList();
                }

                locationLoaded = true;
            }

            @Override
            public void onDownloadProgress(int i, int i1, int i2) {

            }

            @Override
            public void onLocationFailed(int i, Error error) {
                Log.d("NAVIGINE_LOCATION", "onLocationFailed: " + error.getMessage());
            }

            @Override
            public void onLocationCancelled(int i) {

            }
        };

        /*
         * DETECTS BEACON WITH NOTIFICATION
         *
         * To receive notification from the beacon add NotificationListener to NotificationManager.
         * If the notification is successfully received onNotificationLoaded will be invoked.
         * params:
         *        notification - object which contains notification info
         * If the notification receive failed onNotificationFailed will be invoked.
         * params:
         *        error - object that contains info about error
         */
        notificationListener = new NotificationListener() {
            @Override
            public void onNotificationLoaded(Notification notification) {
                Log.d("NAVIGINE_NOTIFICATION", "onNotificationLoaded: " + notification.getTitle());
            }

            @Override
            public void onNotificationFailed(Error error) {
                Log.d("NAVIGINE_NOTIFICATION", "onNotificationLoaded: " + error.getMessage());
            }
        };

        /*
         * DISPLAY ROUTE PATH
         *
         * To display the built route add RouteListener to RouteManager. When RouteManager update route,
         * onPathsUpdated will be invoked.
         * params:
         *         arrayList - list of RoutePaths. RoutePath is te object which contains info about
         *         route events, route length and provide list of route location points.
         *To draw a route path is used a polylineMapObject.
         */
        routeListener = new RouteListener() {
            @Override
            public void onPathsUpdated(ArrayList<RoutePath> arrayList) {
                if (arrayList.isEmpty()) {
                    return;
                }

                points.clear();

                RoutePath routePath = arrayList.get(0); //get available route path

                for (LocationPoint locationPoint : routePath.getPoints()) {
                    if (locationPoint.getSublocationId() == mSublocation.getId()) {
                        points.add(locationPoint.getPoint()); //add route points to list
                    }
                }
                //draw polyline by points
                if (!points.isEmpty()) {
                    LocationPolyline polyline = new LocationPolyline(new Polyline(points), mLocation.getId(), mSublocation.getId());
                    polylineMapObject.setPolyLine(polyline);
                    polylineMapObject.setVisible(true);
                } else {
                    polylineMapObject.setVisible(false);
                }

                updateRouteInfo(routePath);//update info about directions
            }
        };

    }

    private void initViews(View view) {
        gvMain                  = view.findViewById(R.id.sub_loc_list);
        swap                    = view.findViewById(R.id.swap_sublocations);
        record                  = view.findViewById(R.id.record);
        locationView            = view.findViewById(R.id.location_view);
        mDirectionLayout        = view.findViewById(R.id.navigation_fragment__direction_layout);
        mVenueRouteLayout       = view.findViewById(R.id.navigation_fragment__route_view);
        mRouteDivider           = view.findViewById(R.id.navigation_fragment__route_divider);
        mDirectionImageView     = view.findViewById(R.id.navigation_fragment__direction_image);
        mDirectionTextView      = view.findViewById(R.id.navigation_fragment__direction_distance);
        makeRouteSheet          = view.findViewById(R.id.navigation_fragment__make_route_sheet);
        venueBottomSheet        = view.findViewById(R.id.navigation_fragment__venue_sheet);
        mRouteChooseFromLayout  = makeRouteSheet.findViewById(R.id.make_route__choose_from_layout);
        venueScrollView         = venueBottomSheet.findViewById(R.id.venue_dialog__description_view);
        mSheetDescription       = venueBottomSheet.findViewById(R.id.venue_dialog__description);
        mSheetTitle             = venueBottomSheet.findViewById(R.id.venue_dialog__title);
        mVenuePhone             = venueBottomSheet.findViewById(R.id.venue_dialog__phone_tv);
        mVenueCategory          = venueBottomSheet.findViewById(R.id.venue_dialog__category_tv);
        mCallButton             = venueBottomSheet.findViewById(R.id.venue_dialog__call);
        mSheetVenueImage        = venueBottomSheet.findViewById(R.id.venue_dialog__image);
        cancelRouteSheet        = view.findViewById(R.id.navigation_fragment__cancel_route_sheet);
        venuesListSheet         = view.findViewById(R.id.navigation_fragment__venues_sheet);
        mMakeRouteBehavior      = BottomSheetBehavior.from(makeRouteSheet);
        mCancelRouteBehaviour   = BottomSheetBehavior.from(cancelRouteSheet);
        mSheetBehavior          = BottomSheetBehavior.from(venueBottomSheet);
        mVenuesBehavior         = BottomSheetBehavior.from(venuesListSheet);
        mToText                 = makeRouteSheet.findViewById(R.id.make_route__to_text);
        mFromCurrentText        = makeRouteSheet.findViewById(R.id.make_route__from_current_title);
        mStartRouteButton       = makeRouteSheet.findViewById(R.id.start_route__button);
        mFromImageView          = makeRouteSheet.findViewById(R.id.make_route__from_current_image);
        mCancelRouteDistance    = cancelRouteSheet.findViewById(R.id.cancel_route_sheet__distance_tv);
        mCancelRouteTime        = cancelRouteSheet.findViewById(R.id.cancel_route_sheet__time_tv);
        mRouteSheetCancelButton = cancelRouteSheet.findViewById(R.id.cancel_route_sheet__cancel_button);
        cancelRouteListView     = cancelRouteSheet.findViewById(R.id.cancel_route_sheet__list_view);
        mVenueCancelButton      = venuesListSheet.findViewById(R.id.venues__cancel_button);
        mMakeVenueRouteButton   = view.findViewById(R.id.navigation_fragment__route_button);
    }

    private void setViewsParams() {
        mMakeRouteBehavior.   setState(BottomSheetBehavior.STATE_HIDDEN);
        mCancelRouteBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
        mSheetBehavior.       setState(BottomSheetBehavior.STATE_HIDDEN);
        mVenuesBehavior.      setState(BottomSheetBehavior.STATE_HIDDEN);
        mMakeRouteBehavior.   setState(BottomSheetBehavior.STATE_HIDDEN);
        mCancelRouteBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
        mVenuesBehavior.      setSkipCollapsed(true);
        locationView.         setBackgroundColor(Color.argb(255, 235, 235, 235));
        locationView.         getLocationViewController().setStickToBorder(true);
        mVenueRouteLayout.    setVisibility(GONE);
        mDirectionLayout.     setVisibility(GONE);
    }

    private void setViewsListeners() {

        mStartRouteButton.setOnClickListener(v ->
        {
            if (mFromVenue == null && mFromPoint == null && mSelectMapPoint) // if starting point doesn't exist then return
                return;
            onMakeRoute(); // set target (endpoint) and display route path
        });

        mCancelRouteBehaviour.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {
                if (i == BottomSheetBehavior.STATE_HIDDEN)
                    onCancelRoute(null);
            }

            @Override
            public void onSlide(@NonNull View view, float v) {
            }
        });

        mSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(View view, int i) {
                if (i == BottomSheetBehavior.STATE_HIDDEN) {
                    mPinVenue = null;
                    mVenueRouteLayout.setVisibility(GONE);
                    venueScrollView.fullScroll(View.FOCUS_UP);
                    venueScrollView.smoothScrollTo(0, 0);
                }
            }

            @Override
            public void onSlide(View view, float v) {
                mVenueRouteLayout.setVisibility(VISIBLE);
                mRouteDivider.setVisibility(v > 0 ? VISIBLE : View.INVISIBLE);
                mVenueRouteLayout.animate().y(v <= 0 ? view.getY() + mSheetBehavior.getPeekHeight() - mVenueRouteLayout.getHeight() : view.getHeight() - mVenueRouteLayout.getHeight()).setDuration(0).start();
            }
        });

        mMakeRouteBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            float translationY = 0.0f;
            float viewMaxY = 0.0f;

            @Override
            public void onStateChanged(@NonNull View view, int i) {
                if (i == BottomSheetBehavior.STATE_HIDDEN) {
                    mStartRouteButton.setBackground(getResources().getDrawable(R.drawable.login_done_button));
                    setActiveMakeRouteButton(false, false);
                    cancelPin();

                    if (translationY < 0) {
                        locationView.animate().y(0).setDuration(300);
                        translationY = 0.0f;
                    }
                } else if (i == BottomSheetBehavior.STATE_EXPANDED) {
                    if (mPinPoint != null) {
                        viewMaxY = view.getY();
                        translationY = viewMaxY - locationView.getLocationViewController().metersToScreenPosition(mPinPoint.getPoint(), false).y - 180;
                        if (translationY < 0)
                            locationView.animate().y(translationY).setDuration(300);
                    }
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {
                if (mPinPoint != null && translationY < 0 && view.getY() - 180 < viewMaxY) {
                    locationView.animate().y(0).setDuration(300);
                    translationY = 0.0f;
                }
            }
        });


        swap.setOnClickListener(view1 -> {
            if (gvMain.getVisibility() == View.VISIBLE)
                gvMain.setVisibility(View.GONE);
            else if (gvMain.getVisibility() == View.GONE)
                gvMain.setVisibility(View.VISIBLE);
        });

        record.setOnClickListener(view1 -> {
            if (isRecording) {
                NavigineApp.NavigationManager.stopLogRecording();//stop log record
                isRecording = false;
                record.setText("Rec");
                record.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            } else {
                NavigineApp.NavigationManager.startLogRecording();//start log record
                isRecording = true;
                record.setText("Stop");
                record.setTextColor(getResources().getColor(R.color.removeColor));
            }
        });

        makeRouteSheet.         setOnTouchListener((view, motionEvent) -> true);
        cancelRouteSheet.       setOnTouchListener((view, motionEvent) -> true);
        mVenueCancelButton.     setOnClickListener(view1 -> mVenuesBehavior.setState(BottomSheetBehavior.STATE_HIDDEN));
        mRouteSheetCancelButton.setOnClickListener(v -> mCancelRouteBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN));

        /*
         * PERFORM AN ACTION ON PICKED MAP OBJECT OR FEATURE OBJECT
         *
         * For performing action on picked object set PickListener on location view.
         * It's called after locationViewController pickMapFeaturetAt or pickMapObjectAt event occurs.
         * If you picked map object then onMapObjectPickComplete is called.
         * params:
         *              mapObjectResult - contains information about picked map object,
         *              it's coordinates on location view, attached location and sublocation ids.
         *              Also you can set values for map object fields.
         *              point - screen (x, y) coordinates of tap
         * If you picked map feature then onMapFeaturePickComplete is called.
         * params:
         *              hashmap - contains entry set (like key -> value) of properties picked feature
         *              object and it's value.
         *              point - screen (x,y) coordinates of tap
         */
        locationView.getLocationViewController().setPickListener(new PickListener() {
            @Override
            public void onMapObjectPickComplete(MapObjectPickResult mapObjectPickResult, PointF point) {
                if(mapObjectPickResult == null) {
                    return;
                }
                Log.d("NAVIGINE_LOG", "map object {id " + mapObjectPickResult.getMapObject().getId() + " type " + mapObjectPickResult.getMapObject().getType() + "}");
            }

            @Override
            public void onMapFeaturePickComplete(HashMap<String, String> hashMap, PointF point) {
                if (hashMap == null) {
                    return;
                }
                for (int i = 0; i < mSublocation.getVenues().size(); i++) {
                    Venue v = mSublocation.getVenues().get(i);
                    if (v.getName().equals(hashMap.get("name"))) {
                        mPinVenue = v; // set picked feature object (venue)
                        showVenueBottomSheet(); // show venue info
                        break;
                    }
                }
            }
        });


        /*
         * PERFORMING AN ACTION BY SINGLE-TAP EVENT
         *
         * Set responder for single-tap action on location view.
         * onSingleTapUp called when press gesture has finished.
         * params:
         *        x - screen x-coordinates
         *        y - screen y-coordinates
         *
         * onSingleTapConfirmed called when tap gesture has finished.
         * params:
         *        x - screen x-coordinates
         *        y - screen y-coordinates
         */
        locationView.getLocationViewController().getTouchInput().setTapResponder(new TouchInput.TapResponder() {
            @Override
            public boolean onSingleTapUp(float x, float y) {
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(float x, float y) {
                handleClick(x, y); // pick map object or cancel the set pin
                return true;
            }
        });

        /**
         * PERFORMING AN ACTION BY LONG-PRESS EVENT
         *
         * In order to perform an action on an long-press event, for example, set a destination to
         * build a route to it, set LongPressResponder for location view through location view
         * controller.
         * params:
         *        x - screen x-coordinates
         *        y - screen y-coordinates
         */
        locationView.getLocationViewController().getTouchInput().setLongPressResponder(new TouchInput.LongPressResponder() {
            @Override
            public void onLongPress(float x, float y) {
                if (hasTarget() || mFromPoint == null)
                    return; //if target already exist or user position is not defined then cancel
                NavigineApp.RouteManager.clearTargets(); //removing all the target points to where the routes were built.
                Point p = locationView.getLocationViewController().screenPositionToMeters(new PointF(x, y)); // transform screen coordinates to location view coordinates
                handleLongClick(p); // set a pin on the location
            }
        });
    }

    private void initAdapters() {
        mRouteEventAdapter = new RouteEventAdapter(mCancelRouteList);
    }

    private void setAdapters() {
        cancelRouteListView.setAdapter(mRouteEventAdapter);
    }

    private void addListeners() {
        /*
         *  SHOW USER CURRENT POSITION ON LOCATION
         *  For details see positionListener implementation
         */
        NavigineApp.NavigationManager.addPositionListener(positionListener);

        /*
         *  ZONE ENTRY OR EXIT NOTIFYING
         *  For details see zoneListener implementation
         */
        NavigineApp.ZoneManager.addZoneListener(zoneListener);


        /*
         *  UPDATE LOCATION ON LOCATION VIEW
         *  For details see locationListener implementation
         */
        NavigineApp.LocationManager.addLocationListener(locationListener);

        /*
         *  DETECTS BEACON WITH NOTIFICATION
         *  For details see notificationListener implementation
         */
        NavigineApp.NotificationManager.addNotificationListener(notificationListener);
    }

    /*
     * add drawing objects to location view
     */
    private void initLocationViewObjects() {
        // To display the user position on location view create IconMapObject and add it to
        // location view through LocationViewController. Also set icon size and image.
        mPosition = locationView.getLocationViewController().addIconMapObject();
        mPosition.setSize(30, 30);
        mPosition.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.blue_dot));

        // To display the built route on location view create PolylineMapObject and add it to
        // location view through LocationViewController. Set the polyline color in the format
        // (r, g, b, alpha) in range 0..1, and width (in meters)
        polylineMapObject = locationView.getLocationViewController().addPolylineMapObject();
        polylineMapObject.setColor(1, 0, 1, 1);
        polylineMapObject.setWidth(3);

        // To display the route pin on location view create IconMapObject and add it to
        // location view through LocationViewController. Also set icon size and image.
        targetPoint = locationView.getLocationViewController().addIconMapObject();
        targetPoint.setSize(24, 84);
        targetPoint.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_to_point_png));
    }


    private void makeRoute() {
        /**
         * MAKE A ROUTE TO AN ARBITRARY POINT (MAP OBJECT)
         *
         * First, you need to set the end point of the route.
         * End point can be as existing at the location feature map object (e.g. venue) or map object
         * (e.g. pin), defined by user.
         * If you want to set an arbitrary destination point at a location and display this on the
         * location view first you need to create IconMapObject by adding it to location view through LocationViewController.
         * Also set icon size and image.
         */
        targetPoint = locationView.getLocationViewController().addIconMapObject();
        targetPoint.setSize(24, 84);
        targetPoint.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_to_point_png));

        /**
         * Then to add the created target point to a location, you must perform long-press action on
         * the desired position on the map. To handle this event set LongPressResponder to the
         * LocationViewController of the location view:
         */
        locationView.getLocationViewController().getTouchInput().setLongPressResponder(new TouchInput.LongPressResponder() {
            @Override
            public void onLongPress(float x, float y) {
                if (hasTarget()) return; //if target already exist then cancel
                NavigineApp.RouteManager.clearTargets(); //removing all the target points to where the routes were built.
                Point p = locationView.getLocationViewController().screenPositionToMeters(new PointF(x, y)); // transform screen coordinates to location view coordinates
                handleLongClick(p); // logic for setting a target point (pin) on the location
            }
        });


        /**
         * MAKE A ROUTE TO A VENUE (FEATURE OBJECT)
         *
         * First, set PickListener to LocationViewController of location view. This methods will be invoked
         * when map object or feature map object will be picked by user.
         */

        locationView.getLocationViewController().setPickListener(new PickListener() {
            @Override
            public void onMapObjectPickComplete(MapObjectPickResult mapObjectPickResult, PointF point) {
                if(mapObjectPickResult == null) {
                    return;
                }
                Log.d("NAVIGINE_LOG", "map object {id " + mapObjectPickResult.getMapObject().getId() + " type " + mapObjectPickResult.getMapObject().getType() + "}");
            }

            @Override
            public void onMapFeaturePickComplete(HashMap<String, String> hashMap, PointF point) {
                if (hashMap == null) { // the click was not made on the feature map object
                    return;
                }
                for (int i = 0; i < mSublocation.getVenues().size(); i++) {
                    Venue v = mSublocation.getVenues().get(i); // get venue of current sublocation
                    if (v.getName().equals(hashMap.get("name"))) { // find picked venue by name
                        mPinVenue = v; // set picked feature object (venue)
                        showVenueBottomSheet(); // show venue info
                        break;
                    }
                }
            }
        });

        /**
         *
         * If you setup PickListener, select the desired feature object (e.g. venue) on the location by perform single-tap action.
         * To properly handle this event, set TapResponder to the LocationViewController of the location view.
         * When you click by map feature object (e.g. venue) onSingleTapConfirmed will be invoked.
         */
        locationView.getLocationViewController().getTouchInput().setTapResponder(new TouchInput.TapResponder() {
            @Override
            public boolean onSingleTapUp(float x, float y) {
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(float x, float y) {
                handleClick(x, y); //handle click with x,y screen coordinates
                return true;
            }
        });

        /**
         * Let's say when setting a target point (e.g.pin or venue) you show a dialog containing
         * route information and a 'start' button. By pressing the 'start' button, you must call the method (e.g. onMakeRoute())
         * in which the target point is set by calling method setTarget of RouteManager and after that
         * add RouteListener to RouteManager.
         */
        onMakeRoute(); // call this by clicking 'start' button in your route info dialog


        /**
         * Don't forget to remove RouteListener after using
         */
    }

    public void onMakeRoute() {
        hideAndShowBottomSheets(mSheetBehavior, mVenuesBehavior, mMakeRouteBehavior, BottomSheetBehavior.STATE_HIDDEN); // hide route sheet

        if (mPinPoint != null) {
            mTargetPoint = mPinPoint;
            mTargetVenue = null;
            mPinPoint = null;
            mPinVenue = null;
            mToVenue = null;

            Log.d("NAVIGINE_LOG", "Set target point");

            NavigineApp.RouteManager.setTarget(mTargetPoint); // set target point (pin) to RouteManager

        } else if (mToVenue != null) {
            mTargetVenue = mToVenue;
            mTargetPoint = null;
            mPinVenue = null;
            mToVenue = null;
            mPinPoint = null;

            Log.d("NAVIGINE_LOG", "Set venue target " + mTargetVenue.getId());

            NavigineApp.RouteManager.setTarget(new LocationPoint(mTargetVenue.getPoint(), mLocation.getId(), mSublocation.getId())); // set venue as target point to RouteManager
        }

        /*
         *  DISPLAY ROUTE PATH
         *  For details see routeListener implementation
         */
        NavigineApp.RouteManager.addRouteListener(routeListener);
        mCancelRouteBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private void handleClick(float x, float y) {

        if (mLocation == null || mCurrentSubLocationIndex < 0)
            return;

        if (mTargetPoint != null || mTargetVenue != null || mPinPoint != null || mPinVenue != null || mMakeRouteBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            cancelPin(); // if target point (pin) set and the route isn't built yet then cancel it
            hideAndShowBottomSheets(mMakeRouteBehavior, mSheetBehavior, mVenuesBehavior, BottomSheetBehavior.STATE_HIDDEN); // hide venue sheet
            return;
        }

        // pick feature object on location view
        // this will trigger a method onMapFeaturePickComplete invoke
        locationView.getLocationViewController().pickMapFeaturetAt(x, y);
    }

    private void cancelPin() {
        if (mLocation == null || mCurrentSubLocationIndex < 0)
            return;

        if (mSublocation == null)
            return;

        if (hasTarget())
            return;

        mPinPoint = null;
        mPinVenue = null;
        mToVenue = null;
        mFromVenue = null;
        mFromPoint = null;

        targetPoint.setVisible(false);
        polylineMapObject.setVisible(false);
    }

    private void handleLongClick(Point p) {
        // check if location exist and there is at least 1 sublocation
        if (mLocation == null || mCurrentSubLocationIndex < 0)
            return;

        makePin(p); // set pin to location view
    }

    private void makePin(Point P) {
        if (mLocation == null || mCurrentSubLocationIndex < 0)
            return;

        if (mSublocation == null)
            return;

        if (P.getX() < 0.0f || P.getX() > mSublocation.getWidth() || P.getY() < 0.0f || P.getY() > mSublocation.getHeight())
            return; // Missing the map

        if (hasTarget())
            return;

        mPinPoint = new LocationPoint(P, mLocation.getId(), mSublocation.getId()); // create a location point representing target point (pin)
        mPinVenue = null;
        targetPoint.setPosition(mPinPoint); // place pin icon on location view at the specific position
        targetPoint.setVisible(true);

        mToText.setText("To:       Point (" + String.format("%.1f", mPinPoint.getPoint().getX()) + ", " + String.format("%.1f", mPinPoint.getPoint().getY()) + ")");
        hideAndShowBottomSheets(mSheetBehavior, mVenuesBehavior, mMakeRouteBehavior, BottomSheetBehavior.STATE_EXPANDED); // show dialog with route info
    }


    private void hideAndShowBottomSheets(BottomSheetBehavior hideFirst, BottomSheetBehavior hideSecond, BottomSheetBehavior show, int showState) {
        hideFirst.setState(BottomSheetBehavior.STATE_HIDDEN);
        hideSecond.setState(BottomSheetBehavior.STATE_HIDDEN);
        show.setState(showState);
        if (show == mMakeRouteBehavior) {
            setActiveMakeRouteButton(false, false);
        }
    }

    private void setActiveMakeRouteButton(boolean isSelectMapPoint, boolean isGiveChoose) {
        mSelectMapPoint = isSelectMapPoint;
        if (isGiveChoose) {
            mFromPoint = null;
            mFromVenue = null;
        }

        mFromCurrentText.setText(isSelectMapPoint ? "From: Select Point on Map" : "From: Current Location");
        mFromCurrentText.setTextColor(isSelectMapPoint ? getResources().getColor(R.color.gray) : getResources().getColor(R.color.colorPrimary));
        mStartRouteButton.setBackground(isSelectMapPoint ? getResources().getDrawable(R.drawable.login_empty_button) : getResources().getDrawable(R.drawable.login_done_button));
        mFromImageView.setImageResource(isSelectMapPoint ? R.drawable.ic_to_point_png : R.drawable.ic_current_point);
        if (isSelectMapPoint)
            mFromImageView.setColorFilter(getResources().getColor(R.color.gray));
        else
            mFromImageView.clearColorFilter();
    }

    private boolean hasTarget() {
        return mTargetPoint != null || mTargetVenue != null;
    }

    private void updateRouteInfo(RoutePath routePath) {

        mLastActiveRoutePath = routePath;
        if (mLastActiveRoutePath == null)
            return;

        // Check if location is loaded
        if (mLocation == null || mCurrentSubLocationIndex < 0)
            return;


        String infoText = "No route path!";
        String timeText = "";
        mCancelRouteBehaviour.setState(mCancelRouteBehaviour.getState());


        if (mLastActiveRoutePath.getLength() != 0 && mFromPoint == null && mFromVenue == null) {
            int i = 0;
            if (mLastActiveRoutePath.getEvents().size() >= 1)
                showDirections(mLastActiveRoutePath);
            float distance = 0.0f;
            RouteEventType type = null;

            for (RouteEvent ev : mLastActiveRoutePath.getEvents()) {
                distance = ev.getDistance();
                type = ev.getType();
                if (distance >= 1) {
                    break;
                }
            }

            if (distance != 0.0f) {
                double time = (distance / 1.43) / 60;
                if (time >= 1) {
                    infoText = String.format(Locale.ENGLISH, "You have less than %.0f meters to go", distance);
                    timeText = String.format(Locale.ENGLISH, "(~%.0f min)", time);
                } else {
                    String distanceText = String.format(Locale.ENGLISH, "after %.0f meters", distance);
                    if (type == RouteEventType.TURN_RIGHT) {
                        infoText = String.format(Locale.ENGLISH, "You should turn right %s", distanceText);
                    } else if (type == RouteEventType.TURN_LEFT) {
                        infoText = String.format(Locale.ENGLISH, "You should turn left %s", distanceText);
                    } else if (type == RouteEventType.TRANSITION) {
                        infoText = String.format(Locale.ENGLISH, "You should change floor %s", distanceText);
                    }
                    timeText = time + " min";
                }
            }
        } else if (mLastActiveRoutePath != null) {
            if (mLastActiveRoutePath.getEvents().size() >= 1)
                showDirections(mLastActiveRoutePath);
            float distance = 0.0f;
            RouteEventType type = null;

            for (RouteEvent ev : mLastActiveRoutePath.getEvents()) {
                distance = ev.getDistance();
                type = ev.getType();
                if (distance >= 1) {
                    break;
                }
            }

            if (distance != 0.0f) {
                double time = (distance / 1.43) / 60;
                if (time >= 1) {
                    infoText = String.format(Locale.ENGLISH, "You have less than %.0f meters to go", distance);
                    timeText = String.format(Locale.ENGLISH, "(~%.0f min)", time);
                } else {
                    String distanceText = String.format(Locale.ENGLISH, "after %.0f meters", distance);
                    if (type == RouteEventType.TURN_RIGHT) {
                        infoText = String.format(Locale.ENGLISH, "You should turn right %s", distanceText);
                    } else if (type == RouteEventType.TURN_LEFT) {
                        infoText = String.format(Locale.ENGLISH, "You should turn left %s", distanceText);
                    } else if (type == RouteEventType.TRANSITION) {
                        infoText = String.format(Locale.ENGLISH, "You should change floor %s", distanceText);
                    }
                    timeText = time + " min";
                }
            }
        } else {
            mDirectionLayout.setVisibility(GONE);
        }

        if (mFromPoint != null || mFromVenue != null) {
            RoutePath path = makeRoutePath();
            if (path != null && path.getLength() != 0.0f) {
                float distance = path.getLength();
                infoText = String.format(Locale.ENGLISH, "%.0f m", distance);

                double time = (distance / 1.43) / 60;
                timeText = time < 1 ? "< 1 min" : String.format(Locale.ENGLISH, "%.0f min", time);

                addRouteEventsToList(path);
            }
        }
        mCancelRouteDistance.setText(infoText);
        mCancelRouteTime.setText(timeText);
        mRouteEventAdapter.notifyDataSetChanged();
    }

    private RoutePath makeRoutePath() {

        RoutePath path = null;
        if (mFromVenue != null) {
            if (mTargetVenue != null)
                path = NavigineApp.RouteManager.makeRoute(
                        new LocationPoint(mFromVenue.getPoint(), mFromVenue.getLocationId(), mFromVenue.getSublocationId()),
                        new LocationPoint(mTargetPoint.getPoint(), mTargetVenue.getLocationId(), mTargetVenue.getSublocationId()));
            else if (mTargetPoint != null)
                path = NavigineApp.RouteManager.makeRoute(
                        new LocationPoint(mFromVenue.getPoint(), mFromVenue.getLocationId(), mFromVenue.getSublocationId()),
                        mTargetPoint);
        } else {
            if (mTargetVenue != null)
                path = NavigineApp.RouteManager.makeRoute(mFromPoint,
                        new LocationPoint(mTargetVenue.getPoint(), mTargetVenue.getLocationId(), mTargetVenue.getSublocationId()));
            else if (mTargetPoint != null)
                path = NavigineApp.RouteManager.makeRoute(mFromPoint, mTargetPoint);
        }
        return path;
    }


    public void onCancelRoute(View v) {
        mTargetPoint = null;
        mTargetVenue = null;
        mPinPoint = null;
        mPinVenue = null;
        mFromPoint = null;
        mFromVenue = null;
        mLastActiveRoutePath = null;

        NavigineApp.RouteManager.removeRouteListener(routeListener);

        polylineMapObject.setVisible(false);
        targetPoint.setVisible(false);


        NavigineApp.RouteManager.cancelTarget();
        NavigineApp.RouteManager.clearTargets();

        mDirectionLayout.setVisibility(GONE);
    }

    private void showDirections(RoutePath path) {
        addRouteEventsToList(path);
        RouteEventType type = null;
        float nextTurnDistance = 0;
        for (RouteEvent event : path.getEvents()) {
            nextTurnDistance = event.getDistance();
            type = event.getType();
            if (nextTurnDistance >= 1) {
                break;
            }
        }

        if (nextTurnDistance < 1) {
            mDirectionLayout.setVisibility(GONE);
            return;
        }

        switch (type) {
            case TURN_LEFT:
                mDirectionImageView.setBackgroundResource(R.drawable.ic_left);
                break;
            case TURN_RIGHT:
                mDirectionImageView.setBackgroundResource(R.drawable.ic_right);
                break;
            case TRANSITION:
                mDirectionImageView.setBackgroundResource(R.drawable.ic_escalator);
                break;
        }
        mDirectionTextView.setText(String.format(Locale.ENGLISH, "%.0f m", nextTurnDistance));
        mDirectionLayout.setVisibility(VISIBLE);
    }

    private void addRouteEventsToList(RoutePath path) {
        int distance = 0;
        List<RouteEvent> events = new ArrayList<>();
        for (RouteEvent event : path.getEvents()) {
            distance += event.getDistance();
            if (distance >= 1) {
                events.add(event);
            }
        }
        mCancelRouteList.clear();
        mCancelRouteList.addAll(events);
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private void showVenueBottomSheet() {
        mSheetDescription.setMovementMethod(new ScrollingMovementMethod());

        String titleText = mPinVenue.getName();
        if (titleText.length() > 25)
            titleText = titleText.substring(0, 24) + "…";

        String categoryText = mPinVenue.getName();
        if (categoryText.length() > 30)
            categoryText = categoryText.substring(0, 28) + "…";

        mSheetTitle.setText(titleText);
        mSheetDescription.setText(mPinVenue.getDescript());
        mVenuePhone.setText(mPinVenue.getPhone());
        mVenueCategory.setText(categoryText);

        mCallButton.setOnClickListener(view ->
        {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_DIAL); // Action for what intent called for
            intent.setData(Uri.parse("tel: " + mPinVenue.getPhone())); // Data with intent respective action on intent
            startActivity(intent);
        });

        String bm = mPinVenue.getImageId();
        if (!bm.equals("")) {
            mSheetVenueImage.setVisibility(VISIBLE);
            Glide
                    .with(Objects.requireNonNull(getContext()))
                    .load(bm)
                    .apply(new RequestOptions().centerCrop())
                    .into(mSheetVenueImage);
        } else
            mSheetVenueImage.setImageDrawable(Objects.requireNonNull(getActivity()).getResources().getDrawable(R.drawable.elm_loading_venue_photo));

        mMakeVenueRouteButton.setVisibility(mFromPoint == null ? GONE : VISIBLE);

        mMakeVenueRouteButton.setOnClickListener(view ->
        {
            mPinPoint = null;
            mToVenue = mPinVenue;
            mSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            mVenueRouteLayout.setVisibility(GONE);
            String title = mPinVenue.getName();
            if (title.length() > 20)
                title = title.substring(0, 18) + "";
            mToText.setText("To:       Venue " + title);
            hideAndShowBottomSheets(mSheetBehavior, mVenuesBehavior, mMakeRouteBehavior, BottomSheetBehavior.STATE_EXPANDED);
        });

        hideAndShowBottomSheets(mMakeRouteBehavior, mVenuesBehavior, mSheetBehavior, BottomSheetBehavior.STATE_COLLAPSED);
    }


    // location loader to list view
    private class SublocationsAdapter extends BaseAdapter
    {
        @Override
        public int getCount()
        {
            return mSublocations.size();
        }

        @Override
        public Object getItem(int i)
        {
            return mSublocations.get(i);
        }

        @Override
        public long getItemId(int pos)
        {
            return pos;
        }

        private void updateList()
        {
            notifyDataSetChanged();
        }

        @SuppressLint({"InflateParams", "ClickableViewAccessibility"})
        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup)
        {
            Sublocation sublocation     = mSublocations.get(i);
            String      sublocationName = sublocation.getName();

            View view = convertView;
            if (view == null)
            {
                LayoutInflater inflater = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.item, null);
            }

            TextView titleTextView = view.findViewById(R.id.tvText);

            if (sublocationName.length() >= 12)
                sublocationName = sublocationName.substring(0, 10) + "...";

            titleTextView.setText(sublocationName);

            view.setOnClickListener(v -> {
                mSublocation = sublocation; // set selected item as current sublocation
                NavigineApp.CurrentSublocation = mSublocation;
                mCurrentSubLocationIndex = i;
                locationView.getLocationViewController().setSublocationId(sublocation.getId());
                gvMain.setVisibility(View.GONE);
            });

            return view;
        }
    }
}

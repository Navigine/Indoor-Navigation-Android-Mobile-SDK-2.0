package com.navigine.navigine.demo.ui.fragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.navigine.navigine.demo.utils.Constants.KEY_ID_SUBLOCATION;
import static com.navigine.navigine.demo.utils.Constants.KEY_VENUE_POINT;
import static com.navigine.navigine.demo.utils.Constants.KEY_VENUE_SUBLOCATION;
import static com.navigine.navigine.demo.utils.Constants.LOCATION_CHANGED;
import static com.navigine.navigine.demo.utils.Constants.NOTIFICATION_PUSH_ID;
import static com.navigine.navigine.demo.utils.Constants.TAG;
import static com.navigine.navigine.demo.utils.Constants.VENUE_SELECTED;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.navigine.idl.java.AnimationType;
import com.navigine.idl.java.Camera;
import com.navigine.idl.java.IconMapObject;
import com.navigine.idl.java.Location;
import com.navigine.idl.java.LocationPoint;
import com.navigine.idl.java.LocationPolyline;
import com.navigine.idl.java.LocationViewListener;
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
import com.navigine.navigine.demo.R;
import com.navigine.navigine.demo.adapters.route.RouteEventAdapter;
import com.navigine.navigine.demo.adapters.venues.VenueListAdapter;
import com.navigine.navigine.demo.models.PushNotification;
import com.navigine.navigine.demo.ui.custom.lists.BottomSheetListView;
import com.navigine.navigine.demo.ui.custom.lists.ListViewLimit;
import com.navigine.navigine.demo.ui.dialogs.sheets.VenueBottomSheet;
import com.navigine.navigine.demo.utils.KeyboardController;
import com.navigine.navigine.demo.utils.NavigineSdkManager;
import com.navigine.navigine.demo.viewmodel.SharedViewModel;
import com.navigine.view.LocationView;
import com.navigine.view.TouchInput;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class NavigationFragment extends Fragment {

    // Constants
    private static final int    ERROR_MESSAGE_TIMEOUT = 5000;
    private static final String KEY_VENUE             = "name";
    private static final float  ADJUST_ZOOM_FACTOR    = 1.7f;

    private SharedViewModel viewModel = null;

    // GUI parameters
    private Window                    window                     = null;
    private BottomNavigationView      mNavigationView            = null;
    private SearchView                mSearchField               = null;
    private ConstraintLayout          mNavigationLayout          = null;
    private VenueBottomSheet          mVenueBottomSheet          = null;
    private ConstraintLayout          mNoLocationLayout          = null;
    private ConstraintLayout          mMakeRouteSheet            = null;
    private ConstraintLayout          mCancelRouteSheet          = null;
    private ConstraintLayout          mVenueListLayout           = null;
    private ConstraintLayout          mSearchLayout              = null;
    private LinearLayout              mRouteChooseFromLayout     = null;
    private LinearLayout              mSublocationsLayout        = null;
    private FrameLayout               mArrowUpLayout             = null;
    private FrameLayout               mArrowDownLayout           = null;
    private FrameLayout               mZoomInLayout              = null;
    private FrameLayout               mZoomOutLayout             = null;
    private FrameLayout               mCircularProgress          = null;
    private CircularProgressIndicator mCircularProgressIndicator = null;
    private LocationView              mLocationView              = null;
    private TextView                  mErrorMessageLabel         = null;
    private TextView                  mFromCurrentText           = null;
    private TextView                  mToText                    = null;
    private TextView                  mCancelRouteDistance       = null;
    private TextView                  mCancelRouteTime           = null;
    private TextView                  mVenuesEmptyTextView       = null;
    private AppCompatImageView        mFromImageView             = null;
    private MaterialButton            mSearchBtn                 = null;
    private MaterialButton            mSearchBtnClose            = null;
    private FrameLayout               mAdjustModeButton          = null;
    private MaterialButton            mChoseMapButton            = null;
    private Button                    mStartRouteButton          = null;
    private Button                    mRouteMakeChangeButton     = null;
    private MaterialButton            mRouteSheetCancelButton    = null;
    private BottomSheetBehavior       mMakeRouteBehavior         = null;
    private BottomSheetBehavior       mCancelRouteBehaviour      = null;
    private BottomSheetListView       mCancelRouteListView       = null;
    private IconMapObject             mPositionIcon              = null;
    private ListViewLimit             mSublocationsListView      = null;
    private RecyclerView              mVenueListView             = null;

    private DividerItemDecoration itemDivider        = null;


    private boolean mAdjustMode           = false;
    private boolean mSelectMapPoint       = false;

    private long mErrorMessageTime = 0;


    private Position mPosition = null;

    private LocationPoint mPinPoint    = null;
    private LocationPoint mTargetPoint = null;
    private LocationPoint mFromPoint   = null;

    private Venue mToVenue     = null;
    private Venue mFromVenue   = null;
    private Venue mTargetVenue = null;
    private Venue mPinVenue    = null;

    private Location    mLocation    = null;
    private Sublocation mSublocation = null;

    private int mSublocationId = -1;

    private List<RouteEvent>  mCancelRouteList  = new ArrayList<>();
    private ArrayList<Point>  mPoints           = new ArrayList<>();
    private List<Integer>     mSublocationsIds  = new ArrayList<>();

    private RouteEventAdapter      mRouteEventAdapter      = null;
    private ArrayAdapter<String>   mSublocationsAdapter    = null;
    private VenueListAdapter       mVenueListAdapter       = null;

    private IconMapObject     mTargetPointIcon     = null;
    private PolylineMapObject mPolylineMapObject   = null;
    private RoutePath         mRoutePath           = null;
    private RoutePath         mLastActiveRoutePath = null;

    private static Handler  mHandler = new Handler();

    private RouteListener        mRouteListener        = null;
    private PositionListener     mPositionListener     = null;
    private LocationViewListener mLocationViewListener = null;
    private NotificationListener mNotificationListener = null;

    private LocationManager  mLocationManager  = null;
    private BluetoothManager mBluetoothManager = null;
    private BluetoothAdapter mBluetoothAdapter = null;

    private StateReceiver mReceiver = null;
    private IntentFilter  mFilter   = null;

    private boolean isGpsEnabled       = false;
    private boolean isNetworkEnabled   = false;
    private boolean mSetupPosition     = true;
    private boolean locationChanged    = false;
    private boolean locationLoaded     = false;

    private Snackbar snackBar = null;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSystemServices();
        initViewModels();
        initListeners();
        initBroadcastReceiver();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navigation, container, false);
        initViews(view);
        setViewsParams();
        initLocationViewObjects();
        initAdapters();
        setAdapters();
        setViewsListeners();
        setObservers();
        setupRoutePin();
        addListeners();

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        onHiddenChanged(!isVisible());
        checkGpsState();
        checkBluetoothState();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            window.setStatusBarColor(mNavigationLayout.getVisibility() == VISIBLE ?
                    ContextCompat.getColor(requireActivity(), R.color.colorOnBackground) :
                    ContextCompat.getColor(requireActivity(), R.color.colorBackground));
            if (locationChanged) showCircularProgress();
            if (locationLoaded) loadMap();
            if (mLocationView != null) mLocationView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mLocationView.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        removeListeners();
    }

    private void initViews(View view) {
        window                     = requireActivity().getWindow();
        //Layouts
        mNavigationView            = requireActivity().findViewById(R.id.main__bottom_navigation);
        mSearchLayout              = view.findViewById(R.id.navigation_fragment_search);
        mVenueListLayout           = view.findViewById(R.id.navigation_fragment__venue_listview);
        mNoLocationLayout          = view.findViewById(R.id.navigation_fragment__no_location_layout);
        mNavigationLayout          = view.findViewById(R.id.navigation_fragment__navigation_layout);
        mArrowUpLayout             = view.findViewById(R.id.navigation_fragment__arrow_up);
        mArrowDownLayout           = view.findViewById(R.id.navigation_fragment__arrow_down);
        mZoomInLayout              = view.findViewById(R.id.navigation_fragment__zoom_in_view);
        mZoomOutLayout             = view.findViewById(R.id.navigation_fragment__zoom_out_view);
        mSublocationsLayout        = view.findViewById(R.id.navigation_fragment__sublocations_container);
        //Sheets
        mVenueBottomSheet          = new VenueBottomSheet();
        mMakeRouteSheet            = view.findViewById(R.id.navigation_fragment__make_route_sheet);
        mCancelRouteSheet          = view.findViewById(R.id.navigation_fragment__cancel_route_sheet);
        mRouteChooseFromLayout     = mMakeRouteSheet.findViewById(R.id.make_route__choose_from_layout);
        mCancelRouteListView       = mCancelRouteSheet.findViewById(R.id.cancel_route_sheet__list_view);
        mMakeRouteBehavior         = BottomSheetBehavior.from(mMakeRouteSheet);
        mCancelRouteBehaviour      = BottomSheetBehavior.from(mCancelRouteSheet);
        //ImageViews
        mFromImageView             = mMakeRouteSheet.findViewById(R.id.make_route__from_current_image);
        //Buttons
        mSearchBtn                 = view.findViewById(R.id.navigation_fragment__search_btn);
        mSearchBtnClose            = view.findViewById(R.id.navigation_fragment__search_btn_close);
        mRouteSheetCancelButton    = mCancelRouteSheet.findViewById(R.id.cancel_route_sheet__cancel_button);
        mRouteMakeChangeButton     = mMakeRouteSheet.findViewById(R.id.make_route__change_route_сhoose);
        mChoseMapButton            = view.findViewById(R.id.navigation_fragment__choose_map);
        mStartRouteButton          = mMakeRouteSheet.findViewById(R.id.start_route__button);
        mAdjustModeButton          = view.findViewById(R.id.navigation_fragment__adjust_mode_button);
        //TextViews
        mFromCurrentText           = mMakeRouteSheet.findViewById(R.id.make_route__from_current_title);
        mToText                    = mMakeRouteSheet.findViewById(R.id.make_route__to_text);
        mCancelRouteDistance       = mCancelRouteSheet.findViewById(R.id.cancel_route_sheet__distance_tv);
        mCancelRouteTime           = mCancelRouteSheet.findViewById(R.id.cancel_route_sheet__time_tv);
        mErrorMessageLabel         = view.findViewById(R.id.navigation_fragment__error_message_label);
        mVenuesEmptyTextView       = view.findViewById(R.id.li_venues_empty);
        //Progress
        mCircularProgress          = view.findViewById(R.id.navigation_fragment__progress_circular);
        mCircularProgressIndicator = view.findViewById(R.id.navigation_fragment__progress_circular_indicator);
        //LocationView
        mLocationView              = view.findViewById(R.id.navigation_fragment__location_view);
        //ListView
        mSublocationsListView      = view.findViewById(R.id.navigation_fragment__sublocations_lv);
        mVenueListView             = view.findViewById(R.id.recycler_list_venues);
        //Dividers
        itemDivider                = new DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL);
        //Search
        mSearchField               = view.findViewById(R.id.navigation_fragment__search_field);
    }

    private void setViewsParams() {
        mSearchLayout.         getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        mMakeRouteBehavior.    setState(BottomSheetBehavior.STATE_HIDDEN);
        mCancelRouteBehaviour. setState(BottomSheetBehavior.STATE_HIDDEN);
        mLocationView.         setBackgroundColor(Color.argb(255, 235, 235, 235));
        mLocationView.         getLocationViewController().setStickToBorder(true);
        itemDivider.           getDrawable().setColorFilter(ContextCompat.getColor(requireActivity(), R.color.colorBackground), PorterDuff.Mode.SRC);
        mVenueListView.        addItemDecoration(itemDivider);
        mErrorMessageLabel.    setVisibility(GONE);

        mLocationView.getLocationViewController().setPickRadius(10);

    }


    private void setViewsListeners() {

        mSearchField.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            boolean isEmpty = mSearchField.getQuery().toString().isEmpty();
            if (hasFocus) {
                if (!isEmpty) mSearchLayout.setBackgroundColor(Color.WHITE);
                mSearchField.setBackgroundResource(R.drawable.bg_rounded_search_light);
                mSearchBtnClose.  setVisibility(VISIBLE);
                mVenueListLayout. setVisibility(isEmpty ? GONE : VISIBLE);
            } else {
                if (isEmpty) mSearchLayout.setBackgroundColor(Color.TRANSPARENT);
                mSearchField.setBackgroundResource(R.drawable.bg_rounded_search);
                KeyboardController.hideSoftKeyboard(requireActivity());
            }
        });

        mSearchField.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    mSearchLayout.    setBackgroundColor(Color.TRANSPARENT);
                    mSearchBtn.       setVisibility(GONE);
                    mSearchBtnClose.  setVisibility(VISIBLE);
                    mVenueListLayout. setVisibility(GONE);
                } else {
                    mSearchLayout.    setBackgroundColor(Color.WHITE);
                    mSearchBtnClose.  setVisibility(GONE);
                    mSearchBtn.       setVisibility(VISIBLE);
                    mVenueListLayout. setVisibility(VISIBLE);
                }
                mVenueListAdapter.filter(newText);
                return true;
            }
        });

        mSearchBtnClose.setOnClickListener(v -> {
            mSearchField.     clearFocus();
            mSearchBtnClose.  setVisibility(GONE);
        });

        mSublocationsListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (totalItemCount > 3) {
                    View first = view.getChildAt(firstVisibleItem);
                    View last  = view.getChildAt(visibleItemCount - 1);
                    if (first != null) {
                        boolean halfVisible = first.getTop() != 0;
                        mArrowUpLayout.setVisibility(halfVisible ? VISIBLE : GONE);
                    }
                    if (last != null) {
                        boolean halfVisible = last.getBottom() != view.getHeight();
                        mArrowDownLayout.setVisibility(halfVisible ? VISIBLE : GONE);
                    }
                }
            }
        });

        mSublocationsListView.setOnItemClickListener((parent, view, position, id) -> {
            loadSubLocation(position);
        });


        mArrowUpLayout.setOnClickListener(v -> {
            int index = mSublocationsListView.getFirstVisiblePosition();
            mSublocationsListView.smoothScrollToPosition(index + -1);
        });

        mArrowDownLayout.setOnClickListener(v -> {
            int index = mSublocationsListView.getLastVisiblePosition();
            mSublocationsListView.smoothScrollToPosition(index + 1);
        });

        mSublocationsAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (mSublocationsAdapter.getCount() > 3) {
                    mArrowUpLayout.  setVisibility(VISIBLE);
                    mArrowDownLayout.setVisibility(VISIBLE);
                } else {
                    mArrowUpLayout.  setVisibility(GONE);
                    mArrowDownLayout.setVisibility(GONE);
                }
            }
        });


        mChoseMapButton.setOnClickListener(v -> mNavigationView.setSelectedItemId(R.id.navigation_locations));

        mMakeRouteSheet.setOnTouchListener((view, motionEvent) -> true);

        mStartRouteButton.setOnClickListener(v ->
        {
            if (mFromVenue == null && mFromPoint == null && mSelectMapPoint)
                return;
            onMakeRoute();
        });

        mMakeRouteBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            float translationY = 0.0f;
            float viewMaxY = 0.0f;

            @Override
            public void onStateChanged(@NonNull View view, int i) {
                if (i == BottomSheetBehavior.STATE_HIDDEN) {
                    setActiveMakeRouteButton(false, false);
                    cancelPin();

                    if (translationY < 0) {
                        mLocationView.animate().y(0).setDuration(300);
                        translationY = 0.0f;
                    }
                } else if (i == BottomSheetBehavior.STATE_EXPANDED) {
                    if (mPinPoint != null) {
                        viewMaxY = view.getY();
                        translationY = viewMaxY - mLocationView.getLocationViewController().metersToScreenPosition(mPinPoint.getPoint(), false).y - 180;
                        if (translationY < 0)
                            mLocationView.animate().y(translationY).setDuration(300);
                    }
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {
                if (mPinPoint != null && translationY < 0 && view.getY() - 180 < viewMaxY) {
                    mLocationView.animate().y(0).setDuration(300);
                    translationY = 0.0f;
                }
            }
        });

        mCancelRouteSheet.setOnTouchListener((view, motionEvent) -> true);
        mCancelRouteBehaviour.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {
                if (i == BottomSheetBehavior.STATE_HIDDEN)
                    onCancelRoute();
            }

            @Override
            public void onSlide(@NonNull View view, float v) {
            }
        });

        mRouteChooseFromLayout. setOnClickListener(v -> setActiveMakeRouteButton(!mSelectMapPoint, true));
        mRouteMakeChangeButton. setOnClickListener(v -> setActiveMakeRouteButton(!mSelectMapPoint, true));
        mRouteSheetCancelButton.setOnClickListener(v -> mCancelRouteBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN));
        mZoomInLayout.          setOnClickListener(v -> onZoomIn());
        mZoomOutLayout.         setOnClickListener(v -> onZoomOut());
        mAdjustModeButton.      setOnClickListener(v -> toggleAdjustMode());


        mLocationView.getLocationViewController().setPickListener(new PickListener() {
            @Override
            public void onMapObjectPickComplete(MapObjectPickResult mapObjectPickResult, PointF pointF) {
            }

            @Override
            public void onMapFeaturePickComplete(HashMap<String, String> hashMap, PointF pointF) {
                if (hashMap != null && hashMap.containsKey(KEY_VENUE)) {
                    for (int i = 0; i < mSublocation.getVenues().size(); i++) {
                        Venue v = mSublocation.getVenues().get(i);
                        if (v.getName().equals(hashMap.get(KEY_VENUE))) {
                            mPinVenue = v;
                            showVenueBottomSheet();
                            break;
                        }
                    }
                }
            }
        });

        mLocationView.getLocationViewController().getTouchInput().setTapResponder(new TouchInput.TapResponder() {
            @Override
            public boolean onSingleTapUp(float x, float y) {
                return false;
            }

            @Override
            public boolean onSingleTapConfirmed(float x, float y) {
                handleClick(x, y);
                return true;
            }
        });

        mLocationView.getLocationViewController().getTouchInput().setLongPressResponder((x, y) -> {
            if (hasTarget() || mFromPoint == null) return;
            NavigineSdkManager.RouteManager.clearTargets();
            Point location = mLocationView.getLocationViewController().screenPositionToMeters(new PointF(x, y));
            setRoutePin(location);
            handleLongClick(x, y);
        });

        mLocationView.getLocationViewController().setLocationViewListener(mLocationViewListener);
    }

    private void setObservers() {
        viewModel.mLocation.observe(getViewLifecycleOwner(), location -> {
            mLocation = location;
            locationLoaded = mLocation != null;

            if (locationLoaded) {
                mSublocationsListView.  clearChoices();
                mSublocationsAdapter.   clear();
                mVenueListAdapter.      clear();
                mSublocationsIds.       clear();

                mSublocationsLayout.setVisibility(mLocation.getSublocations().size() <= 1 ? GONE : VISIBLE);

                for (Sublocation sublocation : mLocation.getSublocations()) {
                    mSublocationsIds.    add(sublocation.getId());
                    mSublocationsAdapter.add(sublocation.getName());
                    mVenueListAdapter.   add(sublocation.getVenues(), mLocation);
                }
                if (isVisible()) loadMap();
            }
        });;
    }

    private void initAdapters() {
        mSublocationsAdapter    = new ArrayAdapter<>(requireActivity(), R.layout.list_item_sublocation);
        mRouteEventAdapter      = new RouteEventAdapter();
        mVenueListAdapter       = new VenueListAdapter();
    }

    private void setAdapters() {
        mSublocationsListView.setAdapter(mSublocationsAdapter);
        mCancelRouteListView. setAdapter(mRouteEventAdapter);
        mVenueListView.       setAdapter(mVenueListAdapter);
    }


    private void initLocationViewObjects() {
        mPolylineMapObject = mLocationView.getLocationViewController().addPolylineMapObject();
        mPolylineMapObject.setColor(76.0f/255, 217.0f/255, 100.0f/255, 1);
        mPolylineMapObject.setWidth(3);
        mPolylineMapObject.setStyle("{style: 'points', placement_min_length_ratio: 0, placement_spacing: 8px, size: [8px, 8px], placement: 'spaced'}");

        mPositionIcon = mLocationView.getLocationViewController().addIconMapObject();
        mPositionIcon.setSize(30, 30);
        mPositionIcon.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_current_point_png));
        mPositionIcon.setStyle("{ order: 1, collide: false}");
        mPositionIcon.setVisible(false);
    }

    private void getSystemServices() {
        mLocationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
        mBluetoothManager = (BluetoothManager) requireActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
    }

    private void initViewModels() {
        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    private void initListeners() {

        mPositionListener = new PositionListener() {

            @Override
            public void onPositionUpdated(Position position) {
                mPosition = position;
                if (mAdjustMode) {
                    int id = position.getSublocationId();
                    if (mSublocation.getId() != id) {
                        mSublocation = mLocation.getSublocationById(id);
                        loadSubLocation(mLocation.getSublocations().indexOf(mSublocation));
                    }
                    adjustDevice(position.getPoint());
                }
                mFromPoint = new LocationPoint(position.getPoint(), position.getLocationId(), position.getSublocationId());
                mPositionIcon.setVisible(true);
                if (mSetupPosition) {
                    mSetupPosition = false;
                    mPositionIcon.setPosition(mFromPoint);
                } else {
                    mPositionIcon.setPositionAnimated(mFromPoint, 1.0f, AnimationType.CUBIC);
                }
            }

            @Override
            public void onPositionError(Error error) {
                mPosition = null;
                mAdjustModeButton.setSelected(false);
                mPositionIcon.setVisible(false);
                Log.e(TAG, getString(R.string.err_position_update) +  ":" + error.getMessage());
            }
        };

        mRouteListener = new RouteListener() {
            @Override
            public void onPathsUpdated(ArrayList<RoutePath> arrayList) {

                mPoints.clear();

                try {
                    mRoutePath = arrayList.get(0);

                    for (LocationPoint locationPoint : mRoutePath.getPoints()) {
                        if (locationPoint.getSublocationId() == mSublocation.getId()) {
                            mPoints.add(locationPoint.getPoint());
                        }
                    }

                    if (!mPoints.isEmpty()) {
                        LocationPolyline polyline = new LocationPolyline(new Polyline(mPoints), mLocation.getId(), mSublocation.getId());
                        mPolylineMapObject.setPolyLine(polyline);
                        mPolylineMapObject.setVisible(true);
                    } else {
                        mPolylineMapObject.setVisible(false);
                    }
                } catch (IndexOutOfBoundsException e) {
                    mRoutePath = null;
                    mPolylineMapObject.setVisible(false);
                }

                handleDeviceUpdate(mRoutePath);
            }
        };

        mLocationViewListener = new LocationViewListener() {
            @Override
            public void onLocationViewComplete() {
            }

            @Override
            public void onLocationViewWillChangeAnimated(boolean b) {
                if (!b && mAdjustMode) {
                    toggleAdjustMode();
                }
            }

            @Override
            public void onLocationViewIsChanging() {
            }

            @Override
            public void onLocationViewDidChangeAnimated(boolean b) {
            }
        };

        mNotificationListener = new NotificationListener() {
            @Override
            public void onNotificationLoaded(Notification notification) {
                String url = new String(Base64.decode(notification.getImageId(), Base64.DEFAULT), StandardCharsets.UTF_8);
                Glide.with(requireActivity()).asBitmap().load(url).listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        NotificationManagerCompat.from(requireActivity()).notify(NOTIFICATION_PUSH_ID, PushNotification.create(requireActivity(), notification, null, null));
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        NotificationManagerCompat.from(requireActivity()).notify(NOTIFICATION_PUSH_ID, PushNotification.create(requireActivity(), notification, resource, url));
                        return false;
                    }
                }).submit();
            }

            @Override
            public void onNotificationFailed(Error error) {
            }
        };
    }

    private void initBroadcastReceiver() {
        mReceiver = new StateReceiver();
        mFilter = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        mFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        mFilter.addAction(LOCATION_CHANGED);
        mFilter.addAction(VENUE_SELECTED);
    }

    private void addListeners() {
        requireActivity().registerReceiver(mReceiver, mFilter);
        NavigineSdkManager.NavigationManager.addPositionListener(mPositionListener);
        NavigineSdkManager.NotificationManager.addNotificationListener(mNotificationListener);
    }

    private void removeListeners() {
        requireActivity().unregisterReceiver(mReceiver);
        NavigineSdkManager.NavigationManager.removePositionListener(mPositionListener);
        NavigineSdkManager.RouteManager.removeRouteListener(mRouteListener);
        NavigineSdkManager.NotificationManager.removeNotificationListener(mNotificationListener);
    }

    private void showCircularProgress() {
        mCircularProgress.setVisibility(VISIBLE);
        mHandler.postDelayed(() -> {
            mCircularProgressIndicator.show();
            window.setStatusBarColor(ContextCompat.getColor(requireActivity(), R.color.colorBackground));
        }, 200);
    }

    private void hideCircularProgress() {
        mCircularProgressIndicator.hide();
        mHandler.postDelayed(() -> {
            mCircularProgress.setVisibility(GONE);
            window.setStatusBarColor(mNavigationLayout.getVisibility() == VISIBLE ? ContextCompat.getColor(requireActivity(), R.color.colorOnBackground) : ContextCompat.getColor(requireActivity(), R.color.colorBackground));
        }, 700);
    }

    private void setupRoutePin() {
        mTargetPointIcon = mLocationView.getLocationViewController().addIconMapObject();
        mTargetPointIcon.setSize(36, 108);
        mTargetPointIcon.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_to_point_png));
        mTargetPointIcon.setStyle("{ order: 100, collide: false}");
    }


    private void setRoutePin(Point point) {
        mPinPoint = new LocationPoint(point, mLocation.getId(), mSublocation.getId());
        mTargetPointIcon.setPosition(mPinPoint);
        mTargetPointIcon.setVisible(true);
    }

    private void setActiveMakeRouteButton(boolean isSelectMapPoint, boolean isGiveChoose) {
        mSelectMapPoint = isSelectMapPoint;
        if (isGiveChoose) {
            mFromPoint = null;
            mFromVenue = null;
        }
        mFromCurrentText.setText(mSelectMapPoint ? "From: Select Point on Map" : "From: Current Location");
        mFromCurrentText.setTextColor(mSelectMapPoint ? getResources().getColor(R.color.colorError) : getResources().getColor(R.color.colorPrimary));
        mFromImageView.setImageResource(mSelectMapPoint ? R.drawable.ic_to_point : R.drawable.ic_current_point);
        if (mSelectMapPoint)
            mFromImageView.setColorFilter(getResources().getColor(R.color.colorError));
        else
            mFromImageView.clearColorFilter();
    }

    private void hideAndShowBottomSheets(@Nullable BottomSheetBehavior hideFirst, @Nullable BottomSheetBehavior show, int showState) {
        if(hideFirst != null) hideFirst.setState(BottomSheetBehavior.STATE_HIDDEN);
        if(show != null) show.setState(showState);
        if (show != null && show == mMakeRouteBehavior) {
            setActiveMakeRouteButton(false, false);
            if (mAdjustMode) {
                toggleAdjustMode();
            }
        }
    }


    public void onZoomIn() {
        float currentZoomFactor = mLocationView.getLocationViewController().getZoomFactor();
        mLocationView.getLocationViewController().setZoomFactor(currentZoomFactor * 2.f);
    }

    public void onZoomOut() {
        float currentZoomFactor = mLocationView.getLocationViewController().getZoomFactor();
        mLocationView.getLocationViewController().setZoomFactor(currentZoomFactor / 2.f);
    }

    public void onMakeRoute() {

        if (mSelectMapPoint) {
            mTargetPoint = mPinPoint;
            mTargetVenue = mToVenue;
            mPinPoint = null;
            mPinVenue = null;
            mToVenue = null;
            return;
        }

        if (mPinPoint != null) {
            mTargetPoint = mPinPoint;
            mTargetVenue = null;
            mPinPoint = null;
            mPinVenue = null;
            mToVenue = null;

            Log.d(TAG, "Set target point");

            NavigineSdkManager.RouteManager.setTarget(mTargetPoint);

        } else if (mToVenue != null) {
            mTargetVenue = mToVenue;
            mTargetPoint = null;
            mPinVenue = null;
            mToVenue = null;
            mPinPoint = null;

            Log.d(TAG, "Set venue target " + mTargetVenue.getId());

            NavigineSdkManager.RouteManager.setTarget(new LocationPoint(mTargetVenue.getPoint(), mLocation.getId(), mSublocation.getId()));
        }

        NavigineSdkManager.RouteManager.addRouteListener(mRouteListener);
        if (mVenueBottomSheet.isAdded()) mVenueBottomSheet.dismiss();
        hideAndShowBottomSheets(null, mMakeRouteBehavior, BottomSheetBehavior.STATE_HIDDEN);
    }

    public void onCancelRoute() {
        mTargetPoint = null;
        mTargetVenue = null;
        mPinPoint    = null;
        mPinVenue    = null;
        mFromPoint   = null;
        mFromVenue   = null;

        mLastActiveRoutePath = null;

        NavigineSdkManager.RouteManager.removeRouteListener(mRouteListener);

        mPolylineMapObject.setVisible(false);
        mTargetPointIcon.  setVisible(false);

        NavigineSdkManager.RouteManager.cancelTarget();
        NavigineSdkManager.RouteManager.clearTargets();

    }

    @SuppressLint("UseRequireInsteadOfGet")
    private void showVenueBottomSheet() {
        String titleText = mPinVenue.getName();
        if (titleText.length() > 25)
            titleText = titleText.substring(0, 24) + "…";

        String categoryText = mPinVenue.getName();
        if (categoryText.length() > 30)
            categoryText = categoryText.substring(0, 28) + "…";

        String bm = new String(Base64.decode(mPinVenue.getImageId(), Base64.DEFAULT), StandardCharsets.UTF_8);

        mVenueBottomSheet.setSheetTitle(titleText);
        mVenueBottomSheet.setDescription(mPinVenue.getDescript());
        mVenueBottomSheet.setImageRef(bm);
        mVenueBottomSheet.setRouteButtonVisibility(mFromPoint == null ? GONE : VISIBLE);
        mVenueBottomSheet.setRouteButtonClick(v -> {
                mPinPoint = null;
                mToVenue = mPinVenue;
                String title = mPinVenue.getName();
                if (title.length() > 20)
                    title = title.substring(0, 18) + "";
                mToText.setText("To:       Venue " + title);
                if (mVenueBottomSheet.isAdded()) mVenueBottomSheet.dismiss();
                hideAndShowBottomSheets(null, mMakeRouteBehavior, BottomSheetBehavior.STATE_EXPANDED);
            });

        mVenueBottomSheet.show(getParentFragmentManager(), null);
        hideAndShowBottomSheets(mMakeRouteBehavior, null, BottomSheetBehavior.STATE_COLLAPSED);
    }

    public void toggleAdjustMode() {
        if (mPosition == null) {
            Snackbar.make(mAdjustModeButton, R.string.err_position_define, 500)
                    .setBackgroundTint(ContextCompat.getColor(requireActivity(), R.color.colorError))
                    .setTextColor(Color.WHITE)
                    .show();
            return;
        }
        mAdjustMode = !mAdjustMode;
        mAdjustModeButton.setSelected(mAdjustMode);
    }

    public void setErrorMessage(String message) {
        mErrorMessageLabel.setText(message);
        mErrorMessageLabel.setVisibility(VISIBLE);
        mErrorMessageTime = System.currentTimeMillis();
    }

    public void cancelErrorMessage() {
        mErrorMessageTime = 0;
        mErrorMessageLabel.setVisibility(GONE);
    }

    private void loadMap() {

        if (loadSubLocation(0)) {
            mNoLocationLayout.setVisibility(GONE);
            mNavigationLayout.setVisibility(VISIBLE);
        } else {
            mNavigationLayout.setVisibility(GONE);
            mNoLocationLayout.setVisibility(VISIBLE);
        }

        hideCircularProgress();

        locationChanged = false;
        locationLoaded  = false;
    }

    private boolean loadSubLocation(int index) {

        if (mLocation == null || mLocation.getSublocations().size() == 0 || index < 0 || index >= mLocation.getSublocations().size())
            return false;

        if (mSublocationsIds.contains(mSublocationId)) {
            mSublocation = mLocation.getSublocationById(mSublocationId);
            index = mLocation.getSublocations().indexOf(mSublocation);
            mSublocationId = -1;
        } else {
            mSublocation = mLocation.getSublocations().get(index);
        }

        Log.d(TAG, String.format(Locale.ENGLISH, "Loading sublocation %s (%.2f x %.2f)", mSublocation.getName(), mSublocation.getWidth(), mSublocation.getHeight()));

        if (mSublocation.getWidth() < 1.0f || mSublocation.getHeight() < 1.0f) {
            String text = String.format(Locale.ENGLISH, "Loading sublocation failed: invalid size: %.2f x %.2f", mSublocation.getWidth(), mSublocation.getHeight());
            Log.e(TAG, text);
            showPopupMessage(text);
            return false;
        }

        mLocationView.post(() -> {
            mLocationView.getLocationViewController().setSublocationId(mSublocation.getId());
            float pixelWidth = mLocationView.getWidth() / getResources().getDisplayMetrics().density;
            mLocationView.getLocationViewController().setMaxZoomFactor((pixelWidth * 16.f) / mSublocation.getWidth());
            mLocationView.getLocationViewController().setMinZoomFactor((pixelWidth / 16.f) / mSublocation.getWidth());
            mLocationView.getLocationViewController().setZoomFactor(pixelWidth / mSublocation.getWidth());
        });

        mSublocationsListView.setItemChecked(index, true);

        return true;
    }

    private void showPopupMessage(String text) {
       snackBar = Snackbar.make(requireView(), text, Snackbar.LENGTH_INDEFINITE)
                .setBackgroundTint(ContextCompat.getColor(requireActivity(), R.color.colorError))
                .setTextColor(Color.WHITE)
                .addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {

                    @Override
                    public void onShown(Snackbar transientBottomBar) {
                        ObjectAnimator.ofFloat(mChoseMapButton, "translationY", -100f).start();
                    }

                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        ObjectAnimator.ofFloat(mChoseMapButton, "translationY", 0).start();
                    }
                });
       snackBar.show();
        ObjectAnimator.ofFloat(mChoseMapButton, "translationY", -100f).start();
    }

    private void handleClick(float x, float y) {
        if (mTargetPoint != null || mTargetVenue != null || mPinPoint != null || mPinVenue != null || mMakeRouteBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            cancelPin();
            return;
        }
        mLocationView.getLocationViewController().pickMapFeaturetAt(x, y);
    }

    private void handleLongClick(float x, float y) {
        makePin(mLocationView.getLocationViewController().screenPositionToMeters(new PointF(x, y)));
    }

    private void makePin(Point P) {

        if (P.getX() < 0.0f || P.getX() > mSublocation.getWidth() || P.getY() < 0.0f || P.getY() > mSublocation.getHeight())
            return;

        if (hasTarget())
            return;

        mPinPoint = new LocationPoint(P, mLocation.getId(), mSublocation.getId());
        mPinVenue = null;
        mToText.setText("To:       Point (" + String.format("%.1f", mPinPoint.getPoint().getX()) + ", " + String.format("%.1f", mPinPoint.getPoint().getY()) + ")");
        if (mVenueBottomSheet.isAdded()) mVenueBottomSheet.dismiss();
        hideAndShowBottomSheets(null, mMakeRouteBehavior, BottomSheetBehavior.STATE_EXPANDED);
    }

    private void cancelPin() {

        if (hasTarget())
            return;

        mPinPoint = null;
        mPinVenue = null;
        mToVenue = null;
        mFromVenue = null;
        mFromPoint = null;

        mTargetPointIcon.setVisible(false);
        mPolylineMapObject.setVisible(false);
    }


    private RoutePath makeRoutePath() {

        RoutePath path = null;
        if (mFromVenue != null) {
            if (mTargetVenue != null)
                path = NavigineSdkManager.RouteManager.makeRoute(
                        new LocationPoint(mFromVenue.getPoint(), mFromVenue.getLocationId(), mFromVenue.getSublocationId()),
                        new LocationPoint(mTargetPoint.getPoint(), mTargetVenue.getLocationId(), mTargetVenue.getSublocationId()));
            else if (mTargetPoint != null)
                path = NavigineSdkManager.RouteManager.makeRoute(
                        new LocationPoint(mFromVenue.getPoint(), mFromVenue.getLocationId(), mFromVenue.getSublocationId()),
                        mTargetPoint);
        } else {
            if (mTargetVenue != null)
                path = NavigineSdkManager.RouteManager.makeRoute(mFromPoint,
                        new LocationPoint(mTargetVenue.getPoint(), mTargetVenue.getLocationId(), mTargetVenue.getSublocationId()));
            else if (mTargetPoint != null)
                path = NavigineSdkManager.RouteManager.makeRoute(mFromPoint, mTargetPoint);
        }
        return path;
    }

    private void adjustDevice(Point point) {
        float zoom = mLocationView.getLocationViewController().getMaxZoomFactor() / ADJUST_ZOOM_FACTOR;
        Camera camera = new Camera(point, zoom);
        mLocationView.getLocationViewController().flyToCamera(camera, 1000, null);
    }

    private void handleDeviceUpdate(RoutePath routePath) {

        if (mErrorMessageTime > 0 && mErrorMessageTime + ERROR_MESSAGE_TIMEOUT < System.currentTimeMillis())
            cancelErrorMessage();

        if (mLocation == null) return;

        String infoText = "No route path!";
        String timeText = "";
        if (mCancelRouteBehaviour.getState() == BottomSheetBehavior.STATE_HIDDEN)
            mCancelRouteBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);

        mLastActiveRoutePath = routePath;
        if (mLastActiveRoutePath != null) {

            if (mLastActiveRoutePath.getLength() != 0 && mFromPoint == null && mFromVenue == null) {
                int i = 0;
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
            } else if (mLastActiveRoutePath.getEvents().size() >= 1) {
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
        }
        infoText = String.format("(%s)", infoText);

        mCancelRouteDistance.setText(infoText);
        mCancelRouteTime.    setText(timeText);

        mRouteEventAdapter.submit(mCancelRouteList, mSublocation);
    }

    private boolean hasTarget() {
        return mTargetPoint != null || mTargetVenue != null;
    }

    private void addRouteEventsToList(RoutePath path) {
        if (path != null) {
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
        } else
            mCancelRouteList.clear();
    }



    private void checkGpsState() {
        if (mLocationManager != null) {
            isGpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGpsEnabled || !isNetworkEnabled) {
                setErrorMessage("Your GPS is not enabled, " +
                        "please turn it on to get the Navigation results!");
            } else
                cancelErrorMessage();
        }
    }

    private void checkBluetoothState() {
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                setErrorMessage("Your Bluetooth is not enabled, " +
                        "please turn it on to get the Navigation results!");
            } else
                cancelErrorMessage();
        }
    }

    private void zoomToVenue(int sublocationIndex, float[] venueCoords) {
        if (mAdjustMode) toggleAdjustMode();
        loadSubLocation(sublocationIndex);
        mLocationView.post(() -> adjustDevice(new Point(venueCoords[0], venueCoords[1])));
    }

    private class StateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case LocationManager.PROVIDERS_CHANGED_ACTION:
                    checkGpsState();
                    break;
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    checkBluetoothState();
                    break;
                case LOCATION_CHANGED:

                    locationChanged = true;

                    if (intent.getExtras() != null) {
                        mSublocationId = intent.getIntExtra(KEY_ID_SUBLOCATION, -1);
                        mNavigationView.setSelectedItemId(R.id.navigation_navigation);
                        break;
                    }

                    if (snackBar != null)
                        snackBar.dismiss();

                    if (mAdjustMode) {
                        toggleAdjustMode();
                    }

                    mCancelRouteBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
                    mSearchField.setQuery("", false);
                    mSearchBtnClose.performClick();
                    break;
                case VENUE_SELECTED:
                    int sublocationId = intent.getIntExtra(KEY_VENUE_SUBLOCATION, 0);
                    float[] point     = intent.getFloatArrayExtra(KEY_VENUE_POINT);
                    int sublocationIndex = mLocation.getSublocations().indexOf(mLocation.getSublocationById(sublocationId));
                    mSearchField.clearFocus();
                    mSearchLayout.setBackgroundColor(Color.TRANSPARENT);
                    zoomToVenue(sublocationIndex, point);
                    break;
            }
        }
    }
}

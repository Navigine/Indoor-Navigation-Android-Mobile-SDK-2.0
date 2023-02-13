package com.navigine.navigine.demo.ui.fragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.navigine.navigine.demo.utils.Constants.CIRCULAR_PROGRESS_DELAY_HIDE;
import static com.navigine.navigine.demo.utils.Constants.CIRCULAR_PROGRESS_DELAY_SHOW;
import static com.navigine.navigine.demo.utils.Constants.DL_QUERY_LOCATION_ID;
import static com.navigine.navigine.demo.utils.Constants.DL_QUERY_SUBLOCATION_ID;
import static com.navigine.navigine.demo.utils.Constants.DL_QUERY_VENUE_ID;
import static com.navigine.navigine.demo.utils.Constants.KEY_VENUE_CATEGORY;
import static com.navigine.navigine.demo.utils.Constants.KEY_VENUE_POINT;
import static com.navigine.navigine.demo.utils.Constants.KEY_VENUE_SUBLOCATION;
import static com.navigine.navigine.demo.utils.Constants.LOCATION_CHANGED;
import static com.navigine.navigine.demo.utils.Constants.TAG;
import static com.navigine.navigine.demo.utils.Constants.VENUE_FILTER_OFF;
import static com.navigine.navigine.demo.utils.Constants.VENUE_FILTER_ON;
import static com.navigine.navigine.demo.utils.Constants.VENUE_SELECTED;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.divider.MaterialDividerItemDecoration;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textview.MaterialTextView;
import com.navigine.idl.java.AnimationType;
import com.navigine.idl.java.Camera;
import com.navigine.idl.java.Category;
import com.navigine.idl.java.IconMapObject;
import com.navigine.idl.java.Location;
import com.navigine.idl.java.LocationPoint;
import com.navigine.idl.java.LocationPolyline;
import com.navigine.idl.java.LocationViewListener;
import com.navigine.idl.java.MapObjectPickResult;
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
import com.navigine.navigine.demo.adapters.sublocations.SublocationsAdapter;
import com.navigine.navigine.demo.adapters.venues.VenueListAdapter;
import com.navigine.navigine.demo.adapters.venues.VenuesIconsListAdapter;
import com.navigine.navigine.demo.models.VenueIconObj;
import com.navigine.navigine.demo.ui.custom.lists.BottomSheetListView;
import com.navigine.navigine.demo.ui.custom.lists.ListViewLimit;
import com.navigine.navigine.demo.ui.dialogs.sheets.BottomSheetVenue;
import com.navigine.navigine.demo.utils.ColorUtils;
import com.navigine.navigine.demo.utils.DimensionUtils;
import com.navigine.navigine.demo.utils.KeyboardController;
import com.navigine.navigine.demo.utils.NavigineSdkManager;
import com.navigine.navigine.demo.utils.VenueIconsListProvider;
import com.navigine.navigine.demo.viewmodel.SharedViewModel;
import com.navigine.view.LocationView;
import com.navigine.view.TouchInput;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NavigationFragment extends BaseFragment{

    private static final String KEY_VENUE       = "name";
    public static final int LOCATION_LOAD_DELAY = 5_000;

    private SharedViewModel viewModel = null;

    private Window                        window                     = null;
    private SearchView                    mSearchField               = null;
    private ConstraintLayout              mSearchPanel               = null;
    private ConstraintLayout              mNavigationLayout          = null;
    private ConstraintLayout              mNoLocationLayout          = null;
    private ConstraintLayout              mMakeRouteSheet            = null;
    private ConstraintLayout              mCancelRouteSheet          = null;
    private LinearLayout                  mSearchLayout              = null;
    private FrameLayout                   mTransparentBackground     = null;
    private FrameLayout                   mVenueListLayout           = null;
    private FrameLayout                   mVenueIconsLayout          = null;
    private FrameLayout                   mArrowUpLayout             = null;
    private FrameLayout                   mArrowDownLayout           = null;
    private FrameLayout                   mZoomInLayout              = null;
    private FrameLayout                   mZoomOutLayout             = null;
    private FrameLayout                   mCircularProgress          = null;
    private FrameLayout                   mAdjustModeButton          = null;
    private MaterialTextView              mFromCurrentText           = null;
    private MaterialTextView              mToText                    = null;
    private MaterialTextView              mCancelRouteDistance       = null;
    private MaterialTextView              mCancelRouteTime           = null;
    private MaterialTextView              mWarningMessage            = null;
    private MaterialTextView              mDelayMessage              = null;
    private CircularProgressIndicator     mCircularProgressIndicator = null;
    private LocationView                  mLocationView              = null;
    private ImageView                     mFromImageView             = null;
    private ImageView                     mSearchBtnClear            = null;
    private MaterialButton                mSearchBtn                 = null;
    private MaterialButton                mSearchBtnClose            = null;
    private MaterialButton                mChoseMapButton            = null;
    private MaterialButton                mStartRouteButton          = null;
    private MaterialButton                mRouteSheetCancelButton    = null;
    private BottomSheetBehavior           mMakeRouteBehavior         = null;
    private BottomSheetBehavior           mCancelRouteBehaviour      = null;
    private BottomSheetListView           mCancelRouteListView       = null;
    private ListViewLimit                 mSublocationsListView      = null;
    private RecyclerView                  mVenueIconsListView        = null;
    private RecyclerView                  mVenueListView             = null;
    private BottomSheetVenue              mVenueBottomSheet          = null;
    private IconMapObject                 mPositionIcon              = null;
    private MaterialDividerItemDecoration mItemDivider               = null;
    private HorizontalScrollView          mChipsScroll               = null;
    private ChipGroup                     mChipGroup                 = null;

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

    private List<RouteEvent>        mCancelRouteList          = new ArrayList<>();
    private ArrayList<Point>        mPoints                   = new ArrayList<>();
    private List<Venue>             mVenuesList               = new ArrayList<>();
    private List<VenueIconObj>      mFilteredVenueIconsList   = new ArrayList<>();
    private Map<Chip, VenueIconObj> mChipsMap                 = new HashMap<>();

    private RouteEventAdapter                mRouteEventAdapter      = null;
    private VenueListAdapter                 mVenueListAdapter       = null;
    private VenuesIconsListAdapter           mVenuesIconsListAdapter = null;
    private SublocationsAdapter<Sublocation> mSublocationsAdapter    = null;

    private IconMapObject     mPinIconTarget       = null;
    private IconMapObject     mPinIconFrom         = null;
    private PolylineMapObject mPolylineMapObject   = null;
    private RoutePath         mRoutePath           = null;
    private RoutePath         mLastActiveRoutePath = null;

    private static Handler  mHandler = new Handler();

    private RouteListener        mRouteListener             = null;
    private PositionListener     mPositionListener          = null;
    private LocationViewListener mLocationViewListener      = null;

    private StateReceiver mReceiver = null;
    private IntentFilter  mFilter   = null;

    private boolean mAdjustMode        = false;
    private boolean mSelectMapPoint    = false;
    private boolean mLocationChanged   = false;
    private boolean mLocationLoaded    = false;
    private boolean mRouting           = false;
    private boolean mSetupPosition     = true;

    private int mSublocationId = -1;
    private int mVenueId       = -1;

    private float mZoomCameraDefault = 0f;

    private final Runnable mDelayMessageCallback = () -> {
        if (mCircularProgressIndicator.isShown()) {
            mDelayMessage.setVisibility(VISIBLE);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViewModels();
        initListeners();
        initBroadcastReceiver();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navigation, container, false);
        handleArgs();
        initViews(view);
        setViewsParams();
        initLocationViewObjects();
        initAdapters();
        setAdapters();
        setViewsListeners();
        setObservers();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        onHiddenChanged(!isVisible());
        addListeners();
    }

    @Override
    public void onPause() {
        super.onPause();
        mLocationView.onPause();
        removeListeners();
    }

    @Override
    protected void updateStatusBar() {
        window.setStatusBarColor(mNavigationLayout.getVisibility() == VISIBLE ?
                ContextCompat.getColor(requireActivity(), R.color.colorOnBackground) :
                ContextCompat.getColor(requireActivity(), R.color.colorBackground));
    }

    @Override
    protected void updateUiState() {
        if (mLocationChanged) showLoadProgress();
        if (mLocationLoaded)  loadMap();
        if (mLocationView != null) mLocationView.onResume();
    }

    @Override
    protected void updateWarningMessageState() {
        if (!hasLocationPermission())
        {
            showWarning(getString(R.string.err_permission_location));
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        {
            if (!hasBluetoothPermission())
            {
                showWarning(getString(R.string.err_permission_bluetooth));
                return;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        {
            if (!hasBackgroundLocationPermission())
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                    showWarning(getString(R.string.err_permission_location_background_api_30, requireActivity().getPackageManager().getBackgroundPermissionOptionLabel()));
                else
                    showWarning(getString(R.string.err_permission_location_background));
                return;
            }
        }

        if (!isGpsEnabled() && !isBluetoothEnabled())
        {
            showWarning(getString(R.string.err_navigation_state_gps_bluetooth));
            return;
        }

        if (!isBluetoothEnabled())
        {
            showWarning(getString(R.string.err_navigation_state_bluetooth));
            return;
        }

        if (!isGpsEnabled())
        {
            showWarning(getString(R.string.err_navigation_state_gps));
            return;
        }

        hideWarning();
    }

    private void showWarning(String message) {
        mWarningMessage.setText(message);
        mWarningMessage.setVisibility(VISIBLE);
    }

    private void hideWarning() {
        mWarningMessage.setVisibility(GONE);
    }

    private void showWarningTemp(String message, long delayMillis) {
        if (mWarningMessage.getVisibility() == VISIBLE) {
            mWarningMessage.setText(message);
            mWarningMessage.postDelayed(this::updateWarningMessageState, delayMillis);
        } else {
            showWarning(message);
            mWarningMessage.postDelayed(this::hideWarning, delayMillis);
        }
    }

    private void handleArgs() {
        Bundle args = getArguments();
        mLocationChanged = args != null;
        if (mLocationChanged) {
            try {
                String locationId    = args.getString(DL_QUERY_LOCATION_ID);
                String sublocationId = args.getString(DL_QUERY_SUBLOCATION_ID);
                String venueId       = args.getString(DL_QUERY_VENUE_ID);

                if (locationId != null) {
                    if (sublocationId != null) mSublocationId = Integer.parseInt(sublocationId);
                    if (venueId != null)       mVenueId = Integer.parseInt(venueId);

                    NavigineSdkManager.LocationManager.setLocationId(Integer.parseInt(locationId));
                }

            } catch (UnsupportedOperationException | NullPointerException e) {
                Log.e(TAG, getString(R.string.err_deep_link_parse));
            }
        }
        setArguments(null);
    }

    private void initViews(View view) {
        window                     = requireActivity().getWindow();
        mTransparentBackground     = view.findViewById(R.id.navigation__search_transparent_bg);
        mSearchLayout              = view.findViewById(R.id.navigation_search);
        mVenueListLayout           = view.findViewById(R.id.navigation__venue_listview);
        mVenueIconsLayout          = view.findViewById(R.id.navigation__venue_icons);
        mNoLocationLayout          = view.findViewById(R.id.navigation__no_location_layout);
        mNavigationLayout          = view.findViewById(R.id.navigation__navigation_layout);
        mVenueBottomSheet          = new BottomSheetVenue();
        mMakeRouteSheet            = view.findViewById(R.id.navigation__make_route_sheet);
        mCancelRouteSheet          = view.findViewById(R.id.navigation__cancel_route_sheet);
        mCancelRouteListView       = mCancelRouteSheet.findViewById(R.id.cancel_route_sheet__list_view);
        mMakeRouteBehavior         = BottomSheetBehavior.from(mMakeRouteSheet);
        mCancelRouteBehaviour      = BottomSheetBehavior.from(mCancelRouteSheet);
        mFromImageView             = mMakeRouteSheet.findViewById(R.id.make_route__from_current_image);
        mSearchBtn                 = view.findViewById(R.id.navigation__search_btn);
        mSearchBtnClose            = view.findViewById(R.id.navigation__search_btn_close);
        mRouteSheetCancelButton    = mCancelRouteSheet.findViewById(R.id.cancel_route_sheet__cancel_button);
        mChoseMapButton            = view.findViewById(R.id.no_location__button_choose_map);
        mStartRouteButton          = mMakeRouteSheet.findViewById(R.id.start_route__button);
        mAdjustModeButton          = view.findViewById(R.id.navigation__adjust_mode_button);
        mFromCurrentText           = mMakeRouteSheet.findViewById(R.id.make_route__from_current_title);
        mToText                    = mMakeRouteSheet.findViewById(R.id.make_route__to_text);
        mCancelRouteDistance       = mCancelRouteSheet.findViewById(R.id.cancel_route_sheet__distance_tv);
        mCancelRouteTime           = mCancelRouteSheet.findViewById(R.id.cancel_route_sheet__time_tv);
        mWarningMessage            = view.findViewById(R.id.navigation__warning);
        mCircularProgress          = view.findViewById(R.id.navigation__progress_circular);
        mCircularProgressIndicator = view.findViewById(R.id.navigation__progress_circular_indicator);
        mDelayMessage              = view.findViewById(R.id.navigation__msg_delay);
        mLocationView              = view.findViewById(R.id.navigation__location_view);
        mVenueIconsListView        = view.findViewById(R.id.recycler_list_venue_icons);
        mVenueListView             = view.findViewById(R.id.recycler_list_venues);
        mItemDivider               = new MaterialDividerItemDecoration(requireActivity(), MaterialDividerItemDecoration.VERTICAL);
        mSearchPanel               = view.findViewById(R.id.navigation__search_panel);
        mSearchField               = view.findViewById(R.id.navigation__search_field);
        mSearchBtnClear            = mSearchField.findViewById(R.id.search_close_btn);
        mZoomInLayout              = view.findViewById(R.id.panel_zoom__zoom_in);
        mZoomOutLayout             = view.findViewById(R.id.panel_zoom__zoom_out);
        mArrowUpLayout             = view.findViewById(R.id.panel_sublocations__arrow_up);
        mArrowDownLayout           = view.findViewById(R.id.panel_sublocations__arrow_down);
        mSublocationsListView      = view.findViewById(R.id.panel_sublocations__listview);
        mChipsScroll               = view.findViewById(R.id.navigation__search_chips_scroll);
        mChipGroup                 = view.findViewById(R.id.navigation__search_chips_group);

    }

    private void setViewsParams() {
        if (mSearchBtnClear != null) mSearchBtnClear.setEnabled(false);
        mNavigationLayout.    getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        mSearchLayout.        getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        mChipsScroll.         getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        mSearchPanel.         getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        mVenueListLayout.     getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        mMakeRouteBehavior.   setState(BottomSheetBehavior.STATE_HIDDEN);
        mCancelRouteBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
        mLocationView.        setBackgroundColor(Color.argb(255, 235, 235, 235));
        mLocationView.        getLocationViewController().setStickToBorder(true);
        mItemDivider.         setDividerColor(ContextCompat.getColor(requireActivity(), R.color.colorBackground));
        mItemDivider.         setLastItemDecorated(false);
        mVenueListView.       addItemDecoration(mItemDivider);

        mLocationView.getLocationViewController().setPickRadius(10);
    }


    private void setViewsListeners() {

        mTransparentBackground.setOnClickListener(v -> onHandleCancelSearch());

        mSearchField.setOnQueryTextFocusChangeListener(this::onSearchBoxFocusChange);

        mSearchField.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                onHandleSearchQueryChange(newText);
                return true;
            }
        });

        mSearchBtnClose.setOnClickListener(v -> onHandleCancelSearch());

        mSublocationsListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) { }

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

        mSublocationsListView.setOnItemClickListener((parent, view, position, id) -> loadSubLocation(position));

        mArrowUpLayout.setOnClickListener(v -> {
            int index = mSublocationsListView.getFirstVisiblePosition();
            mSublocationsListView.smoothScrollToPosition(index -1);
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

        mChoseMapButton.setOnClickListener(v -> onMapChoose());

        mMakeRouteSheet.setOnTouchListener((view, motionEvent) -> true);

        mStartRouteButton.setOnClickListener(v ->
        {
            if (mPinPoint == null && mToVenue == null) {
                Toast.makeText(requireActivity(), R.string.navigation_destination_select, Toast.LENGTH_SHORT).show();
                return;
            }

            if (mFromVenue == null && mFromPoint == null && mSelectMapPoint) return;

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
                if (i == BottomSheetBehavior.STATE_HIDDEN) onCancelRoute();
            }

            @Override
            public void onSlide(@NonNull View view, float v) { }
        });

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
            if (hasTarget() || mPosition == null) return;
            Point p = mLocationView.getLocationViewController().screenPositionToMeters(new PointF(x, y));
            NavigineSdkManager.RouteManager.clearTargets();
            setRoutePin(p);
            resetPinVenue();
            updateDestinationText("To:       Point (" + String.format("%.1f", mPinPoint.getPoint().getX()) + ", " + String.format("%.1f", mPinPoint.getPoint().getY()) + ")");
            updateRouteSheetInfo();
        });

        mLocationView.getLocationViewController().setLocationViewListener(mLocationViewListener);
    }

    private void onHandleSearchQueryChange(String query) {
        if (query.isEmpty()) {
            hideSearchButton();
            showSearchCLoseBtn();
            hideVenueListLayout();
            showVenueIconsLayout();
            populateVenueIconsLayout();
        } else {
            hideSearchCLoseBtn();
            showSearchButton();
            hideVenueIconsLayout();
            showVenueListLayout();
        }
        filterVenueListByQuery(query);
    }

    private void filterVenueListByQuery(String query) {
        mVenueListAdapter.filter(query);
    }

    private void showSearchButton() {
        mSearchBtn.setVisibility(VISIBLE);
    }

    private void hideSearchButton() {
        mSearchBtn.setVisibility(GONE);
    }

    private void showVenueIconsLayout() {
        mVenueIconsLayout.setVisibility(VISIBLE);
    }

    private void hideVenueIconsLayout() {
        mVenueIconsLayout.setVisibility(GONE);
    }

    private void showVenueListLayout() {
        mVenueListLayout.setVisibility(VISIBLE);
    }

    private void hideVenueListLayout() {
        mVenueListLayout.setVisibility(GONE);
    }

    private void hideSearchCLoseBtn() {
        mSearchBtnClose.setVisibility(GONE);
    }

    private void showSearchCLoseBtn() {
        mSearchBtnClose.setVisibility(VISIBLE);
    }

    private void changeSearchBoxStroke(int color) {
        ((GradientDrawable) mSearchField.getBackground()).setStroke(DimensionUtils.STROKE_WIDTH, color);
    }

    private void changeSearchLayoutBackground(int color) {
        mSearchLayout.getBackground().setTint(color);
    }

    private void showTransparentLayout() {
        mTransparentBackground.setBackground(ContextCompat.getDrawable(requireActivity(), android.R.drawable.screen_background_dark_transparent));
        mTransparentBackground.setClickable(true);
        mTransparentBackground.setFocusable(true);
    }

    private void hideTransparentLayout() {
        mTransparentBackground.setBackground(null);
        mTransparentBackground.setClickable(false);
        mTransparentBackground.setFocusable(false);
    }

    private void setObservers() {
        viewModel.mLocation.observe(getViewLifecycleOwner(), location -> {

            mLocation = location;
            mLocationLoaded = mLocation != null;

            if (mLocationLoaded) {
                mSublocationsListView.  clearChoices();
                mSublocationsAdapter.   clear();
                mVenueListAdapter.      clear();
                mVenuesList.            clear();

                mSublocationsListView.setVisibility(mLocation.getSublocations().size() <= 1 ? GONE : VISIBLE);

                for (Sublocation sublocation : mLocation.getSublocations()) {
                    mVenuesList.      addAll(sublocation.getVenues());
                }

                mVenueListAdapter.submit(mVenuesList, mLocation);
                mSublocationsAdapter.submit(mLocation.getSublocations());
                updateFilteredVenuesIconsList();

                if (isVisible()) loadMap();
            }
        });
    }

    private void initAdapters() {
        mSublocationsAdapter    = new SublocationsAdapter<>(requireActivity(), R.layout.list_item_sublocation);
        mRouteEventAdapter      = new RouteEventAdapter();
        mVenuesIconsListAdapter = new VenuesIconsListAdapter();
        mVenueListAdapter       = new VenueListAdapter();
    }

    private void setAdapters() {
        mSublocationsListView.setAdapter(mSublocationsAdapter);
        mCancelRouteListView. setAdapter(mRouteEventAdapter);
        mVenueIconsListView.  setAdapter(mVenuesIconsListAdapter);
        mVenueListView.       setAdapter(mVenueListAdapter);
    }


    private void initLocationViewObjects() {
        mPolylineMapObject = mLocationView.getLocationViewController().addPolylineMapObject();
        mPolylineMapObject.setColor(76.0f/255, 217.0f/255, 100.0f/255, 1);
        mPolylineMapObject.setWidth(3);
        mPolylineMapObject.setStyle("{style: 'points', placement_min_length_ratio: 0, placement_spacing: 8px, size: [8px, 8px], placement: 'spaced', collide: false}");

        mPositionIcon = mLocationView.getLocationViewController().addIconMapObject();
        mPositionIcon.setSize(30, 30);
        mPositionIcon.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_current_point_png));
        mPositionIcon.setStyle("{ order: 1, collide: false}");
        mPositionIcon.setVisible(false);
    }

    private void initViewModels() {
        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    private void initListeners() {

        mPositionListener = new PositionListener() {

            @Override
            public void onPositionUpdated(Position position) {
                mPosition = position;
                LocationPoint lp = mPosition.getLocationPoint();
                if (lp == null) {
                    return;
                }

                if (mAdjustMode) {
                    int id = lp.getSublocationId();
                    if (mSublocation.getId() != id) {
                        mSublocation = mLocation.getSublocationById(id);
                        loadSubLocation(mLocation.getSublocations().indexOf(mSublocation));
                    }
                    adjustDevice(lp.getPoint());
                }
                mFromPoint = lp;
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
                Log.e(TAG, getString(R.string.err_navigation_position_update) +  ":" + error.getMessage());
            }
        };

        mRouteListener = new RouteListener() {
            @Override
            public void onPathsUpdated(ArrayList<RoutePath> arrayList) {

                if (!mRouting) return;

                if (arrayList == null || arrayList.isEmpty()) {
                    cancelRouteAndHideSheet();
                    showWarningTemp(getString(R.string.err_navigation_no_route), 3000);
                    return;
                }

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
            public void onLocationViewComplete() { }

            @Override
            public void onLocationViewWillChangeAnimated(boolean b) {
                if (!b && mAdjustMode) {
                    toggleAdjustMode();
                }
            }

            @Override
            public void onLocationViewIsChanging() { }

            @Override
            public void onLocationViewDidChangeAnimated(boolean b) { }
        };

    }

    private void initBroadcastReceiver() {
        mReceiver = new StateReceiver();
        mFilter = new IntentFilter();
        mFilter.addAction(LOCATION_CHANGED);
        mFilter.addAction(VENUE_SELECTED);
        mFilter.addAction(VENUE_FILTER_ON);
        mFilter.addAction(VENUE_FILTER_OFF);
    }

    private void addListeners() {
        requireActivity().registerReceiver(mReceiver, mFilter);
        NavigineSdkManager.NavigationManager.addPositionListener(mPositionListener);
        NavigineSdkManager.RouteManager.addRouteListener(mRouteListener);
    }

    private void removeListeners() {
        requireActivity().unregisterReceiver(mReceiver);
        NavigineSdkManager.NavigationManager.removePositionListener(mPositionListener);
        NavigineSdkManager.RouteManager.removeRouteListener(mRouteListener);
    }

    private void showLoadProgress() {
        showCircularProgress();
        showMessageDelay();
    }

    private void hideLoadProgress() {
        hideCircularProgress();
        hideMessageDelay();
    }

    private void showCircularProgress() {
        mCircularProgress.setVisibility(VISIBLE);
        mHandler.postDelayed(() -> {
            mCircularProgressIndicator.show();
            window.setStatusBarColor(ContextCompat.getColor(requireActivity(), R.color.colorBackground));
        }, CIRCULAR_PROGRESS_DELAY_SHOW);
        showMessageDelay();
    }

    private void hideCircularProgress() {
        mCircularProgressIndicator.hide();
        mHandler.postDelayed(() -> {
            mCircularProgress.setVisibility(GONE);
            window.setStatusBarColor(mNavigationLayout.getVisibility() == VISIBLE ? ContextCompat.getColor(requireActivity(), R.color.colorOnBackground) : ContextCompat.getColor(requireActivity(), R.color.colorBackground));
        }, CIRCULAR_PROGRESS_DELAY_HIDE);
    }

    private void showMessageDelay() {
        mHandler.postDelayed(mDelayMessageCallback, LOCATION_LOAD_DELAY);
    }

    private void hideMessageDelay() {
        mHandler.removeCallbacks(mDelayMessageCallback);
        mDelayMessage.setVisibility(GONE);
    }

    private void setRoutePin(Point point) {
        mPinPoint = new LocationPoint(point, mLocation.getId(), mSublocation.getId());
        if (mSelectMapPoint) {
            if (mPinIconFrom == null) mPinIconFrom = mLocationView.getLocationViewController().addIconMapObject();
            setupPinIcon(mPinIconFrom, R.drawable.ic_from_point_png, mPinPoint);
        } else {
            if (mPinIconTarget == null) mPinIconTarget = mLocationView.getLocationViewController().addIconMapObject();
            setupPinIcon(mPinIconTarget, R.drawable.ic_to_point_png, mPinPoint);
        }
    }

    private void updatePinIconState(IconMapObject pinIcon, boolean visible) {
        if (pinIcon != null) pinIcon.setVisible(visible);
    }

    private void updatePinIconsState(boolean visible, IconMapObject... pinIcons) {
        for (IconMapObject pinIcon : pinIcons) if (pinIcon != null) pinIcon.setVisible(visible);
    }

    private void resetPinVenue() {
        mPinVenue = null;
    }

    private void setupPinIcon(IconMapObject pinMapObject, @DrawableRes int pinIcon, LocationPoint pinLocationPoint) {
        pinMapObject.setSize(36, 108);
        pinMapObject.setBitmap(BitmapFactory.decodeResource(getResources(), pinIcon));
        pinMapObject.setStyle("{ order: 100, collide: false}");
        pinMapObject.setPosition(pinLocationPoint);
        pinMapObject.setVisible(true);
    }

    private void updateDestinationText(String text) {
        if (mToText != null)
            mToText.setText(text);
    }

    private void setActiveMakeRouteButton(boolean isSelectMapPoint, boolean isGiveChoose) {
        mSelectMapPoint = isSelectMapPoint;
        if (isGiveChoose) {
            mFromPoint = null;
            mFromVenue = null;
        }
        if (mSelectMapPoint) {
            mFromCurrentText.setText("From: Select Point on Map");
            mFromCurrentText.setTextColor(getResources().getColor(R.color.colorError));
            mFromImageView.setImageResource(R.drawable.ic_to_point);
            updatePinIconState(mPinIconFrom, true);
        } else {
            mFromCurrentText.setText("From: Current Location");
            mFromCurrentText.setTextColor(getResources().getColor(R.color.colorPrimary));
            mFromImageView.setImageResource(R.drawable.ic_current_point);
            updatePinIconState(mPinIconFrom, false);
        }

    }

    private void hideAndShowBottomSheets(@Nullable BottomSheetBehavior hideFirst, @Nullable BottomSheetBehavior show, int showState) {
        if (hideFirst != null) hideFirst.setState(BottomSheetBehavior.STATE_HIDDEN);
        if (show != null) show.setState(showState);
        if (show != null && show == mMakeRouteBehavior) {
            setActiveMakeRouteButton(false, false);
            if (mAdjustMode) {
                toggleAdjustMode();
            }
        }
    }

    private void updateRouteSheetInfo() {
        if (mMakeRouteBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            hideAndShowBottomSheets(null, mMakeRouteBehavior, BottomSheetBehavior.STATE_EXPANDED);
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

        setRoutingFlag();
    }

    private void setRoutingFlag() {
        mRouting = true;
    }

    private void resetRoutingFlag() {
        mRouting = false;
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
        updatePinIconsState(false, mPinIconFrom, mPinIconTarget);

        NavigineSdkManager.RouteManager.cancelTarget();
        NavigineSdkManager.RouteManager.clearTargets();

        resetRoutingFlag();
    }

    private void hideRouteSheet() {
        mCancelRouteBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    private void cancelRouteAndHideSheet() {
        if (mCancelRouteBehaviour.getState() != BottomSheetBehavior.STATE_HIDDEN) hideRouteSheet();
        else onCancelRoute();
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
            updateDestinationText("To:       " + title);
            if (mVenueBottomSheet.isAdded()) mVenueBottomSheet.dismiss();
            hideAndShowBottomSheets(null, mMakeRouteBehavior, BottomSheetBehavior.STATE_EXPANDED);
        });

        mVenueBottomSheet.show(getParentFragmentManager(), null);
        hideAndShowBottomSheets(mMakeRouteBehavior, null, BottomSheetBehavior.STATE_COLLAPSED);
    }

    public void toggleAdjustMode() {
        if (mPosition == null)
            showWarningTemp(getString(R.string.err_navigation_position_define), 1500);
        else {
            mAdjustMode = !mAdjustMode;
            mAdjustModeButton.setSelected(mAdjustMode);
        }
    }

    private void resetLocationFlags() {
        mLocationChanged = false;
        mLocationLoaded  = false;
    }

    private void loadMap() {

        if (mLocation == null || mLocation.getSublocations().size() == 0) return;

        Sublocation sublocation = mLocation.getSublocationById(mSublocationId);
        int index = sublocation == null ? 0 : mLocation.getSublocations().indexOf(sublocation);

        if (loadSubLocation(index)) {
            mNoLocationLayout.setVisibility(GONE);
            mNavigationLayout.setVisibility(VISIBLE);
        } else {
            mNavigationLayout.setVisibility(GONE);
            mNoLocationLayout.setVisibility(VISIBLE);
        }

        hideLoadProgress();
        resetLocationFlags();
        onMapLoaded();
    }

    private boolean loadSubLocation(int index) {

        if (index < 0 || index >= mLocation.getSublocations().size())
            return false;

        mSublocation = mLocation.getSublocations().get(index);

        Log.d(TAG, getString(R.string.navigation_sublocation_load, mSublocation.getName(), mSublocation.getWidth(), mSublocation.getHeight()));

        if (mSublocation.getWidth() < 1.0f || mSublocation.getHeight() < 1.0f) {
            String text = getString(R.string.navigation_sublocation_load_failed, mSublocation.getWidth(), mSublocation.getHeight());
            Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
            Log.e(TAG, text);
            return false;
        }

        return mLocationView.post(() -> {
            mLocationView.getLocationViewController().setSublocationId(mSublocation.getId());
            float pixelWidth = mLocationView.getWidth() / getResources().getDisplayMetrics().density;
            mLocationView.getLocationViewController().setMaxZoomFactor((pixelWidth * 16.f) / mSublocation.getWidth());
            mLocationView.getLocationViewController().setMinZoomFactor((pixelWidth / 16.f) / mSublocation.getWidth());
            mLocationView.getLocationViewController().setZoomFactor(pixelWidth / mSublocation.getWidth());
            mLocationView.getLocationViewController().applyFilter("", getVenueLayerExp());
            setupZoomCameraDefault();
            selectSublocationListItem(index);
        });
    }

    private void selectSublocationListItem(int index) {
        mSublocationsListView.setItemChecked(index, true);
    }

    private void setupZoomCameraDefault() {
        mZoomCameraDefault = mLocationView.getLocationViewController().getCamera().getZoom();
    }

    private void handleClick(float x, float y) {
        if (mTargetPoint != null || mTargetVenue != null || mPinPoint != null || mPinVenue != null || mMakeRouteBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            cancelPin();
            return;
        }
        mLocationView.getLocationViewController().pickMapFeaturetAt(x, y);
    }

    private void cancelPin() {

        if (hasTarget())
            return;

        mPinPoint = null;
        mPinVenue = null;
        mToVenue = null;
        mFromVenue = null;
        mFromPoint = null;

        mPolylineMapObject.setVisible(false);
        updatePinIconsState(false, mPinIconFrom, mPinIconTarget);
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
        Camera camera = new Camera(point, mZoomCameraDefault * 2, 0);
        mLocationView.getLocationViewController().flyToCamera(camera, 1000, null);
    }

    private void handleDeviceUpdate(RoutePath routePath) {

        if (mLocation == null) return;

        String infoText = "";
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
                        timeText = String.format(Locale.ENGLISH, "%.0f min", time);
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

    private void pointCameraToVenue(int sublocationIndex, float[] venueCoords) {
        loadSubLocation(sublocationIndex);
        zoomToVenue(venueCoords);
    }

    private void zoomToVenue(float[] venueCoords) {
        if (mAdjustMode) toggleAdjustMode();
        mLocationView.post(() -> adjustDevice(new Point(venueCoords[0], venueCoords[1])));
    }

    private void zoomToVenue(Venue venue) {
        if (mAdjustMode) toggleAdjustMode();
        mLocationView.post(() -> adjustDevice(venue.getPoint()));
    }

    private void onMapChoose() {
        openLocationsScreen();
    }

    private void hideVenueLayouts() {
        mVenueListLayout.setVisibility(GONE);
        mVenueIconsLayout.setVisibility(GONE);
    }

    private void onCloseSearch() {
        changeSearchLayoutBackground(Color.TRANSPARENT);
        mSearchField.clearFocus();
        mSearchBtnClose.setVisibility(GONE);
        hideVenueLayouts();
    }

    private void clearSearchQuery() {
        mSearchField.setQuery("", false);
    }

    private Venue findVenueById(int venueId) {
        if (mLocation != null) {
            for (Sublocation sublocation : mLocation.getSublocations()) {
                Venue venue = sublocation.getVenueById(venueId);
                if (venue != null) return venue;
            }
        }
        return null;
    }

    private void onMapLoaded() {
        Venue venue = findVenueById(mVenueId);
        if (venue != null) zoomToVenue(venue);
        mVenueId = -1;
    }

    private void applyVenueFilter(List<VenueIconObj> venueIconObjs) {
        String filter = getVenueFilterFunc(venueIconObjs);
        String layer  = getVenueLayerExp();
        mLocationView.post(() -> mLocationView.getLocationViewController().applyFilter(filter, layer));
    }

    private void resetVenueFilter() {
        mLocationView.post(() -> mLocationView.getLocationViewController().applyFilter("", getVenueLayerExp()));
    }

    private String getVenueFilterFunc(List<VenueIconObj> venueIconObjs) {
        if (venueIconObjs.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("function () { return (");
        for (int i = 0; i < venueIconObjs.size(); i++) {
            sb.append("feature.kind == '");
            sb.append(venueIconObjs.get(i).getCategoryName());
            sb.append("'");
            if (i != venueIconObjs.size() - 1) sb.append(" || ");
            else sb.append(") }");
        }
        return sb.toString();
    }

    private String getVenueLayerExp() {
        return "base:pois:venues";
    }

    private void updateFilteredVenuesIconsList() {
        mFilteredVenueIconsList.clear();

        Map<Integer, String> mCategoriesIdsAll       = new HashMap();
        List<Integer>        mCategoriesIdsCurr      = new ArrayList<>();

        for (Category category : mLocation.getCategories()) {
            mCategoriesIdsAll.put(category.getId(), category.getName());
        }
        for (Venue venue : mVenuesList) {
            mCategoriesIdsCurr.add(venue.getCategoryId());
        }
        mCategoriesIdsAll.keySet().retainAll(mCategoriesIdsCurr);

        for (VenueIconObj venueIconObj : VenueIconsListProvider.VenueIconsList) {
            if (mCategoriesIdsAll.containsValue(venueIconObj.getCategoryName())) {
                mFilteredVenueIconsList.add(new VenueIconObj(venueIconObj.getImageDrawable(), venueIconObj.getCategoryName()));
            }
        }
    }

    private void excludeVenueFromFiltered(VenueIconObj venueIconObj) {
        mReceiver.filteredVenues.remove(venueIconObj);
    }

    private void unselectVenueIcon(VenueIconObj venueIconObj) {
        mFilteredVenueIconsList.get(mFilteredVenueIconsList.indexOf(venueIconObj)).setActivated(false);
    }

    private List<VenueIconObj> getFilteredVenuesIconsList() {
        return mFilteredVenueIconsList;
    }

    private void populateVenueIconsLayout() {
        if (mVenueIconsLayout.getVisibility() == VISIBLE)
            mVenuesIconsListAdapter.updateList(getFilteredVenuesIconsList());
    }

    private void onHandleCancelSearch() {
        onCloseSearch();
        hideTransparentLayout();
    }

    private void updateSearchViewWithChips(List<VenueIconObj> venueIconObjs) {
        removeChipsFromGroup();
        mappingChipsToVenueIcons(venueIconObjs);
    }

    private Chip createChip(String chipText, @DrawableRes int iconRes) {
        Chip chip = new Chip(requireActivity());
        chip.setText(chipText);
        chip.setChipIcon(ContextCompat.getDrawable(requireActivity(), iconRes));
        chip.setCloseIconVisible(true);
        chip.setClickable(true);
        chip.setCheckable(false);
        chip.setCloseIconTint(ContextCompat.getColorStateList(requireActivity(), R.color.colorPrimary));
        chip.setEllipsize(TextUtils.TruncateAt.END);
        chip.setOnCloseIconClickListener(v -> onCancelChip((Chip) v));
        return chip;
    }

    private void addChipToGroup(Chip chip) {
        mChipGroup.addView(chip);
    }

    private void mappingChipsToVenueIcons(List<VenueIconObj> venueIconObjs) {
        for (int i = 0; i < venueIconObjs.size(); i++) {
            VenueIconObj v = venueIconObjs.get(i);
            Chip chip = createChip(v.getCategoryName(), v.getImageDrawable());
            addChipToGroup(chip);
            mChipsMap.put(chip, v);
        }
    }

    private void removeChipsFromGroup() {
        mChipGroup.removeAllViews();
        mChipsMap.clear();
    }

    private void removeChip(Chip chip) {
        mChipGroup.removeView(chip);
        mChipsMap. remove(chip);
    }

    private VenueIconObj getMappingVenueIcon(Chip chip) {
        return mChipsMap.get(chip);
    }

    private void onCancelChip(Chip chip) {
        VenueIconObj venueIconObj = getMappingVenueIcon(chip);
        excludeVenueFromFiltered(venueIconObj);
        unselectVenueIcon(venueIconObj);
        removeChip(chip);
        populateVenueIconsLayout();
        applyVenueFilter(mReceiver.filteredVenues);
    }

    private void onSearchBoxFocusChange(View v, boolean hasFocus) {
        boolean isQueryEmpty = ((SearchView) v).getQuery().toString().isEmpty();
        if (hasFocus) {
            showTransparentLayout();
            changeSearchLayoutBackground(Color.WHITE);
            changeSearchBoxStroke(ColorUtils.COLOR_PRIMARY);
            showSearchCLoseBtn();
            if (isQueryEmpty) {
                hideVenueListLayout();
                showVenueIconsLayout();
                populateVenueIconsLayout();
            } else {
                hideVenueIconsLayout();
                showVenueListLayout();
            }
        } else {
            changeSearchBoxStroke(ColorUtils.COLOR_SECONDARY);
            KeyboardController.hideSoftKeyboard(requireActivity());
        }
    }

    private class StateReceiver extends BroadcastReceiver {

        private ArrayList<VenueIconObj> filteredVenues = null;

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case LOCATION_CHANGED:
                    mLocationChanged = true;
                    hideMessageDelay();
                    if (mAdjustMode) toggleAdjustMode();
                    cancelRouteAndHideSheet();
                    removeChipsFromGroup();
                    clearSearchQuery();
                    onCloseSearch();
                    hideTransparentLayout();
                    break;
                case VENUE_SELECTED:
                    int sublocationId = intent.getIntExtra(KEY_VENUE_SUBLOCATION, 0);
                    float[] point     = intent.getFloatArrayExtra(KEY_VENUE_POINT);
                    int sublocationIndex = mLocation.getSublocations().indexOf(mLocation.getSublocationById(sublocationId));
                    onCloseSearch();
                    hideTransparentLayout();
                    pointCameraToVenue(sublocationIndex, point);
                    break;
                case VENUE_FILTER_ON:
                case VENUE_FILTER_OFF:
                    filteredVenues = intent.getParcelableArrayListExtra(KEY_VENUE_CATEGORY);
                    if (filteredVenues != null) {
                        if (intent.getAction().equals(VENUE_FILTER_ON)) {
                            updateSearchViewWithChips(filteredVenues);
                            applyVenueFilter(filteredVenues);
                        }
                        else {
                            removeChipsFromGroup();
                            resetVenueFilter();
                        }
                    }
                    onCloseSearch();
                    hideTransparentLayout();
                    break;
            }
        }
    }
}
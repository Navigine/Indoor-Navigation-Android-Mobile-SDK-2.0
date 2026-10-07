package com.navigine.navigine.demo.ui.fragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.navigine.navigine.demo.service.NavigationService.ACTION_POSITION_ERROR;
import static com.navigine.navigine.demo.service.NavigationService.ACTION_POSITION_UPDATED;
import static com.navigine.navigine.demo.utils.Constants.CIRCULAR_PROGRESS_DELAY_HIDE;
import static com.navigine.navigine.demo.utils.Constants.CIRCULAR_PROGRESS_DELAY_SHOW;
import static com.navigine.navigine.demo.utils.Constants.DL_QUERY_LOCATION_ID;
import static com.navigine.navigine.demo.utils.Constants.DL_QUERY_SUBLOCATION_ID;
import static com.navigine.navigine.demo.utils.Constants.DL_QUERY_VENUE_ID;
import static com.navigine.navigine.demo.utils.Constants.KEY_VENUE_CATEGORY;
import static com.navigine.navigine.demo.utils.Constants.KEY_VENUE_ID;
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
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
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
import com.navigine.idl.java.Camera;
import com.navigine.idl.java.Category;
import com.navigine.idl.java.GlobalPoint;
import com.navigine.idl.java.IconMapObject;
import com.navigine.idl.java.InputListener;
import com.navigine.idl.java.Location;
import com.navigine.idl.java.LocationPoint;
import com.navigine.idl.java.LocationWindow;
import com.navigine.idl.java.MapFilterCondition;
import com.navigine.idl.java.MapObjectPickResult;
import com.navigine.idl.java.NavigineSdk;
import com.navigine.idl.java.PickListener;
import com.navigine.idl.java.Placement;
import com.navigine.idl.java.Point;
import com.navigine.idl.java.RouteAnnotation;
import com.navigine.idl.java.RouteAnnotationType;
import com.navigine.idl.java.RouteEndpoint;
import com.navigine.idl.java.RouteEvent;
import com.navigine.idl.java.RouteEventType;
import com.navigine.idl.java.RouteLayer;
import com.navigine.idl.java.RouteLayerListener;
import com.navigine.idl.java.RouteLayerStatus;
import com.navigine.idl.java.RouteListener;
import com.navigine.idl.java.RouteNode;
import com.navigine.idl.java.RoutePath;
import com.navigine.idl.java.RouteProgress;
import com.navigine.idl.java.RouteRemainingStyle;
import com.navigine.idl.java.RouteView;
import com.navigine.idl.java.Sublocation;
import com.navigine.idl.java.TurnType;
import com.navigine.idl.java.Venue;
import com.navigine.image.ImageProvider;
import com.navigine.navigine.demo.R;
import com.navigine.navigine.demo.adapters.route.RouteAnnotationAdapter;
import com.navigine.navigine.demo.adapters.venues.VenueListAdapter;
import com.navigine.navigine.demo.adapters.venues.VenuesIconsListAdapter;
import com.navigine.navigine.demo.models.VenueIconObj;
import com.navigine.navigine.demo.service.NavigationService;
import com.navigine.navigine.demo.ui.custom.lists.BottomSheetListView;
import com.navigine.navigine.demo.ui.dialogs.sheets.BottomSheetMapSettings;
import com.navigine.navigine.demo.ui.dialogs.sheets.BottomSheetRouteFinish;
import com.navigine.navigine.demo.ui.dialogs.sheets.BottomSheetVenue;
import com.navigine.navigine.demo.utils.ColorUtils;
import com.navigine.navigine.demo.utils.DimensionUtils;
import com.navigine.navigine.demo.utils.KeyboardController;
import com.navigine.navigine.demo.utils.NavigineSdkManager;
import com.navigine.navigine.demo.utils.RouteFormatter;
import com.navigine.navigine.demo.utils.VenueIconsListProvider;
import com.navigine.navigine.demo.viewmodel.SharedViewModel;
import com.navigine.view.DefaultNavigationView;
import com.navigine.view.DefaultNavigationViewConfig;
import com.navigine.view.widgets.FloorSelectorViewConfig;
import com.navigine.view.widgets.FollowMeButtonConfig;
import com.navigine.view.widgets.ZoomControlsConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NavigationFragment extends BaseFragment {

    private static final String KEY_VENUE = "name";
    public static final int LOCATION_LOAD_DELAY = 5_000;
    private static final String VENUE_FILTER_VALUE_DEFAULT = "";
    public static final String VENUE_FILTER_LAYER_DEFAULT = "venues";

    private static final String BARRIER_LAYER = "barrier";
    private static final ArrayList<String> BARRIER_3D_VALUES_ON = new ArrayList<>(Arrays.asList("line", "polygon"));
    private static final ArrayList<String> BARRIER_3D_VALUES_OFF = new ArrayList<>(Collections.singletonList("none"));

    private static final float ROUTE_WIDTH = 8f;
    private static final float ROUTE_DOT_SIZE = 8f;
    private static final float ROUTE_DOT_SPACING = 8f;

    private SharedViewModel viewModel = null;

    private Window window = null;
    private SearchView mSearchField = null;
    private ConstraintLayout mSearchPanel = null;
    private ConstraintLayout mNavigationLayout = null;
    private ConstraintLayout mNoLocationLayout = null;
    private ConstraintLayout mMakeRouteSheet = null;
    private ConstraintLayout mCancelRouteSheet = null;
    private LinearLayout mSearchLayout = null;
    private FrameLayout mTransparentBackground = null;
    private FrameLayout mVenueListLayout = null;
    private FrameLayout mVenueIconsLayout = null;
    private FrameLayout mCircularProgress = null;
    private MaterialTextView mFromCurrentText = null;
    private MaterialTextView mToText = null;
    private MaterialTextView mCancelRouteDistance = null;
    private MaterialTextView mCancelRouteTime = null;
    private MaterialTextView mWarningMessage = null;
    private MaterialTextView mDelayMessage = null;
    private CircularProgressIndicator mCircularProgressIndicator = null;
    private DefaultNavigationView mLocationView = null;
    private ImageView mFromImageView = null;
    private ImageView mSearchBtnClear = null;
    private MaterialButton mSearchBtn = null;
    private MaterialButton mSearchBtnClose = null;
    private MaterialButton mChoseMapButton = null;
    private MaterialButton mStartRouteButton = null;
    private MaterialButton mRouteSheetCancelButton = null;
    private BottomSheetBehavior mMakeRouteBehavior = null;
    private BottomSheetBehavior mCancelRouteBehaviour = null;
    private BottomSheetListView mCancelRouteListView = null;
    private RecyclerView mVenueIconsListView = null;
    private RecyclerView mVenueListView = null;
    private BottomSheetVenue mVenueBottomSheet = null;
    private MaterialDividerItemDecoration mItemDivider = null;
    private HorizontalScrollView mChipsScroll = null;
    private ChipGroup mChipGroup = null;
    private ImageButton mFabMapSettings = null;


    private LocationPoint mPinPoint = null;
    private LocationPoint mTargetPoint = null;
    private LocationPoint mFromPoint = null;

    private Venue mToVenue = null;
    private Venue mFromVenue = null;
    private Venue mTargetVenue = null;
    private Venue mPinVenue = null;

    private Location mLocation = null;
    private Sublocation mSublocation = null;

    private List<Venue> mVenuesList = new ArrayList<>();
    private List<VenueIconObj> mFilteredVenueIconsList = new ArrayList<>();
    private Map<Chip, VenueIconObj> mChipsMap = new HashMap<>();

    private RouteAnnotationAdapter mRouteEventAdapter = null;
    private VenueListAdapter mVenueListAdapter = null;
    private VenuesIconsListAdapter mVenuesIconsListAdapter = null;

    private IconMapObject mPinIconTarget = null;
    private IconMapObject mPinIconFrom = null;

    private static Handler mHandler = new Handler();


    private StateReceiver mStateReceiver = null;
    private PositionReceiver mPositionReceiver = null;

    private IntentFilter mStateReceiverFilter = null;
    private IntentFilter mPositionReceiverFilter = null;

    private RouteLayer mRouteLayer = null;
    private RouteLayerListener mRouteLayerListener = null;
    private final List<RouteAnnotation> mRouteAnnotations = new ArrayList<>();
    private boolean mRouteFinishPending = false;

    private InputListener mInputListener = null;
    private PickListener mPickListener = null;


    private boolean mSelectMapPoint = false;
    private boolean mLocationChanged = false;
    private boolean mLocationLoaded = false;
    private boolean mRouting = false;
    private boolean m3dEnabled = false;

    private boolean mOrientationPointState = false;

    private int mSublocationId = -1;
    private int mVenueId = -1;

    private float mZoomCameraDefault = 0f;

    private final Runnable mDelayMessageCallback = () -> {
        if (mCircularProgressIndicator.isShown()) {
            mDelayMessage.setVisibility(VISIBLE);
        }
    };

    private LocationPoint mPositionLocationPoint = null;

    private BottomSheetRouteFinish mBottomSheetRouteFinish = null;

    private static final int ROUTE_COLOR = Color.argb(255, 76, 217, 100);
    private static final float ADJUST_ZOOM_MULTIPLIER = 2f;
    private static final int CAMERA_FLY_DURATION_MS = 1000;


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
        initRouteLayer();
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
        if (mRouteFinishPending) {
            mRouteFinishPending = false;
            showRouteFinishSheet();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mLocationView.onStop();
        removeListeners();
    }

    @Override
    public void onDestroyView() {
        if (mRouteLayer != null) {
            mRouteLayer.removeRouteLayerListener(mRouteLayerListener);
            mRouteLayer.clear();
            mRouteLayer = null;
        }
        LocationWindow window = mLocationView.getLocationWindow();
        if (mInputListener != null) window.removeInputListener(mInputListener);
        if (mPickListener != null) window.removePickListener(mPickListener);
        mInputListener = null;
        mPickListener = null;
        super.onDestroyView();
    }

    @Override
    protected void updateStatusBar() {
        window.setStatusBarColor(mNavigationLayout.getVisibility() == VISIBLE ? ContextCompat.getColor(requireActivity(), R.color.colorOnBackground) : ContextCompat.getColor(requireActivity(), R.color.colorBackground));
    }

    @Override
    protected void updateUiState() {
        if (mLocationChanged) showLoadProgress();
        if (mLocationLoaded) loadMap();
        if (mLocationView != null) mLocationView.onStart();
    }

    @Override
    protected void updateWarningMessageState() {
        if (!hasLocationPermission()) {
            showWarning(getString(R.string.err_permission_location));
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!hasBluetoothPermission()) {
                showWarning(getString(R.string.err_permission_bluetooth));
                return;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!hasBackgroundLocationPermission()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                    showWarning(getString(R.string.err_permission_location_background_api_30, requireActivity().getPackageManager().getBackgroundPermissionOptionLabel()));
                else showWarning(getString(R.string.err_permission_location_background));
                return;
            }
        }

        if (!isGpsEnabled() && !isBluetoothEnabled()) {
            showWarning(getString(R.string.err_navigation_state_gps_bluetooth));
            return;
        }

        if (!isBluetoothEnabled()) {
            showWarning(getString(R.string.err_navigation_state_bluetooth));
            return;
        }

        if (!isGpsEnabled()) {
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
                String locationId = args.getString(DL_QUERY_LOCATION_ID);
                String sublocationId = args.getString(DL_QUERY_SUBLOCATION_ID);
                String venueId = args.getString(DL_QUERY_VENUE_ID);

                if (locationId != null) {
                    if (sublocationId != null) mSublocationId = Integer.parseInt(sublocationId);
                    if (venueId != null) mVenueId = Integer.parseInt(venueId);

                    NavigineSdkManager.LocationManager.setLocationId(Integer.parseInt(locationId));
                }

            } catch (UnsupportedOperationException | NullPointerException e) {
                Log.e(TAG, getString(R.string.err_deep_link_parse));
            }
        }
        setArguments(null);
    }

    private void initViews(View view) {
        window = requireActivity().getWindow();
        mTransparentBackground = view.findViewById(R.id.navigation__search_transparent_bg);
        mSearchLayout = view.findViewById(R.id.navigation_search);
        mVenueListLayout = view.findViewById(R.id.navigation__venue_listview);
        mVenueIconsLayout = view.findViewById(R.id.navigation__venue_icons);
        mNoLocationLayout = view.findViewById(R.id.navigation__no_location_layout);
        mNavigationLayout = view.findViewById(R.id.navigation__navigation_layout);
        mVenueBottomSheet = new BottomSheetVenue();
        mMakeRouteSheet = view.findViewById(R.id.navigation__make_route_sheet);
        mCancelRouteSheet = view.findViewById(R.id.navigation__cancel_route_sheet);
        mCancelRouteListView = mCancelRouteSheet.findViewById(R.id.cancel_route_sheet__list_view);
        mMakeRouteBehavior = BottomSheetBehavior.from(mMakeRouteSheet);
        mCancelRouteBehaviour = BottomSheetBehavior.from(mCancelRouteSheet);
        mFromImageView = mMakeRouteSheet.findViewById(R.id.make_route__from_current_image);
        mSearchBtn = view.findViewById(R.id.navigation__search_btn);
        mSearchBtnClose = view.findViewById(R.id.navigation__search_btn_close);
        mRouteSheetCancelButton = mCancelRouteSheet.findViewById(R.id.cancel_route_sheet__cancel_button);
        mChoseMapButton = view.findViewById(R.id.no_location__button_choose_map);
        mStartRouteButton = mMakeRouteSheet.findViewById(R.id.start_route__button);
        mFromCurrentText = mMakeRouteSheet.findViewById(R.id.make_route__from_current_title);
        mToText = mMakeRouteSheet.findViewById(R.id.make_route__to_text);
        mCancelRouteDistance = mCancelRouteSheet.findViewById(R.id.cancel_route_sheet__distance_tv);
        mCancelRouteTime = mCancelRouteSheet.findViewById(R.id.cancel_route_sheet__time_tv);
        mWarningMessage = view.findViewById(R.id.navigation__warning);
        mCircularProgress = view.findViewById(R.id.navigation__progress_circular);
        mCircularProgressIndicator = view.findViewById(R.id.navigation__progress_circular_indicator);
        mDelayMessage = view.findViewById(R.id.navigation__msg_delay);
        mLocationView = createCustomNavigationView();
        ((ViewGroup) view.findViewById(R.id.navigation__location_view_container)).addView(mLocationView);
        mVenueIconsListView = view.findViewById(R.id.recycler_list_venue_icons);
        mVenueListView = view.findViewById(R.id.recycler_list_venues);
        mItemDivider = new MaterialDividerItemDecoration(requireActivity(), MaterialDividerItemDecoration.VERTICAL);
        mSearchPanel = view.findViewById(R.id.navigation__search_panel);
        mSearchField = view.findViewById(R.id.navigation__search_field);
        mSearchBtnClear = mSearchField.findViewById(R.id.navigation__search_btn_close);
        mChipsScroll = view.findViewById(R.id.navigation__search_chips_scroll);
        mChipGroup = view.findViewById(R.id.navigation__search_chips_group);
        mFabMapSettings = view.findViewById(R.id.navigation__btn_map_settings);
    }

    private void setViewsParams() {
        if (mSearchBtnClear != null) mSearchBtnClear.setEnabled(false);
        mNavigationLayout.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        mSearchLayout.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        mChipsScroll.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        mSearchPanel.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        mVenueListLayout.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        mMakeRouteBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        mCancelRouteBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
        mLocationView.setBackgroundColor(Color.argb(255, 235, 235, 235));
        mLocationView.getLocationWindow().setStickToBorder(true);
        mItemDivider.setDividerColor(ContextCompat.getColor(requireActivity(), R.color.colorBackground));
        mItemDivider.setLastItemDecorated(false);
        mVenueListView.addItemDecoration(mItemDivider);

        mLocationView.getLocationWindow().setPickRadius(10);
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

        mChoseMapButton.setOnClickListener(v -> onMapChoose());

        mMakeRouteSheet.setOnTouchListener((view, motionEvent) -> true);

        mFabMapSettings.setOnClickListener(v -> showMapSettingsSheet());

        mStartRouteButton.setOnClickListener(v -> {
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
                    GlobalPoint pin = toGlobalPoint(mPinPoint);
                    if (pin != null) {
                        viewMaxY = view.getY();
                        translationY = viewMaxY - mLocationView.getLocationWindow().globalToScreenPosition(pin, false).y - 180;
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
            public void onSlide(@NonNull View view, float v) {
            }
        });

        mRouteSheetCancelButton.setOnClickListener(v -> mCancelRouteBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN));

        mPickListener = new PickListener() {
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
        };

        mInputListener = new InputListener() {
            @Override
            public void onViewTap(PointF pointF) {
                handleClick(pointF.x, pointF.y);
            }

            @Override
            public void onViewDoubleTap(PointF pointF) {
            }

            @Override
            public void onViewLongTap(PointF pointF) {
                if (hasTarget() || mPositionLocationPoint == null) return;

                GlobalPoint global = mLocationView.getLocationWindow().screenPositionToGlobal(pointF);
                if (global == null) return;
                Point p = mSublocation.globalToLocal(global).getPoint();
                setRoutePin(p);
                resetPinVenue();
                updateDestinationText("To:       Point (" + String.format("%.1f", mPinPoint.getPoint().getX()) + ", " + String.format("%.1f", mPinPoint.getPoint().getY()) + ")");
                updateRouteSheetInfo();
            }
        };

        mLocationView.getLocationWindow().addPickListener(mPickListener);
        mLocationView.getLocationWindow().addInputListener(mInputListener);
    }

    private DefaultNavigationView createCustomNavigationView() {
        FollowMeButtonConfig followMeConfig = new FollowMeButtonConfig.Builder()
                .marginRight(16)
                .marginBottom(120)
                .build();

        FloorSelectorViewConfig floorSelectorConfig = new FloorSelectorViewConfig.Builder()
                .marginLeft(16)
                .marginTop(180)
                .build();

        ZoomControlsConfig zoomControlsConfig = new ZoomControlsConfig.Builder()
                .build();

        DefaultNavigationViewConfig navigationViewConfig = new DefaultNavigationViewConfig.Builder()
                .build();

        return new DefaultNavigationView(
                requireContext(),
                null,
                navigationViewConfig,
                zoomControlsConfig,
                followMeConfig,
                floorSelectorConfig
        );
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
                mVenueListAdapter.clear();
                mVenuesList.clear();


                for (Sublocation sublocation : mLocation.getSublocations()) {
                    mVenuesList.addAll(sublocation.getVenues());
                }

                mVenueListAdapter.submit(mVenuesList, mLocation);
                updateFilteredVenuesIconsList();

                if (isVisible()) loadMap();
            }
        });
    }

    private void initAdapters() {
        mRouteEventAdapter = new RouteAnnotationAdapter();
        mVenuesIconsListAdapter = new VenuesIconsListAdapter();
        mVenueListAdapter = new VenueListAdapter();
    }

    private void setAdapters() {
        mCancelRouteListView.setAdapter(mRouteEventAdapter);
        mVenueIconsListView.setAdapter(mVenuesIconsListAdapter);
        mVenueListView.setAdapter(mVenueListAdapter);
    }

    private void initRouteLayer() {
        mRouteLayer = NavigineSdk.getInstance().getRouteLayer(mLocationView.getLocationWindow());
        mRouteLayer.setWidth(ROUTE_WIDTH);
        mRouteLayer.setRemainingStyle(RouteRemainingStyle.DOTTED);
        mRouteLayer.setRemainingColor(ROUTE_COLOR);
        mRouteLayer.setRemainingPointSize(ROUTE_DOT_SIZE, ROUTE_DOT_SIZE);
        mRouteLayer.setRemainingPlacement(Placement.SPACED);
        mRouteLayer.setRemainingPlacementSpacing(ROUTE_DOT_SPACING);
        mRouteLayer.setRemainingPlacementMinRatio(0f);
        mRouteLayer.setRemainingCollisionEnabled(false);
        mRouteLayer.setVisible(true);
        mRouteLayer.addRouteLayerListener(mRouteLayerListener);
    }

    private void initViewModels() {
        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    private void initListeners() {
        mRouteLayerListener = new RouteLayerListener() {
            @Override
            public void onRouteChanged(RouteLayerStatus status, @Nullable RouteView route) {
                if (!mRouting) return;
                switch (status) {
                    case ROUTE_UPDATED:
                        if (route != null) onRouteUpdated(route);
                        break;
                    case MISSING_POSITION:
                        showWarningTemp(getString(R.string.err_navigation_no_route), 3000);
                        break;
                    case MISSING_GRAPH:
                    case MISSING_LOCATION:
                    case MISSING_ROUTE:
                    case MISSING_PROJECTION:
                        cancelRouteAndHideSheet();
                        showWarningTemp(getString(R.string.err_navigation_no_route), 3000);
                        break;
                    case IDLE:
                        break;
                }
            }

            @Override
            public void onRouteAdvanced(@Nullable RouteProgress progress) {
                if (!mRouting || progress == null) return;
                renderRouteProgress(progress.getAdvance(), progress.getRemainingDistance(), progress.getRemainingTime());
            }

            @Override
            public void onRouteTargetReached() {
                if (mRouting) showRouteFinishSheet();
            }
        };
    }

    private void initBroadcastReceiver() {
        mStateReceiver = new StateReceiver();
        mPositionReceiver = new PositionReceiver();

        mStateReceiverFilter = new IntentFilter();
        mPositionReceiverFilter = new IntentFilter();

        mStateReceiverFilter.addAction(LOCATION_CHANGED);
        mStateReceiverFilter.addAction(VENUE_SELECTED);
        mStateReceiverFilter.addAction(VENUE_FILTER_ON);
        mStateReceiverFilter.addAction(VENUE_FILTER_OFF);

        mPositionReceiverFilter.addAction(ACTION_POSITION_UPDATED);
        mPositionReceiverFilter.addAction(ACTION_POSITION_ERROR);
    }

    private void addListeners() {
        if (Build.VERSION.SDK_INT >= 33) {
            requireActivity().registerReceiver(mStateReceiver, mStateReceiverFilter, Context.RECEIVER_NOT_EXPORTED);
            requireActivity().registerReceiver(mPositionReceiver, mPositionReceiverFilter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            requireActivity().registerReceiver(mStateReceiver, mStateReceiverFilter);
            requireActivity().registerReceiver(mPositionReceiver, mPositionReceiverFilter);
        }
    }

    private void removeListeners() {
        requireActivity().unregisterReceiver(mStateReceiver);
        requireActivity().unregisterReceiver(mPositionReceiver);
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
            if (mPinIconFrom == null)
                mPinIconFrom = mLocationView.getLocationWindow().addIconMapObject();
            setupPinIcon(mPinIconFrom, R.drawable.ic_from_point_png, mPinPoint);
        } else {
            if (mPinIconTarget == null)
                mPinIconTarget = mLocationView.getLocationWindow().addIconMapObject();
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
        GlobalPoint position = toGlobalPoint(pinLocationPoint);
        if (position == null) return;

        pinMapObject.setSize(36, 108);
        pinMapObject.setBitmap(ImageProvider.fromResource(requireActivity(), pinIcon));
        pinMapObject.setCollisionEnabled(false);
        pinMapObject.setPriority(100f);
        pinMapObject.setPosition(position, pinLocationPoint.getSublocationId());
        pinMapObject.setVisible(true);
    }

    private void updateDestinationText(String text) {
        if (mToText != null) mToText.setText(text);
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
        }
    }

    private void updateRouteSheetInfo() {
        if (mMakeRouteBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            hideAndShowBottomSheets(null, mMakeRouteBehavior, BottomSheetBehavior.STATE_EXPANDED);
        }
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
        } else if (mToVenue != null) {
            mTargetVenue = mToVenue;
            mTargetPoint = null;
        }
        mPinPoint = null;
        mPinVenue = null;
        mToVenue = null;

        LocationPoint target = mTargetPoint != null ? mTargetPoint : venueLocationPoint(mTargetVenue);
        if (!startGuidance(target)) return;

        if (mVenueBottomSheet.isAdded()) mVenueBottomSheet.dismiss();
        hideAndShowBottomSheets(null, mMakeRouteBehavior, BottomSheetBehavior.STATE_HIDDEN);
    }

    private void setRoutingFlag() {
        mRouting = true;
    }

    private void resetRoutingFlag() {
        mRouting = false;
    }

    public void onCancelRoute() {
        resetRoutingFlag();

        mTargetPoint = null;
        mTargetVenue = null;
        mPinPoint = null;
        mPinVenue = null;
        mFromPoint = null;
        mFromVenue = null;

        if (mRouteLayer != null) mRouteLayer.clear();
        mRouteAnnotations.clear();
        mRouteEventAdapter.clear();

        updatePinIconsState(false, mPinIconFrom, mPinIconTarget);
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
        if (titleText.length() > 25) titleText = titleText.substring(0, 24) + "…";

        String categoryText = mPinVenue.getName();
        if (categoryText.length() > 30) categoryText = categoryText.substring(0, 28) + "…";

        String bm = mPinVenue.getImageUrl();

        mVenueBottomSheet.setSheetTitle(titleText);
        mVenueBottomSheet.setDescription(mPinVenue.getDescript());
        mVenueBottomSheet.setImageRef(bm);
        mVenueBottomSheet.setRouteButtonVisibility(mFromPoint == null ? GONE : VISIBLE);

        mVenueBottomSheet.setRouteButtonClick(v -> {
            if (mRouting) {
                mTargetVenue = mPinVenue;
                mTargetPoint = null;
                mPinVenue = null;
                mToVenue = null;
                startGuidance(venueLocationPoint(mTargetVenue));
                if (mVenueBottomSheet.isAdded()) mVenueBottomSheet.dismiss();
                return;
            }

            mPinPoint = null;
            mToVenue = mPinVenue;
            String title = mPinVenue.getName();
            if (title.length() > 20) title = title.substring(0, 18) + "";
            updateDestinationText("To:       " + title);
            if (mVenueBottomSheet.isAdded()) mVenueBottomSheet.dismiss();
            hideAndShowBottomSheets(null, mMakeRouteBehavior, BottomSheetBehavior.STATE_EXPANDED);
        });

        mVenueBottomSheet.show(getParentFragmentManager(), null);
        hideAndShowBottomSheets(mMakeRouteBehavior, null, BottomSheetBehavior.STATE_COLLAPSED);
    }

    private void showMapSettingsSheet() {
        LocationWindow window = mLocationView.getLocationWindow();

        BottomSheetMapSettings.newInstance(
                window.getRotateGestureEnabled(),
                window.getTiltGesturesEnabled(),
                window.getScrollGesturesEnabled(),
                window.getZoomGesturesEnabled(),
                window.getStickToBorder(),
                m3dEnabled,
                new BottomSheetMapSettings.OnSettingsChangedListener() {
                    @Override
                    public void onRotateChanged(boolean e) {
                        window.setRotateGestureEnabled(e);
                    }

                    @Override
                    public void onTiltChanged(boolean e) {
                        window.setTiltGesturesEnabled(e);
                    }

                    @Override
                    public void onScrollChanged(boolean e) {
                        window.setScrollGesturesEnabled(e);
                    }

                    @Override
                    public void onZoomChanged(boolean e) {
                        window.setZoomGesturesEnabled(e);
                    }

                    @Override
                    public void onStickToBorderChanged(boolean e) {
                        window.setStickToBorder(e);
                    }

                    @Override
                    public void on3dChanged(boolean e) {
                        set3dEnabled(e);
                    }
                }
        ).show(getParentFragmentManager(), null);
    }

    private void set3dEnabled(boolean enabled) {
        m3dEnabled = enabled;
        LocationWindow window = mLocationView.getLocationWindow();

        if (enabled) window.setTiltGesturesEnabled(true);

        ArrayList<String> values = enabled ? BARRIER_3D_VALUES_ON : BARRIER_3D_VALUES_OFF;
        ArrayList<MapFilterCondition> conditions = new ArrayList<>();
        conditions.add(new MapFilterCondition("$geometry", values));
        window.applyLayerFilter(BARRIER_LAYER, conditions);
    }

    private void resetLocationFlags() {
        mLocationChanged = false;
        mLocationLoaded = false;
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

        if (index < 0 || index >= mLocation.getSublocations().size()) return false;

        mSublocation = mLocation.getSublocations().get(index);

        if (mSublocation.getWidth() < 1.0f || mSublocation.getHeight() < 1.0f) {
            String text = getString(R.string.navigation_sublocation_load_failed, mSublocation.getWidth(), mSublocation.getHeight());
            Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
            Log.e(TAG, text);
            return false;
        }

        mLocationView.getLocationWindow().setSublocationId(mSublocation.getId());
        return mLocationView.post(() -> {
            float pixelWidth = mLocationView.getWidth() / getResources().getDisplayMetrics().density;
            mLocationView.getLocationWindow().setMaxZoomFactor((pixelWidth * 16.f) / mSublocation.getWidth());
            mLocationView.getLocationWindow().setMinZoomFactor((pixelWidth / 16.f) / mSublocation.getWidth());
            mLocationView.getLocationWindow().setZoomFactor(pixelWidth / mSublocation.getWidth());
            mLocationView.getLocationWindow().applyLayerFilter(VENUE_FILTER_LAYER_DEFAULT, new ArrayList<>());
            setupZoomCameraDefault();
        });
    }

    private void setupZoomCameraDefault() {
        mZoomCameraDefault = mLocationView.getLocationWindow().getCamera().getZoom();
    }

    private void handleClick(float x, float y) {
        if (mRouting) {
            mLocationView.getLocationWindow().pickMapFeatureAt(new PointF(x, y));
            return;
        }
        if (mTargetPoint != null || mTargetVenue != null || mPinPoint != null || mPinVenue != null || mMakeRouteBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            cancelPin();
            return;
        }
        mLocationView.getLocationWindow().pickMapFeatureAt(new PointF(x, y));
    }

    private void cancelPin() {

        if (hasTarget()) return;

        mPinPoint = null;
        mPinVenue = null;
        mToVenue = null;
        mFromVenue = null;
        mFromPoint = null;

        updatePinIconsState(false, mPinIconFrom, mPinIconTarget);
    }

    private void adjustDevice(LocationPoint locationPoint) {
        GlobalPoint target = toGlobalPoint(locationPoint);
        if (target == null) return;
        Camera camera = new Camera(target, mZoomCameraDefault * ADJUST_ZOOM_MULTIPLIER, 0f, 0f);
        mLocationView.getLocationWindow().flyTo(camera, CAMERA_FLY_DURATION_MS, null);

    }

    private void onRouteUpdated(RouteView route) {
        mRouteAnnotations.clear();
        mRouteAnnotations.addAll(route.annotations());

        if (mCancelRouteBehaviour.getState() == BottomSheetBehavior.STATE_HIDDEN)
            mCancelRouteBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);

        float length = route.length();
        renderRouteProgress(0f, length, RouteFormatter.estimateSeconds(length));
    }

    private void renderRouteProgress(float advance, float remainingDistance, float remainingTimeSeconds) {
        List<RouteAnnotation> upcoming = upcomingAnnotations(advance);

        mCancelRouteDistance.setText(upcoming.isEmpty()
                ? RouteFormatter.distance(remainingDistance)
                : RouteFormatter.instruction(upcoming.get(0), upcoming.get(0).getAdvance() - advance));
        mCancelRouteTime.setText(RouteFormatter.duration(remainingTimeSeconds));
        mRouteEventAdapter.submit(upcoming, advance);
    }

    private List<RouteAnnotation> upcomingAnnotations(float advance) {
        List<RouteAnnotation> result = new ArrayList<>();
        for (RouteAnnotation annotation : mRouteAnnotations) {
            if (annotation.getType() != RouteAnnotationType.START && annotation.getAdvance() > advance) {
                result.add(annotation);
            }
        }
        return result;
    }

    @Nullable
    private LocationPoint venueLocationPoint(@Nullable Venue venue) {
        return venue == null ? null : new LocationPoint(venue.getPoint(), venue.getLocationId(), venue.getSublocationId());
    }

    private boolean startGuidance(@Nullable LocationPoint target) {
        GlobalPoint point = toGlobalPoint(target);
        if (point == null || mRouteLayer == null) return false;
        setRoutingFlag();
        mRouteLayer.setTarget(new RouteEndpoint(point, target.getSublocationId()));
        return true;
    }

    private boolean hasTarget() {
        return mTargetPoint != null || mTargetVenue != null;
    }

    private void pointCameraToVenue(int sublocationIndex, float[] venueCoords) {
        if (venueCoords == null || venueCoords.length < 2 || !loadSubLocation(sublocationIndex))
            return;
        LocationPoint venuePoint = new LocationPoint(
                new Point(venueCoords[0], venueCoords[1]), mLocation.getId(), mSublocation.getId());
        mLocationView.post(() -> adjustDevice(venuePoint));
    }


    private void zoomToVenue(Venue venue) {
        LocationPoint venuePoint = new LocationPoint(
                venue.getPoint(), venue.getLocationId(), venue.getSublocationId());
        mLocationView.post(() -> adjustDevice(venuePoint));
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

    private void applyVenueFilter(ArrayList<String> categories, String layer) {
        if (mLocationView == null) return;
        mLocationView.post(() -> {
            ArrayList<MapFilterCondition> conditions = new ArrayList<>();
            conditions.add(new MapFilterCondition("category", categories));
            mLocationView.getLocationWindow().applyLayerFilter(layer, conditions);
        });
    }

    private ArrayList<String> extractCategoryNames(List<VenueIconObj> venueIconObjs) {
        ArrayList<String> names = new ArrayList<>();
        for (VenueIconObj obj : venueIconObjs) {
            names.add(obj.getCategoryName());
        }
        return names;
    }

    private void resetVenueFilter() {
        if (mLocationView != null)
            mLocationView.post(() -> mLocationView.getLocationWindow().applyLayerFilter(VENUE_FILTER_VALUE_DEFAULT, new ArrayList<>()));
    }

    private void updateFilteredVenuesIconsList() {
        mFilteredVenueIconsList.clear();

        Map<Integer, String> mCategoriesIdsAll = new HashMap();
        List<Integer> mCategoriesIdsCurr = new ArrayList<>();

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
        mStateReceiver.filteredVenues.remove(venueIconObj);
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
        mChipsMap.remove(chip);
    }

    private boolean isTurnLeft(TurnType type) {
        return type == TurnType.LEFT_SLIGHT
                || type == TurnType.LEFT_NORMAL
                || type == TurnType.LEFT_SHARP;
    }

    private boolean isTurnRight(TurnType type) {
        return type == TurnType.RIGHT_SLIGHT
                || type == TurnType.RIGHT_NORMAL
                || type == TurnType.RIGHT_SHARP;
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
        applyVenueFilter(extractCategoryNames(mStateReceiver.filteredVenues), VENUE_FILTER_LAYER_DEFAULT);
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

    private void showRouteFinishSheet() {
        if (isStateSaved()) {
            mRouteFinishPending = true;
            return;
        }
        if (mBottomSheetRouteFinish == null)
            mBottomSheetRouteFinish = new BottomSheetRouteFinish(this::onCancelRoute);

        if (!mBottomSheetRouteFinish.isAdded() && !mBottomSheetRouteFinish.isShown()) {
            mBottomSheetRouteFinish.show(getParentFragmentManager(), null);
        }
    }

    @Nullable
    private GlobalPoint toGlobalPoint(@Nullable LocationPoint locationPoint) {
        if (locationPoint == null || mLocation == null) return null;
        Sublocation sublocation = mLocation.getSublocationById(locationPoint.getSublocationId());
        return sublocation == null ? null : sublocation.localToGlobal(locationPoint);
    }

    private class StateReceiver extends BroadcastReceiver {

        private ArrayList<VenueIconObj> filteredVenues = null;

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case LOCATION_CHANGED:
                    mLocationChanged = true;
                    hideMessageDelay();
                    cancelRouteAndHideSheet();
                    removeChipsFromGroup();
                    clearSearchQuery();
                    onCloseSearch();
                    hideTransparentLayout();
                    break;
                case VENUE_SELECTED:
                    int sublocationId = intent.getIntExtra(KEY_VENUE_SUBLOCATION, 0);
                    int venueId = intent.getIntExtra(KEY_VENUE_ID, -1);
                    float[] point = intent.getFloatArrayExtra(KEY_VENUE_POINT);
                    int sublocationIndex = mLocation.getSublocations().indexOf(mLocation.getSublocationById(sublocationId));
                    onCloseSearch();
                    hideTransparentLayout();
                    pointCameraToVenue(sublocationIndex, point);
                    if (venueId != -1) {
                        mPinVenue = findVenueById(venueId);
                        if (mPinVenue != null) showVenueBottomSheet();
                    }
                    break;
                case VENUE_FILTER_ON:
                case VENUE_FILTER_OFF:
                    filteredVenues = intent.getParcelableArrayListExtra(KEY_VENUE_CATEGORY);
                    if (filteredVenues != null) {
                        if (intent.getAction().equals(VENUE_FILTER_ON)) {
                            updateSearchViewWithChips(filteredVenues);
                            applyVenueFilter(extractCategoryNames(filteredVenues), VENUE_FILTER_LAYER_DEFAULT);
                        } else {
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

    private class PositionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) return;
            switch (action) {
                case ACTION_POSITION_UPDATED:

                    float x = intent.getFloatExtra(NavigationService.KEY_POINT_X, -1f);
                    float y = intent.getFloatExtra(NavigationService.KEY_POINT_Y, -1f);
                    int locationId = intent.getIntExtra(NavigationService.KEY_LOCATION_ID, -1);
                    int sublocationId = intent.getIntExtra(NavigationService.KEY_SUBLOCATION_ID, -1);
                    double pointLocationHeading = intent.getDoubleExtra(NavigationService.KEY_LOCATION_HEADING, -1.0);

                    LocationPoint lp = new LocationPoint(new Point(x, y), locationId, sublocationId);

                    if (x == -1f || y == -1f || locationId == -1 || sublocationId == -1) return;

                    mPositionLocationPoint = lp;

                    if (pointLocationHeading != -1) {
                        if (!mOrientationPointState) {
                            mOrientationPointState = true;
                        }
                    } else {
                        if (mOrientationPointState) {
                            mOrientationPointState = false;
                        }
                    }

                    int id = lp.getSublocationId();
                    if (mSublocation != null) {
                        if (mSublocation.getId() != id) {
                            mSublocation = mLocation.getSublocationById(id);
                            loadSubLocation(mLocation.getSublocations().indexOf(mSublocation));
                            adjustDevice(lp);
                        }
                    }

                    mFromPoint = lp;
                    break;
                case ACTION_POSITION_ERROR:
                    mPositionLocationPoint = null;
                    String errMsg = intent.getStringExtra(ACTION_POSITION_ERROR);
                    if (errMsg != null) {
                        Log.e(TAG, getString(R.string.err_navigation_position_update) + ":" + errMsg);
                    }
                    break;
            }
        }
    }
}
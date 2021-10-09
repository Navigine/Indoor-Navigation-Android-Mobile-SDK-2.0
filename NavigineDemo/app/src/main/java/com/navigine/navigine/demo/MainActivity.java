package com.navigine.navigine.demo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private FragmentManager       mFragmentManager    = getSupportFragmentManager();
    private Fragment              mLocationsFragment  = new LocationsFragment();
    private Fragment              mNavigationFragment = new NavigationFragment();
    private Fragment              mDebugFragment      = new DebugFragment();
    private Fragment              mProfileFragment    = new ProfileFragment();
    private Fragment              mLastActive         = mLocationsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (NavigineApp.mNavigineSdk == null)
        {
            finish();
            return;
        }

        mFragmentManager.beginTransaction().add(R.id.main__frame_layout, mProfileFragment,    "Profile").   hide(mProfileFragment).   commitAllowingStateLoss();
        mFragmentManager.beginTransaction().add(R.id.main__frame_layout, mDebugFragment,      "Debug").     hide(mDebugFragment).     commitAllowingStateLoss();
        mFragmentManager.beginTransaction().add(R.id.main__frame_layout, mNavigationFragment, "Navigation").hide(mNavigationFragment).commitAllowingStateLoss();
        mFragmentManager.beginTransaction().add(R.id.main__frame_layout, mLocationsFragment,  "Locations").                           commitAllowingStateLoss();

        BottomNavigationView mBottomNavigation = findViewById(R.id.main__bottom_navigation);
        mBottomNavigation.setOnNavigationItemSelectedListener(menuItem ->
        {

//            if (LocationsFragment.locationChanged)
//            {
//                LocationsFragment.locationChanged = false;
//                mFragmentManager.beginTransaction().remove(mNavigationFragment).commitAllowingStateLoss();
//                mNavigationFragment = new NavigationFragment();
//                mFragmentManager.beginTransaction().add(R.id.main__frame_layout, mNavigationFragment, "Navigation").hide(mNavigationFragment).commitAllowingStateLoss();
//                mFragmentManager.beginTransaction().remove(mMeasuringFragment).commitAllowingStateLoss();
//                mMeasuringFragment = new MeasuringFragment();
//                mFragmentManager.beginTransaction().add(R.id.main__frame_layout, mMeasuringFragment, "Measuring").hide(mMeasuringFragment).commitAllowingStateLoss();
//            }
//            if (NavigineApp.ReferencePointsEnabled)
//            {
//                mFragmentManager.beginTransaction().remove(mMeasuringFragment).commitAllowingStateLoss();
//                mMeasuringFragment = new MeasuringFragment();
//                mFragmentManager.beginTransaction().add(R.id.main__frame_layout, mMeasuringFragment, "Measuring").hide(mMeasuringFragment).commitAllowingStateLoss();
//            }
            switch (menuItem.getItemId())
            {
                case R.id.navigation__menu_locations:
                    mFragmentManager.beginTransaction().hide(mLastActive).show(mLocationsFragment).commitAllowingStateLoss();
                    mLastActive = mLocationsFragment;
                    return true;
                case R.id.navigation__menu_navigation:
                    mFragmentManager.beginTransaction().hide(mLastActive).show(mNavigationFragment).commitAllowingStateLoss();
                    mLastActive = mNavigationFragment;
                    return true;
                case R.id.navigation__menu_debug:
                    mFragmentManager.beginTransaction().hide(mLastActive).show(mDebugFragment).commitAllowingStateLoss();
                    mLastActive = mDebugFragment;
                    return true;
                case R.id.navigation__menu_profile:
                    mFragmentManager.beginTransaction().hide(mLastActive).show(mProfileFragment).commitAllowingStateLoss();
                    mLastActive = mProfileFragment;
                    return true;
            }
            return false;
        });
    }
}

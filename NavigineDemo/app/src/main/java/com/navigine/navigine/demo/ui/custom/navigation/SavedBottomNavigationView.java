package com.navigine.navigine.demo.ui.custom.navigation;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;


public class SavedBottomNavigationView extends BottomNavigationView {

    private int     firstFragmentGraphId = 0;
    private String  selectedItemTag;
    private String  firstFragmentTag;
    private boolean isOnFirstFragment;

    public SavedBottomNavigationView(@NonNull Context context) {
        super(context);
    }

    public SavedBottomNavigationView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SavedBottomNavigationView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void setupItemReselected(
            SparseArray<String> graphIdToTagMap,
            FragmentManager fragmentManager) {
        this.setOnNavigationItemReselectedListener(new OnNavigationItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {
                String newlySelectedItemTag = graphIdToTagMap.get(item.getItemId());
                NavHostFragment selectedFragment = (NavHostFragment) fragmentManager.findFragmentByTag(newlySelectedItemTag);
                NavController navController = selectedFragment.getNavController();
                navController.popBackStack(navController.getGraph().getStartDestination(), false);
            }
        });
    }

    public LiveData<NavController> setupWithNavController(
            List<Integer> navGraphIds,
            FragmentManager fragmentManager,
            int containerId, Intent intent) {
        final BottomNavigationView self = this;

        // Map of tags
        SparseArray<String> graphIdToTagMap = new SparseArray<String>();
        // Result. Mutable live data with the selected controlled
        MutableLiveData<NavController> selectedNavController = new MutableLiveData<NavController>();

        // First create a NavHostFragment for each NavGraph ID
        int index = 0;

        for (Integer navGraphId : navGraphIds) {
            String fragmentTag = getFragmentTag(index);

            // Find or create the Navigation host fragment
            NavHostFragment navHostFragment = obtainNavHostFragment(
                    fragmentManager,
                    fragmentTag,
                    navGraphId,
                    containerId);

            // Obtain its id
            int graphId = navHostFragment.getNavController().getGraph().getId();
            if (index == 0) {
                firstFragmentGraphId = graphId;
            }

            // Save to the map
            graphIdToTagMap.put(graphId, fragmentTag);

            // Attach or detach nav host fragment depending on whether it's the selected item.
            if (this.getSelectedItemId() == graphId) {
                // Update livedata with the selected graph
                selectedNavController.setValue(navHostFragment.getNavController());
                attachNavHostFragment(fragmentManager, navHostFragment, index == 0);
            } else {
                detachNavHostFragment(fragmentManager, navHostFragment);
            }

            index++;
        }

        // Now connect selecting an item with swapping Fragments
        selectedItemTag = graphIdToTagMap.get(this.getSelectedItemId());
        firstFragmentTag = graphIdToTagMap.get(firstFragmentGraphId);
        isOnFirstFragment = selectedItemTag.equals(firstFragmentTag);

        setOnNavigationItemSelectedListener(new OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Don't do anything if the state is state has already been saved.
                if (fragmentManager.isStateSaved()) {
                    return false;
                } else {
                    String newlySelectedItemTag = graphIdToTagMap.get(item.getItemId());
                    if (!selectedItemTag.equals(newlySelectedItemTag)) {
                        // Pop everything above the first fragment (the "fixed start destination")
                        fragmentManager.popBackStack(firstFragmentTag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        NavHostFragment selectedFragment = (NavHostFragment) fragmentManager.findFragmentByTag(newlySelectedItemTag);
                        // Exclude the first fragment tag because it's always in the back stack.
                        if (!firstFragmentTag.equals(newlySelectedItemTag)) {
                            // Commit a transaction that cleans the back stack and adds the first fragment
                            // to it, creating the fixed started destination.
                            FragmentTransaction transaction = fragmentManager.beginTransaction()
                                    .show(selectedFragment)
                                    .setPrimaryNavigationFragment(selectedFragment);


                            for (int i = 0; i < graphIdToTagMap.size(); i++) {
                                int key = graphIdToTagMap.keyAt(i);
                                // get the object by the key.
                                String tag = graphIdToTagMap.get(key);
                                if (!tag.equals(newlySelectedItemTag)) {
                                    transaction.hide(fragmentManager.findFragmentByTag(firstFragmentTag));
                                }
                            }

                            transaction.addToBackStack(firstFragmentTag)
                                    .setReorderingAllowed(true)
                                    .commit();
                        }

                        selectedItemTag = newlySelectedItemTag;
                        isOnFirstFragment = selectedItemTag.equals(firstFragmentTag);
                        selectedNavController.setValue(selectedFragment.getNavController());
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        });

        // Optional: on item reselected, pop back stack to the destination of the graph
        setupItemReselected(graphIdToTagMap, fragmentManager);

        // Handle deep link
        setupDeepLinks(navGraphIds, fragmentManager, containerId, intent);

        // Finally, ensure that we update our BottomNavigationView when the back stack changes
        fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (!isOnFirstFragment && !isOnBackStack(fragmentManager, firstFragmentTag)) {
                    self.setSelectedItemId(firstFragmentGraphId);
                }

                // Reset the graph if the currentDestination is not valid (happens when the back
                // stack is popped after using the back button).
                NavController controller = selectedNavController.getValue();
                if (controller != null) {
                    if (controller.getCurrentDestination() == null) {
                        controller.navigate(controller.getGraph().getId());
                    }
                }
            }
        });
        return selectedNavController;
    }

    private void setupDeepLinks(
            List<Integer> navGraphIds,
            FragmentManager fragmentManager,
            int containerId,
            Intent intent
    ) {
        int index = 0;
        for (Integer navGraphId : navGraphIds) {
            String fragmentTag = getFragmentTag(index);

            // Find or create the Navigation host fragment
            NavHostFragment navHostFragment = obtainNavHostFragment(
                    fragmentManager, fragmentTag, navGraphId, containerId);

            // Handle Intent
            int graphId = navHostFragment.getNavController().getGraph().getId();
            if (navHostFragment.getNavController().handleDeepLink(intent)
                    && this.getSelectedItemId() != graphId) {
                this.setSelectedItemId(graphId);
            }

            index++;
        }
    }

    private void detachNavHostFragment(
            FragmentManager fragmentManager,
            NavHostFragment navHostFragment) {
        fragmentManager.beginTransaction()
                .hide(navHostFragment)
                .commitNow();
    }

    private void attachNavHostFragment(
            FragmentManager fragmentManager,
            NavHostFragment navHostFragment,
            boolean isPrimaryNavFragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction()
                .show(navHostFragment);

        if (isPrimaryNavFragment) {
            transaction.setPrimaryNavigationFragment(navHostFragment);
        }

        transaction.commitNow();
    }

    private NavHostFragment obtainNavHostFragment(
            FragmentManager fragmentManager,
            String fragmentTag,
            int navGraphId,
            int containerId) {
        // If the Nav Host fragment exists, return it
        Fragment fragment = fragmentManager.findFragmentByTag(fragmentTag);
        if (fragment != null && fragment instanceof NavHostFragment) {
            return (NavHostFragment) fragment;
        }

        // Otherwise, create it and return it.
        NavHostFragment navHostFragment = NavHostFragment.create(navGraphId);
        fragmentManager.beginTransaction()
                .add(containerId, navHostFragment, fragmentTag)
                .commitNow();
        return navHostFragment;
    }


    private boolean isOnBackStack(FragmentManager fragmentManager, String backStackName) {
        int backStackCount = fragmentManager.getBackStackEntryCount();
        for (int i = 0; i < backStackCount; i++) {
            FragmentManager.BackStackEntry entry = fragmentManager.getBackStackEntryAt(i);
            if (entry.getName().equals(backStackName)) {
                return true;
            }
        }
        return false;
    }

    public String getFragmentTag(int index) {
        return "bottomNavigation#" + index;
    }
}
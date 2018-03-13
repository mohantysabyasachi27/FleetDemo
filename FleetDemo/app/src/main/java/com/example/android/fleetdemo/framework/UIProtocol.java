package com.example.android.fleetdemo.framework;

/**
 * Created by Azuga on 07-03-2018.
 */

import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.android.fleetdemo.FrameworkUtils;


/**
 * Created by manish on 05/01/16.
 * Copyright (c) 2016, Azuga, Inc. All rights reserved.
 */
public abstract class UIProtocol extends AppCompatActivity {
    private String tempFragmentTag;

    public abstract void setNavBarVisibility(boolean visible);

    public abstract void showLeftNavButton();

    public abstract void hideLeftNavButton();

    public abstract void showRightNavButton(View view, View.OnClickListener clickListener);

    public abstract void hideRightNavButton();

    public abstract void setTitle(String title);

    public abstract void setNavBarBgColor(int color);

    public abstract void showInAppError(String message, String btnText, View.OnClickListener clickListener, int errorType);

    public abstract int getInAppErrorType();

    public abstract void hideInAppError();

    public abstract void navigateToHome();

    public abstract @IdRes int getFragmentContainerId();

    public String getActiveFragmentTag() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count > 0) {
            return getSupportFragmentManager()
                    .getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
        }
        return null;
    }

    // returns a fragment which is active fragment added to LeftMenuActivity
    public BaseFragment getActiveFragment() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            return null;
        }
        String tag = getSupportFragmentManager()
                .getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
        return (BaseFragment) getSupportFragmentManager().findFragmentByTag(tag);
    }

    public BaseFragment getFragmentFromHistory(String tag) {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            return null;
        }
        return (BaseFragment) getSupportFragmentManager().findFragmentByTag(tag);
    }

    public int getBackStackFragmentsCount() {
        return getSupportFragmentManager().getBackStackEntryCount();
    }

    public boolean isFragmentInHistory(String fragmentTag) {
        return null != getSupportFragmentManager().findFragmentByTag(fragmentTag);
    }

    public boolean showFragmentFromHistory(String fragmentTag) {
        if (isFragmentInHistory(fragmentTag)) {
            FragmentManager supportFragmentManager = getSupportFragmentManager();
            return supportFragmentManager.popBackStackImmediate(fragmentTag, 0);
        }

        return false;
    }

    public boolean popFragmentFromBackStack() {
        return getSupportFragmentManager().popBackStackImmediate();
    }

    public void clearBackStack() {
        getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    public void addFragment(BaseFragment fragmentToAdd, boolean addToHistory) {
        // Due to android bug in REPLACE api we have to remove any hanging fragments here...
        if (!FrameworkUtils.isEmptyOrWhitespace(tempFragmentTag)) {
            Fragment tempFragment = getSupportFragmentManager().findFragmentByTag(tempFragmentTag);
            if (tempFragment != null) {
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(0, 0, 0, 0);
                fragmentTransaction.hide(tempFragment);
                fragmentTransaction.remove(tempFragment);
                fragmentTransaction.commit();
                getSupportFragmentManager().executePendingTransactions();
                tempFragmentTag = null;
            }
        }

        if (!addToHistory) {
            tempFragmentTag = fragmentToAdd.getFragmentTag();
        } else {
            tempFragmentTag = null;
        }

        // Hide the keyboard before adding new fragment
        UIService.getInstance().hideKeyboard();

        // Add the new fragment here...
        BaseFragment activeFragment = getActiveFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(fragmentToAdd.getFragmentEnterAnimationId(),
                fragmentToAdd.getFragmentExitAnimationId(), fragmentToAdd.getFragmentPopEnterAnimationId(),
                fragmentToAdd.getFragmentPopExitAnimationId());

        if (activeFragment != null) {
            fragmentTransaction.hide(activeFragment);
        }

        fragmentTransaction.replace(getFragmentContainerId(), fragmentToAdd, fragmentToAdd.getFragmentTag());
        if (addToHistory) {
            fragmentTransaction.addToBackStack(fragmentToAdd.getFragmentTag());
        }
        fragmentTransaction.commit();
        getSupportFragmentManager().executePendingTransactions();
        showLeftNavButton();
    }
}
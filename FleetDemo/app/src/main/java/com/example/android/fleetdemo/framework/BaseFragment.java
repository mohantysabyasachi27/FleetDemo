package com.example.android.fleetdemo.framework;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;

import com.example.android.fleetdemo.R;

/**
 * Created by Azuga on 07-03-2018.
 */
public abstract class BaseFragment extends android.support.v4.app.Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(getFragmentTag(), "onCreate");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.d(getFragmentTag(), "onCreateView");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Logger.d(getFragmentTag(), "onStart");
        // Hide or show the nav bar depending upon the fragment...
        if (!isChildFragment())
            UIService.getInstance().hideRightNavigationButton();
        UIService.getInstance().setNavBarVisibility(showNavigationBar());
        UIService.getInstance().setTitle(getFragmentDisplayName());
        // Except Communication error hide any other kind of error on new screen..
        if (UIService.getInstance().getInAppErrorType() != UIService.ERROR_TYPE_NETWORK) {
            UIService.getInstance().hideInAppError();
        }
    }



    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (UIService.isFragmentAnimationDisabled) {
            Animation anim = new Animation() {
            };
            anim.setDuration(0);
            return anim;
        }

        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.d(getFragmentTag(), "onResume");
    }

    // No need to manage separate enum or constants just use fully qualified class name as fragment tag.
    public String getFragmentTag() {
        return this.getClass().getName();
    }

    public boolean isChildFragment() {
        return false;
    }

    /**
     * Decides if top navigation bar need to be shown... Return false if nav bar need to be hidden.
     */
    public boolean showNavigationBar() {
        return true;
    }





    @Override
    public void onPause() {
        super.onPause();
        Logger.d(getFragmentTag(), "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Logger.d(getFragmentTag(), "onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.d(getFragmentTag(), "onDestroy");
    }

    public int getFragmentEnterAnimationId() {
        return R.anim.slide_in_right;
    }

    public int getFragmentExitAnimationId() {
        return R.anim.slide_out_left;
    }

    public int getFragmentPopEnterAnimationId() {
        return R.anim.slide_in_left;
    }

    public int getFragmentPopExitAnimationId() {
        return R.anim.slide_out_right;
    }

    public boolean canHandleBack() {
        return false;
    }

    public void onBackPressed() {

    }




    abstract protected String getFragmentDisplayName();

    abstract public void refreshData();




}

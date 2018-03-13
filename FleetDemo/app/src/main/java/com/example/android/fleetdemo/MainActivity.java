package com.example.android.fleetdemo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.android.fleetdemo.database.AzugaDatabaseConfig;
import com.example.android.fleetdemo.database.DatabaseService;
import com.example.android.fleetdemo.framework.BaseFragment;
import com.example.android.fleetdemo.framework.UIProtocol;
import com.example.android.fleetdemo.framework.UIService;

public class MainActivity extends UIProtocol {
    android.support.v7.widget.Toolbar toolbar_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        DatabaseService.init(new AzugaDatabaseConfig());
        DatabaseService.getInstance();
        toolbar_title = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar_title);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView mTitle = (TextView) toolbar_title.findViewById(R.id.toolbar_title);
        mTitle.setText("FLEET");
        BaseFragment fragment=null;

        if(AzugaPreferences.getInstance(this).isUserLoggedIn()){
            fragment = new TaskFragment();
            UIService.getInstance().addFragment(fragment);
           // getSupportFragmentManager().beginTransaction().replace(R.id.container,fragment).commit();
        }else{
            fragment = new LoginFragment();
            UIService.getInstance().addFragment(fragment);
           // getSupportFragmentManager().beginTransaction().replace(R.id.container,fragment).commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.logout:
                DatabaseService.getInstance().clearDatabase();
                AzugaPreferences.getInstance(this).resetUser();
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                Fragment fragment = new LoginFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.container,fragment).commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setNavBarVisibility(boolean visible) {

    }

    @Override
    public void showLeftNavButton() {

    }

    @Override
    public void hideLeftNavButton() {

    }

    @Override
    public void showRightNavButton(View view, View.OnClickListener clickListener) {

    }

    @Override
    public void hideRightNavButton() {

    }

    @Override
    public void setTitle(String title) {

    }

    @Override
    public void setNavBarBgColor(int color) {

    }

    @Override
    public void showInAppError(String message, String btnText, View.OnClickListener clickListener, int errorType) {

    }

    @Override
    public int getInAppErrorType() {
        return 0;
    }

    @Override
    public void hideInAppError() {

    }

    @Override
    public void navigateToHome() {

    }

    @Override
    public int getFragmentContainerId() {
        return R.id.container;
    }

    @Override
    public void onBackPressed() {
        BaseFragment baseFragment = UIService.getInstance().getActiveFragment();
        if (baseFragment != null && baseFragment.canHandleBack()) {
            baseFragment.onBackPressed();
        } else if (UIService.getInstance().getBackStackFragmentsCount() <= 1) {
            moveTaskToBack(true);
        } else {
            super.onBackPressed();
            showLeftNavButton();
        }
    }
}

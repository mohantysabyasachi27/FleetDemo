package com.example.android.fleetdemo.Adapter;

import android.view.View;

/**
 * Created by Azuga on 27-02-2018.
 */

public interface ClickListener {
    public void onClick(View view, int position);
    public void onLongClick(View view,int position);
}

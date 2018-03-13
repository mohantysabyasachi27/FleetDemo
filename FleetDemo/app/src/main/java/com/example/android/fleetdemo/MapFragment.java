package com.example.android.fleetdemo;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.android.fleetdemo.POJO.LatLongDescp;
import com.example.android.fleetdemo.POJO.Process;
import com.example.android.fleetdemo.POJO.Step;
import com.example.android.fleetdemo.Route.DirectionsJSONParser;
import com.example.android.fleetdemo.Route.MapMarkerBounce;
import com.example.android.fleetdemo.Route.WayPointJsonParser;
import com.example.android.fleetdemo.database.DatabaseService;
import com.example.android.fleetdemo.framework.BaseFragment;
import com.example.android.fleetdemo.framework.PermissionHelper;
import com.example.android.fleetdemo.framework.UIService;
import com.example.android.fleetdemo.gps.GpsListener;
import com.example.android.fleetdemo.gps.GpsService;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.android.volley.VolleyLog.TAG;
import static com.google.android.gms.maps.model.JointType.ROUND;

/**
 * Created by Azuga on 06-03-2018.
 */

public class MapFragment extends BaseFragment implements OnMapReadyCallback , GoogleMap.OnMapLongClickListener, GoogleMap.InfoWindowAdapter,GpsListener,View.OnClickListener {
    protected MapView mMapView;
    GoogleMap gMap;
    ArrayList<LatLng> markerPoints;
    ArrayList<LatLng> m_waypoints;
    ArrayList<LatLongDescp> lat_long_waypoints;
    private DialogWidget progressDialog;
    RelativeLayout fif_location_off_view;
    ImageView my_location_btn;
    private Location myLocation = null;
    List<Marker>markerList;
    Handler handler = new Handler();

    public MapFragment() {

    }

    public static MapFragment getInstance(ArrayList<LatLongDescp> latLongDescps){
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("latlng",latLongDescps);
        MapFragment mapFragment = new MapFragment();
        mapFragment.setArguments(bundle);
        return mapFragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        markerPoints = new ArrayList<>();
        m_waypoints = new ArrayList<>();
        markerList = new ArrayList<>();
        lat_long_waypoints = new ArrayList<>();
        sydney = new LatLng(12.947670, 77.689169);
        checkAndStartGPSListener();
//        markerPoints.add(new LatLng(12.947670, 77.689169));
//        markerPoints.add(new LatLng(12.947670, 77.689169));
//        markerPoints.add(new LatLng(12.959172, 77.697419));
//        markerPoints.add(new LatLng(12.926031, 77.676246));
//        m_waypoints.add(new LatLng(12.959172, 77.697419));
//        m_waypoints.add(new LatLng(12.926031, 77.676246));

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_frag, container, false);
        mMapView = (MapView) view.findViewById(R.id.mapview);
        my_location_btn = (ImageView) view.findViewById(R.id.wzat_my_location_btn);
        my_location_btn.setOnClickListener(this);
        mMapView.onCreate(savedInstanceState);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(getArguments() != null){
            lat_long_waypoints = getArguments().getParcelableArrayList("latlng");
        }
        for(LatLongDescp latLongDescp : lat_long_waypoints){
            m_waypoints.add(new LatLng(latLongDescp.getLatitude(),latLongDescp.getLogitude()));
        }
        fif_location_off_view = (RelativeLayout)view.findViewById(R.id.fif_location_off_view);
        fif_location_off_view.setOnClickListener(this);
    }

    @SuppressWarnings({"MissingPermission"})
    private boolean setMyLocationEnabled(boolean enabled) {
        if (PermissionHelper.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                || PermissionHelper.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            if (gMap != null) {
                gMap.setMyLocationEnabled(enabled);
            }
            return true;
        }

        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMapView != null) {
            mMapView.onResume();
        }
        GpsService.getInstance().addGpsListener(this);
//        if (gMap != null){
//            setMyLocationEnabled(true);
//        }else{
//            GpsService.getInstance().addGpsListener(this);
//
//        }
//        if (!PermissionHelper.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
//                || !PermissionHelper.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
//                || (!FrameworkUtils.isLocationEnabled(getActivity()))) {
//            fif_location_off_view.setVisibility(View.VISIBLE);
//        } else {
//            fif_location_off_view.setVisibility(View.GONE);
//            GpsService.getInstance().addGpsListener(this);
//        }

    }

    @Override
    public void onPause() {
        if (mMapView != null) {
            mMapView.onPause();
        }
        FrameworkUtils.resetVolleyRequestQueue();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mMapView != null) {
            try {
                mMapView.onDestroy();
            } catch (NullPointerException e) {
                Log.e("test", "Error while attempting MapView.onDestroy(), ignoring exception", e);
            }
        }
        super.onDestroy();
    }

    @Override
    protected String getFragmentDisplayName() {
        return null;
    }

    @Override
    public void refreshData() {

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mMapView != null) {
            mMapView.onLowMemory();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMapView != null) {
            mMapView.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.gMap = googleMap;
        setMyLocationEnabled(false);
        gMap.setOnMarkerClickListener(new MapMarkerBounce());
        gMap.setTrafficEnabled(false);
        gMap.setIndoorEnabled(false);
        gMap.setBuildingsEnabled(false);
        gMap.getUiSettings().setZoomControlsEnabled(true);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), 13);
        gMap.animateCamera(cameraUpdate);
     //   gMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(markerPoints.get(0).latitude,markerPoints.get(0).longitude)));
//        gMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
//                .target(googleMap.getCameraPosition().target)
//                .zoom(17)
//                .bearing(30)
//                .tilt(45)
//                .build()));
        addMarkers(m_waypoints);
        addMarkers(markerPoints);

        gMap.setInfoWindowAdapter(this);
        if (markerPoints.size() >= 2) {
            makeDirectionApiRequest();
        }


    }

    private void addMarkers(ArrayList<LatLng> m_waypoints) {
        for(LatLng latLng  : m_waypoints){
            Marker marker = gMap.addMarker(new MarkerOptions().position(latLng));
            markerList.add(marker);
        }

    }


    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String sensor = "sensor=false";
        String waypoints = "";
        waypoints = "waypoints=optimize:true|";
        for (int i = 0; i < m_waypoints.size(); i++) {
            LatLng point = (LatLng) m_waypoints.get(i);
            waypoints += point.latitude + "," + point.longitude + "|";
        }
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + waypoints;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }


    @Override
    public void onMapLongClick(LatLng latLng) {
        gMap.clear();
        markerPoints.clear();
    }

    @Override
    public View getInfoWindow(Marker marker) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.trips_map_info_view, null);
        return v;

    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    public void showProgressBar(final String message) {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.setProgressMessage(message);
                    return;
                }

                DialogWidget.DialogBuilder builder = new DialogWidget.DialogBuilder(getActivity(), true);
                builder.setCancelable(false);
                builder.setCanceledOnTouchOutside(false);
                progressDialog = builder.showProgressDialog(message);
            }
        });
    }

    public void hideProgressBar() {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.cancel();
                    progressDialog = null;
                }
            }
        });
    }

    public void makeDirectionApiRequest() {
        requestCreateSession(new FrameworkUtils.AllStateVolleyCallBack() {
            @Override
            public void onResponse(JsonArray jsonArray) {
                hideProgressBar();

            }

            @Override
            public void onResponse(JsonObject object) {
                hideProgressBar();
            }

            @Override
            public void onResponse(String response) {
                hideProgressBar();
                // parseResponse(response);


            }

            @Override
            public void onError(VolleyError error) {
                Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                hideProgressBar();
            }
        });
    }

    private List<LatLng> polyLineList;
    private float v;
    private double lat, lng;
    private LatLng startPosition, endPosition;
    private int index, next;
    private LatLng sydney;

    private PolylineOptions polylineOptions, blackPolylineOptions;
    private Polyline blackPolyline, greyPolyLine;


    boolean toPoll = true;
    public void requestCreateSession(@NonNull final FrameworkUtils.AllStateVolleyCallBack callBack) {
        if (!FrameworkUtils.isDataConnectionOn()) {
            Toast.makeText(getContext(), "Bummer! No internet connection.", Toast.LENGTH_LONG).show();
            return;
        }
        String url = "";
        showProgressBar("Authenticating");
        if (markerPoints.size() >= 2) {
            LatLng origin = markerPoints.get(0);
            LatLng dest = markerPoints.get(1);
            url = getDirectionsUrl(origin, dest);
            Log.d("test","url is :"+ url);
        }
        try {

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                    url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("AllStateSessionRequest", "session request" + response);
                            try {
                                if (response == null) {
                                    return;
                                }
                                hideProgressBar();
                                decodeResponse(response);
                            } catch (Exception e) {
                                Log.e("AllStateSessionRequest", "Error parsing response", e);
                                callBack.onError(new VolleyError("Invalid Response, " + e.getMessage()));

                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    callBack.onError(new VolleyError(error.getMessage()));
                }
            }) {

                @Override
                public String getBodyContentType() {
                    return "application/json";
                }


            };
            FrameworkUtils.getVolleyRequestQueue().add(jsonObjectRequest);
        } catch (Exception e) {
            Log.e("AllStateSessionRequest", "Exception while creating session", e);
            callBack.onError(new VolleyError(e.getMessage()));
        }
    }

    public void makeSimulationRequest() {
        sendSimulationRequest(new FrameworkUtils.AllStateVolleyCallBack() {
            @Override
            public void onResponse(JsonArray jsonArray) {
                hideProgressBar();

            }

            @Override
            public void onResponse(JsonObject object) {
                hideProgressBar();
                parseSimulartionRequest(object);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(toPoll){
                            makeSimulationRequest();
                        }else{
                            handler.removeCallbacks(this);
                        }
                    }
                }, polling_interval);
            }

            @Override
            public void onResponse(String response) {
                hideProgressBar();
                // parseResponse(response);


            }

            @Override
            public void onError(VolleyError error) {
                Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                hideProgressBar();
            }
        });
    }

    int polling_interval = 1000;
    Marker marker;
    private void parseSimulartionRequest(JsonObject object) {
        JsonObject step = object.getAsJsonObject("step");
        Double lat = step.get("lat").getAsDouble();
        Double lon = step.get("lon").getAsDouble();
        boolean process = step.get("process").getAsBoolean();
        if (!process) {
            if(marker != null){
                marker.remove();
            }
            toPoll = true;
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 13);
            gMap.animateCamera(cameraUpdate);
            marker =gMap.addMarker(new MarkerOptions()
                    .position(new LatLng(lat, lon)).icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
        } else {
            toPoll = false;
            DatabaseService.getInstance().clearDatabase();
            AzugaPreferences.getInstance(getContext()).setUserLocation(true);
            TaskFragment taskFragment = TaskFragment.getInstance( AzugaPreferences.getInstance(getContext()).getLoggedInUser());
            UIService.getInstance().addFragment(taskFragment,false);
            polling_interval = 1000;
        }


    }

    public void sendSimulationRequest( final FrameworkUtils.AllStateVolleyCallBack callBack){



        if (!FrameworkUtils.isDataConnectionOn()) {
            Toast.makeText(getContext(), "Bummer! No internet connection.", Toast.LENGTH_LONG).show();
            return;
        }

        //  showProgressBar("Authenticating");
        String sessionURL = "http://10.19.0.221:9000/simulation";
        try {

            StringRequest request = new StringRequest(Request.Method.GET, sessionURL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("AllStateSessionRequest", "session request" + response);
                    try {
                        if ("".equals(response)) {
                            toPoll = false;
                            DatabaseService.getInstance().clearDatabase();
                            AzugaPreferences.getInstance(getContext()).setUserLocation(true);
                            TaskFragment taskFragment = TaskFragment.getInstance( AzugaPreferences.getInstance(getContext()).getLoggedInUser());
                            UIService.getInstance().addFragment(taskFragment,false);
                            return;
                        }
                        toPoll = true;
                        Log.d("test","sim reponse is :"+ response);
                        callBack.onResponse(new JsonParser().parse(response).getAsJsonObject());
                    } catch (Exception e) {
                        Log.e("AllStateSessionRequest", "Error parsing response", e);
                        callBack.onError(new VolleyError("Invalid Response, " + e.getMessage()));

                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    callBack.onError(new VolleyError(error.getMessage()));
                }
            }) {

                @Override
                public String getBodyContentType() {
                    return "application/json";
                }

            };
            FrameworkUtils.getVolleyRequestQueue().add(request);
        } catch (Exception e) {
            Log.e("AllStateSessionRequest", "Exception while creating session", e);
            callBack.onError(new VolleyError(e.getMessage()));
        }
    }



    private void decodeResponse(JSONObject response) {
        Log.d(TAG, response + "");
        try {
            JSONArray jsonArray = response.getJSONArray("routes");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject route = jsonArray.getJSONObject(i);
                JSONObject poly = route.getJSONObject("overview_polyline");
                String polyline = poly.getString("points");
                polyLineList = decodePoly(polyline);
                Log.d(TAG, polyLineList + "");
            }
            //Adjusting bounds
            List<Step> stepList = new ArrayList<>();
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng latLng : polyLineList) {
                Log.d("test","lat is :"+ latLng.latitude +",long:"+ latLng.longitude);
                String end_lat=String.valueOf(latLng.latitude);
                String end_long = String.valueOf(latLng.longitude);
                Step end_step = new Step(end_lat,end_long,false);
                stepList.add(end_step);
                builder.include(latLng);


            }
            Log.d("test","steps size :"+ stepList.size());
            Process process = new Process("sds",stepList);
            Gson gson = new Gson();
            String json = gson.toJson(process);
            Log.d("test","json  is :"+ json.toString());
            if(   AzugaPreferences.getInstance(getContext()).getUserLocation()){

            }else{
                sendPostRequest(json);
            }


            String sdfds= json;
            LatLngBounds bounds = builder.build();
            CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 2);
            gMap.animateCamera(mCameraUpdate);

            polylineOptions = new PolylineOptions();
            polylineOptions.color(Color.GRAY);
            polylineOptions.width(5);
            polylineOptions.startCap(new SquareCap());
            polylineOptions.endCap(new SquareCap());
            polylineOptions.jointType(ROUND);
            polylineOptions.addAll(polyLineList);
            greyPolyLine = gMap.addPolyline(polylineOptions);

            blackPolylineOptions = new PolylineOptions();
            blackPolylineOptions.width(5);
            blackPolylineOptions.color(Color.BLACK);
            blackPolylineOptions.startCap(new SquareCap());
            blackPolylineOptions.endCap(new SquareCap());
            blackPolylineOptions.jointType(ROUND);
            blackPolyline = gMap.addPolyline(blackPolylineOptions);

            makeSimulationRequest();

//            gMap.addMarker(new MarkerOptions()
//                    .position(polyLineList.get(polyLineList.size() - 1)));

//            ValueAnimator polylineAnimator = ValueAnimator.ofInt(0, 100);
//            polylineAnimator.setDuration(2000);
//            polylineAnimator.setInterpolator(new LinearInterpolator());
//            polylineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                @Override
//                public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                    List<LatLng> points = greyPolyLine.getPoints();
//                    int percentValue = (int) valueAnimator.getAnimatedValue();
//                    int size = points.size();
//                    int newPoints = (int) (size * (percentValue / 100.0f));
//                    List<LatLng> p = points.subList(0, newPoints);
//                    blackPolyline.setPoints(p);
//                }
//            });
//            polylineAnimator.start();
//            marker = gMap.addMarker(new MarkerOptions().position(sydney)
//                    .flat(true)
//                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
//            handler = new Handler();
//            index = -1;
//            next = 1;
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    if (index < polyLineList.size() - 1) {
//                        index++;
//                        next = index + 1;
//                    }
//                    if (index < polyLineList.size() - 1) {
//                        startPosition = polyLineList.get(index);
//                        endPosition = polyLineList.get(next);
//                    }
//                    ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
//                    valueAnimator.setInterpolator(new LinearInterpolator());
//                    valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                        @Override
//                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                            v = valueAnimator.getAnimatedFraction();
//                            lng = v * endPosition.longitude + (1 - v)
//                                    * startPosition.longitude;
//                            lat = v * endPosition.latitude + (1 - v)
//                                    * startPosition.latitude;
//                            LatLng newPos = new LatLng(lat, lng);
//                            if(index == polyLineList.size() -1){
//                                return;
//                            }
//                            marker.setPosition(newPos);
//                            marker.setAnchor(0.5f, 0.5f);
//                            marker.setRotation(getBearing(startPosition, newPos));
//                            gMap.moveCamera(CameraUpdateFactory
//                                    .newCameraPosition
//                                            (new CameraPosition.Builder()
//                                                    .target(newPos)
//                                                    .zoom(15.5f)
//                                                    .build()));
//                        }
//                    });
//                    valueAnimator.start();
//                    handler.postDelayed(this, 300);
//                }
//            }, 300);


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void drawLine(List<LatLng> points) {
        if (points == null) {
            Log.e("Draw Line", "got null as parameters");
            return;
        }
        int i = 0;
        for (LatLng latLng : points) {
            //  gMap.addMarker(new MarkerOptions().position(latLng).title("Location Point " + i));
            i++;
        }

        PolylineOptions rectLine = new PolylineOptions().width(8).color(Color.rgb(64, 180, 229));// RGB code: R: 87 G: 161 B:
        if (points != null && points.size() > 0) {

            for (int j = 0; j < points.size(); j++) {
                rectLine.add(points.get(j));
            }
        }
        // Adding route on the map
        //   gMap.addPolyline(rectLine);

    }



    @Override
    public void onGpsPointReceived(Location location) {
        if (location == null) {
            return;
        }

        this.myLocation = location;
        markerPoints.add(new LatLng(myLocation.getLatitude(),myLocation.getLongitude()));
        markerPoints.add(new LatLng(myLocation.getLatitude(),myLocation.getLongitude()));
        mMapView.getMapAsync(this);
        GpsService.getInstance().removeGpsListener(this);



    }

    @Override
    public void onError(String error) {

    }

    public boolean isLocationOn() {
        return isLocationEnabled(getActivity());
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @SuppressWarnings("deprecation")
    public static boolean isLocationEnabled(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            String providers = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            if (TextUtils.isEmpty(providers)) {
                return false;
            }
            return providers.contains(LocationManager.GPS_PROVIDER);
        } else {
            final int locationMode;
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            switch (locationMode) {
                case Settings.Secure.LOCATION_MODE_BATTERY_SAVING:
                case Settings.Secure.LOCATION_MODE_HIGH_ACCURACY:
                case Settings.Secure.LOCATION_MODE_SENSORS_ONLY:
                    return true;
                case Settings.Secure.LOCATION_MODE_OFF:
                default:
                    return false;
            }
        }
    }

    private void checkAndStartGPSListener() {
        if (!isLocationOn()) {
            Toast.makeText(getActivity(), "Please turn on Location under settings", Toast.LENGTH_SHORT).show();
            return;
        }
        GpsService.getInstance().addGpsListener(this);
        Toast.makeText(getActivity(), "Fetching location", Toast.LENGTH_SHORT).show();
    }
    LatLngBounds.Builder builder;
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fif_location_off_view :
                if (PermissionHelper.requestPermission(getActivity(), PermissionHelper.REQUEST_GPS_PERMISSION, R.string.permission_gps_explain,
                        R.string.permission_gps_blocked, Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    if (!FrameworkUtils.isLocationEnabled(getActivity())) {
                        GpsService.getInstance().addGpsListener(MapFragment.this);
                    }
                }
                break;

            case R.id.wzat_my_location_btn :
                if (myLocation == null) {
                    checkAndStartGPSListener();
                    break;
                }else{
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), 13);
                    gMap.animateCamera(cameraUpdate);
                }
                break;
        }

    }

    public List<List<HashMap<String,String>>> parse(JSONObject jObject){

        List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String,String>>>();
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;

        try {

            jRoutes = jObject.getJSONArray("routes");
            List<Step> stepList = new ArrayList<>();
            /** Traversing all routes */
            for(int i=0;i<jRoutes.length();i++){
                jLegs = ( (JSONObject)jRoutes.get(i)).getJSONArray("legs");
                List path = new ArrayList<HashMap<String, String>>();

                /** Traversing all legs */
                for(int j=0;j<jLegs.length();j++){
                    jSteps = ( (JSONObject)jLegs.get(j)).getJSONArray("steps");


                    /** Traversing all steps */
                    for(int k=0;k<jSteps.length();k++){
                        JSONObject jsonObject=((JSONObject)jSteps.get(k)).getJSONObject("start_location");
                        String s_lat=jsonObject.getString("lat");
                        String s_long = jsonObject.getString("lng");
                        Step step = new Step(s_lat,s_long,false);
                        stepList.add(step);
                        if(k == jSteps.length()-1&& j==jLegs.length()-1){
                            JSONObject end=((JSONObject)jSteps.get(k)).getJSONObject("end_location");
                            String end_lat=end.getString("lat");
                            String end_long = end.getString("lng");
                            Step end_step = new Step(end_lat,end_long,false);
                            stepList.add(end_step);
                        }
                        String polyline = "";
                        polyline = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("polyline")).get("points");
                        polyLineList = decodePoly(polyline);
                        builder = new LatLngBounds.Builder();

                        /** Traversing all points */
                        for(int l=0;l<polyLineList.size();l++){
                            builder.include(polyLineList.get(i));
                            HashMap<String, String> hm = new HashMap<String, String>();
                            hm.put("lat", Double.toString(((LatLng)polyLineList.get(l)).latitude) );
                            hm.put("lng", Double.toString(((LatLng)polyLineList.get(l)).longitude) );
                            path.add(hm);
                        }

                    }

                    routes.add(path);
                }
            }
            Process process = new Process("sds",stepList);
            Gson gson = new Gson();
            String json = gson.toJson(process);
            Log.d("test","json  is :"+ json.toString());
            String sdfds= json;
        } catch (JSONException e) {
            e.printStackTrace();
        }catch (Exception e){
        }
        return routes;
    }



    private float getBearing(LatLng begin, LatLng end) {
        double lat = Math.abs(begin.latitude - end.latitude);
        double lng = Math.abs(begin.longitude - end.longitude);

        if (begin.latitude < end.latitude && begin.longitude < end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)));
        else if (begin.latitude >= end.latitude && begin.longitude < end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
        else if (begin.latitude < end.latitude && begin.longitude >= end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
        return -1;
    }


    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }



    private void sendPostRequest(String postBody) {
        sendNetworkRequest(new FrameworkUtils.AllStateVolleyCallBack() {
            @Override
            public void onResponse(JsonArray jsonArray) {
                hideProgressBar();

            }

            @Override
            public void onResponse(JsonObject object) {

            }

            @Override
            public void onResponse(String response) {
                hideProgressBar();
                if ("true".equalsIgnoreCase(response)) {

                }
            }

            @Override
            public void onError(VolleyError error) {
                hideProgressBar();
                Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
            }
        }, postBody);
    }

    public void sendNetworkRequest(@NonNull final FrameworkUtils.AllStateVolleyCallBack callBack, final String postBody) {


        if (!FrameworkUtils.isDataConnectionOn()) {
            Toast.makeText(getContext(), "Bummer! No internet connection.", Toast.LENGTH_LONG).show();
            return;
        }

      //  showProgressBar("Authenticating");
        String sessionURL = "http://10.19.0.221:9000/simulation";
        try {
            Log.d("test","post sim :"+ sessionURL);
            StringRequest request = new StringRequest(Request.Method.POST, sessionURL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("AllStateSessionRequest", "session request" + response);
                    try {
                        if (response == null) {
                            return;
                        }
                        Log.d("test","post sim reponse is :"+ response);
                        callBack.onResponse(response);
                    } catch (Exception e) {
                        Log.e("AllStateSessionRequest", "Error parsing response", e);
                        callBack.onError(new VolleyError("Invalid Response, " + e.getMessage()));

                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    callBack.onError(new VolleyError(error.getMessage()));
                }
            }) {

                @Override
                public String getBodyContentType() {
                    return "application/json";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        Log.d("test","post body is :"+ postBody);
                        return postBody == null ? null : postBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        callBack.onError(new VolleyError(uee.getMessage()));
                        return null;
                    }
                }
            };
            FrameworkUtils.getVolleyRequestQueue().add(request);
        } catch (Exception e) {
            Log.e("AllStateSessionRequest", "Exception while creating session", e);
            callBack.onError(new VolleyError(e.getMessage()));
        }
    }
}

package com.example.android.fleetdemo.Route;

/**
 * Created by Azuga on 06-03-2018.
 */

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.animation.LinearInterpolator;

import com.example.android.fleetdemo.POJO.Process;
import com.example.android.fleetdemo.POJO.Step;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



public class DirectionsJSONParser {

    /** Receives a JSONObject and returns a list of lists containing latitude and longitude */
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
                        List<LatLng> list = decodePoly(polyline);

                        /** Traversing all points */
                        for(int l=0;l<list.size();l++){
                            HashMap<String, String> hm = new HashMap<String, String>();
                            hm.put("lat", Double.toString(((LatLng)list.get(l)).latitude) );
                            hm.put("lng", Double.toString(((LatLng)list.get(l)).longitude) );
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
    private PolylineOptions polylineOptions, blackPolylineOptions;
    private Polyline blackPolyline, greyPolyLine;


    /**
     * Method to decode polyline points
     * Courtesy : jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     * */
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
}
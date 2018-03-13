package com.example.android.fleetdemo.Route;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Azuga on 06-03-2018.
 */

public class WayPointJsonParser {

    public List<String> parse(JsonObject jObject) throws JSONException {
        List<String> waypoints = new ArrayList<>();
        JsonArray routesArray = jObject.getAsJsonArray("routes");
        if(routesArray != null && routesArray.size()>0){
            JsonObject routeObject = routesArray.get(0).getAsJsonObject();
            JsonArray waypointOrderArray =routeObject.getAsJsonArray("waypoint_order");
            if(waypointOrderArray != null && waypointOrderArray.size()>0){
                for(int i=0;i<waypointOrderArray.size();i++){
                    waypoints.add(waypointOrderArray.get(i).getAsString());
                }
            }
        }
        return waypoints;
    }
}

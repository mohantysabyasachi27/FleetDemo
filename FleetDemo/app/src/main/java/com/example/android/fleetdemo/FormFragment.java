package com.example.android.fleetdemo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.android.fleetdemo.Adapter.FormFieldAdapter;
import com.example.android.fleetdemo.POJO.Field;
import com.example.android.fleetdemo.POJO.LatLongDescp;
import com.example.android.fleetdemo.POJO.UserTask;
import com.example.android.fleetdemo.database.DatabaseService;
import com.example.android.fleetdemo.framework.BaseFragment;
import com.example.android.fleetdemo.framework.UIService;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mikepenz.itemanimators.SlideRightAlphaAnimator;
import com.example.android.fleetdemo.database.DatabaseService;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Azuga on 27-02-2018.
 */

public class FormFragment extends BaseFragment implements View.OnClickListener {

    RecyclerView form_list;
    String task_Id;
    private DialogWidget progressDialog;
    TextView submit_button;
    boolean isError = false;
    public static FormFragment getInstance(String id) {
        FormFragment formFragment = new FormFragment();
        Bundle bundle = new Bundle();
        bundle.putString("ID", id);
        formFragment.setArguments(bundle);
        return formFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.form_fragment, container, false);
        return view;
    }

    @Override
    protected String getFragmentDisplayName() {
        return null;
    }

    @Override
    public void refreshData() {

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null) {

            if (getArguments() != null) {
                task_Id = getArguments().getString("ID");
            }
        }

        form_list = view.findViewById(R.id.form_list);
        submit_button = view.findViewById(R.id.submit_button);
        submit_button.setOnClickListener(this);
        form_list.setLayoutManager(new LinearLayoutManager(getContext()));
        makeNetWorkRequest();
    }

    private void makeNetWorkRequest() {
        requestCreateSession(new FrameworkUtils.AllStateVolleyCallBack() {
            @Override
            public void onResponse(JsonArray jsonArray) {
                isError = false;
            }

            @Override
            public void onResponse(JsonObject object) {
                hideProgressBar();
                parseResponse(object);
                isError = false;
            }

            @Override
            public void onResponse(String response) {
                hideProgressBar();
                isError = false;

            }

            @Override
            public void onError(VolleyError error) {
                hideProgressBar();
                isError = true;
                Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    List<Field> formFieldList;
    ArrayList<LatLongDescp> waypoints;
    private void parseResponse(JsonObject jsonObject) {

        if (jsonObject == null) {
            return;
        }
        formFieldList = new ArrayList<>();
        waypoints = new ArrayList<>();
        if (jsonObject.get("variables") != null) {
            JsonArray jsonFieldArray = jsonObject.get("variables").getAsJsonArray();
            int size = jsonFieldArray.size();
            for (int i = 0; i < size; i++) {
                JsonObject fieldJsonObject = jsonFieldArray.get(i).getAsJsonObject();
                Field field = new Field();
                if (fieldJsonObject.get("name").isJsonNull()) {
                    field.setName("");
                } else {
                    field.setName(fieldJsonObject.get("name").getAsString());
                }
                if (fieldJsonObject.get("variableType").isJsonNull()) {
                    field.setVariableType("");
                } else {
                    field.setVariableType(fieldJsonObject.get("variableType").getAsString());
                }
                if (fieldJsonObject.get("id").isJsonNull()) {
                    field.setId("");
                } else {
                    field.setId(fieldJsonObject.get("id").getAsString());
                }
                if (fieldJsonObject.get("defaultValue").isJsonNull()) {
                    field.setDefaultValue("");
                } else {
                    field.setDefaultValue(fieldJsonObject.get("defaultValue").getAsString());
                }
                formFieldList.add(field);
            }

        }

        if (jsonObject.get("locations") != null) {
            AzugaPreferences.getInstance(getContext()).isArrayLocationFound(true);
            JsonArray jsonFieldArray = jsonObject.get("locations").getAsJsonArray();
            int size = jsonFieldArray.size();
            for (int i = 0; i < size; i++) {
                JsonObject fieldJsonObject = jsonFieldArray.get(i).getAsJsonObject();
                LatLongDescp field = new LatLongDescp();
                if (fieldJsonObject.get("latitude").isJsonNull()) {
                    field.setLatitude(-1);
                } else {
                    field.setLatitude(fieldJsonObject.get("latitude").getAsDouble());
                }
                if (fieldJsonObject.get("longitute").isJsonNull()) {
                    field.setLogitude(-1);
                } else {
                    field.setLogitude(fieldJsonObject.get("longitute").getAsDouble());
                }
                if (fieldJsonObject.get("name").isJsonNull()) {
                    field.setName("");
                } else {
                    field.setName(fieldJsonObject.get("name").getAsString());
                }

                waypoints.add(field);
            }

        }else{
            AzugaPreferences.getInstance(getContext()).isArrayLocationFound(false);
        }
        FormFieldAdapter formFieldAdapter = new FormFieldAdapter(formFieldList);
        form_list.setItemAnimator(new SlideRightAlphaAnimator());
        form_list.setAdapter(formFieldAdapter);


    }

    public void requestCreateSession(@NonNull final FrameworkUtils.AllStateVolleyCallBack callBack) {
        if (FrameworkUtils.isEmptyOrWhitespace(task_Id)) {
            Toast.makeText(getContext(), "Task Id cannot be empty", Toast.LENGTH_LONG).show();
            return;
        }

        if (!FrameworkUtils.isDataConnectionOn()) {
            Toast.makeText(getContext(), "Bummer! No internet connection.", Toast.LENGTH_LONG).show();
            return;
        }

        showProgressBar("Authenticating");
        String sessionURL = "http://10.19.0.221:8900/workflow/tasks/" + task_Id + "/variable";
        try {

            StringRequest request = new StringRequest(Request.Method.GET, sessionURL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("test", "session request" + response);
                    try {
                        if (response == null) {
                            return;
                        }
                        callBack.onResponse(new JsonParser().parse(response).getAsJsonObject());
                    } catch (Exception e) {
                        Log.e("test", "Error parsing response", e);
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
            Log.e("test", "Exception while creating session", e);
            callBack.onError(new VolleyError(e.getMessage()));
        }
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.submit_button:
                if(isError){
                    return;
                }
                JsonObject jsonObject = new JsonObject();


                String postJson = "{\"variables\":{";
                int list_size = formFieldList.size();
                String abc = "";
                for (int a = 0; a < list_size; a++) {
                    if ("String".equalsIgnoreCase(formFieldList.get(a).getVariableType())) {
                        FormFieldAdapter.MyEditTextViewHolder viewHolder = (FormFieldAdapter.MyEditTextViewHolder) form_list.findViewHolderForAdapterPosition(a);
                        Log.d("test", viewHolder.field_value.getText().toString());
                        abc = abc + "\"" + viewHolder.field_title.getText() + "\":{\"value\":" + "\"" + viewHolder.field_value.getText().toString() + "\"}";
                        if (a == list_size - 1) {
                            abc = abc + "}}";
                        } else {
                            abc = abc + ",";
                        }

                    } else if ("Boolean".equalsIgnoreCase(formFieldList.get(a).getVariableType())) {
                        FormFieldAdapter.MyCheckBoxViewHolder viewHolder = (FormFieldAdapter.MyCheckBoxViewHolder) form_list.findViewHolderForAdapterPosition(a);
                        Log.d("test", viewHolder.field_value.getText().toString());
                        abc = abc + "\"" + viewHolder.field_title.getText() + "\":{\"value\":" + "\"" + String.valueOf(viewHolder.field_value.isChecked()) + "\"}";
                        if (a == list_size - 1) {
                            abc = abc + "}}";
                        } else {
                            abc = abc + ",";
                        }
                    }
                }

                Log.d("test", "json string is :" + postJson + abc);
                String postBody = postJson + abc;
                sendPostRequest(postBody);
                break;
        }
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
                    if(AzugaPreferences.getInstance(getContext()).isArrayLocationFound()){
                        MapFragment mapFragment = MapFragment.getInstance(waypoints);
                        UIService.getInstance().addFragment(mapFragment,false);
                    }else{
                         DatabaseService.getInstance().clearDatabase();
                          TaskFragment taskFragment = TaskFragment.getInstance(  AzugaPreferences.getInstance(getContext()).getLoggedInUser());
                          UIService.getInstance().addFragment(taskFragment,false);

                    }

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

        DatabaseService databaseService;
        databaseService = DatabaseService.getInstance();

        String whereClause = "id "+"=\"" + task_Id + "\"";
        List<UserTask> task = databaseService.read(UserTask.class, whereClause);

        if (!FrameworkUtils.isDataConnectionOn()) {
            Toast.makeText(getContext(), "Bummer! No internet connection.", Toast.LENGTH_LONG).show();
            return;
        }

        UserTask t = task.get(0);
        showProgressBar("Authenticating");
        String sessionURL = "http://10.19.0.221:8900/workflow/tasks/" + task_Id + "/"+ t.processInstanceId;


        try {

            StringRequest request = new StringRequest(Request.Method.POST, sessionURL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("AllStateSessionRequest", "session request" + response);
                    try {
                        if (response == null) {
                            return;
                        }
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

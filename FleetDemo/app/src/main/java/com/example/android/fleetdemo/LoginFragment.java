package com.example.android.fleetdemo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.android.fleetdemo.POJO.UserTask;
import com.example.android.fleetdemo.database.DatabaseService;
import com.example.android.fleetdemo.framework.BaseFragment;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Azuga on 27-02-2018.
 */

public class LoginFragment extends BaseFragment implements View.OnClickListener {
    private DialogWidget progressDialog;

    TextView login_signin_button;
    EditText login_usr_field;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       // final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.AppFullScreenTheme);

        // clone the inflater using the ContextThemeWrapper
        //LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        View view = inflater.inflate(R.layout.fragment_login,container,false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
    }
    @Override
    public void onStop() {
        super.onStop();
        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
    }

    @Override
    protected String getFragmentDisplayName() {
        return "FLEET";
    }

    @Override
    public void refreshData() {

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        login_signin_button = view.findViewById(R.id.login_signin_button);
        login_signin_button.setOnClickListener(this);
        login_usr_field = view.findViewById(R.id.login_usr_field);
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
        switch (v.getId()){
            case R.id.login_signin_button :

                requestCreateSession(new FrameworkUtils.AllStateVolleyCallBack() {
                    @Override
                    public void onResponse(JsonArray jsonArray) {
                        hideProgressBar();
                        parseResponse(jsonArray);
                        TaskFragment taskFragment = TaskFragment.getInstance(login_usr_field.getText().toString());
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container,taskFragment).commit();
                        AzugaPreferences.getInstance(getContext()).setUserLogged(login_usr_field.getText().toString());
                    }

                    @Override
                    public void onResponse(JsonObject object) {

                    }

                    @Override
                    public void onResponse(String response) {

                    }

                    @Override
                    public void onError(VolleyError error) {
                        Toast.makeText(getContext(),"Error",Toast.LENGTH_SHORT).show();
                        hideProgressBar();
                    }
                });
                break;
        }
    }

    private void parseResponse(JsonArray jsonArray) {
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<UserTask>>() {}.getType();
        List<UserTask> userTaskList = null;
        if(jsonArray!= null && jsonArray instanceof JsonArray) {
            userTaskList = gson.fromJson(jsonArray, listType);
        }
        if (userTaskList != null && !userTaskList.isEmpty()) {
            DatabaseService databaseService = DatabaseService.getInstance();
            for (UserTask userTask : userTaskList) {
                databaseService.insertOrUpdate(userTask);
            }
        }
    }

    public  void requestCreateSession(@NonNull final FrameworkUtils.AllStateVolleyCallBack callBack) {
        String enteredUserId = login_usr_field.getText().toString();
        if(FrameworkUtils.isEmptyOrWhitespace(enteredUserId)){
            Toast.makeText(getContext(),"Username cannot be blank",Toast.LENGTH_LONG).show();
            return;
        }

        if (!FrameworkUtils.isDataConnectionOn()) {
            Toast.makeText(getContext(),"Bummer! No internet connection.",Toast.LENGTH_LONG).show();
            return;
        }

        showProgressBar("Authenticating");
        String sessionURL = "http://10.19.0.221:8900/workflow/"+login_usr_field.getText().toString().trim()+"/tasks";
        try {

            StringRequest request = new StringRequest(Request.Method.GET, sessionURL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("AllStateSessionRequest", "session request" + response);
                    try {
                        if(response == null){
                            return;
                        }
                        callBack.onResponse(new JsonParser().parse(response).getAsJsonArray());
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
}

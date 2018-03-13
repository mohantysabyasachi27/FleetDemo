package com.example.android.fleetdemo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.android.fleetdemo.Adapter.ClickListener;
import com.example.android.fleetdemo.Adapter.RecyclerTouchListener;
import com.example.android.fleetdemo.Adapter.TaskListAdapter;
import com.example.android.fleetdemo.POJO.UserTask;
import com.example.android.fleetdemo.database.DatabaseService;
import com.example.android.fleetdemo.database.DatabaseTablesEnum;
import com.example.android.fleetdemo.framework.BaseFragment;
import com.example.android.fleetdemo.framework.UIService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.mikepenz.itemanimators.ScaleUpAnimator;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Azuga on 27-02-2018.
 */

public class TaskFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    RecyclerView task_list;
    List<UserTask> userTaskList;
    TextView no_tasks;
    SwipeRefreshLayout task_swipeRefreshLayout;
    boolean isEmpty = false;
    String userId;
    TaskListAdapter taskListAdapter;
    private DialogWidget progressDialog;

    public static TaskFragment getInstance(String userId) {
        Bundle bundle = new Bundle();
        bundle.putString("USER_ID", userId);
        TaskFragment taskFragment = new TaskFragment();
        taskFragment.setArguments(bundle);
        return taskFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task, container, false);
        return view;
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            if (getArguments() != null) {
                userId = getArguments().getString("USER_ID");
            }
        }
        no_tasks = view.findViewById(R.id.no_tasks);
        userId = AzugaPreferences.getInstance(getContext()).getLoggedInUser();
        AzugaPreferences.getInstance(getContext()).setIsUserLoggedIn(true);
        task_list = (RecyclerView) view.findViewById(R.id.task_list);
        task_swipeRefreshLayout = view.findViewById(R.id.task_swipeRefreshLayout);
        task_swipeRefreshLayout.setOnRefreshListener(this);
        task_swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        task_list.setLayoutManager(new LinearLayoutManager(getContext()));
        task_list.setItemAnimator(new ScaleUpAnimator());

        task_list.addOnItemTouchListener(new RecyclerTouchListener(getContext(),
                task_list, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                FormFragment formFragment = FormFragment.getInstance(userTaskList.get(position).id);
                UIService.getInstance().addFragment(formFragment,true);
               // getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, formFragment).addToBackStack("task").commit();
            }

            @Override
            public void onLongClick(View view, int position) {

            }

        }));

    }

    @Override
    public void onResume() {
        super.onResume();
        makeNetworkRequesy();

    }

    @Override
    protected String getFragmentDisplayName() {
        return "TASK LIST";
    }

    @Override
    public void refreshData() {

    }

    public void makeNetworkRequesy() {
        requestCreateSession(new FrameworkUtils.AllStateVolleyCallBack() {
            @Override
            public void onResponse(JsonArray jsonArray) {
                hideProgressBar();
                task_swipeRefreshLayout.setRefreshing(false);
                parseResponse(jsonArray);
                if (isEmpty) {
                    no_tasks.setVisibility(View.VISIBLE);
                    task_list.setVisibility(View.GONE);
                } else {
                    userTaskList = FrameworkUtils.retrieveInfoFromDB(UserTask.class, DatabaseTablesEnum.USER_TASK, null);
                    taskListAdapter = new TaskListAdapter(userTaskList);
                    no_tasks.setVisibility(View.GONE);
                    task_list.setVisibility(View.VISIBLE);
                    task_list.setAdapter(taskListAdapter);


                }

            }

            @Override
            public void onResponse(JsonObject object) {

            }

            @Override
            public void onResponse(String response) {

            }

            @Override
            public void onError(VolleyError error) {
                Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                hideProgressBar();
            }
        });
    }

    private void parseResponse(JsonArray jsonArray) {
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<UserTask>>() {
        }.getType();
        List<UserTask> userTaskList = null;
        if (jsonArray != null && jsonArray instanceof JsonArray) {
            userTaskList = gson.fromJson(jsonArray, listType);
        }
        if (userTaskList != null && !userTaskList.isEmpty()) {
            DatabaseService databaseService = DatabaseService.getInstance();
            for (UserTask userTask : userTaskList) {
                databaseService.insertOrUpdate(userTask);
            }
            isEmpty = false;
        } else {
            isEmpty = true;
        }
    }

    public void requestCreateSession(@NonNull final FrameworkUtils.AllStateVolleyCallBack callBack) {
        String enteredUserId = userId;//login_usr_field.getText().toString();
        if (FrameworkUtils.isEmptyOrWhitespace(enteredUserId)) {
            Toast.makeText(getContext(), "Username cannot be blank", Toast.LENGTH_LONG).show();
            return;
        }

        if (!FrameworkUtils.isDataConnectionOn()) {
            Toast.makeText(getContext(), "Bummer! No internet connection.", Toast.LENGTH_LONG).show();
            return;
        }

        showProgressBar("Authenticating");
        String sessionURL = "http://10.19.0.221:8900/workflow/" + userId + "/tasks";
        try {

            StringRequest request = new StringRequest(Request.Method.GET, sessionURL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("AllStateSessionRequest", "session request" + response);
                    try {
                        if (response == null) {
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

    @Override
    public void onRefresh() {
        DatabaseService.getInstance().clearDatabase();
        makeNetworkRequesy();
    }
}


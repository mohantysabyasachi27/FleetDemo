package com.example.android.fleetdemo.Adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.fleetdemo.POJO.UserTask;
import com.example.android.fleetdemo.R;

import java.util.List;

/**
 * Created by Azuga on 27-02-2018.
 */

public class TaskListAdapter extends  RecyclerView.Adapter<TaskListAdapter.MyViewHolder>{

    private List<UserTask> userTaskList;
    public TaskListAdapter(List<UserTask> userTaskList) {
        this.userTaskList = userTaskList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        UserTask userTask = userTaskList.get(position);
        holder.title.setText(userTask.name);
    }

    @Override
    public int getItemCount() {
        return userTaskList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.task_title);
        }
    }


}




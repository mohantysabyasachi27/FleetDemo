package com.example.android.fleetdemo.Adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.example.android.fleetdemo.POJO.Field;
import com.example.android.fleetdemo.R;

import java.util.List;

/**
 * Created by Azuga on 28-02-2018.
 */

public class FormFieldAdapter extends  RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Field> formFieldList;
    private String[] arrTemp;
    private final int EDIT_TEXT = 0, CHECKBOX = 1;
    public FormFieldAdapter(List<Field> formFieldList) {
        this.formFieldList = formFieldList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        RecyclerView.ViewHolder viewHolder=null;
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

        switch (viewType) {
            case EDIT_TEXT:
                View editTextVH = inflater.inflate(R.layout.form_list_row_edit_text, viewGroup, false);
                viewHolder = new MyEditTextViewHolder(editTextVH);
                break;
            case CHECKBOX:
                View checkBoXVH = inflater.inflate(R.layout.form_list_row_checkbox, viewGroup, false);
                viewHolder = new MyCheckBoxViewHolder(checkBoXVH);
                break;

        }
        return viewHolder;
    }
    //Returns the view type of the item at position for the purposes of view recycling.
    @Override
    public int getItemViewType(int position) {
        if ("String".equalsIgnoreCase(formFieldList.get(position).getVariableType())) {
            return EDIT_TEXT;
        } else if ("Boolean".equalsIgnoreCase(formFieldList.get(position).getVariableType())) {
            return CHECKBOX;
        }
        return -1;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case EDIT_TEXT:
                MyEditTextViewHolder vh1 = (MyEditTextViewHolder) holder;
                configureEditTextVH(vh1, position);
                break;
            case CHECKBOX:
                MyCheckBoxViewHolder vh2 = (MyCheckBoxViewHolder) holder;
                configureCheckBoxVH(vh2, position);
                break;
        }
    }

    private void configureCheckBoxVH(MyCheckBoxViewHolder vh2, int position) {
        vh2.field_title.setText(formFieldList.get(position).getName());
        String default_value = formFieldList.get(position).getDefaultValue();
        if(default_value == null){
            vh2.field_value.setChecked(false);
        }else if("true".equalsIgnoreCase(default_value)){
            vh2.field_value.setChecked(true);
        }else{
            vh2.field_value.setChecked(false);
        }
    }

    private void configureEditTextVH(MyEditTextViewHolder vh1, int position) {
        vh1.field_title.setText(formFieldList.get(position).getName());
        String default_value = formFieldList.get(position).getDefaultValue();
        vh1.field_value.setText(default_value==null?"":default_value);

    }

    @Override
    public int getItemCount() {
        return formFieldList.size();
    }

    public class MyEditTextViewHolder extends RecyclerView.ViewHolder {
        public TextView field_title;
        public EditText field_value;

        public MyEditTextViewHolder(View view) {
            super(view);
            field_value = view.findViewById(R.id.field_value);
            field_title = (TextView) view.findViewById(R.id.field_title);
        }
    }

    public class MyCheckBoxViewHolder extends RecyclerView.ViewHolder {
        public TextView field_title;
        public CheckBox field_value;

        public MyCheckBoxViewHolder(View view) {
            super(view);
            field_value = view.findViewById(R.id.field_value);
            field_title = (TextView) view.findViewById(R.id.field_title);
        }
    }
}


package com.example.android.fleetdemo.POJO;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FormField {

    @SerializedName("field")
    @Expose
    private List<Field> field = null;

    public List<Field> getField() {
        return field;
    }

    @Override
    public String toString() {
        return "FormField{" +
                "field=" + field +
                '}';
    }

    public void setField(List<Field> field) {
        this.field = field;
    }



}

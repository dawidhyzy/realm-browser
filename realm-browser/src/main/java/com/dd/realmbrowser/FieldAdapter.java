package com.dd.realmbrowser;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author Dawid Hyży <dawid.hyzy@seedlabs.io>
 * @since 22/09/15.
 */
public class FieldAdapter extends ArrayAdapter<Field>{
    public FieldAdapter(Context context, int resource, List<Field> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = super.getView(position, convertView, parent);
        ((TextView)convertView.findViewById(android.R.id.text1)).setText(getItem(position).getName());
        return convertView;

    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        convertView = super.getDropDownView(position, convertView, parent);
        ((TextView)convertView.findViewById(android.R.id.text1)).setText(getItem(position).getName());
        return convertView;
    }
}

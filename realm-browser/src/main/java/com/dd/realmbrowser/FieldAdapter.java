package com.dd.realmbrowser;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.dd.realmbrowser.utils.MagicUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;

/**
 * @author Dawid Hyży <dawid.hyzy@seedlabs.io>
 * @since 25/09/15.
 */
public class FieldAdapter extends RecyclerView.Adapter<FieldAdapter.ViewHolder> {

    Realm realm;
    RealmObject realmObject;
    List<Field> fields = new ArrayList<>(0);

    private Listener mListener;

    public FieldAdapter(Realm realm, RealmObject realmObject){
        this.realm = realm;
        this.realmObject = realmObject;
        this.fields.addAll(Arrays.asList(realmObject.getClass().getSuperclass().getDeclaredFields()));
    }

    public void setListener(Listener mListener) {
        this.mListener = mListener;
    }

    public interface Listener {
        void onRealmObjectSelected(@NonNull RealmObject realmObject);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_field, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.itemView.setBackgroundColor(position % 2 == 0 ?
                holder.itemView.getContext().getResources().getColor(R.color.rb_grey) :
                holder.itemView.getContext().getResources().getColor(R.color.rb_white));

        Field field = fields.get(position);
        if (MagicUtils.isParameterizedField(field)) {
            holder.value.setText(MagicUtils.createParameterizedName(field));
            holder.value.setEnabled(false);
            holder.value.addTextChangedListener(emptyTextWatcher);
        }else if(field.getType() != String.class
                && field.getType() != long.class
                && field.getType() != int.class
                && field.getType() != boolean.class
                && field.getType() != double.class){
            String methodName = MagicUtils.createGetterMethodName(field);
            holder.value.setText(MagicUtils.invokeGetterMethod(realmObject, methodName));
            holder.value.setEnabled(false);
            holder.value.addTextChangedListener(emptyTextWatcher);
            RealmObject nestedRealmObject = MagicUtils.invokeRealmObjectGetterMethod(realmObject, methodName);
            holder.itemView.setOnClickListener(createRealmObjectClickListener(nestedRealmObject));

        }else{
            String methodName = MagicUtils.createGetterMethodName(field);
            holder.value.setText(MagicUtils.invokeGetterMethod(realmObject, methodName));
            holder.value.setEnabled(true);
            holder.value.addTextChangedListener(createTextChangeListener(field));
        }
        holder.name.setText(field.getName());
    }

    private TextWatcher createTextChangeListener(Field field){
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                realm.beginTransaction();
                String methodName = MagicUtils.createSetterMethodName(field);

                String text = s.toString();

                if (text.length() == 0) {
                    realm.cancelTransaction();
                    return;
                }

                if (field.getType() == String.class) {
                    MagicUtils.invokeSetterMethod(realmObject, methodName, text);
                } else if (field.getType() == long.class) {
                    try {
                        MagicUtils.invokeSetterMethod(realmObject, methodName, Long.parseLong(text));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                } else if (field.getType() == int.class) {
                    try {
                        MagicUtils.invokeSetterMethod(realmObject, methodName, Integer.parseInt(text));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                } else if (field.getType() == double.class) {
                    try {
                        MagicUtils.invokeSetterMethod(realmObject, methodName, Double.parseDouble(text));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                } else if (field.getType() == boolean.class) {
                    MagicUtils.invokeSetterMethod(realmObject, methodName, Boolean.parseBoolean(text));
                }
                realm.commitTransaction();
            }
        };
    }

    private View.OnClickListener createRealmObjectClickListener(@NonNull final RealmObject realmObject) {
        return v -> {
            if(mListener != null) {
                mListener.onRealmObjectSelected(realmObject);
            }
        };
    }

    TextWatcher emptyTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @Override
    public int getItemCount() {
        return fields.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        EditText value;
        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            value = (EditText) itemView.findViewById(R.id.value);
        }
    }
}

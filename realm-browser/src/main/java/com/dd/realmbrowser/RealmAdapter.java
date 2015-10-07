package com.dd.realmbrowser;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.dd.realmbrowser.model.RealmPreferences;
import com.dd.realmbrowser.utils.MagicUtils;
import io.realm.RealmObject;

import java.lang.reflect.Field;
import java.util.AbstractList;
import java.util.List;

class RealmAdapter extends RecyclerView.Adapter<RealmAdapter.ViewHolder> {

    public interface Listener {
        void onRealmListSelected(@NonNull RealmObject realmObject, @NonNull Field field);
        void onRealmObjectSelected(@NonNull RealmObject realmObject);
    }

    private AbstractList<? extends RealmObject> mRealmObjects;
    private Context mContext;
    private List<Field> mFieldList;
    private Listener mListener;
    private RealmPreferences mRealmPreferences;

    public RealmAdapter(@NonNull Context context, @NonNull AbstractList<? extends RealmObject> realmObjects,
                        @NonNull List<Field> fieldList, @NonNull Listener listener) {
        mRealmPreferences = new RealmPreferences(context);
        mContext = context;
        mRealmObjects = realmObjects;
        mFieldList = fieldList;
        mListener = listener;
    }

    public void setFieldList(List<Field> fieldList) {
        mFieldList = fieldList;
    }

    @Override
    public RealmAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_realm_browser, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public int getItemCount() {
        return mRealmObjects.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.itemView.setBackgroundColor(position % 2 == 0 ?
                mContext.getResources().getColor(R.color.rb_grey) :
                mContext.getResources().getColor(R.color.rb_white));

        if (mFieldList.isEmpty()) {
            holder.txtIndex.setText(null);
            holder.txtColumn1.setText(null);
            holder.txtColumn2.setText(null);
            holder.txtColumn3.setText(null);
        } else {
            holder.txtIndex.setText(String.valueOf(position));

            RealmObject realmObject = mRealmObjects.get(position);
            initRowWeight(holder);
            initRowTextWrapping(holder);

            if (mFieldList.size() == 1) {
                initRowText(holder.txtColumn1, holder.parent, realmObject, mFieldList.get(0));
                holder.txtColumn2.setText(null);
                holder.txtColumn3.setText(null);
            } else if (mFieldList.size() == 2) {
                initRowText(holder.txtColumn1, holder.parent, realmObject, mFieldList.get(0));
                initRowText(holder.txtColumn2, holder.parent, realmObject, mFieldList.get(1));
                holder.txtColumn3.setText(null);
            }  else if (mFieldList.size() == 3) {
                initRowText(holder.txtColumn1, holder.parent, realmObject, mFieldList.get(0));
                initRowText(holder.txtColumn2, holder.parent, realmObject, mFieldList.get(1));
                initRowText(holder.txtColumn3, holder.parent, realmObject, mFieldList.get(2));
            }

        }
    }

    private void initRowTextWrapping(ViewHolder holder) {
        boolean shouldWrapText = mRealmPreferences.shouldWrapText();
        holder.txtColumn1.setSingleLine(!shouldWrapText);
        holder.txtColumn2.setSingleLine(!shouldWrapText);
        holder.txtColumn3.setSingleLine(!shouldWrapText);
    }

    private void initRowWeight(ViewHolder holder) {
        LinearLayout.LayoutParams layoutParams2 = createLayoutParams();
        LinearLayout.LayoutParams layoutParams3 = createLayoutParams();

        if (mFieldList.size() == 1) {
            layoutParams2.weight = 0;
            layoutParams3.weight = 0;
        } else if (mFieldList.size() == 2) {
            layoutParams2.weight = 1;
            layoutParams3.weight = 0;
        }  else if (mFieldList.size() == 3) {
            layoutParams2.weight = 1;
            layoutParams3.weight = 1;
        }
        holder.txtColumn2.setLayoutParams(layoutParams2);
        holder.txtColumn3.setLayoutParams(layoutParams3);
    }

    private void initRowText(TextView txtColumn, LinearLayout parent, RealmObject realmObject, Field field) {
        if (MagicUtils.isParameterizedField(field)) {
            txtColumn.setText(MagicUtils.createParameterizedName(field));
            txtColumn.setOnClickListener(createRealmListClickListener(realmObject, field));
        } else {
            String methodName = MagicUtils.createGetterMethodName(field);
            txtColumn.setText(MagicUtils.invokeGetterMethod(realmObject, methodName));
            txtColumn.setOnClickListener(emptyListener);
            parent.setOnClickListener(createRealmObjectClickListener(realmObject));
        }
    }

    View.OnClickListener emptyListener = v -> {};

    private View.OnClickListener createRealmObjectClickListener(@NonNull final RealmObject realmObject) {
        return v -> mListener.onRealmObjectSelected(realmObject);
    }

    private View.OnClickListener createRealmListClickListener(@NonNull final RealmObject realmObject, @NonNull final Field field) {
        return v -> mListener.onRealmListSelected(realmObject, field);
    }

    private LinearLayout.LayoutParams createLayoutParams() {
        return new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout parent;
        public TextView txtIndex;
        public TextView txtColumn1;
        public TextView txtColumn2;
        public TextView txtColumn3;

        public ViewHolder(View v) {
            super(v);
            parent = (LinearLayout) v.findViewById(R.id.parent);
            txtIndex = (TextView) v.findViewById(R.id.txtIndex);
            txtColumn1 = (TextView) v.findViewById(R.id.txtColumn1);
            txtColumn2 = (TextView) v.findViewById(R.id.txtColumn2);
            txtColumn3 = (TextView) v.findViewById(R.id.txtColumn3);
        }
    }

    public void setRealmObjects(AbstractList<? extends RealmObject> realmObjects){
        this.mRealmObjects = realmObjects;
        notifyDataSetChanged();
    }

}

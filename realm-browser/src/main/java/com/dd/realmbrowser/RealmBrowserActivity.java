package com.dd.realmbrowser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.realmbrowser.utils.L;
import com.dd.realmbrowser.utils.MagicUtils;
import com.jakewharton.rxbinding.widget.RxTextView;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class RealmBrowserActivity extends BaseActivity implements RealmAdapter.Listener {

    private static final String EXTRAS_REALM_MODEL_INDEX = "REALM_MODEL_INDEX";

    private Realm mRealm;
    private Class<? extends RealmObject> mRealmObjectClass;
    private RealmAdapter mAdapter;
    private TextView mTxtIndex;
    private TextView mTxtColumn1;
    private TextView mTxtColumn2;
    private TextView mTxtColumn3;
    private Spinner mSpinnerFields;
    private EditText mEditTxtSearch;
    private List<Field> mTmpSelectedFieldList;
    private List<Field> mSelectedFieldList;
    private List<Field> mFieldsList;

    public static void start(Activity activity, int realmModelIndex, RealmConfiguration realmConfiguration){
        Intent intent = new Intent(activity, RealmBrowserActivity.class);
        intent.putExtra(EXTRAS_REALM_MODEL_INDEX, realmModelIndex);
        RealmConfigurationProvider.getInstance().setRealmConfiguration(realmConfiguration);
        activity.startActivity(intent);
    }

    public static void start(Activity activity, RealmConfiguration realmConfiguration){
        Intent intent = new Intent(activity, RealmBrowserActivity.class);
        RealmConfigurationProvider.getInstance().setRealmConfiguration(realmConfiguration);
        activity.startActivity(intent);
    }

    public static void start(Activity activity, int realmModelIndex, String realmFileName) {
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(activity.getApplicationContext()).name(realmFileName).build();
        start(activity, realmModelIndex, realmConfiguration);
    }

    public static void start(Activity activity, String realmFileName) {
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(activity.getApplicationContext()).name(realmFileName).build();
        start(activity, realmConfiguration);
    }

    @Override int getLayoutResource() {
        return R.layout.ac_realm_browser;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RealmConfiguration realmConfiguration =
                RealmConfigurationProvider.getInstance().getRealmConfiguration();

        try {
            mRealm = Realm.getInstance(realmConfiguration);
        }catch (IllegalArgumentException e){
            Toast.makeText(this, "Non-default schema unsupported", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            onBackPressed();
            return;
        }
        final AbstractList<? extends RealmObject> realmObjects;

        if(getIntent().getExtras() != null && getIntent().getExtras().containsKey(EXTRAS_REALM_MODEL_INDEX)) {
            int index = getIntent().getIntExtra(EXTRAS_REALM_MODEL_INDEX, 0);
            mRealmObjectClass = RealmBrowser.getInstance().getRealmModelList().get(index);
            realmObjects = mRealm.allObjects(mRealmObjectClass);
        } else {
            RealmObject object = RealmObjectProvider.getInstance().getObject();
            Field field = RealmObjectProvider.getInstance().getField();
            String methodName = MagicUtils.createGetterMethodName(field);
            realmObjects = invokeMethod(object, methodName);
            if(MagicUtils.isParameterizedField(field)) {
                ParameterizedType pType = (ParameterizedType) field.getGenericType();
                Class<?> pTypeClass = (Class<?>) pType.getActualTypeArguments()[0];
                mRealmObjectClass = (Class<? extends RealmObject>) pTypeClass;
            }
        }

        mSelectedFieldList = new ArrayList<>();
        mTmpSelectedFieldList = new ArrayList<>();
        mFieldsList = new ArrayList<>();
        mFieldsList.addAll(Arrays.asList(mRealmObjectClass.getDeclaredFields()));

        mAdapter = new RealmAdapter(this, realmObjects, mSelectedFieldList, this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);

        mTxtIndex = (TextView) findViewById(R.id.txtIndex);
        mTxtColumn1 = (TextView) findViewById(R.id.txtColumn1);
        mTxtColumn2 = (TextView) findViewById(R.id.txtColumn2);
        mTxtColumn3 = (TextView) findViewById(R.id.txtColumn3);

        selectDefaultFields();
        updateColumnTitle(mSelectedFieldList);

        List<Field> queryableFields = new ArrayList<>(0);

        Observable.from(mFieldsList)
                .filter(field -> field.getType() == String.class
                                || field.getType() == long.class
                                || field.getType() == int.class
                                || field.getType() == double.class
                                || field.getType() == boolean.class)
                .subscribe(queryableFields::add);

        mSpinnerFields = (Spinner) findViewById(R.id.fields);
        mEditTxtSearch = (EditText) findViewById(R.id.query);

        mSpinnerFields.setAdapter(new FieldSpinnerAdapter(this, android.R.layout.simple_spinner_item, queryableFields));
        mSpinnerFields.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mEditTxtSearch.setHint(
                        ((Field) parent.getAdapter().getItem(position)).getType() == String.class ?
                                getString(R.string.beginWith) : getString(R.string.equalTo));
                mEditTxtSearch.getText().clear();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        RxTextView.textChangeEvents(mEditTxtSearch)
                .compose(bindToLifecycle())
                .debounce(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map(s -> s.text().toString())
                .subscribe(text -> {
                    Field selectedField = (Field) mSpinnerFields.getSelectedItem();
                    RealmResults realmResults = null;
                    if (selectedField.getType() == String.class) {
                        realmResults = mRealm.where(mRealmObjectClass).beginsWith(selectedField.getName(), text).findAll();
                    } else if (selectedField.getType() == long.class) {
                        try {
                            realmResults = mRealm.where(mRealmObjectClass).equalTo(selectedField.getName(), Long.parseLong(text)).findAll();
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    } else if (selectedField.getType() == int.class) {
                        try {
                            realmResults = mRealm.where(mRealmObjectClass).equalTo(selectedField.getName(), Integer.parseInt(text)).findAll();
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    } else if (selectedField.getType() == double.class) {
                        try {
                            realmResults = mRealm.where(mRealmObjectClass).equalTo(selectedField.getName(), Double.parseDouble(text)).findAll();
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    } else if (selectedField.getType() == boolean.class) {
                        realmResults = mRealm.where(mRealmObjectClass).equalTo(selectedField.getName(), Boolean.parseBoolean(text)).findAll();
                    }
                    if (realmResults != null) {
                        mAdapter.setRealmObjects(realmResults);
                    } else {
                        mAdapter.setRealmObjects(realmObjects);
                    }

                });

    }

    @Override
    protected void onResume() {
        mAdapter.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (mRealm != null) {
            mRealm.close();
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_columns) {
            showColumnsDialog();
        } if (id == R.id.action_settings) {
            SettingsActivity.start(this);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.browser_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onRealmListSelected(@NonNull RealmObject realmObject, @NonNull Field field) {
        RealmObjectProvider.getInstance().setObject(realmObject);
        RealmObjectProvider.getInstance().setField(field);
        RealmBrowserActivity.start(this, RealmConfigurationProvider.getInstance().getRealmConfiguration());
    }

    @Override
    public void onRealmObjectSelected(@NonNull RealmObject realmObject) {
        RealmObjectActivity.start(this, realmObject);
    }

    @Nullable
    public static RealmList<? extends RealmObject> invokeMethod(Object realmObject, String methodName) {
        RealmList<? extends RealmObject> result = null;
        try {
            Method method = realmObject.getClass().getMethod(methodName);
            result = (RealmList<? extends RealmObject>) method.invoke(realmObject);
        } catch (NoSuchMethodException e) {
            L.e(e.toString());
        } catch (InvocationTargetException e) {
            L.e(e.toString());
        } catch (IllegalAccessException e) {
            L.e(e.toString());
        }
        return result;

    }

    private void selectDefaultFields() {
        mSelectedFieldList.clear();
        Observable.from(mFieldsList)
                .take(3)
                .doOnNext(mSelectedFieldList::add)
                .subscribe();
    }

    private void updateColumnTitle(List<Field> columnsList) {
        mTxtIndex.setText("#");

        LinearLayout.LayoutParams layoutParams2 = createLayoutParams();
        LinearLayout.LayoutParams layoutParams3 = createLayoutParams();

        if (columnsList.size() > 0) {
            mTxtColumn1.setText(columnsList.get(0).getName());

            if (columnsList.size() > 1) {
                mTxtColumn2.setText(columnsList.get(1).getName());
                layoutParams2.weight = 1;

                if (columnsList.size() > 2) {
                    mTxtColumn3.setText(columnsList.get(2).getName());
                    layoutParams3.weight = 1;
                } else {
                    layoutParams3.weight = 0;
                }
            } else {
                layoutParams2.weight = 0;
            }
        }

        mTxtColumn2.setLayoutParams(layoutParams2);
        mTxtColumn3.setLayoutParams(layoutParams3);
    }

    @NonNull
    private LinearLayout.LayoutParams createLayoutParams() {
        return new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void showColumnsDialog() {
        final String[] items = new String[mFieldsList.size()];
        for (int i = 0; i < items.length; i++) {
            Field field = mFieldsList.get(i);
            items[i] = field.getName();
        }

        boolean[] checkedItems = new boolean[mFieldsList.size()];
        for (int i = 0; i < checkedItems.length; i++) {
            checkedItems[i] = mSelectedFieldList.contains(mFieldsList.get(i));
        }

        mTmpSelectedFieldList.clear();
        mTmpSelectedFieldList.addAll(mSelectedFieldList);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Columns to display");
        builder.setMultiChoiceItems(items, checkedItems,
                (dialog, indexSelected, isChecked) -> {
                    Field field = mFieldsList.get(indexSelected);
                    if (isChecked) {
                        mTmpSelectedFieldList.add(field);
                    } else if (mTmpSelectedFieldList.contains(field)) {
                        mTmpSelectedFieldList.remove(field);
                    }
                })
                .setPositiveButton("OK", (dialog, id) -> {
                    if (mTmpSelectedFieldList.isEmpty()) {
                        selectDefaultFields();
                    } else {
                        mSelectedFieldList.clear();
                        mSelectedFieldList.addAll(mTmpSelectedFieldList);
                    }
                    updateColumnTitle(mSelectedFieldList);
                    mAdapter.setFieldList(mSelectedFieldList);
                    mAdapter.notifyDataSetChanged();
                })
                .setNegativeButton("Cancel", (dialog, id) -> {
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}

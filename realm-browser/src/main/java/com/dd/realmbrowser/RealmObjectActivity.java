package com.dd.realmbrowser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;

/**
 * @author Dawid Hyży <dawid.hyzy@seedlabs.io>
 * @since 23/09/15.
 */
public class RealmObjectActivity extends BaseActivity implements FieldAdapter.Listener{

    private FieldAdapter mAdapter;

    @Override
    protected int getLayoutResource() {
        return R.layout.ac_realm_object;
    }

    public static void start(Activity activity, RealmObject realmObject){
        Intent intent = new Intent(activity, RealmObjectActivity.class);
        RealmObjectProvider.getInstance().setObject(realmObject);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Realm realm = Realm.getInstance(RealmConfigurationProvider.getInstance().getRealmConfiguration());

        mAdapter = new FieldAdapter(realm, RealmObjectProvider.getInstance().getObject());
        mAdapter.setListener(this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);

    }

    @Override
    public void onRealmObjectSelected(@NonNull RealmObject realmObject) {
        RealmObjectActivity.start(this, realmObject);
    }
}

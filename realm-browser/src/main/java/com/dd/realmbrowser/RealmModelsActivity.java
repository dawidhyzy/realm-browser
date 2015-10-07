package com.dd.realmbrowser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.jakewharton.rxbinding.widget.RxAdapterView;

import io.realm.RealmConfiguration;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;

public class RealmModelsActivity extends BaseActivity {

    public static void start(@NonNull Activity activity, @NonNull String realmFileName) {
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(activity.getApplicationContext()).name(realmFileName).build();
        start(activity, realmConfiguration);
    }

    public static void start(@NonNull Activity activity, RealmConfiguration realmConfiguration){
        Intent intent = new Intent(activity, RealmModelsActivity.class);
        RealmConfigurationProvider.getInstance().setRealmConfiguration(realmConfiguration);
        activity.startActivity(intent);
    }

    @Override
    int getLayoutResource() {
        return R.layout.ac_realm_list_view;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<String> modelList = new ArrayList<>();

        if(RealmConfigurationProvider.getInstance().getRealmConfiguration() != null){
            RealmBrowser.getInstance().clearRealmModel();
            RealmBrowser.getInstance().addRealmModel(RealmConfigurationProvider.getInstance().getRealmConfiguration().getSchemaMediator().getModelClasses());
        }

        Observable.from(RealmBrowser.getInstance().getRealmModelList())
                .subscribe(file -> modelList.add(file.getSimpleName()));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, modelList);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);

        RxAdapterView.itemClickEvents(listView)
                .compose(bindToLifecycle())
                .subscribe(adapterViewItemClickEvent -> onItemClicked(adapterViewItemClickEvent.position()));
    }

    private void onItemClicked(int position) {
        RealmBrowserActivity.start(this, position, RealmConfigurationProvider.getInstance().getRealmConfiguration());
    }
}

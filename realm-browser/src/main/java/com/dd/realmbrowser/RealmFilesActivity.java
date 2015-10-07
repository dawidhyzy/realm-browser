package com.dd.realmbrowser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.jakewharton.rxbinding.widget.RxAdapterView;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.exceptions.RealmMigrationNeededException;
import rx.Observable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RealmFilesActivity extends BaseActivity {

    private List<String> mIgnoreExtensionList;
    private ArrayAdapter<String> mAdapter;

    public static void start(@NonNull Activity activity) {
        Intent intent = new Intent(activity, RealmFilesActivity.class);
        activity.startActivity(intent);
    }

    @Override
    int getLayoutResource() {
        return R.layout.ac_realm_list_view;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIgnoreExtensionList = Arrays.asList(".log", ".lock");

        File dataDir = new File(getApplicationInfo().dataDir, "files");
        File[] files = dataDir.listFiles();
        List<String> fileList = new ArrayList<>();

        Observable.from(files)
                .map(File::getName)
                .filter(this::isValid)
                .doOnNext(fileList::add)
                .subscribe();

        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fileList);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(mAdapter);
        RxAdapterView.itemClickEvents(listView)
                .compose(bindToLifecycle())
                .subscribe(adapterViewItemClickEvent -> onItemClicked(adapterViewItemClickEvent.position()));
    }

    private boolean isValid(String fileName) {
        boolean isValid = true;
        int index = fileName.lastIndexOf(".");
        if (index > 0) {
            String extension = fileName.substring(index);
            isValid = !mIgnoreExtensionList.contains(extension);
        }
        return isValid;
    }

    private void onItemClicked(int position) {
        try {
            String realmFileName = mAdapter.getItem(position);
            RealmModelsActivity.start(this, realmFileName);
        } catch (RealmMigrationNeededException e) {
            Toast.makeText(getApplicationContext(), "RealmMigrationNeededException", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Can't open realm instance", Toast.LENGTH_SHORT).show();
        }
    }
}

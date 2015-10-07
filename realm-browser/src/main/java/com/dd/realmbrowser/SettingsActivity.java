package com.dd.realmbrowser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import com.dd.realmbrowser.model.RealmPreferences;
import com.jakewharton.rxbinding.widget.RxCompoundButton;

public class SettingsActivity extends BaseActivity {

    private RealmPreferences mRealmPreferences;

    public static void start(@NonNull Activity activity) {
        Intent intent = new Intent(activity, SettingsActivity.class);
        activity.startActivity(intent);
    }

    @Override
    int getLayoutResource() {
        return R.layout.ac_realm_settings;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRealmPreferences = new RealmPreferences(getApplicationContext());

        initView();
    }

    private void initView() {
        CheckBox cbWrapText = (CheckBox) findViewById(R.id.cbWrapText);
        cbWrapText.setChecked(mRealmPreferences.shouldWrapText());
        RxCompoundButton.checkedChanges(cbWrapText).subscribe(mRealmPreferences::setShouldWrapText);
    }
}

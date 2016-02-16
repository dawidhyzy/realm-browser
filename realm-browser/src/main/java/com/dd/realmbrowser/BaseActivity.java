package com.dd.realmbrowser;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.Toolbar;

import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

/**
 * @author Dawid Hyży <dawid.hyzy@seedlabs.io>
 * @since 25/09/15.
 */
public abstract class BaseActivity extends RxAppCompatActivity {

    protected Toolbar toolbar;

    abstract @LayoutRes int getLayoutResource();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
    }
}

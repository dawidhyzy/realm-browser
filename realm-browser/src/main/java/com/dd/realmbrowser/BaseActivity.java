package com.dd.realmbrowser;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

/**
 * @author Dawid Hyży <dawid.hyzy@seedlabs.io>
 * @since 25/09/15.
 */
public abstract class BaseActivity extends RxAppCompatActivity {

    abstract @LayoutRes int getLayoutResource();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());
    }
}

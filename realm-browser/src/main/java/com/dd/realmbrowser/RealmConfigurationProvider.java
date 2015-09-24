package com.dd.realmbrowser;

import io.realm.RealmConfiguration;

/**
 * @author Dawid Hyży <dawid.hyzy@seedlabs.io>
 * @since 18/09/15.
 */

class RealmConfigurationProvider{

    private static final RealmConfigurationProvider sInstance = new RealmConfigurationProvider();
    private RealmConfiguration realmConfiguration;

    public static RealmConfigurationProvider getInstance() { return sInstance; }

    public RealmConfiguration getRealmConfiguration() {
        return realmConfiguration;
    }

    public void setRealmConfiguration(RealmConfiguration realmConfiguration) {
        this.realmConfiguration = realmConfiguration;
    }
}

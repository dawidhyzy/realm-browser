package com.dd.realmbrowser;

import io.realm.RealmObject;

import java.lang.reflect.Field;
import java.util.List;

class RealmObjectProvider {

    private static final RealmObjectProvider sInstance = new RealmObjectProvider();
    private RealmObject mObject;
    private Field mField;

    public static RealmObjectProvider getInstance() {
        return sInstance;
    }

    public void setObject(RealmObject object) {
        mObject = object;
    }

    public RealmObject getObject() {
        return mObject;
    }

    public Field getField() {
        return mField;
    }

    public void setField(Field field) {
        mField = field;
    }

}

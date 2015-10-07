package com.dd.realmbrowser.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

import io.realm.RealmObject;

public class MagicUtils {

    @Nullable
    public static String createGetterMethodName(@NonNull Field field) {
        String methodName;
        if (field.getType().equals(boolean.class)) {
            if (field.getName().contains("is")) {
                methodName = field.getName();
            } else {
                methodName = "is" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
            }
        } else {
            methodName = "get" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
        }

        return methodName;
    }

    @Nullable
    public static String createSetterMethodName(@NonNull Field field) {
        return "set" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
    }

    @NonNull
    public static RealmObject invokeRealmObjectGetterMethod(Object realmObject, String methodName) {
        RealmObject resultObj = null;
        try {
            Method method = realmObject.getClass().getMethod(methodName);
            resultObj = (RealmObject) method.invoke(realmObject);
        } catch (NoSuchMethodException e) {
            L.e(e.toString());
        } catch (InvocationTargetException e) {
            L.e(e.toString());
        } catch (IllegalAccessException e) {
            L.e(e.toString());
        }
        return resultObj;
    }

    @NonNull
    public static String invokeGetterMethod(Object realmObject, String methodName) {
        String result = "null";
        try {
            Method method = realmObject.getClass().getMethod(methodName);
            Object resultObj = method.invoke(realmObject);
            if(resultObj != null) {
                result = resultObj.toString();
            }
        } catch (NoSuchMethodException e) {
            L.e(e.toString());
        } catch (InvocationTargetException e) {
            L.e(e.toString());
        } catch (IllegalAccessException e) {
            L.e(e.toString());
        }
        return result;
    }

    @NonNull
    public static void invokeSetterMethod(Object realmObject, String methodName, Object value) {
        try {
            Class argClass = null;
            if(value.getClass() == Integer.class){
                argClass = int.class;
            }else if(value.getClass() == Long.class){
                argClass = long.class;
            }else if(value.getClass() == Double.class){
                argClass = double.class;
            }else if(value.getClass() == Boolean.class){
                argClass = boolean.class;
            }else{
                argClass = value.getClass();
            }
            Method method = realmObject.getClass().getMethod(methodName, argClass);

            Object resultObj = method.invoke(realmObject, value);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            L.e(e.toString());
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            L.e(e.toString());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            L.e(e.toString());
        }

    }

    public static  boolean isParameterizedField(@NonNull Field field) {
        return field.getGenericType() instanceof ParameterizedType;
    }

    @Nullable
    public static  String createParameterizedName(@NonNull Field field) {
        ParameterizedType pType = (ParameterizedType) field.getGenericType();
        String rawType = pType.getRawType().toString();
        int rawTypeIndex = rawType.lastIndexOf(".");
        if(rawTypeIndex > 0) {
            rawType = rawType.substring(rawTypeIndex + 1);
        }

        String argument = pType.getActualTypeArguments()[0].toString();
        int argumentIndex = argument.lastIndexOf(".");
        if(argumentIndex > 0) {
            argument = argument.substring(argumentIndex + 1);
        }

        return rawType + "<" + argument + ">";
    }
}

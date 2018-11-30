package com.hunter.wallet.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class SharedPreferencesUtils {

    public static boolean writeString(Context context, String sfName, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(sfName, MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(key, value);
        return edit.commit();
    }

    public static String getString(Context context, String sfName, String key, String defValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(sfName, MODE_PRIVATE);
        return sharedPreferences.getString(key, defValue);
    }

    public static Map<String, ?> getAll(Context context, String sfName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(sfName, MODE_PRIVATE);
        return sharedPreferences.getAll();
    }

    public static Long getLong(Context context, String sfName, String key, long defValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(sfName, MODE_PRIVATE);
        return sharedPreferences.getLong(key, defValue);
    }

    public static boolean writeLong(Context context, String sfName, String key, long value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(sfName, MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putLong(key, value);
        return edit.commit();
    }

    public static boolean deleteString(Context context, String sfName, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(sfName, MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.remove(key);
        return edit.commit();
    }

    public static int getInt(Context context, String sfName, String key, int defValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(sfName, MODE_PRIVATE);
        return sharedPreferences.getInt(key, defValue);
    }

    public static boolean writeInt(Context context, String sfName, String key, int value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(sfName, MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putInt(key, value);
        return edit.commit();
    }

    public static boolean clear(Context context, String sfName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(sfName, MODE_PRIVATE);
        return sharedPreferences.edit().clear().commit();
    }
}

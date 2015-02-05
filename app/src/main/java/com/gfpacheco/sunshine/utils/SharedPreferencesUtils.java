package com.gfpacheco.sunshine.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPreferencesUtils {

    public static String getSharedStringPreference(
            Context context, int preferenceKeyResId, int preferenceDefaultResKeyId) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(
                context.getString(preferenceKeyResId),
                context.getString(preferenceDefaultResKeyId)
        );
    }

}

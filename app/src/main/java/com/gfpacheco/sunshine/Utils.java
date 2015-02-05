package com.gfpacheco.sunshine;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.gfpacheco.sunshine.data.WeatherContract;

import java.text.DateFormat;
import java.util.Date;

public class Utils {

    public static String getSharedStringPreference(
            Context context, int preferenceKeyResId, int preferenceDefaultResKeyId) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(
                context.getString(preferenceKeyResId),
                context.getString(preferenceDefaultResKeyId)
        );
    }

    static String formatTemperature(double temperature, boolean isMetric) {
        double temp;
        if (!isMetric) {
            temp = temperature * 1.8 + 32;
        } else {
            temp = temperature;
        }
        return String.format("%.0f", temp);
    }

    static String formatDate(String dateString) {
        Date date = WeatherContract.getDateFromDb(dateString);
        return DateFormat.getDateInstance().format(date);
    }

    public static boolean isMetricsUnits(Context context) {
        String units = getSharedStringPreference(
                context,
                R.string.pref_units_key,
                R.string.pref_units_metric
        );

        return units.equals(context.getString(R.string.pref_units_metric));
    }
}

package com.gfpacheco.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {
    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_forecast, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // Read weather icon ID from cursor
        // int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_ID);
        // Use placeholder image for now
        ((ImageView) view.findViewById(R.id.list_item_icon)).setImageResource(R.drawable.ic_launcher);

        // Read date from cursor
        // Find TextView and set formatted date on it
        ((TextView) view.findViewById(R.id.list_item_date_text_view))
                .setText(Utils.getFriendlyDayString(context, cursor.getString(ForecastFragment.COL_WEATHER_DATE_TEXT)));

        // Read weather forecast from cursor
        // Find TextView and set weather forecast on it
        ((TextView) view.findViewById(R.id.list_item_forecast_text_view))
                .setText(cursor.getString(ForecastFragment.COL_WEATHER_SHORT_DESC));

        // Read user preference for metric or imperial temperature units
        boolean isMetric = Utils.isMetricsUnits(context);

        // Read high temperature from cursor
        ((TextView) view.findViewById(R.id.list_item_high_text_view))
                .setText(Utils.formatTemperature(cursor.getFloat(ForecastFragment.COL_WEATHER_MAX_TEMP), isMetric));

        // Read low temperature from cursor
        ((TextView) view.findViewById(R.id.list_item_low_text_view))
                .setText(Utils.formatTemperature(cursor.getFloat(ForecastFragment.COL_WEATHER_MIN_TEMP), isMetric));
    }
}
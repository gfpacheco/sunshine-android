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
    private final int VIEW_TYPE_TODAY = 0;
    private final int VIEW_TYPE_FUTURE_DAY = 1;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());

        View view = LayoutInflater.from(context).inflate(
                (viewType == VIEW_TYPE_TODAY) ? R.layout.list_item_forecast_today : R.layout.list_item_forecast,
                parent,
                false
        );
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        // Read weather icon ID from cursor
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        // Use weather art image
        int viewType = getItemViewType(cursor.getPosition());
        if (viewType == VIEW_TYPE_TODAY) {
            viewHolder.iconView
                    .setImageResource(Utils.getArtResourceForWeatherCondition(weatherId));
        } else {
            viewHolder.iconView
                    .setImageResource(Utils.getIconResourceForWeatherCondition(weatherId));
        }

        // Read date from cursor
        // Find TextView and set formatted date on it
        viewHolder.dateView
                .setText(Utils.getFriendlyDayString(context, cursor.getString(ForecastFragment.COL_WEATHER_DATE_TEXT)));

        // Read weather forecast from cursor
        // Find TextView and set weather forecast on it
        viewHolder.shortDescView.setText(cursor.getString(ForecastFragment.COL_WEATHER_SHORT_DESC));

        // Read user preference for metric or imperial temperature units
        boolean isMetric = Utils.isMetricsUnits(context);

        // Read high temperature from cursor
        viewHolder.highTempView
                .setText(Utils.formatTemperature(context, cursor.getFloat(ForecastFragment.COL_WEATHER_MAX_TEMP), isMetric));

        // Read low temperature from cursor
        viewHolder.lowTempView
                .setText(Utils.formatTemperature(context, cursor.getFloat(ForecastFragment.COL_WEATHER_MIN_TEMP), isMetric));
    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView shortDescView;
        public final TextView highTempView;
        public final TextView lowTempView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_text_view);
            shortDescView = (TextView) view.findViewById(R.id.list_item_forecast_text_view);
            highTempView = (TextView) view.findViewById(R.id.list_item_high_text_view);
            lowTempView = (TextView) view.findViewById(R.id.list_item_low_text_view);
        }
    }
}
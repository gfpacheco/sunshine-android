package com.gfpacheco.sunshine;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gfpacheco.sunshine.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int FORECAST_LOADER_ID = 0;

    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE_TEXT,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_WIND_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
    };

    private static final int COL_WEATHER_DATE_TEXT = 1;
    private static final int COL_WEATHER_SHORT_DESC = 2;
    private static final int COL_WEATHER_MIN_TEMP = 3;
    private static final int COL_WEATHER_MAX_TEMP = 4;
    private static final int COL_WEATHER_HUMIDITY = 5;
    private static final int COL_WEATHER_WIND_SPEED = 6;
    private static final int COL_WEATHER_WIND_DEGREES = 7;
    private static final int COL_WEATHER_PRESSURE = 8;
    private static final int COL_WEATHER_CONDITION_ID = 9;

    private String mLocation;
    private String mForecast;
    private ShareActionProvider mShareActionProvider;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(FORECAST_LOADER_ID, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail_fragment, menu);
        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // Attach an intent to this ShareActionProvider.  You can update this at any time,
        // like when the user selects a new piece of data they might like to share.
        if (mForecast != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mLocation != null && !mLocation.equals(Utils.getLocationPreference(getActivity()))) {
            getLoaderManager().restartLoader(FORECAST_LOADER_ID, null, this);
        }
    }

    private Intent createShareForecastIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, mForecast + " #SunshineApp");
        return intent;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Intent intent = getActivity().getIntent();

        if (intent == null || !intent.hasExtra(Intent.EXTRA_TEXT)) {
            return null;
        }

        mLocation = Utils.getLocationPreference(getActivity());
        String mDate = intent.getStringExtra(Intent.EXTRA_TEXT);

        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                mLocation,
                mDate
        );

        return new CursorLoader(
                getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                null
        );
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        FragmentActivity activity = getActivity();
        boolean isMetric = Utils.isMetricsUnits(activity);
        updateView(
                Utils.formatDate(data.getString(COL_WEATHER_DATE_TEXT)),
                data.getString(COL_WEATHER_SHORT_DESC),
                Utils.formatTemperature(activity, data.getDouble(COL_WEATHER_MAX_TEMP), isMetric),
                Utils.formatTemperature(activity, data.getDouble(COL_WEATHER_MIN_TEMP), isMetric),
                activity.getString(R.string.format_humidity, data.getFloat(COL_WEATHER_HUMIDITY)),
                Utils.formatWind(activity, data.getFloat(COL_WEATHER_WIND_SPEED), data.getFloat(COL_WEATHER_WIND_DEGREES)),
                activity.getString(R.string.format_pressure, data.getFloat(COL_WEATHER_PRESSURE)),
                Utils.getArtResourceForWeatherCondition(data.getInt(COL_WEATHER_CONDITION_ID))
        );

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    private void updateView(String dateText, String forecast, String high, String low,
                            String humidity, String wind, String pressure, int icon) {
        Activity activity = getActivity();
        ((TextView) activity.findViewById(R.id.detail_date_text_view)).setText(dateText);
        ((TextView) activity.findViewById(R.id.detail_forecast_text_view)).setText(forecast);
        ((TextView) activity.findViewById(R.id.detail_high_text_view)).setText(high);
        ((TextView) activity.findViewById(R.id.detail_low_text_view)).setText(low);
        ((ImageView) activity.findViewById(R.id.detail_icon)).setImageResource(icon);
        ((TextView) activity.findViewById(R.id.detail_humidity_text_view)).setText(humidity);
        ((TextView) activity.findViewById(R.id.detail_wind_text_view)).setText(wind);
        ((TextView) activity.findViewById(R.id.detail_pressure_text_view)).setText(pressure);
        mForecast = String.format("%s - %s - %s/%s", dateText, forecast, high, low);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}

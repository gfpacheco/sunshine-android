package com.gfpacheco.sunshine;

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

    private static final String LOCATION_KEY = "location";

    private String mLocation;
    private String mForecast;
    private String mDateStr;
    private ShareActionProvider mShareActionProvider;

    private TextView mFriendlyDateView;
    private TextView mDateView;
    private TextView mDescriptionView;
    private TextView mHighTempView;
    private TextView mLowTempView;
    private TextView mHumidityView;
    private TextView mWindView;
    private TextView mPressureView;
    private ImageView mIconView;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static DetailFragment newInstance(String date) {
        DetailFragment detailFragment = new DetailFragment();

        Bundle args = new Bundle();
        args.putString(DetailActivity.DATE_KEY, date);
        detailFragment.setArguments(args);

        return detailFragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            mLocation = savedInstanceState.getString(LOCATION_KEY);
        }

        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(DetailActivity.DATE_KEY)) {
            getLoaderManager().initLoader(FORECAST_LOADER_ID, null, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mDateStr = arguments.getString(DetailActivity.DATE_KEY);
        }

        if (savedInstanceState != null) {
            mLocation = savedInstanceState.getString(LOCATION_KEY);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mFriendlyDateView = (TextView) rootView.findViewById(R.id.detail_friendly_date_text_view);
        mDateView = (TextView) rootView.findViewById(R.id.detail_date_text_view);
        mDescriptionView = (TextView) rootView.findViewById(R.id.detail_forecast_text_view);
        mHighTempView = (TextView) rootView.findViewById(R.id.detail_high_text_view);
        mLowTempView = (TextView) rootView.findViewById(R.id.detail_low_text_view);
        mHumidityView = (TextView) rootView.findViewById(R.id.detail_humidity_text_view);
        mWindView = (TextView) rootView.findViewById(R.id.detail_wind_text_view);
        mPressureView = (TextView) rootView.findViewById(R.id.detail_pressure_text_view);
        mIconView = (ImageView) rootView.findViewById(R.id.detail_icon);
        return rootView;
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
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(DetailActivity.DATE_KEY) &&
                mLocation != null && !mLocation.equals(Utils.getLocationPreference(getActivity()))) {
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
        mLocation = Utils.getLocationPreference(getActivity());

        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                mLocation,
                mDateStr
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
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(LOCATION_KEY, mLocation);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            FragmentActivity activity = getActivity();
            boolean isMetric = Utils.isMetricsUnits(activity);

            mFriendlyDateView.setText(Utils.getFriendlyDayString(activity, data.getString(COL_WEATHER_DATE_TEXT)));
            mDateView.setText(Utils.formatDate(data.getString(COL_WEATHER_DATE_TEXT)));
            mDescriptionView.setText(data.getString(COL_WEATHER_SHORT_DESC));
            mHighTempView.setText(Utils.formatTemperature(activity, data.getDouble(COL_WEATHER_MAX_TEMP), isMetric));
            mLowTempView.setText(Utils.formatTemperature(activity, data.getDouble(COL_WEATHER_MIN_TEMP), isMetric));
            mHumidityView.setText(activity.getString(R.string.format_humidity, data.getFloat(COL_WEATHER_HUMIDITY)));
            mWindView.setText(Utils.formatWind(activity, data.getFloat(COL_WEATHER_WIND_SPEED), data.getFloat(COL_WEATHER_WIND_DEGREES)));
            mPressureView.setText(activity.getString(R.string.format_pressure, data.getFloat(COL_WEATHER_PRESSURE)));
            mIconView.setImageResource(Utils.getArtResourceForWeatherCondition(data.getInt(COL_WEATHER_CONDITION_ID)));

            mForecast = String.format(
                    "%s - %s - %s/%s",
                    mDateView.getText(),
                    mDescriptionView.getText(),
                    mHighTempView.getText(),
                    mLowTempView.getText());

            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}

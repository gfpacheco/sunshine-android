package com.gfpacheco.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gfpacheco.sunshine.data.WeatherContract;


public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

        private static final int FORECAST_LOADER_ID = 0;

        private static final String[] FORECAST_COLUMNS = {
                WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
                WeatherContract.WeatherEntry.COLUMN_DATETEXT,
                WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
                WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
                WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
        };

        private static final int COL_WEATHER_DATETEXT = 1;
        private static final int COL_WEATHER_SHORT_DESC = 2;
        private static final int COL_WEATHER_MIN_TEMP = 3;
        private static final int COL_WEATHER_MAX_TEMP = 4;

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

            boolean isMetric = Utils.isMetricsUnits(getActivity());
            updateView(
                    Utils.formatDate(data.getString(COL_WEATHER_DATETEXT)),
                    data.getString(COL_WEATHER_SHORT_DESC),
                    Utils.formatTemperature(data.getDouble(COL_WEATHER_MAX_TEMP), isMetric),
                    Utils.formatTemperature(data.getDouble(COL_WEATHER_MIN_TEMP), isMetric)
            );

            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }

        private void updateView(String dateText, String forecast, String high, String low) {
            ((TextView) getActivity().findViewById(R.id.detail_date_text_view)).setText(dateText);
            ((TextView) getActivity().findViewById(R.id.detail_forecast_text_view)).setText(forecast);
            ((TextView) getActivity().findViewById(R.id.detail_high_text_view)).setText(high);
            ((TextView) getActivity().findViewById(R.id.detail_low_text_view)).setText(low);
            mForecast = String.format("%s - %s - %s/%s", dateText, forecast, high, low);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            updateView(null, null, null, null);
        }
    }
}

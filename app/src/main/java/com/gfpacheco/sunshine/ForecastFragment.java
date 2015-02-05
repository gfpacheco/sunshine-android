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
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.gfpacheco.sunshine.data.WeatherContract;
import com.gfpacheco.sunshine.data.WeatherContract.LocationEntry;
import com.gfpacheco.sunshine.data.WeatherContract.WeatherEntry;

import java.util.Date;

public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int FORECAST_LOADER_ID = 0;

    private static final String[] FORECAST_COLUMNS = {
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATETEXT,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MIN_TEMP,
            WeatherEntry.COLUMN_MAX_TEMP,
            LocationEntry.COLUMN_LOCATION_SETTING
    };

    private static final int COL_WEATHER_ID = 0;
    private static final int COL_WEATHER_DATETEXT = 1;
    private static final int COL_WEATHER_SHORT_DESC = 2;
    private static final int COL_WEATHER_MIN_TEMP = 3;
    private static final int COL_WEATHER_MAX_TEMP = 4;
    private static final int COL_LOCATION_SETTING = 5;

    private String mLocation;
    private SimpleCursorAdapter mWeekForecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(FORECAST_LOADER_ID, null, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_forecast_fragment, menu);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mLocation != null && !mLocation.equals(Utils.getLocationPreference(getActivity()))) {
            getLoaderManager().restartLoader(FORECAST_LOADER_ID, null, this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateWeather() {
        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
        weatherTask.execute(mLocation);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mWeekForecastAdapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.list_item_forecast,
                null,
                new String[]{
                        WeatherContract.WeatherEntry.COLUMN_DATETEXT,
                        WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
                        WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                        WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
                },
                new int[]{R.id.list_item_date_text_view,
                        R.id.list_item_forecast_text_view,
                        R.id.list_item_high_text_view,
                        R.id.list_item_low_text_view
                },
                0
        );

        mWeekForecastAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                boolean isMetric = Utils.isMetricsUnits(getActivity());

                switch (columnIndex) {
                    case COL_WEATHER_MAX_TEMP:
                    case COL_WEATHER_MIN_TEMP: {
                        // we have to do some formatting and possibly a conversion
                        ((TextView) view).setText(Utils.formatTemperature(
                                cursor.getDouble(columnIndex), isMetric));
                        return true;
                    }
                    case COL_WEATHER_DATETEXT: {
                        String dateString = cursor.getString(columnIndex);
                        TextView dateView = (TextView) view;
                        dateView.setText(Utils.formatDate(dateString));
                        return true;
                    }
                }
                return false;
            }
        });

        ListView listView = (ListView) rootView.findViewById(R.id.list_view_forecast);
        listView.setAdapter(mWeekForecastAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = mWeekForecastAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    String dateString = Utils.formatDate(cursor.getString(COL_WEATHER_DATETEXT));
                    String weatherDescription = cursor.getString(COL_WEATHER_SHORT_DESC);

                    boolean isMetric = Utils.isMetricsUnits(getActivity());
                    String high = Utils.formatTemperature(cursor.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
                    String low = Utils.formatTemperature(cursor.getDouble(COL_WEATHER_MIN_TEMP), isMetric);

                    Intent intent = new Intent(getActivity(), DetailActivity.class).putExtra(
                            Intent.EXTRA_TEXT,
                            String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low)
                    );

                    startActivity(intent);
                }
            }
        });

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final String sortOrder = WeatherEntry.COLUMN_DATETEXT + " ASC";

        String startDate = WeatherContract.getDbDateString(new Date());
        mLocation = Utils.getLocationPreference(getActivity());

        Uri weatherForLocationUri = WeatherEntry.buildWeatherLocationWithStartDate(
                mLocation,
                startDate
        );

        return new CursorLoader(
                getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mWeekForecastAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mWeekForecastAdapter.swapCursor(null);
    }
}
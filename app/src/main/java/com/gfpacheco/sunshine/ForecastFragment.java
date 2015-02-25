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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.gfpacheco.sunshine.data.WeatherContract;
import com.gfpacheco.sunshine.data.WeatherContract.LocationEntry;
import com.gfpacheco.sunshine.data.WeatherContract.WeatherEntry;
import com.gfpacheco.sunshine.sync.SunshineSyncAdapter;

import java.util.Date;

public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int COL_WEATHER_DATE_TEXT = 1;
    public static final int COL_WEATHER_SHORT_DESC = 2;
    public static final int COL_WEATHER_MIN_TEMP = 3;
    public static final int COL_WEATHER_MAX_TEMP = 4;
    public static final int COL_WEATHER_CONDITION_ID = 5;
    public static final int COL_LOCATION_COORD_LAT = 6;
    public static final int COL_LOCATION_COORD_LONG = 7;

    private static final int FORECAST_LOADER_ID = 0;
    private static final String[] FORECAST_COLUMNS = {
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATE_TEXT,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MIN_TEMP,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_WEATHER_ID,
            LocationEntry.COLUMN_COORD_LAT,
            LocationEntry.COLUMN_COORD_LONG
    };
    private static final String SELECTED_INDEX = "selectedIndex";

    private String mLocation;
    private ForecastAdapter mWeekForecastAdapter;
    private int mSelectedIndex = -1;
    private boolean mUseTodayLayout;

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

        switch (id) {
            case R.id.action_refresh:
                updateWeather();
                return true;
            case R.id.action_map:
                openPreferredLocationInMap();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateWeather() {
        SunshineSyncAdapter.syncImmediately(getActivity());
    }

    private void openPreferredLocationInMap() {
        Cursor cursor = mWeekForecastAdapter.getCursor();
        cursor.moveToFirst();

        String locationLat = cursor.getString(COL_LOCATION_COORD_LAT);
        String locationLong = cursor.getString(COL_LOCATION_COORD_LONG);

        Uri geoLocationUri = Uri.parse("geo:" + locationLat + "," + locationLong);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocationUri);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mWeekForecastAdapter = new ForecastAdapter(getActivity(), null, 0);
        mWeekForecastAdapter.setUseTodayLayout(mUseTodayLayout);

        ListView listView = (ListView) rootView.findViewById(R.id.list_view_forecast);
        listView.setAdapter(mWeekForecastAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = mWeekForecastAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    ((Callback) getActivity())
                            .onItemSelected(cursor.getString(COL_WEATHER_DATE_TEXT));
                }
                mSelectedIndex = position;
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_INDEX)) {
            mSelectedIndex = savedInstanceState.getInt(SELECTED_INDEX);
        }

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final String sortOrder = WeatherEntry.COLUMN_DATE_TEXT + " ASC";

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
        if (mSelectedIndex != ListView.INVALID_POSITION) {
            ((ListView) getView().findViewById(R.id.list_view_forecast)).smoothScrollToPosition(mSelectedIndex);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mSelectedIndex != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_INDEX, mSelectedIndex);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mWeekForecastAdapter.swapCursor(null);
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
        if (mWeekForecastAdapter != null) {
            mWeekForecastAdapter.setUseTodayLayout(useTodayLayout);
        }
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(String date);
    }
}
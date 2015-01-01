package dev.emmaguy.pocketwidget.ui;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.OnChartValueSelectedListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import dev.emmaguy.pocketwidget.DataProvider;
import dev.emmaguy.pocketwidget.Logger;
import dev.emmaguy.pocketwidget.R;

public class GraphActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>, OnChartValueSelectedListener, AdapterView.OnItemSelectedListener {
    private Toast mToast;
    private LineChart mChart;
    private Spinner mSpinner;
    private TypedArray mSelectedValues;

    private final ArrayList<String> mDates = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_graph);

        mSelectedValues = getResources().obtainTypedArray(R.array.graph_over_time_values);

        Paint infoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        infoPaint.setTextAlign(Paint.Align.CENTER);
        infoPaint.setTextSize(com.github.mikephil.charting.utils.Utils.convertDpToPixel(14f));
        infoPaint.setColor(getResources().getColor(R.color.black));

        mChart = (LineChart) findViewById(R.id.chart);
        mChart.setPaint(infoPaint, Chart.PAINT_INFO);
        mChart.setNoDataText(getString(R.string.not_enough_data_points));
        mChart.setNoDataTextDescription(getString(R.string.not_enough_data_action));
        mChart.setDrawGridBackground(false);
        mChart.setDrawYValues(true);
        mChart.setDescription("");
        mChart.setStartAtZero(false);
        mChart.setOnChartValueSelectedListener(this);

        mSpinner = (Spinner) findViewById(R.id.spinner);
        mSpinner.setOnItemSelectedListener(this);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                DataProvider.UNREAD_ARTICLES_BY_DATE_URI,
                null,
                null,
                null,
                mSelectedValues.getString(mSpinner.getSelectedItemPosition()));
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        populateGraph(data);
    }

    private void populateGraph(Cursor data) {
        Logger.Log("populateGraph " + data.getCount());
        final DateFormat dateFormat = new SimpleDateFormat();//DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());

        ArrayList<Entry> unreadCounts = new ArrayList<Entry>();
        mDates.clear();

        int index = 0;
        for (int i = 0; i < data.getCount(); i++) {
            if (i == 0) {
                // if orientation changed we need to start from the first one again
                data.moveToFirst();
            } else {
                data.moveToNext();
            }

            try {
                int unreadCount = data.getInt(data.getColumnIndex(DataProvider.UNREAD_COUNT));
                unreadCounts.add(new Entry(unreadCount, index));

                String storedDateValue = data.getString(data.getColumnIndex(DataProvider.DATE));
                Date dateOfEntry = DataProvider.sDateFormat.parse(storedDateValue);

                String date = dateFormat.format(dateOfEntry);
                mDates.add(date);

                index++;
            } catch (Exception e) {
                Logger.sendThrowable(this, "Failed to populate graph", e);
            }
        }

        LineDataSet lineData = new LineDataSet(unreadCounts, getString(R.string.unread_article));
        lineData.setCircleSize(4f);
        lineData.setLineWidth(6f);
        lineData.setCircleColor(getResources().getColor(R.color.pocket_red));
        lineData.setColor(getResources().getColor(R.color.pocket_red));

        mChart.setData(new LineData(mDates, lineData));
        mChart.fitScreen();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onValueSelected(Entry entry, int i) {
        if(mToast != null) {
            mToast.cancel();
        }

        String message = getString(R.string.unread_articles_x, ((int) entry.getVal())) + " " + mDates.get(entry.getXIndex());
        mToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        mToast.show();
    }

    @Override
    public void onNothingSelected() {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mChart.clear();
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}

package dev.emmaguy.pocketwidget.ui;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Paint;
import android.os.Bundle;
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

public class GraphActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>, OnChartValueSelectedListener {
    private LineChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_graph);

        mChart = (LineChart) findViewById(R.id.chart);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                DataProvider.UNREAD_ARTICLES_BY_DATE_URI,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        populateGraph(data);
    }

    private void populateGraph(Cursor data) {
        final DateFormat dateFormat = new SimpleDateFormat();//DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());

        ArrayList<Entry> unreadCounts = new ArrayList<Entry>();
        ArrayList<String> dates = new ArrayList<String>();

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
                dates.add(date);

                index++;
            } catch (Exception e) {
                Logger.Log("Failed to populate graph", e);
            }
        }

        LineDataSet lineData = new LineDataSet(unreadCounts, getString(R.string.unread_article));
        lineData.setCircleSize(4f);
        lineData.setLineWidth(6f);
        lineData.setCircleColor(getResources().getColor(R.color.pocket_red));
        lineData.setColor(getResources().getColor(R.color.pocket_red));

        Paint infoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        infoPaint.setTextAlign(Paint.Align.CENTER);
        infoPaint.setTextSize(com.github.mikephil.charting.utils.Utils.convertDpToPixel(14f));
        infoPaint.setColor(getResources().getColor(R.color.black));

        mChart.setData(new LineData(dates, lineData));
        mChart.setPaint(infoPaint, Chart.PAINT_INFO);
        mChart.setNoDataText(getString(R.string.not_enough_data_points));
        mChart.setNoDataTextDescription(getString(R.string.please_come_back_in_days));
        mChart.setDrawGridBackground(false);
        mChart.setDrawYValues(true);
        mChart.setDescription("");
        mChart.setStartAtZero(false);
        mChart.setOnChartValueSelectedListener(this);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onValueSelected(Entry entry, int i) {
        Toast.makeText(this, getString(R.string.unread_articles_x, ((int)entry.getVal())), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected() {

    }
}

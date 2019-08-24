package com.stfactory.tutorial6_1beaconbasics;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.stfactory.tutorial6_1beaconbasics.adapter.DeviceListAdapter;
import com.stfactory.tutorial6_1beaconbasics.model.CustomBluetoothDevice;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import uk.co.alt236.bluetoothlelib.device.BluetoothLeDevice;
import uk.co.alt236.bluetoothlelib.device.beacon.BeaconType;
import uk.co.alt236.bluetoothlelib.device.beacon.BeaconUtils;
import uk.co.alt236.bluetoothlelib.device.beacon.ibeacon.IBeaconDevice;

public class MainActivity extends BLEScanActivity {

    private static final String MAC_ESTIMOTE_GREEN = "D7:F7:DD:D4:9C:37";
    private static final String MAC_ESTIMOTE_BLUE = "DF:AA:AD:47:5E:50";
    private static final String MAC_ESTIMOTE_PURPLE = "E7:7E:57:AF:C4:A0";

    // Views
    private TextView tvVal;

    /*
        List
     */
    private RecyclerView recyclerView;
    private DeviceListAdapter mLeDeviceListAdapter;

    private Handler mHandler;

    private List<BluetoothDevice> bluetoothDeviceList = new ArrayList<>();
    private List<CustomBluetoothDevice> customBluetoothDevices = new ArrayList<>();

    private DecimalFormat decimalFormat = new DecimalFormat("0.00");

    // Views
    private TextView tvVal1X, tvVal1Y, tvVal1Z;
    private TextView tvVal2X, tvVal2Y, tvVal2Z;

    /*
     Charts
      */
    private LineChart mLineChart1, mLineChart2;

    private int rssi;
    private int txPower;

    private float[] accuracyRaw = new float[3];
    private float[] accuracyFiltered = new float[3];
    List<Float> accuracyMeanValueList = new ArrayList<>();


    private boolean isCentralReady = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        for (int i = 0; i < 3; i++) {
            accuracyRaw[i] = accuracyFiltered[i] = 5.0f;
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mLeDeviceListAdapter = new DeviceListAdapter(this, customBluetoothDevices);

        recyclerView.setAdapter(mLeDeviceListAdapter);

        mHandler = new Handler();


        setViews();
        initCharts();
    }


    @Override
    protected void onResume() {
        super.onResume();

        for (int i = 0; i < 3; i++) {
            accuracyRaw[i] = accuracyFiltered[i] = 5.0f;
        }

        scanBTDevice(true);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isCentralReady = true;
            }
        },1000);

    }

    @Override
    protected void onPause() {
        super.onPause();
        scanBTDevice(false);

    }

    @Override
    public void onBLEScanResult(ScanResult result) {

        BluetoothDevice device = result.getDevice();

        final BluetoothLeDevice deviceLe = new BluetoothLeDevice(device, result.getRssi(), result.getScanRecord().getBytes(), System.currentTimeMillis());

        if (BeaconUtils.getBeaconType(deviceLe) == BeaconType.IBEACON) {
            IBeaconDevice beaconDevice = new IBeaconDevice(deviceLe);

            System.out.println("ScanCallback onScanResult() result: " + result.getScanRecord()
                    + ", data: " + result.getScanRecord().getBytes());

            int index = 0;
            float input;
            float alpha = 0.6f;

            switch (beaconDevice.getAddress()) {
                case MAC_ESTIMOTE_GREEN:
                    index = 0;

                    input = (float) beaconDevice.getAccuracy();

                    // Low-pass Filter
                    accuracyFiltered[index] = SensorFilters.lowPass(input, accuracyRaw[index], alpha);
                    // Mean Average Filter
                    accuracyFiltered[index] = SensorFilters.movingAverage(accuracyMeanValueList, accuracyFiltered[index]);

                    accuracyRaw[index] = input;

                    addEntry(mLineChart1, index, accuracyRaw[index]);
                    addEntry(mLineChart2, index, accuracyFiltered[index]);

                    tvVal1X.setText(decimalFormat.format(accuracyRaw[index]));
                    tvVal2X.setText(decimalFormat.format(accuracyFiltered[index]));

                    break;

                case MAC_ESTIMOTE_BLUE:
                    index = 1;

                    input = (float) beaconDevice.getAccuracy();

                    // Low-pass Filter
                    accuracyFiltered[index] = SensorFilters.lowPass(input, accuracyRaw[index], alpha);
                    // Mean Average Filter
                    accuracyFiltered[index] = SensorFilters.movingAverage(accuracyMeanValueList, accuracyFiltered[index]);

                    accuracyRaw[index] = input;

                    addEntry(mLineChart1, index, accuracyRaw[index]);
                    addEntry(mLineChart2, index, accuracyFiltered[index]);

                    tvVal1Y.setText(decimalFormat.format(accuracyRaw[index]));
                    tvVal2Y.setText(decimalFormat.format(accuracyFiltered[index]));
                    break;

                case MAC_ESTIMOTE_PURPLE:
                    index = 2;
                    input = (float) beaconDevice.getAccuracy();

                    // Low-pass Filter
                    accuracyFiltered[index] = SensorFilters.lowPass(input, accuracyRaw[index], alpha);
                    // Mean Average Filter
                    accuracyFiltered[index] = SensorFilters.movingAverage(accuracyMeanValueList, accuracyFiltered[index]);

                    accuracyRaw[index] = input;

                    addEntry(mLineChart1, index, accuracyRaw[index]);
                    addEntry(mLineChart2, index, accuracyFiltered[index]);

                    tvVal1Z.setText(decimalFormat.format(accuracyRaw[index]));
                    tvVal2Z.setText(decimalFormat.format(accuracyFiltered[index]));
                    break;
            }


            if (isCentralReady
                    && accuracyFiltered[0] < 1
                    && accuracyFiltered[0] < 1
                    && accuracyFiltered[2] < 1) {

                isCentralReady = false;
                showToast("Passing...", Toast.LENGTH_SHORT);
            }
        }
    }

    @Override
    public void onBLEScanFailed(int errorCode) {

        switch (errorCode) {
            case ScanCallback.SCAN_FAILED_ALREADY_STARTED:
                scanBTDevice(false);
                scanBTDevice(true);
                showToast("ScanCallback onBLEScanFailed() SCAN_FAILED_ALREADY_STARTED: ", Toast.LENGTH_SHORT);
                break;

            case ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                showToast("ScanCallback onBLEScanFailed() SCAN_FAILED_APPLICATION_REGISTRATION_FAILED: ", Toast.LENGTH_SHORT);

                break;

            default:
                showToast("ScanCallback onBLEScanFailed() errorCode: " + errorCode, Toast.LENGTH_SHORT);
                break;

        }

    }

    private void initCharts() {

        // set an alternative background color
        // mLineChart1.setBackgroundColor(Color.BLACK);

        // get the legend (only possible after setting data)
        Legend l = mLineChart1.getLegend();

        // modify the legend ...
        l.setForm(LegendForm.LINE);
        // l.setTextColor(Color.WHITE);

        XAxis xl = mLineChart1.getXAxis();
        // xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        // Data sets for first chart
        LineDataSet datasetVal1X = createSet("GREEN", ColorTemplate.rgb("#4CAF50"));
        LineDataSet datasetVal1Y = createSet("BLUE", ColorTemplate.rgb("#00E5FF"));
        LineDataSet datasetVal1Z = createSet("PURPLE", ColorTemplate.rgb("#7E57C2"));
        datasetVal1X.addEntry(new Entry(0, 0));
        datasetVal1Y.addEntry(new Entry(0, 0));
        datasetVal1Z.addEntry(new Entry(0, 0));

        // Data for first chart
        LineData data1 = new LineData(datasetVal1X, datasetVal1Y, datasetVal1Z);
        mLineChart1.setData(data1);
        mLineChart1.getDescription().setText("Raw Beacon");

        // Data sets for second chart
        LineDataSet datasetVal2X = createSet("GREEN", ColorTemplate.rgb("#4CAF50"));
        LineDataSet datasetVal2Y = createSet("BLUE", ColorTemplate.rgb("#00E5FF"));
        LineDataSet datasetVal2Z = createSet("PURPLE", ColorTemplate.rgb("#7E57C2"));
        datasetVal2X.addEntry(new Entry(0, 0));
        datasetVal2Y.addEntry(new Entry(0, 0));
        datasetVal2Z.addEntry(new Entry(0, 0));

        // Data for second chart
        LineData data2 = new LineData(datasetVal2X, datasetVal2Y, datasetVal2Z);
        mLineChart2.setData(data2);
        mLineChart2.getDescription().setText("Filtered Beacon");

    }

    private void addEntry(LineChart chart, int index, float value) {
        LineData data = chart.getData();

        if (data != null) {
            // Get dataset with given index
            ILineDataSet set = data.getDataSetByIndex(index);
            // If dataset with given index null return

            if (set == null) {
                return;
            }

            // add entry to dataset with index of data
            data.addEntry(new Entry(set.getEntryCount(), value), index);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            chart.notifyDataSetChanged();
            // limit the number of visible entries
            chart.setVisibleXRangeMaximum(120);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);
            // move to the latest entry
            chart.moveViewToX(data.getEntryCount());
        }
    }

    private LineDataSet createSet(String label, int color) {

        LineDataSet set = new LineDataSet(null, label);
        set.setAxisDependency(AxisDependency.LEFT);
        set.setColor(color);
        // set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setDrawCircles(false);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    private void setViews() {

        // TextViews for first measurements
        tvVal1X = (TextView) findViewById(R.id.tvVal1X);
        tvVal1Y = (TextView) findViewById(R.id.tvVal1Y);
        tvVal1Z = (TextView) findViewById(R.id.tvVal1Z);
        // TextViews for second measurements
        tvVal2X = (TextView) findViewById(R.id.tvVal2X);
        tvVal2Y = (TextView) findViewById(R.id.tvVal2Y);
        tvVal2Z = (TextView) findViewById(R.id.tvVal2Z);

        Button buttonReset = findViewById(R.id.buttonReset);

        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCentralReady = true;
            }
        });

        // Charts
        mLineChart1 = (LineChart) findViewById(R.id.chart1);
        mLineChart2 = (LineChart) findViewById(R.id.chart2);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Toast.makeText(this, "SELECTED", Toast.LENGTH_SHORT).show();

        switch (item.getItemId()) {
            case R.id.menu_scan:
//                bluetoothDeviceList.clear();
//                customBluetoothDevices.clear();
//                mLeDeviceListAdapter.updateList(customBluetoothDevices);
//                scanBTDevice(true);
                break;
            case R.id.menu_stop:
//                scanBTDevice(false);
                break;
        }
        return true;
    }


}
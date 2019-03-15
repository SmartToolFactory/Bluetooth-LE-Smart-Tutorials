package com.stfactory.tutorial6_1beaconbasics;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

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

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // Views
    private TextView tvVal;

    // Charts
    private LineChart mLineChart;
    /*
     * Datasets, Dataset lists and datas for charts
     */

    // Dataset for first chart
    private LineDataSet dataSet;

    // Data for chart
    private LineData data;

    // Sensor manager and sensors
    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magneticFieldSensor;

    // Values from accelerometer and magnetic field sensor
    private final float[] mAccelerometerReading = new float[3];
    private final float[] mMagnetometerReading = new float[3];
    // Rotation matrix
    private final float[] mRotationMatrix = new float[9];
    private final float[] I = new float[9];
    // Orientation angles(azimuth, pitch, roll)
    private final float[] mOrientationAngles = new float[3];


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // Sensors
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticFieldSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        setViews();
        setCharts();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
        // You must implement this callback in your code.
    }

    @Override
    protected void onResume() {
        super.onResume();

        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magneticFieldSensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Don't receive any more updates from either sensor.
        mSensorManager.unregisterListener(this);
    }

    // Get readings from accelerometer and magnetometer. To simplify
    // calculations, consider storing these readings as unit vectors.
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, mAccelerometerReading, 0, mAccelerometerReading.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, mMagnetometerReading, 0, mMagnetometerReading.length);
        }
        updateOrientationAngles();
    }

    // Compute the three orientation angles based on the most recent readings
    // from the device's accelerometer and magnetometer.
    public void updateOrientationAngles() {

        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(mRotationMatrix, I, mAccelerometerReading, mMagnetometerReading);


        float[] orientationInDegrees = new float[3];

        orientationInDegrees[0] = (float) (Math.round(((Math.toDegrees(mAccelerometerReading[0]) + 360) % 360) * 10) / 10);


        tvVal.setText("Value: " + orientationInDegrees[0]);


        addEntry(mLineChart, 0, orientationInDegrees[0]);


    }


    private void setCharts() {

        // set an alternative background color
        // mLineChart1.setBackgroundColor(Color.BLACK);

        // get the legend (only possible after setting data)
        Legend l = mLineChart.getLegend();

        // modify the legend ...
        l.setForm(LegendForm.LINE);
        // l.setTextColor(Color.WHITE);

        XAxis xl = mLineChart.getXAxis();
        // xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(true);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        // Datasets for chart1
        dataSet = createSet("Azimuth", ColorTemplate.JOYFUL_COLORS[0]);
        dataSet.addEntry(new Entry(0, 0));
        data = new LineData(dataSet);
        mLineChart.setData(data);
        // TODO Chart description
        mLineChart.getDescription().setText("Accelerometer+Magnetic Field");
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
        mLineChart = (LineChart) findViewById(R.id.chart);
        tvVal = (TextView) findViewById(R.id.tvValue);
    }

}
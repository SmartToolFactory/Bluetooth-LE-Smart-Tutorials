package com.example.tutorial8_blebeaconservice;

import java.util.List;

public class SensorFilters {

    private static final float RAD_TO_DEG = (float) (180 / Math.PI);
    private static final int MAX_SAMPLE_SIZE = 10;
    private static final float ALPHA = .1f;

    public static float movingAverage(List<Float> values, float output) {
        if (values.size() == MAX_SAMPLE_SIZE) {
            values.remove(0);
        }
        values.add(output);

        float total = 0;
        for (float item : values) {
            total += item;
        }
        return total / values.size();

    }

    public static float movingAverage(List<Float> values, float output, int sampleSize) {
        if (values.size() == sampleSize) {
            values.remove(0);
        }
        values.add(output);

        float total = 0;
        for (float item : values) {
            total += item;
        }
        return total / values.size();

    }

    /**
     * Filters noise from signal with threshold ALPHA value, low-frequency </br>
     * <strong>y[i] := y[i-1] + a * (x[i] - y[i-1])</strong>
     *
     * @param input  Values retrieved from sensor
     * @param output Current values and final values
     * @return LP filtered output values
     */
    public static float[] lowPass(float[] input, float[] output) {
        if (output == null)
            return input;

        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    /**
     * Filters noise from signal with threshold ALPHA value, low-frequency </br>
     * <strong>y[i] := y[i-1] + a * (x[i] - y[i-1])</strong>
     *
     * @param input
     * @param output
     * @param alpha
     * @return
     */
    public static float lowPass(float input, float output, float alpha) {
        return (output + alpha * (input - output));

    }

    public static List<Float> roll(List<Float> list, float newMember) {
        if (list.size() == MAX_SAMPLE_SIZE) {
            list.remove(0);
        }
        list.add(newMember);
        return list;
    }

    public static float averageList(List<Float> tallyUp) {

        if (tallyUp == null || tallyUp.size() == 0) {
            return 0;
        }

        float total = 0;
        for (float item : tallyUp) {
            total += item;
        }
        total = total / tallyUp.size();

        return total;
    }

    /**
     * Converts two vectors into an angle in degrees
     *
     * @param y magnitude of the vector
     * @param x magnitude of the vector
     * @return angle in degrees
     */
    public static float convertToAngle(float y, float x) {
        return (float) (Math.atan2(y, x) * RAD_TO_DEG);
    }

}

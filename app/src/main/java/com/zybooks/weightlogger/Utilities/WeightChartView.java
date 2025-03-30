package com.zybooks.weightlogger.Utilities;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.zybooks.weightlogger.Data.WeightDatabaseHelper;
import com.zybooks.weightlogger.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeightChartView extends View {
    private List<WeightDatabaseHelper.WeightEntry> entries = new ArrayList<>();
    private double goalWeight = 0;
    private boolean hasData = false;

    private final Paint linePaint = new Paint();
    private final Paint pointPaint = new Paint();
    private final Paint textPaint = new Paint();
    private final Paint gridPaint = new Paint();
    private final Paint goalPaint = new Paint();
    private final Paint axisLabelPaint = new Paint();

    private float minWeight = 0;
    private float maxWeight = 0;
    private final Path linePath = new Path();
    private final List<PointF> points = new ArrayList<>();
    private final int paddingLeft = 80;
    private final int paddingRight = 40;
    private final int paddingTop = 40;
    private final int paddingBottom = 80;
    private final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final SimpleDateFormat outputFormat = new SimpleDateFormat("MM/dd", Locale.US);

    public WeightChartView(Context context) {
        super(context);
        init();
    }

    public WeightChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WeightChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Line paint
        linePaint.setColor(ContextCompat.getColor(getContext(), R.color.teal_700));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(4f);
        linePaint.setAntiAlias(true);

        // Point paint
        pointPaint.setColor(ContextCompat.getColor(getContext(), R.color.teal_700));
        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setAntiAlias(true);

        // Text paint
        textPaint.setColor(ContextCompat.getColor(getContext(), R.color.text_primary_light));
        textPaint.setTextSize(28f);
        textPaint.setAntiAlias(true);

        // Grid paint
        gridPaint.setColor(Color.LTGRAY);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1f);
        gridPaint.setAlpha(100);

        // Goal line paint
        goalPaint.setColor(ContextCompat.getColor(getContext(), R.color.amber_700));
        goalPaint.setStyle(Paint.Style.STROKE);
        goalPaint.setStrokeWidth(3f);
        goalPaint.setPathEffect(new android.graphics.DashPathEffect(new float[]{10, 10}, 0));

        // Axis label paint
        axisLabelPaint.setColor(ContextCompat.getColor(getContext(), R.color.text_secondary_light));
        axisLabelPaint.setTextSize(24f);
        axisLabelPaint.setAntiAlias(true);
    }

    public void setData(List<WeightDatabaseHelper.WeightEntry> entries, double goalWeight) {
        this.entries = new ArrayList<>(entries);
        // Sort by date from oldest to newest
        this.entries.sort((entry1, entry2) -> {
            try {
                Date date1 = inputFormat.parse(entry1.getDate());
                Date date2 = inputFormat.parse(entry2.getDate());
                if (date1 != null && date2 != null) {
                    return date1.compareTo(date2);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return 0;
        });

        this.goalWeight = goalWeight;

        // Calculate min and max weight
        if (!entries.isEmpty()) {
            hasData = true;
            minWeight = Float.MAX_VALUE;
            maxWeight = Float.MIN_VALUE;

            for (WeightDatabaseHelper.WeightEntry entry : entries) {
                float weight = (float) entry.getWeight();
                if (weight < minWeight) minWeight = weight;
                if (weight > maxWeight) maxWeight = weight;
            }

            // Include goal weight in range
            if (goalWeight > 0) {
                if (goalWeight < minWeight) minWeight = (float) goalWeight;
                if (goalWeight > maxWeight) maxWeight = (float) goalWeight;
            }

            // Add padding to range
            float range = maxWeight - minWeight;
            minWeight = Math.max(0, minWeight - range * 0.1f);
            maxWeight = maxWeight + range * 0.1f;
        }

        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        if (!hasData || entries.isEmpty()) {
            // Draw no data message
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("No weight data available", getWidth() / 2f, getHeight() / 2f, textPaint);
            return;
        }

        int width = getWidth();
        int height = getHeight();
        int chartWidth = width - paddingLeft - paddingRight;
        int chartHeight = height - paddingTop - paddingBottom;

        // Clear previous points
        points.clear();
        linePath.reset();

        // Draw axes
        canvas.drawLine(paddingLeft, height - paddingBottom, width - paddingRight, height - paddingBottom, gridPaint); // X-axis
        canvas.drawLine(paddingLeft, paddingTop, paddingLeft, height - paddingBottom, gridPaint); // Y-axis

        // Draw horizontal grid lines and Y-axis labels
        int numYLabels = 5;
        float weightRange = maxWeight - minWeight;
        for (int i = 0; i <= numYLabels; i++) {
            float y = height - paddingBottom - ((float) (chartHeight * i) / numYLabels);
            canvas.drawLine(paddingLeft, y, width - paddingRight, y, gridPaint);

            float labelValue = minWeight + (weightRange * i / numYLabels);
            axisLabelPaint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(String.format(Locale.US, "%.1f", labelValue), paddingLeft - 10, y + axisLabelPaint.getTextSize() / 3, axisLabelPaint);
        }

        // Calculate points for line
        for (int i = 0; i < entries.size(); i++) {
            WeightDatabaseHelper.WeightEntry entry = entries.get(i);
            float x = paddingLeft + ((float) (chartWidth * i) / (entries.size() - 1));
            if (entries.size() == 1) {
                x = paddingLeft + chartWidth / 2f;
            }

            float normalizedWeight = (float) ((entry.getWeight() - minWeight) / weightRange);
            float y = height - paddingBottom - (normalizedWeight * chartHeight);

            points.add(new PointF(x, y));

            if (i == 0) {
                linePath.moveTo(x, y);
            } else {
                linePath.lineTo(x, y);
            }

            // Draw point
            canvas.drawCircle(x, y, 8, pointPaint);

            // Draw weight value
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(String.format(Locale.US, "%.1f", entry.getWeight()), x, y - 15, textPaint);

            // Draw X-axis label (date)
            if (i == 0 || i == entries.size() - 1 || entries.size() <= 5 || i % (entries.size() / 5) == 0) {
                try {
                    Date date = inputFormat.parse(entry.getDate());
                    String formattedDate = date != null ? outputFormat.format(date) : entry.getDate();
                    axisLabelPaint.setTextAlign(Paint.Align.CENTER);
                    canvas.drawText(formattedDate, x, height - paddingBottom + 30, axisLabelPaint);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        // Draw the line connecting points
        canvas.drawPath(linePath, linePaint);

        // Draw goal weight line if available
        if (goalWeight > 0) {
            float normalizedGoal = (float) ((goalWeight - minWeight) / weightRange);
            float goalY = height - paddingBottom - (normalizedGoal * chartHeight);

            canvas.drawLine(paddingLeft, goalY, width - paddingRight, goalY, goalPaint);

            // Draw goal label
            textPaint.setColor(ContextCompat.getColor(getContext(), R.color.amber_700));
            textPaint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText("Goal: " + String.format(Locale.US, "%.1f", goalWeight), paddingLeft + 10, goalY - 10, textPaint);
            textPaint.setColor(ContextCompat.getColor(getContext(), R.color.text_primary_light)); // Reset color
        }
    }
}
package com.example.project2025.Utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class LineChartView extends View {
    private Paint axisPaint, textPaint, linePaint, circlePaint, titlePaint, gridPaint;
    private List<Float> values = new ArrayList<>();
    private List<String> labels = new ArrayList<>();
    private String title = "";

    public LineChartView(Context context) {
        super(context);
        init();
    }

    public LineChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LineChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisPaint.setColor(Color.DKGRAY);
        axisPaint.setStrokeWidth(dp(2));

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.LTGRAY);
        gridPaint.setStrokeWidth(dp(1));

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.DKGRAY);
        textPaint.setTextSize(sp(12));

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#3F51B5"));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(dp(3));

        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(Color.parseColor("#3F51B5"));
        circlePaint.setStyle(Paint.Style.FILL);

        titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(Color.BLACK);
        titlePaint.setTextSize(sp(16));
        titlePaint.setFakeBoldText(true);
    }

    public void setData(List<Float> values, List<String> labels, String title) {
        this.values = values;
        this.labels = labels;
        this.title = title;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (values == null || values.isEmpty()) return;

        int w = getWidth();
        int h = getHeight();

        int paddingLeft = dp(44);
        int paddingRight = dp(24);
        int paddingTop = dp(16);
        int paddingBottom = dp(56);

        // Draw Title
        if (title != null && !title.isEmpty()) {
            float tw = titlePaint.measureText(title);
            canvas.drawText(title, (w - tw) / 2f, paddingTop + dp(20), titlePaint);
            paddingTop += dp(40); 
        }

        float chartW = w - paddingLeft - paddingRight;
        float chartH = h - paddingTop - paddingBottom;

        float maxVal = 0;
        for (Float v : values) if (v > maxVal) maxVal = v;
        if (maxVal == 0) maxVal = 1;

        // Draw grid lines (5 horizontal)
        int gridCount = 5;
        for (int i = 0; i <= gridCount; i++) {
            float y = paddingTop + chartH * i / gridCount;
            canvas.drawLine(paddingLeft, y, w - paddingRight, y, gridPaint);
        }

        // Draw axes
        canvas.drawLine(paddingLeft, paddingTop, paddingLeft, h - paddingBottom, axisPaint);
        canvas.drawLine(paddingLeft, h - paddingBottom, w - paddingRight, h - paddingBottom, axisPaint);

        // Draw Y-axis labels
        for (int i = 0; i <= gridCount; i++) {
            float val = maxVal - maxVal * i / gridCount;
            float y = paddingTop + chartH * i / gridCount + dp(4);
            canvas.drawText(String.valueOf((int) val), dp(8), y, textPaint);
        }

        // Draw X-axis labels and data line
        float stepX = chartW / (values.size() - 1);
        Path path = new Path();

        for (int i = 0; i < values.size(); i++) {
            float x = paddingLeft + i * stepX;
            float y = paddingTop + chartH - (values.get(i) / maxVal) * chartH;

            // draw x-axis labels
            String lbl = labels.get(i);
            float tw = textPaint.measureText(lbl);
            canvas.drawText(lbl, x - tw / 2f, h - dp(36), textPaint);

            // draw line path
            if (i == 0) path.moveTo(x, y);
            else path.lineTo(x, y);
        }

        canvas.drawPath(path, linePaint);

        // Draw circles on points
        for (int i = 0; i < values.size(); i++) {
            float x = paddingLeft + i * stepX;
            float y = paddingTop + chartH - (values.get(i) / maxVal) * chartH;
            canvas.drawCircle(x, y, dp(4), circlePaint);
        }
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private float sp(int v) {
        return v * getResources().getDisplayMetrics().scaledDensity;
    }
}

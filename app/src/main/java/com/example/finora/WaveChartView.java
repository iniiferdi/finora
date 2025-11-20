package com.example.finora;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class WaveChartView extends View {

    private Paint linePaint;
    private Paint pointPaint;
    private Paint pointStrokePaint;

    private int selectedIndex = 3; // Oktober (index ke-3 dari 6 bulan)

    // Data posisi Y (0.0 – 1.0 dari tinggi)
    private float[] points = new float[]{
            0.65f, 0.30f, 0.80f, 0.55f, 0.70f, 0.35f
    };

    public WaveChartView(Context context) {
        super(context);
        init();
    }

    public WaveChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {

        linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(8f);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setAntiAlias(true);

        pointPaint = new Paint();
        pointPaint.setColor(Color.WHITE);
        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setAntiAlias(true);

        pointStrokePaint = new Paint();
        pointStrokePaint.setColor(Color.BLACK);
        pointStrokePaint.setStrokeWidth(6f);
        pointStrokePaint.setStyle(Paint.Style.STROKE);
        pointStrokePaint.setAntiAlias(true);

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();

        float step = w / (points.length - 1);

        Path path = new Path();
        path.moveTo(0, h * points[0]);

        // Buat kurva halus model cubic Bézier
        for (int i = 0; i < points.length - 1; i++) {
            float x1 = step * i;
            float y1 = h * points[i];

            float x2 = step * (i + 1);
            float y2 = h * points[i + 1];

            float midX = (x1 + x2) / 2;

            path.cubicTo(
                    midX, y1,
                    midX, y2,
                    x2, y2
            );
        }

        canvas.drawPath(path, linePaint);

        // --- Gambar titik bulan aktif ---
        float px = step * selectedIndex;
        float py = h * points[selectedIndex];

        float radius = 22f;

        canvas.drawCircle(px, py, radius, pointPaint);
        canvas.drawCircle(px, py, radius, pointStrokePaint);
    }
}

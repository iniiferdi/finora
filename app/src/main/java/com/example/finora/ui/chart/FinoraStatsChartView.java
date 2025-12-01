package com.example.finora.ui.chart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class FinoraStatsChartView extends View {

    private float[] income = new float[]{};
    private float[] expense = new float[]{};

    private Paint incomePaint, expensePaint, gridPaint, textPaint,
            tooltipPaint, tooltipTextPaint, highlightPaint;

    private int selectedIndex = -1;

    public FinoraStatsChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {

        // ===== Minimalist Base Paints =====

        // Income (Putih → Abu muda)
        incomePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        incomePaint.setStyle(Paint.Style.FILL);

        // Expense (Abu → Abu lebih gelap)
        expensePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        expensePaint.setStyle(Paint.Style.FILL);

        // Grid abu-abu super soft
        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(0x13000000);    // sangat halus
        gridPaint.setStrokeWidth(1.2f);

        // Teks hitam soft
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFF222222);
        textPaint.setTextSize(32);

        // Tooltip modern putih
        tooltipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tooltipPaint.setColor(Color.WHITE);
        tooltipPaint.setShadowLayer(16, 0, 8, 0x22000000);

        tooltipTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tooltipTextPaint.setColor(0xFF111111);
        tooltipTextPaint.setTextSize(34);

        // Highlight soft black glow
        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setColor(0x1A000000);
        highlightPaint.setStyle(Paint.Style.FILL);
    }

    public void setData(float[] incomeData, float[] expenseData) {
        this.income = incomeData;
        this.expense = expenseData;
        selectedIndex = -1;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();

        float padding = 80f;
        float maxValue = Math.max(getMax(income), getMax(expense));
        float dx = (w - padding * 2) / income.length;
        float barWidth = dx * 0.28f;

        // ===== GRID (halus banget) =====
        for (int i = 1; i <= 4; i++) {
            float y = h - padding - (h - padding * 2) * (i / 4f);
            canvas.drawLine(padding, y, w - padding, y, gridPaint);
        }

        for (int i = 0; i < income.length; i++) {

            float xCenter = padding + dx * i + dx / 2;

            float incomeTop = h - padding - (income[i] / maxValue) * (h - padding * 2);
            float expenseTop = h - padding - (expense[i] / maxValue) * (h - padding * 2);

            RectF incomeRect = new RectF(
                    xCenter - barWidth - 8,
                    incomeTop,
                    xCenter - 8,
                    h - padding
            );

            RectF expenseRect = new RectF(
                    xCenter + 8,
                    expenseTop,
                    xCenter + barWidth + 8,
                    h - padding
            );

            // ===== Monochrome Gradients =====
            incomePaint.setShader(new LinearGradient(
                    0, incomeTop, 0, h,
                    0xFFFFFFFF,         // putih
                    0x11CCCCCC,         // abu super soft
                    Shader.TileMode.CLAMP
            ));

            expensePaint.setShader(new LinearGradient(
                    0, expenseTop, 0, h,
                    0xFFDDDDDD,         // abu muda
                    0x22000000,         // hitam tipis
                    Shader.TileMode.CLAMP
            ));

            // Rounded bar
            canvas.drawRoundRect(incomeRect, 22, 22, incomePaint);
            canvas.drawRoundRect(expenseRect, 22, 22, expensePaint);

            // ===== HIGHLIGHT =====
            if (selectedIndex == i) {
                canvas.drawRoundRect(incomeRect, 24, 24, highlightPaint);
                canvas.drawRoundRect(expenseRect, 24, 24, highlightPaint);

                drawTooltip(
                        canvas,
                        xCenter,
                        Math.min(incomeTop, expenseTop) - 50,
                        income[i],
                        expense[i]
                );
            }
        }
    }

    private void drawTooltip(Canvas canvas, float x, float y, float income, float expense) {
        String text1 = "Income: " + (int) income;
        String text2 = "Expense: " + (int) expense;

        float padding = 28f;
        float width = Math.max(
                tooltipTextPaint.measureText(text1),
                tooltipTextPaint.measureText(text2)
        ) + padding * 2;

        float height = 130;

        RectF bubble = new RectF(
                x - width / 2,
                y - height,
                x + width / 2,
                y
        );

        canvas.drawRoundRect(bubble, 26, 26, tooltipPaint);

        canvas.drawText(text1, bubble.left + padding, bubble.top + 48, tooltipTextPaint);
        canvas.drawText(text2, bubble.left + padding, bubble.top + 100, tooltipTextPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float padding = 80;
        float dx = (getWidth() - padding * 2) / income.length;

        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            int index = (int) ((x - padding) / dx);
            if (index >= 0 && index < income.length) {
                selectedIndex = index;
                invalidate();
            }
        }
        return true;
    }

    private float getMax(float[] arr) {
        float m = arr[0];
        for (float v : arr) if (v > m) m = v;
        return m;
    }
}

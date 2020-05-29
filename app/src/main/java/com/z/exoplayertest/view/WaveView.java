package com.z.exoplayertest.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import com.z.exoplayertest.R;

import java.util.Random;

import androidx.annotation.Nullable;

public class WaveView extends View {
    private Paint mPaint;

    private Path mPath;

    private float mDrawHeight;

    private float mDrawWidth;

    private float amplitude[];
    private float waveWidth;
    private float waveStart, waveEnd;
    private boolean isMax = true;
    private Context context;

    public WaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    private void init() {
        mPath = new Path();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStrokeWidth(1.5f);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(context.getColor(R.color.colorTitle));
        CornerPathEffect cornerPathEffect = new CornerPathEffect(300);
        mPaint.setPathEffect(cornerPathEffect);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        widthMeasureSpec = measureWidth(widthMeasureSpec);
        heightMeasureSpec = measureHeight(heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        mDrawWidth = getMeasuredWidth() - paddingLeft - paddingRight;
        mDrawHeight = getMeasuredHeight() - paddingTop - paddingBottom;
        initOthers();
    }

    public void setWaveWidth(float waveWidth, boolean isMax) {
        this.waveWidth = waveWidth;
        this.isMax = isMax;
        invalidate();
    }

    private void initOthers() {
        waveStart = (mDrawWidth - waveWidth) / 2;
        waveEnd = waveStart + waveWidth;
        float mAmplitude = isMax ? mDrawHeight / 2 : mDrawHeight / 4;
        amplitude = new float[20];
        Random random = new Random();
        for (int i = 0; i < 20; i++) {
            if (i % 2 == 0) {
                amplitude[i] = mDrawHeight / 2 + (random.nextFloat() + 0.3f) * mAmplitude;
            } else {
                amplitude[i] = mDrawHeight / 2 - (random.nextFloat() + 0.3f) * mAmplitude;
            }
        }
    }

    private int measureWidth(int spec) {
        int mode = MeasureSpec.getMode(spec);
        if (mode == MeasureSpec.UNSPECIFIED) {
            DisplayMetrics dm = getResources().getDisplayMetrics();
            int width = dm.widthPixels;
            spec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        } else if (mode == MeasureSpec.AT_MOST) {
            int value = MeasureSpec.getSize(spec);
            spec = MeasureSpec.makeMeasureSpec(value, MeasureSpec.EXACTLY);
        }
        return spec;
    }

    private int measureHeight(int spec) {
        int mode = MeasureSpec.getMode(spec);
        if (mode == MeasureSpec.EXACTLY) {
            return spec;
        }

        // 其他模式下的最大高度
        int height = (int) dip2px(50);

        if (mode == MeasureSpec.AT_MOST) {
            int preValue = MeasureSpec.getSize(spec);
            if (preValue < height) {
                height = preValue;
            }
        }
        spec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        return spec;
    }

    private float dip2px(float dp) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, dm);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPath.reset();
        mPath.moveTo(0, mDrawHeight / 2);
        mPath.lineTo(waveStart, mDrawHeight / 2);
        //使前端直线慢慢过渡，不要太平滑
        for (int i = 0; i < 6; i++) {
            if (amplitude[0] > 0) {
                mPath.lineTo(waveStart, mDrawHeight / 2 + i * 2);
            } else {
                mPath.lineTo(waveStart, mDrawHeight / 2 - i * 2);
            }
        }

        for (int i = 0; i < amplitude.length; i++) {
            mPath.lineTo(waveStart + i * waveWidth / 20, amplitude[i]);
        }
        mPath.lineTo(waveEnd, mDrawHeight / 2);
        //使尾端直线慢慢过渡，不要太平滑
        for (int i = 0; i < 6; i++) {
            mPath.lineTo(waveEnd + i * 2, mDrawHeight / 2);
        }
        mPath.lineTo(mDrawWidth + 40, mDrawHeight / 2);
        canvas.drawPath(mPath, mPaint);
    }
}
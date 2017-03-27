package com.kj.anim.wave.doublewaves.view;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;

import com.kj.anim.wave.doublewaves.R;

/**
 * @author kangjian
 * @version 1.0
 * @title WaveViewDraw
 * @description 运用ValueAnimator 点的移动来实现
 * @created 2017/3/24 23:11
 * @changeRecord [修改记录] <br/>
 */

public class WaveViewDraw extends View {

    public static final String TAG = WaveViewDraw.class.getSimpleName();
    public static final float START_POSITION = 0f;

    private static boolean WAVE_BITMAP = true;

    private Paint mPaint;

    private Drawable wave_total;

    private Bitmap wave_total_bp;

    private long duration;

    private Point currentPoint; //控制浪位置的坐标点
    private Rect src;  //需要绘图的大小
    private Rect dst;  //屏幕上绘画的位置

    private boolean isFirstIn = true;

    public WaveViewDraw(Context context) {
        super(context);
    }

    public WaveViewDraw(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(attrs, context);
    }

    public WaveViewDraw(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs, context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WaveViewDraw(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(attrs, context);
    }

    private void initView(AttributeSet attrs, Context context) {

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.WaveViewDraw);   //动态获取的方式
        duration = (long) typedArray.getFloat(R.styleable.WaveViewDraw_wave_duration, 16000f);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (WAVE_BITMAP)
            wave_total = ContextCompat.getDrawable(context, R.drawable.ic_wave_total);
        else
            wave_total = ContextCompat.getDrawable(context, R.drawable.ic_wave_small);
        wave_total_bp = drawableToBitamp(wave_total);

        src = new Rect();
        dst = new Rect();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (currentPoint == null) {
            currentPoint = new Point(START_POSITION, START_POSITION);
            drawWave(canvas);
            startWaveAnimation();
        } else {
            drawWave(canvas);
        }

    }

    private Bitmap drawableToBitamp(Drawable drawable) {
        Bitmap bitmap;
        BitmapDrawable bd = (BitmapDrawable) drawable;
        bitmap = bd.getBitmap();
        return bitmap;
    }

    private void drawWave(Canvas canvas) {
        float left = currentPoint.getX();

        int offSet = (int) left;
        int start_offSet = 1920 - offSet;

        if (WAVE_BITMAP) {
            drawImage(canvas, wave_total_bp, 0, 0, offSet, 1080, start_offSet, 0); //底层浪的,左侧
            drawImage(canvas, wave_total_bp, offSet, 0, 1920, 1080, 0, 0); //先画底层浪,右侧
            drawImage(canvas, wave_total_bp, 0, 0, offSet, 1080, start_offSet, 1080);     //上层浪,左侧
            drawImage(canvas, wave_total_bp, offSet, 0, 1920, 1080, 0, 1080);             //上层浪,右侧
        } else {

            drawImage(canvas, wave_total_bp, 0, 0, offSet, 230, start_offSet, 0); //底层浪的,左侧
            drawImage(canvas, wave_total_bp, offSet, 0, 1920, 230, 0, 0); //先画底层浪,右侧
            drawImage(canvas, wave_total_bp, 0, 0, offSet, 230, start_offSet, 230);     //上层浪,左侧
            drawImage(canvas, wave_total_bp, offSet, 0, 1920, 230, 0, 230);             //上层浪,右侧
        }
    }

    /**
     * 画浪
     *
     * @param canvas
     * @param bitmap
     * @param x      绘制起点 left
     * @param y      绘制起点 top
     * @param w      绘制终点 右上角
     * @param h      绘制终点 右下角
     * @param bx     需要绘图的 left
     * @param by     需要绘图的 top
     */
    private void drawImage(Canvas canvas, Bitmap bitmap, int x, int y, int w, int h, int bx, int by) {
        src.left = bx;
        src.top = by;
        src.right = bx + w;
        src.bottom = by + h;
        dst.left = x;
        dst.top = y;
        dst.right = x + w;
        dst.bottom = y + h;
        canvas.drawBitmap(bitmap, src, dst, mPaint);
    }

    /**
     * 动画
     */
    private void startWaveAnimation() {
        Point startPoint = new Point(0, START_POSITION);
        Point endPoint = new Point(1920, START_POSITION);
        ValueAnimator anim = ValueAnimator.ofObject(new PointEvaluator(), startPoint, endPoint);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentPoint = (Point) animation.getAnimatedValue();
                invalidate();
            }
        });
        anim.setInterpolator(new LinearInterpolator());
        anim.setDuration(duration);
        anim.setRepeatMode(ValueAnimator.RESTART);
        anim.setRepeatCount(Animation.INFINITE);
        anim.start();
    }

    /**
     * 导引浪移动的点类
     */
    private class Point {
        private float x;
        private float y;

        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }
    }

    /**
     * 计算过渡值的类
     */
    private class PointEvaluator implements TypeEvaluator {
        @Override
        public Object evaluate(float fraction, Object startValue, Object endValue) {
            Point startPoint = (Point) startValue;
            Point endPoint = (Point) endValue;
            float x = startPoint.getX() + fraction * (endPoint.getX() - startPoint.getX());
            float y = startPoint.getY() + fraction * (endPoint.getY() - startPoint.getY());
            Point point = new Point(x, y);
            return point;
        }
    }
}
package com.kj.anim.wave.doublewaves.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.kj.anim.wave.doublewaves.R;

/**
 * @author kangjian
 * @version 1.0
 * @title DoubleWavesCalculateView
 * @description 双层浪view
 *              background_color:        整个view的背景颜色 [默认值: TRANSPARENT(透明)]
 *              wave_front_acolor:       最上层浪的初始浪尖带alpha的颜色值 [默认值:0x4d7743fb]
 *              wave_back_acolor:        下层浪的初始浪尖带alpha的颜色值   [默认值:0x666837f4]
 *              wave_front_acolor_after: 最上层浪在整个view的bottom位置的带alpha的颜色值 [默认值:0x007743fb]
 *              wave_back_acolor_after:  下层浪在整个view的bottom位置的带alpha的颜色值   [默认值:0x006837f4]
 *              wave_front_alpha:        上层浪画笔的alpha值 [默认值255,既不透明]
 *              wave_back_alpha:         下层浪画笔的alpha值 [默认值255,既不透明]
 *              wave_weight:             两个浪在屏幕的位置,越小位置越高 0.0-1.0 [默认值0.5 , 即在屏幕中央]
 * @created 2017/3/27 17:53
 * @changeRecord [修改记录] <br/>
 */

public class DoubleWavesCalculateView extends View {

    private static final String TAG = "DoubleWavesCal";

    private static final int LARGE = 0;
    private static final int MIDDLE = 1;
    private static final int LITTLE = 2;

    private final float DEFAULT_WAVE_ALPHA = 1f; // 透明度
    private final float DEFAULT_WAVE_AMPLITUDE = 0.5f; // 振幅

    private final int WAVE_HEIGHT_LARGE = 52;
    private final int WAVE_HEIGHT_MIDDLE = 8;
    private final int WAVE_HEIGHT_LITTLE = 5;

    private final float WAVE_LENGTH_MULTIPLE_LARGE = 1.5f;
    private final float WAVE_LENGTH_MULTIPLE_MIDDLE = 1f;
    private final float WAVE_LENGTH_MULTIPLE_LITTLE = 0.5f;

    private final float WAVE_HZ_FAST = 0.12f;
    private final float WAVE_HZ_NORMAL = 0.09f;
    private final float WAVE_HZ_SLOW = 0.02f;

    public final int DEFAULT_ABOVE_WAVE_ALPHA = 255;
    public final int DEFAULT_BLOW_WAVE_ALPHA = 255;

    private final float X_SPACE = 20;
    private final double PI2 = 2 * Math.PI;

    private Path mAboveWavePath_front = new Path();
    private Path mAboveWavePath_back = new Path();

    private Paint mAboveWavePaint_front = new Paint();
    private Paint mAboveWavePaint_back = new Paint();
    private Paint mBlowWavePaint = new Paint();

    //颜色
    private int mAboveWaveColor_front;
    private int mAboveWaveColor_back;
    private int mAboveWaveColor_afront;
    private int mAboveWaveColor_aback;
    private int mAboveWaveColor_afront_after;
    private int mAboveWaveColor_aback_after;
    private int mBlowWaveColor;

    //透明度
    private int mWave_front_alpha;
    private int mWave_back_alpha;

    private float mWaveWeight;

    private float mWaveMultiple;
    private float mWaveLength;
    private int mWaveHeight;
    private float mMaxRight;
    private float mWaveHz;

    private float mAboveOffset = 0.0f;
    private float mBlowOffset;

    private RefreshProgressRunnable mRefreshProgressRunnable;

    private int left, right, bottom;

    private double omega;

    public DoubleWavesCalculateView(Context context) {
        this(context, null);
    }

    public DoubleWavesCalculateView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DoubleWavesCalculateView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.DoubleWavesView);
        mAboveWaveColor_afront = attributes.getColor(R.styleable.DoubleWavesView_wave_front_acolor, 0x4d7743fb);       //上层浪从浪尖起始的颜色
        mAboveWaveColor_aback = attributes.getColor(R.styleable.DoubleWavesView_wave_back_acolor, 0x666837f4);         //下层狼从浪尖起始的颜色
        mAboveWaveColor_afront_after = attributes.getColor(R.styleable.DoubleWavesView_wave_front_acolor_after, 0x007743fb);   //上层浪到屏幕底部的颜色
        mAboveWaveColor_aback_after = attributes.getColor(R.styleable.DoubleWavesView_wave_back_acolor_after, 0x006837f4);     //下层浪到屏幕底部的颜色

        mWave_front_alpha = attributes.getInt(R.styleable.DoubleWavesView_wave_front_alpha, DEFAULT_ABOVE_WAVE_ALPHA);  //上层浪的透明度
        mWave_back_alpha = attributes.getInt(R.styleable.DoubleWavesView_wave_back_alpha, DEFAULT_ABOVE_WAVE_ALPHA);    //下层浪的透明度

        mBlowWaveColor = attributes.getColor(R.styleable.DoubleWavesView_background_color, Color.TRANSPARENT);
        mWaveWeight = attributes.getFloat(R.styleable.DoubleWavesView_wave_weight, 0.5f);                          //控制浪在界面的位置,越大越低(默认在屏幕中间)
        attributes.recycle();

        init();
    }

    private void init() {
        mAboveWavePaint_front.setAlpha(mWave_front_alpha);
        mAboveWavePaint_front.setStyle(Paint.Style.FILL);
//        mAboveWavePaint_front.setAntiAlias(true); //去掉抗锯齿,略微提高效率.

        mAboveWavePaint_back.setAlpha(mWave_back_alpha);
        mAboveWavePaint_back.setStyle(Paint.Style.FILL);
//        mAboveWavePaint_back.setAntiAlias(true);

        mBlowWavePaint.setColor(mBlowWaveColor);
        mBlowWavePaint.setAlpha(0);
        mBlowWavePaint.setStyle(Paint.Style.FILL);
//        mBlowWavePaint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWaveMultiple = getWaveMultiple(LARGE);     //浪的长度倍数
        mWaveHeight = getWaveHeight(LARGE);         //浪的高度
        mWaveHz = getWaveHz(LITTLE);                //赫兹
        mBlowOffset = mWaveHeight * 0.4f;           //目前没用到的值

        mWaveLength = getWidth() * mWaveMultiple;
        left = getLeft();
        right = getRight();
        bottom = getBottom() + 2;
        mMaxRight = right + X_SPACE;
        omega = PI2 / mWaveLength;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        long timeStart = System.currentTimeMillis();
        super.onDraw(canvas);

//        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
//        Canvas drawCanvas = new Canvas(bitmap);     //本画布转换为一张位图
//        drawCanvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2, mBlowWavePaint);[这里不要用这个画布了,否则效率降低,50毫秒左右执行一次,现在0毫秒]
//        drawCanvas.drawRect(getLeft(),getTop(),getRight(),getBottom(), mBlowWavePaint);     //底图背景
        canvas.drawRect(getLeft(), getTop(), getRight(), getBottom(), mBlowWavePaint);     //底图背景
//        mAboveWavePaint_back.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
//        mAboveWavePaint_front.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
//        drawCanvas.drawPath(mAboveWavePath_back, mAboveWavePaint_back);
//        drawCanvas.drawPath(mAboveWavePath_front, mAboveWavePaint_front);

        mAboveWavePaint_front.setShader(lg_front);
        mAboveWavePaint_back.setShader(lg_back);
        canvas.drawPath(mAboveWavePath_back, mAboveWavePaint_back);
        canvas.drawPath(mAboveWavePath_front, mAboveWavePaint_front);
//        mAboveWavePaint_back.setXfermode(null);
//        mAboveWavePaint_front.setXfermode(null);
//        canvas.drawBitmap(bitmap, 0.0f, 0.0f, new Paint());     //把之前那张图画到本view上
//        bitmap.recycle();
        long timeEnd = System.currentTimeMillis();
        Log.i("KJ", "onDraw()时间间隔:-->>" + (timeEnd - timeStart));
    }

    /**
     * 获取浪的长度倍数
     *
     * @param size
     * @return
     */
    private float getWaveMultiple(int size) {
        switch (size) {
            case DoubleWavesCalculateView.LARGE:
                return WAVE_LENGTH_MULTIPLE_LARGE;
            case DoubleWavesCalculateView.MIDDLE:
                return WAVE_LENGTH_MULTIPLE_MIDDLE;
            case DoubleWavesCalculateView.LITTLE:
                return WAVE_LENGTH_MULTIPLE_LITTLE;
        }
        return 0;
    }

    /**
     * 获取浪的高度
     *
     * @param size
     * @return
     */
    private int getWaveHeight(int size) {
        switch (size) {
            case DoubleWavesCalculateView.LARGE:
                return WAVE_HEIGHT_LARGE;
            case DoubleWavesCalculateView.MIDDLE:
                return WAVE_HEIGHT_MIDDLE;
            case DoubleWavesCalculateView.LITTLE:
                return WAVE_HEIGHT_LITTLE;
        }
        return 0;
    }

    /**
     * 获取波浪的赫兹
     *
     * @param size
     * @return
     */
    private float getWaveHz(int size) {
        switch (size) {
            case DoubleWavesCalculateView.LARGE:
                return WAVE_HZ_FAST;
            case DoubleWavesCalculateView.MIDDLE:
                return WAVE_HZ_NORMAL;
            case DoubleWavesCalculateView.LITTLE:
                return WAVE_HZ_SLOW;
        }
        return 0;
    }

    LinearGradient lg_front;
    LinearGradient lg_back;

    /**
     * calculate wave track
     */
    private void calculatePath() {
        mAboveWavePath_front.reset();
        mAboveWavePath_back.reset();
        getWaveOffset();    //获取偏移量
        float y_front, y_back;
        mAboveWavePath_front.moveTo(0, bottom);
        mAboveWavePath_back.moveTo(0, bottom);
        for (float x = 0; x <= mMaxRight; x += X_SPACE) {
            y_front = (float) (mWaveHeight * Math.sin(omega * x + mAboveOffset) + Math.max(mWaveHeight, getHeight() * mWaveWeight));
            y_back = (float) (mWaveHeight * Math.sin(omega * 1.5 * x - mAboveOffset + 5 * Math.PI / 4) + Math.max(mWaveHeight, getHeight() * mWaveWeight));
            mAboveWavePath_front.lineTo(x, y_front);
            mAboveWavePath_back.lineTo(x, y_back);
            lg_front = new LinearGradient(0, y_front, 0, getHeight(), mAboveWaveColor_afront, mAboveWaveColor_afront_after, Shader.TileMode.CLAMP);
            lg_back = new LinearGradient(0, y_back, 0, getHeight(), mAboveWaveColor_aback, mAboveWaveColor_aback_after, Shader.TileMode.CLAMP);
        }
        mAboveWavePath_front.lineTo(right, bottom);
        mAboveWavePath_back.lineTo(right, bottom);
    }

    /**
     * 这个在生命周期中先于onMeasure执行
     *
     * @param visibility
     */
    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (View.GONE == visibility) {
            removeCallbacks(mRefreshProgressRunnable);
        } else {
            removeCallbacks(mRefreshProgressRunnable);
            mRefreshProgressRunnable = new RefreshProgressRunnable();
            post(mRefreshProgressRunnable);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    /**
     * mVaveHz决定每次移动的距离,直观看到就是快慢
     */
    private void getWaveOffset() {
        if (mBlowOffset > Float.MAX_VALUE - 100) {
            mBlowOffset = 0;
        } else {
            mBlowOffset += mWaveHz;
        }

        if (mAboveOffset > Float.MAX_VALUE - 100) {
            mAboveOffset = 0;
        } else {
            mAboveOffset += mWaveHz;
        }
    }

    /**
     * 刷新界面的Runnable
     */
    private class RefreshProgressRunnable implements Runnable {
        public void run() {
            synchronized (DoubleWavesCalculateView.this) {
                long start = System.currentTimeMillis();

                calculatePath();

                invalidate();

                long gap = 16 - (System.currentTimeMillis() - start);
                postDelayed(this, gap < 0 ? 0 : gap);
            }
        }
    }
}

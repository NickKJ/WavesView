package com.kj.anim.wave.doublewaves.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.kj.anim.wave.doublewaves.R;

import java.lang.ref.WeakReference;

/**
 * @author kangjian
 * @version 1.0
 * @title WaveShaderView
 * @description 实现背景图双层浪的UI效果。   【利用: BitmapShader 】实现
 * 技术点+注意点:
 *      1.记着把资源文件放在:drawable-nodpi文件下,放drawable下Android系统会根据各个设备伸缩,造成最终很乱。
 *      2.本类是根据 1920*1080 分辨率的屏幕写的,所以里面一些起始单位位置也是根据1920*1080去写。
 *      3.FRONT_ANIM_DURATION 控制前层浪的速率; BACK_ANIM_DURATION 控制后层浪的速率。需要的话,可以把这两个写在属性控制里。
 *      4.运用动画实现波浪的平移
 * @created 2017/3/25 11:33
 * @changeRecord [修改记录] <br/>
 */

public class DoubleWavesShaderView extends View {

    private static final String TAG = "DoubleWavesShader";

    private static final int FRONT_ANIM_DURATION = 40000;
    private static final int BACK_ANIM_DURATION = 20000;

    private Wave mBackWave;
    private Wave mFrontWave;

    public DoubleWavesShaderView(Context context) {
        super(context);
        if(isInEditMode())
            return;
        init(context, null, 0);
    }

    public DoubleWavesShaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if(isInEditMode())
            return;
        init(context, attrs, 0);
    }

    public DoubleWavesShaderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if(isInEditMode())
            return;
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.WaveView);

        int colorFront = attributes.getColor(R.styleable.WaveView_wave_front_acolor, 0);
        int colorBack = attributes.getColor(R.styleable.WaveView_wave_back_acolor, 0);

        attributes.recycle();

        Bitmap wave = BitmapFactory.decodeResource(getResources(), R.drawable.pic_wave_tt);

        Bitmap backWaveBitmap = Bitmap.createBitmap(wave, 0, 0, 1920, 240);
        mBackWave = new Wave(this, backWaveBitmap, BACK_ANIM_DURATION, colorBack);

        Bitmap frontWaveBitmap = Bitmap.createBitmap(wave, 0, 240, 1920, 240);
        mFrontWave = new Wave(this, frontWaveBitmap, FRONT_ANIM_DURATION, colorFront);

        wave.recycle();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(isInEditMode())              //就是为了解决可视化编辑器无法识别自定义控件报错加的
            return;
        mBackWave.startWaveAnim();
        mFrontWave.startWaveAnim();
    }

    @Override
    protected void onDetachedFromWindow() {
        mFrontWave.stopWaveAnim();
        mBackWave.stopWaveAnim();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if(isInEditMode())
            return;
        if (View.VISIBLE == visibility) {
            mBackWave.startWaveAnim();
            mFrontWave.startWaveAnim();
        } else {
            mBackWave.stopWaveAnim();
            mBackWave.stopWaveAnim();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(!isInEditMode()) {
            mBackWave.onDraw(canvas);
            mFrontWave.onDraw(canvas);
        }
        super.onDraw(canvas);
    }

    private static class Wave {
        private Bitmap bitmap;
        private Paint paint;
        private BitmapShader shader;
        private Matrix matrix;
        private ObjectAnimator animator;
        private int durMillis;
        private int offset;
        private WeakReference<View> viewRef;

        public Wave(View v, Bitmap b, int durationMillis, int color) {
            bitmap = b;
            shader = new BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP);
            paint = new Paint();
            paint.setShader(shader);
            paint.setAntiAlias(true);
            matrix = new Matrix();
            durMillis = durationMillis;
            viewRef = new WeakReference<>(v);
            if (color != 0) {
                paint.setColorFilter(new LightingColorFilter(0X02FFFFFF, color));
            }
        }

        public void startWaveAnim() {
            if (animator != null)
                animator.end();
            animator = ObjectAnimator.ofInt(this, "wavePos", 0, 1920);
            animator.setInterpolator(new LinearInterpolator());
            animator.setDuration(durMillis);
            animator.setRepeatMode(ValueAnimator.RESTART);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.start();
        }

        public void stopWaveAnim() {
            if (animator != null)
                animator.end();
            animator = null;
        }

        public void onDraw(Canvas canvas) {
            matrix.setTranslate(offset, 0);
            shader.setLocalMatrix(matrix);
            canvas.drawRect(0, 0, 1920, canvas.getHeight(), paint);
        }

        public void setWavePos(int pos) {
            boolean changed = (offset != pos);
            offset = pos;
            View v = viewRef.get();
            if (v != null && changed)
                v.invalidate();
        }
    }
}

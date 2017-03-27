package com.kj.anim.wave.doublewaves.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import com.kj.anim.wave.doublewaves.R;

/**
 * @author kangjian
 * @version 1.0
 * @title WaveViewDraw
 * @description 实现图片浪的滚动动画操作
 * 通过动画的方式实现
 * @created 2017/3/24 22:21
 * @changeRecord [修改记录] <br/>
 */

public class WavePicView extends FrameLayout {

    private static final String TAG = WavePicView.class.getSimpleName();

    private String mLeftTag;
    private String mCenterTag;

    private FrameLayout left_WaveView;
    private FrameLayout center_WaveView;

    public WavePicView(Context context) {
        super(context);
        initWavePicView();
    }

    public WavePicView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
        initWavePicView();
    }

    public WavePicView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
        initWavePicView();
    }

    private void initWavePicView() {
        this.setClickable(false);
        this.setFocusable(false);
        this.setAnimationCacheEnabled(false);
    }

    protected void init(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.WavePicView);
        mLeftTag = typedArray.getString(R.styleable.WavePicView_left_image);
        mCenterTag = typedArray.getString(R.styleable.WavePicView_center_image);

        typedArray.recycle();
    }

    /**
     * 在这里执行动画
     *
     * @param changed
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        animateDisplayWave();
    }

    /**
     * 这里找内部控件
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (mLeftTag != null) {
            left_WaveView = (FrameLayout) findViewWithTag(mLeftTag);
        }
        if (mCenterTag != null) {
            center_WaveView = (FrameLayout) findViewWithTag(mCenterTag);
        }
    }

    /**
     * 动画
     */
    private void animateDisplayWave() {
        if (left_WaveView != null && center_WaveView != null) {
            left_WaveView.setAnimationCacheEnabled(false);
            center_WaveView.setAnimationCacheEnabled(false);
            ObjectAnimator transX_waveLeft = ObjectAnimator.ofFloat(left_WaveView, "translationX", 0, 1920);
            ObjectAnimator transX_waveCenter = ObjectAnimator.ofFloat(center_WaveView, "translationX", 0, 1920);
            transX_waveLeft.setRepeatMode(ValueAnimator.RESTART);
            transX_waveLeft.setRepeatCount(Animation.INFINITE);
            transX_waveCenter.setRepeatMode(ValueAnimator.RESTART);
            transX_waveCenter.setRepeatCount(Animation.INFINITE);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.setDuration(16000);
            animatorSet.setInterpolator(new LinearInterpolator());
            animatorSet.playTogether(transX_waveLeft, transX_waveCenter);
            animatorSet.start();
        }
    }
}

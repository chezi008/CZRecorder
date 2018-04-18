package com.module.mp3recorder.widget;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.module.mp3recorder.utils.DateUtils;

/**
 * @author ：chezi008 on 2018/4/17 21:03
 * @description ：自定义麦克风录音控件，带录音时长
 * @email ：chezi008@163.com
 */
public class MicView extends LinearLayout {

    private TextView mTvTime;
    private DoughnutProgress mDoughnutProgress;

    private long startTime = 0;
    private boolean isAnimationStart;
    private Handler mHandler;
    private Runnable mTimeRunnable;
    private MicListener micListener;

    public MicView(Context context) {
        this(context, null);
    }

    public MicView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public void setMicListener(MicListener micListener) {
        this.micListener = micListener;
    }

    private void initView() {
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String seconds = DateUtils.toTime((int) (System.currentTimeMillis() - startTime));
                mTvTime.setText(seconds);
            }
        };
        mTimeRunnable = new Runnable() {
            @Override
            public void run() {
                while (isAnimationStart) {
                    try {
                        mHandler.sendEmptyMessage(0);
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };


        mTvTime = new TextView(getContext());
        mTvTime.setTextColor(Color.argb(255, 230, 85, 35));

        LinearLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(mTvTime, params);

        mDoughnutProgress = new DoughnutProgress(getContext());
        addView(mDoughnutProgress, params);

        mDoughnutProgress.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startAnimation();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        if (event.getY() < 0) {
                            stopAnimation();
                        } else {

                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        stopAnimation();
                        return true;
                    default:
                        break;
                }
                return false;
            }
        });

    }

    private void startAnimation() {
        if (!isAnimationStart) {
            isAnimationStart = true;
            startTime = System.currentTimeMillis();
            mDoughnutProgress.startAnimation();
            mTvTime.setVisibility(VISIBLE);
            new Thread(mTimeRunnable).start();
            if (micListener != null) {
                micListener.onStart();
            }
        }
    }

    private void stopAnimation() {
        isAnimationStart = false;
        startTime = 0;
        mDoughnutProgress.stopAnimation();
        mTvTime.setVisibility(GONE);
        if (micListener != null) {
            micListener.onStop();
        }
    }

    public interface MicListener {
        void onStart();

        void onStop();
    }
}

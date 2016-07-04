package com.youku.player;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.baseproject.utils.Logger;
import com.youku.player.base.YoukuPlayerView.LayoutChangeListener;
import com.youku.player.plugin.MediaPlayerDelegate;

public class NewSurfaceView extends SurfaceView {

    public boolean isFullScreen;
    private int mVideoWidth;
    private int mVideoHeight;
    private int mViewPercent = MediaPlayerDelegate.PLAY_100;
    private int mParentWidth;
    private int mParentHeight;
    private int mOrientation;
    private LayoutChangeListener mLayoutChangeListener;
    private static String TAG = LogTag.TAG_PREFIX + NewSurfaceView.class.getSimpleName();

    // 当视频比例与view比例小于0.01时不再进行resize
    private static final float RESIZE_RATE = 0.01f;

    public NewSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public NewSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NewSurfaceView(Context context) {
        super(context);
    }

    public void setVideoSize(int w, int h) {
        mVideoWidth = w;
        mVideoHeight = h;
        // getHolder().setFixedSize(mVideoWidth, mVideoHeight);
        requestLayout();
    }

    public float getSizeRatio()
    {
        if(mVideoWidth==0 || mVideoHeight==0)
            return 0.0f;

        return mVideoHeight*1.0f/mVideoWidth;
    }

    public void setParentSize(int width, int height) {
        mParentWidth = width;
        mParentHeight = height;
    }

    public void setOrientation(int orientation) {
        mOrientation = orientation;
    }

    public void setViewPercent(int percent) {
        mViewPercent = percent;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
        int videoWidth = mVideoWidth;
        int videoHeight = mVideoHeight;
        if (mOrientation == 1 || mOrientation == 2) {
            videoHeight = mVideoWidth;
            videoWidth = mVideoHeight;
        }
        // Logger.d(
        // TAG,
        // "NewSurfaceView onMeasure:"
        // + MeasureSpec.toString(widthMeasureSpec) + " "
        // + MeasureSpec.toString(heightMeasureSpec));
        if (!(mViewPercent == MediaPlayerDelegate.PLAY_FULL && isFullScreen)) {
            if (videoWidth > 0 && videoHeight > 0) {
                // Logger.d(TAG, "resize");

                int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
                int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
                // Logger.e(TAG, "NewSurfaceView onMeasure:" + widthSpecSize
                // + " " + heightSpecSize);
                width = widthSpecSize;
                height = heightSpecSize;

                if (isFullScreen) {
                    width = mParentWidth;
                    height = mParentHeight;
                    if (mViewPercent == MediaPlayerDelegate.PLAY_75) {
                        width = width / 4 * 3;
                        height = height / 4 * 3;
                    } else if (mViewPercent == MediaPlayerDelegate.PLAY_50) {
                        width = width / 2;
                        height = height / 2;
                    }
                }
//                Logger.d(TAG, "111videoWidth:" + videoWidth + " videoHeight:" + videoHeight + " width:" + width + " height:" + height);

//                Logger.d(TAG, (Math.abs(videoWidth * height - videoHeight * width) / (1.0 * height * videoHeight)) + "");
                if (Math.abs(videoWidth * height - videoHeight * width) > RESIZE_RATE * height * videoHeight) {
                    if (videoWidth * height < videoHeight * width) {
                        width = videoWidth * height / videoHeight;
                    } else if (videoWidth * height > videoHeight * width) {
                        height = videoHeight * width / videoWidth;
                    }
                    // 奇数宽高在有些设备上会造成只显示半屏
                    if (width % 2 == 1)
                        width -= 1;
                    if (height % 2 == 1)
                        height -= 1;
                }
//                Logger.d(TAG, "222videoWidth:" + videoWidth + " videoHeight:" + videoHeight + " width:" + width + " height:" + height);
            } else {
                // Logger.d(TAG, "not resize");
            }
        }

         Logger.d(TAG, "NewSurfaceViewsetMeasuredDimension:" + width + " "
         + height);
        setMeasuredDimension(width, height);
    }

    protected static String sizeToString(int size) {
        if (size == LayoutParams.WRAP_CONTENT) {
            return "wrap-content";
        }
        if (size == LayoutParams.MATCH_PARENT) {
            return "match-parent";
        }
        return String.valueOf(size);
    }

    public void recreateSurfaceHolder() {
        setVisibility(View.INVISIBLE);
        setVisibility(View.VISIBLE);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
         Logger.d(TAG, "smingerPlayerNewSurfaceView onLayout:" + changed + " " + left +
                 " "
                 + top + " " + right + " " + bottom);
        if (mLayoutChangeListener != null)
            mLayoutChangeListener.onLayoutChange();
    }

    // @Override
    // public void setLayoutParams(LayoutParams params) {
    // super.setLayoutParams(params);
    // Logger.e(TAG,
    // Log.getStackTraceString(new Exception("setLayoutParams")));
    // }

    public void setLayoutChangeListener(LayoutChangeListener listener) {
        mLayoutChangeListener = listener;
    }

    public int getOrientation() {
        return mOrientation;
    }
}

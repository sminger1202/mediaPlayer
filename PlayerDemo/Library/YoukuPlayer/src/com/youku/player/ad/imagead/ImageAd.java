package com.youku.player.ad.imagead;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Handler;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.baseproject.utils.UIUtils;
import com.baseproject.utils.Util;
import com.youku.android.player.R;
import com.youku.player.apiservice.IPlayerAdControl;
import com.youku.player.apiservice.IPlayerUiControl;
import com.youku.player.goplay.AdvInfo;
import com.youku.player.plugin.MediaPlayerDelegate;
import com.youku.player.ui.widget.YpYoukuDialog;
import com.youku.player.util.DetailMessage;
import com.youku.player.util.DisposableStatsUtils;
import com.youku.player.util.SessionUnitil;
import com.youku.player.util.URLContainer;

/**
 * abstract image ad.
 */
public abstract class ImageAd {

    protected Activity mActivity;
    protected MediaPlayerDelegate mMediaPlayerDelegate;
    protected IPlayerUiControl mPlayerUiControl;
    protected IPlayerAdControl mPlayerAdControl;
    protected LayoutInflater mLayoutInflater;
    protected View mAdView;
    protected AdvInfo mAdvInfo;

    protected static Handler mHandler = new Handler();
    protected static final int TIME_OUT = 10000;// ms
    protected static final int COUNTDOWN_DEFAULT = 5;// s
    protected int mSavedCount;

    protected IImageAdCallback mImageAdCallback;
    protected ViewGroup.LayoutParams mContainerParams;

    public ImageAd(Activity context, MediaPlayerDelegate mediaPlayerDelegate,
                   IPlayerUiControl playerUiControl, IPlayerAdControl playerAdControl) {
        mActivity = context;
        mMediaPlayerDelegate = mediaPlayerDelegate;
        mPlayerUiControl = playerUiControl;
        mPlayerAdControl = playerAdControl;
        mLayoutInflater = LayoutInflater.from(context);
    }

    public abstract void start(final AdvInfo advInfo, IImageAdCallback callback);

    protected void disposeAdLoss(int step) {
        DisposableStatsUtils.disposeAdLoss(mActivity, step,
                SessionUnitil.playEvent_session, URLContainer.AD_LOSS_MP);
    }

    public abstract void release();

    public abstract void dismiss();

    public abstract void pauseTimer();

    public abstract void startTimer();

    public abstract boolean isSaveOnResume();

    public abstract boolean isAutoPlayAfterClick();

    public abstract void setAutoPlayAfterClick(boolean autoPlay);

    public abstract boolean isSaveOnOrientChange();

    public abstract void onStop();

    public void setContainer(ViewGroup viewGroup) {
        if (viewGroup != null) {
            viewGroup.removeAllViews();
            viewGroup.addView(mAdView, getParams());
        }
    }

    protected ViewGroup.LayoutParams getParams() {
        if (mContainerParams == null) {
            mContainerParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
        return mContainerParams;
    }

    protected void setImageAdShowing(boolean isShow) {
        if (mPlayerAdControl != null) {
            mPlayerAdControl.setImageAdShowing(isShow);
        }
    }

    protected void updateImageAdPlugin() {
        if (mPlayerUiControl != null) {
            mPlayerUiControl.updatePlugin(DetailMessage.PLUGIN_SHOW_IMAGE_AD);
        }
    }

    protected void setPluginHolderPaddingZero() {
        if (mPlayerUiControl != null && UIUtils.hasKitKat()) {
            mPlayerUiControl.setPluginHolderPaddingZero();
        }
    }

    protected boolean isLand() {
        if (mActivity != null) {
            Display getOrient = mActivity.getWindowManager()
                    .getDefaultDisplay();
            return getOrient.getWidth() > getOrient.getHeight();
        }
        return false;
    }

    protected boolean isDownLoadDialogNotShowing() {
        return mDownLoadDialog == null || !mDownLoadDialog.isShowing();
    }

    public void onResume() {
        if (mDownLoadDialog == null || !mDownLoadDialog.isShowing()) {
            startTimer();
        }
    }

    protected YpYoukuDialog mDownLoadDialog = null;

    /**
     * 当在广告点击的url为app下载地址，让用户选择是否下载
     */
    protected void creatSelectDownloadDialog(Activity activity, boolean isWifi, final String url) {
        if (mDownLoadDialog != null && mDownLoadDialog.isShowing()) {//防止连续点击弹出多个提示框
            return;
        }
        mDownLoadDialog = new YpYoukuDialog(activity);
        mDownLoadDialog.setNormalPositiveBtn(
                R.string.youku_ad_dialog_selectdownload_cancel,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startTimer();//方法内部已经做了广告是否显示的判断
                        mDownLoadDialog.dismiss();
                    }
                });
        mDownLoadDialog.setNormalNegtiveBtn(
                R.string.youku_ad_dialog_selectdownload_ok,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!Util.hasInternet()) {
                            Toast.makeText(mActivity.getApplicationContext(), "当前无网络连接", Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            if (mPlayerAdControl != null) {
                                mPlayerAdControl.onMoreInfoClicked(url, mAdvInfo);
                            }
                        }
                        /////////////////////////////////////////////////////dismissImageAD();
                        if (mImageAdCallback != null) {
                            mImageAdCallback.onAdDismiss();
                        }
                        if (mMediaPlayerDelegate != null) {
                            mMediaPlayerDelegate.pluginManager.onLoading();
                            mMediaPlayerDelegate.startPlayAfterImageAD();
                        }
                    }
                });
        mDownLoadDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                startTimer();
                mDownLoadDialog.dismiss();
            }
        });
        mDownLoadDialog.setMessage(isWifi ? R.string.youku_ad_dialog_selectdownload_message_wifi : R.string.youku_ad_dialog_selectdownload_message_3g);
        mDownLoadDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                    startTimer();//方法内部已经做了广告是否显示的判断
                    mDownLoadDialog.dismiss();
                }
                return true;
            }
        });
        mDownLoadDialog.setCanceledOnTouchOutside(false);
        if (activity.isFinishing()) {
            return;
        }
        mDownLoadDialog.show();
        mPlayerAdControl.onDownloadDialogShow(mAdvInfo);
        pauseTimer();//当dialog显示出来时，暂停timer
    }

}

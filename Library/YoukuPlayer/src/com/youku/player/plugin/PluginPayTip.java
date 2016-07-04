package com.youku.player.plugin;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.youku.android.player.R;
import com.youku.player.goplay.GoplayException;
import com.youku.player.util.AnalyticsWrapper;

public class PluginPayTip extends PluginOverlay {
	MediaPlayerDelegate mediaPlayerDelegate;
	Activity mActivity;

	LayoutInflater mLayoutInflater;
	View mContainerView;
	private Button mCloseButton;
	TextView mTipTextView;
	private RelativeLayout mPayTipLayout;
	private Resources mResources;
	Button mArrowButton;
	private String mVid;
	private TipState mState = TipState.SHOW_FULL;
	protected boolean isHide;
	private boolean isRealStart;

	public PluginPayTip(Activity context,
			MediaPlayerDelegate mediaPlayerDelegate) {
		super(context, mediaPlayerDelegate);
		this.mediaPlayerDelegate = mediaPlayerDelegate;
		mActivity = context;
		mResources = context.getResources();
		mLayoutInflater = LayoutInflater.from(context);
		init(context);
	}

	private Handler mHandler = new Handler();

	protected void init(Context context) {
		mContainerView = mLayoutInflater.inflate(R.layout.yp_plugin_paytip,
				null);
		addView(mContainerView);
		findView();
		setFull();
	}
	
	private void setFullText() {
		if (mediaPlayerDelegate != null
				&& mediaPlayerDelegate.videoInfo != null
				&& mediaPlayerDelegate.videoInfo.mPayInfo != null
				&& mediaPlayerDelegate.videoInfo.mPayInfo.trail != null) {
			mTipTextView
					.setText(Html
							.fromHtml(mediaPlayerDelegate.videoInfo.mPayInfo.trail.trialStr
									+ "<br><font color=#ff6d02>立即购买</font>观看完整版"));
		}
	}

	private void setFull() {
		setFullText();
		mTipTextView.getLayoutParams().width = (int) mResources
				.getDimension(R.dimen.paytip_full_tip_width);
		mTipTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
				mResources.getDimension(R.dimen.paytip_full_textsize));
		mPayTipLayout.getLayoutParams().height = (int) mResources
				.getDimension(R.dimen.paytip_full_height);
	}

	public void findView() {
		mCloseButton = (Button) mContainerView.findViewById(R.id.paytip_close);
		mTipTextView = (TextView) mContainerView.findViewById(R.id.paytip_bt);
		mPayTipLayout = (RelativeLayout) mContainerView
				.findViewById(R.id.paytip_layout);
		mArrowButton = (Button) mContainerView.findViewById(R.id.paytip_arrow);
		mCloseButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onCloseClick();
				mState = TipState.CLOSED;
				close(null);
			}
		});
		OnClickListener okClickLisher = new OnClickListener() {

			@Override
			public void onClick(View v) {
				onTextAndArrowButtonClick();
			}
		};
		mTipTextView.setOnClickListener(okClickLisher);
		mArrowButton.setOnClickListener(okClickLisher);
		mContainerView.setVisibility(View.GONE);
	}

	protected void onTextAndArrowButtonClick() {
		onOkClick();
		mState = TipState.CLOSED;
		close(null);
	}

	protected void onCloseClick() {

	}

	@Override
	public void onBufferingUpdateListener(int percent) {
		// TODO Auto-generated method stub

	}

    protected void onOkClick() {
        if (mediaPlayerDelegate != null) {
            mediaPlayerDelegate.needPay();
			AnalyticsWrapper.vipVideoClick(mActivity.getApplicationContext(),
					mediaPlayerDelegate.videoInfo, mediaPlayerDelegate.mIUserInfo.isVip(), mediaPlayerDelegate.isFullScreen);
        }
    }

	@Override
	public void onCompletionListener() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onErrorListener(int what, int extra) {
		if (!mActivity.isFinishing())
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mState = TipState.SHOW_FULL;
					mContainerView.setVisibility(View.GONE);
				}
			});
		return false;
	}

	@Override
	public void OnPreparedListener() {

	}

	@Override
	public void OnSeekCompleteListener() {

	}

	@Override
	public void OnVideoSizeChangedListener(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnTimeoutListener() {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnCurrentPositionChangeListener(int currentPosition) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLoadedListener() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLoadingListener() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNotifyChangeVideoQuality() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRealVideoStarted() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUp() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDown() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClearUpDownFav() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFavor() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUnFavor() {
		// TODO Auto-generated method stub

	}

	@Override
	public void newVideo() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onVolumnUp() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onVolumnDown() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMute(boolean mute) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onVideoChange() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onVideoInfoGetting() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onVideoInfoGetted() {
		mState = TipState.SHOW_FULL;
		setFull();
	}

	@Override
	public void onRealVideoStart() {
		if (mActivity.isFinishing())
			return;
		isRealStart = true;
		showProperTip();
	}

	private void showProperTip() {
		if (mediaPlayerDelegate == null
				|| mediaPlayerDelegate.videoInfo == null
				|| mediaPlayerDelegate.videoInfo.getLookTen() != 1 || isHide
				|| !isRealStart)
			return;
		mContainerView.setVisibility(View.GONE);
		if (!TextUtils.isEmpty(mVid)
				&& mVid.equals(mediaPlayerDelegate.videoInfo.getVid())) {
			switch (mState) {
			case CLOSED:
				return;
			case SHOW_FULL:
				setFull();
				break;
			case SHOW_SIMPLE:
				setSample();
				break;
			default:
				break;
			}
		} else
			mState = TipState.SHOW_FULL;

		mVid = mediaPlayerDelegate.videoInfo.getVid();
		show();
		if (mState == TipState.SHOW_FULL) {
			mHandler.removeCallbacksAndMessages(null);
			mHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					if (mActivity.isFinishing()
							|| mContainerView.getVisibility() != View.VISIBLE)
						return;
					mState = TipState.CLOSED;
					close(new AnimationListener() {

						@Override
						public void onAnimationStart(Animation animation) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onAnimationRepeat(Animation animation) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onAnimationEnd(Animation animation) {
							if (mActivity.isFinishing() || isHide)
								return;
							setSample();
							show();
						}
					});
				}
			}, 5000);
		}

	}

	private void setSample() {
		mState = TipState.SHOW_SIMPLE;
		mTipTextView.setText(Html.fromHtml("<font color=#ff6d02>立即购买</font>"));
		mTipTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
				mResources.getDimension(R.dimen.paytip_small_textsize));
		mTipTextView.getLayoutParams().width = (int) mResources
				.getDimension(R.dimen.paytip_small_tip_width);
		mPayTipLayout.getLayoutParams().height = (int) mResources
				.getDimension(R.dimen.paytip_close_height);
	}

	public void show() {
		mContainerView.setVisibility(View.VISIBLE);
		Animation animation = AnimationUtils.loadAnimation(mActivity,
				R.anim.paytip_right_in);
		mPayTipLayout.startAnimation(animation);
	}

	public void close(AnimationListener listener) {
		Animation animation = AnimationUtils.loadAnimation(mActivity,
				R.anim.paytip_right_out);
		if (listener != null)
			animation.setAnimationListener(listener);
		mContainerView.startAnimation(animation);
		mContainerView.setVisibility(View.INVISIBLE);
	}

	@Override
	public void onPlayNoRightVideo(GoplayException e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlayReleateNoRightVideo() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onADplaying() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onVideoInfoGetFail(boolean needRetry) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setVisible(boolean visible) {
		if (mediaPlayerDelegate.isFullScreen) {
			((RelativeLayout.LayoutParams) mPayTipLayout.getLayoutParams())
					.setMargins(0, 0, 0, (int) mResources
							.getDimension(R.dimen.paytip_full_margin_bottom));
		} else {
			((RelativeLayout.LayoutParams) mPayTipLayout.getLayoutParams())
					.setMargins(0, 0, 0, (int) mResources
							.getDimension(R.dimen.paytip_small_margin_bottom));
		}
	}

	@Override
	public void back() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRelease() {
		mContainerView.setVisibility(View.INVISIBLE);
		isHide = false;
		isRealStart = false;
	}

	@Override
	public boolean isShowing() {
		return mContainerView.getVisibility() == View.VISIBLE;
	}

	private static enum TipState {
		SHOW_FULL, SHOW_SIMPLE, CLOSED
	}

	public void hide() {
		isHide = true;
		if (isShowing())
			close(null);
	}

	public void unHide() {
		isHide = false;
		showProperTip();
	}

}

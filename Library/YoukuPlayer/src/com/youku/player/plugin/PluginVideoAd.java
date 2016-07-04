package com.youku.player.plugin;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.baseproject.utils.Logger;
import com.baseproject.utils.Util;
import com.youdo.AdApplicationContext;
import com.youdo.AdManager;
import com.youdo.XAdSDKResource;
import com.youdo.ad.interfaces.IAdApplicationContext;
import com.youdo.ad.interfaces.IAdManager;
import com.youku.android.player.R;
import com.youku.player.LogTag;
import com.youku.player.ad.AdForward;
import com.youku.player.apiservice.IAdPlayerCallback;
import com.youku.player.apiservice.IPlayerAdControl;
import com.youku.player.apiservice.IPlayerUiControl;
import com.youku.player.base.Plantform;
import com.youku.player.config.MediaPlayerConfiguration;
import com.youku.player.goplay.AdvInfo;
import com.youku.player.goplay.GoplayException;
import com.youku.player.goplay.Profile;
import com.youku.player.goplay.Stat;
import com.youku.player.goplay.StaticsUtil;
import com.youku.player.goplay.VideoAdvInfo;
import com.youku.player.service.DisposableHttpTask;
import com.youku.player.ui.widget.YpYoukuDialog;
import com.youku.player.util.AnalyticsWrapper;
import com.youku.player.util.DetailMessage;
import com.youku.player.util.DetailUtil;
import com.youku.player.util.DisposableStatsUtils;
import com.youku.player.util.PlayerUtil;

import org.openad.constants.IOpenAdContants;
import org.openad.events.IXYDEvent;
import org.openad.events.IXYDEventListener;

public abstract class PluginVideoAd extends PluginOverlay implements DetailMessage,IAdPlayerCallback {

    protected final Activity mActivity;
    protected final MediaPlayerDelegate mediaPlayerDelegate;
    protected final IPlayerUiControl mPlayerUiControl;
    protected final IPlayerAdControl mPlayerAdControl;

	LayoutInflater mLayoutInflater;
	View containerView;
	TextView endPage;
	TextView ad_more;
	TextView mCountUpdateTextView;
	private TextView mCountUpdateDescripMinuteTextView;
	private TextView mCountUpdateMinuteTextView;
	private ImageView mSwitchPlayer;
	// youku控件
	LinearLayout mCountUpdateWrap;
	TextView mAdSkip;
	LinearLayout mAdSkipBlank;
    protected ImageButton mMuteButton;
	RelativeLayout mAdTrueViewSkipLayout;
	TextView mAdTrueViewSkipTipTV;
	TextView mAdTrueViewPlay;
	protected ImageView mBackButton;

	// 去详情的父view
	protected View mSwitchParent;
	private View seekLoadingContainerView;
	protected ImageButton play_adButton;

	protected String TAG = "PluginVideoAd";

	private final static int TUDOU_ADSKIP_REQUESTCODE = 20002;

	protected RelativeLayout mAdPageHolder = null;
	protected RelativeLayout play_controller_header = null;
	protected RelativeLayout bottom_text_layout = null;

    private int mVoice = 1;

	// interactive ad
	private static final int INTERACTIVE_AD_TIMEOUT = 5;// s
	private RelativeLayout mInteractiveAdContainer = null;
	private RelativeLayout mInteractiveAdGoFull;
	private IAdApplicationContext mAdApplicationContext;
	private IAdManager mAdManager = null;
	private org.json.JSONObject mCurrentAdData;
	private InteractiveAdListener mInteractiveAdListener = null;
	private boolean isInteractiveAdShow = false;
	private boolean isInteractiveAdHide = false;
	private String mInteractiveAdVideoRs = null; // 互动广告对应视频素材

	protected boolean canSkipTrueViewAd = false;

    public PluginVideoAd(Activity context, MediaPlayerDelegate mediaPlayerDelegate,
                         IPlayerUiControl playerUiControl, IPlayerAdControl playerAdControl) {
        super(context, mediaPlayerDelegate);
        this.mediaPlayerDelegate = mediaPlayerDelegate;
        mActivity = context;
        mPlayerUiControl = playerUiControl;
        mPlayerAdControl = playerAdControl;
        mLayoutInflater = LayoutInflater.from(context);
        init(context);
    }

	protected void init(Context context) {
		if (MediaPlayerConfiguration.getInstance().getPlatform() == Plantform.YOUKU) {
			containerView = mLayoutInflater.inflate(
					R.layout.yp_player_ad_youku, null);
		} else {
			containerView = mLayoutInflater.inflate(
					R.layout.yp_player_ad_tudou, null);
		}
		addView(containerView);
		mCountUpdateTextView = (TextView) containerView
				.findViewById(R.id.my_ad_count);
		mAdSkipBlank = (LinearLayout) containerView
				.findViewById(R.id.my_ad_blank);
		mAdSkip = (TextView) containerView.findViewById(R.id.my_ad_skip);
		mMuteButton = (ImageButton) containerView.findViewById(R.id.mute);
		if (mMuteButton != null)
			mMuteButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					mVoice = 1 - mVoice;
					mediaPlayerDelegate.enableVoice(mVoice);
					setMuteButtonState();
				}
			});
		initMuteButton();
		mAdPageHolder = (RelativeLayout) containerView
				.findViewById(R.id.ad_page_holder);
		if (MediaPlayerConfiguration.getInstance().getPlatform() == Plantform.YOUKU) {
			mInteractiveAdContainer = (RelativeLayout) containerView
					.findViewById(R.id.interactive_ad_container);
			mInteractiveAdGoFull = (RelativeLayout) containerView
					.findViewById(R.id.interactive_ad_gofull_layout);
			mInteractiveAdGoFull.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					isInteractiveAdHide = false;
					if (mAdApplicationContext != null) {
						mAdApplicationContext.show();
					}
					mInteractiveAdGoFull.setVisibility(View.GONE);
					mInteractiveAdContainer.setVisibility(View.VISIBLE);
					if (mAdPageHolder != null) {
						mAdPageHolder.setVisibility(View.GONE);
					}
					mPlayerUiControl.goFullScreen();
					mPlayerUiControl.setOrientionDisable();
				}

			});
		}

		play_controller_header = (RelativeLayout) containerView
				.findViewById(R.id.play_controller_header);
		bottom_text_layout = (RelativeLayout) containerView
				.findViewById(R.id.bottom_text_layout);

		mCountUpdateWrap = (LinearLayout) containerView.findViewById(R.id.my_ad_count_wrap);
		mAdSkip.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (Util.hasInternet()) {
					mPlayerAdControl.onSkipAdClicked();
				} else {
					Toast.makeText(mActivity.getApplicationContext(), "当前无网络连接", Toast.LENGTH_SHORT)
							.show();
				}
				if (mediaPlayerDelegate != null && mediaPlayerDelegate.videoInfo != null){
					AnalyticsWrapper.adSkipClick(mActivity.getApplicationContext(), mediaPlayerDelegate.videoInfo.getVid(), mediaPlayerDelegate.isFullScreen);
			}

            }
		});

		mSwitchPlayer = (ImageView) containerView
				.findViewById(R.id.gofullscreen);
		mSwitchParent = containerView.findViewById(R.id.gofulllayout);
		ad_more = (TextView) containerView.findViewById(R.id.ad_more);
		play_adButton = (ImageButton) containerView
				.findViewById(R.id.ib_detail_play_control_ad_play);

		mSwitchParent.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mediaPlayerDelegate.isFullScreen) {
					mPlayerUiControl.goSmall();
					if (MediaPlayerConfiguration.getInstance().getPlatform() == Plantform.TUDOU) {
						if (PlayerUtil.isYoukuTablet(mActivity)) {
							mSwitchPlayer.setImageResource(R.drawable.plugin_ad_gofull_tudou_pad);
						} else {
							mSwitchPlayer.setImageResource(R.drawable.plugin_ad_gofull_tudou);
						}
					} else {
						mSwitchPlayer.setImageResource(R.drawable.plugin_ad_gofull_youku);
					}
				} else {
					mPlayerUiControl.goFullScreen();
					if (MediaPlayerConfiguration.getInstance().getPlatform() == Plantform.TUDOU) {
						mSwitchPlayer.setImageResource(R.drawable.plugin_ad_gosmall_tudou);
					} else {
						mSwitchPlayer.setImageResource(R.drawable.plugin_ad_gosmall_youku);
					}
				}
			}
		});

		mBackButton = (ImageView) findViewById(R.id.btn_back);
		mBackButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mediaPlayerDelegate.isFullScreen) {
					mPlayerUiControl.goSmall();
				}
			}
		});

		/*---trueView---*/
		mAdTrueViewPlay = (TextView)containerView.findViewById(R.id.ad_trueview_play);
		mAdTrueViewSkipLayout = (RelativeLayout) containerView.findViewById(R.id.ad_trueview_skip_layout);
		mAdTrueViewSkipTipTV = (TextView) containerView.findViewById(R.id.ad_trueview_skip_tip);
		mCountUpdateDescripMinuteTextView = (TextView) containerView.findViewById(R.id.my_ad_count_descrip_minute);
		mCountUpdateMinuteTextView = (TextView) containerView.findViewById(R.id.my_ad_count_minute);

		seekLoadingContainerView = containerView
				.findViewById(R.id.seek_loading_bg);
		initSeekLoading();
	}

	protected abstract void startPlay();

	@Override
	public void onBufferingUpdateListener(int percent) {

	}

	@Override
	public void onCompletionListener() {

	}

	@Override
	public boolean onErrorListener(int what, int extra) {
        mActivity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				containerView.setVisibility(View.GONE);
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

	}

	@Override
	public void OnTimeoutListener() {

	}

	@Override
	public void OnCurrentPositionChangeListener(int currentPosition) {
	}

	@Override
	public void onLoadedListener() {
        mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				play_adButton.setVisibility(View.GONE);
				hideLoading();
			}
		});
	}

	@Override
	public void onLoadingListener() {
        mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				showLoading();
			}
		});
	}

	@Override
	public void onUp() {

	}

	@Override
	public void onDown() {

	}

	@Override
	public void onFavor() {
	}

	@Override
	public void onUnFavor() {
	}

	@Override
	public void newVideo() {
	}

	@Override
	public void onVolumnUp() {
        if (mMediaPlayerDelegate != null && isADPluginShowing && mMuteButton != null) {
            mVoice = 1;
            mediaPlayerDelegate.enableVoice(mVoice);
            setMuteButtonState();
	}
	}

	@Override
	public void onVolumnDown() {
        if (mMediaPlayerDelegate != null && isADPluginShowing && mMuteButton != null) {
            int volume = ((AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE)).getStreamVolume(AudioManager.STREAM_MUSIC);
            if (volume == 0) {
                mVoice = 0;
                mediaPlayerDelegate.enableVoice(mVoice);
                setMuteButtonState();
	}
        }
    }

	@Override
	public void onMute(boolean mute) {
	}

	@Override
	public void onVideoChange() {
        mActivity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				mCountUpdateTextView.setText("");
				mCountUpdateTextView.setVisibility(View.GONE);
				play_adButton.setVisibility(View.GONE);
				ad_more.setVisibility(View.GONE);
				mSwitchParent.setVisibility(View.GONE);//由mSwitchParent来一同控制mSwitchPlayer的隐藏和显示
				mAdSkip.setVisibility(View.GONE);
            	mAdSkipBlank.setVisibility(View.GONE);
                if (MediaPlayerConfiguration.getInstance().getPlatform() == Plantform.YOUKU) {
                	mCountUpdateWrap.setVisibility(View.GONE);
                    mMuteButton.setVisibility(View.GONE);
                }
				hideTrueViewAd();
			}
		});
	}

	boolean isADPluginShowing = false;

	@Override
	public void onVideoInfoGetting() {
		if (isADPluginShowing) {
			/*
			Track.onError(mActivity, mediaPlayerDelegate.nowVid,
					Profile.GUID, mediaPlayerDelegate.videoInfo.playType,
					PlayCode.VIDEO_ADV_RETURN);
					*/
            mPlayerAdControl.interuptAD();
		}
	}

	@Override
	public void onVideoInfoGetted() {
        initMuteButton();
	}

	@Override
	public void onVideoInfoGetFail(boolean needRetry) {
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			isADPluginShowing = true;
			if (containerView != null)
				containerView.setVisibility(View.VISIBLE);
		} else {
			isADPluginShowing = false;
			if (containerView != null)
				containerView.setVisibility(View.GONE);
		}
	}

	public void notifyUpdate(int count) {

		if (count <= 0) {
			mCountUpdateTextView.setText("");
			mCountUpdateTextView.setVisibility(View.GONE);
			if (MediaPlayerConfiguration.getInstance().getPlatform() == Plantform.YOUKU) {
				mCountUpdateWrap.setVisibility(View.GONE);
			}
			return;
		}
		if (mCountUpdateTextView != null) {
			mCountUpdateWrap.setVisibility(View.VISIBLE);
			setCountUpdateText(count);
		}

		int visibility = mediaPlayerDelegate.isPlayLocalType() ? View.GONE : View.VISIBLE;
		// TODO:要保持“广告剩余时间”和“全屏”,“详细了解”的同步显示，需要把三者处理显示的时机要一致。
		// 目前onStartPlayAD中没有倒计时的参数，故暂时放在这里处理。这些应该在onStartPlayAD方法中处理。
		mSwitchParent.setVisibility(visibility);

		AdvInfo advInfo = getAdvInfo();
		if (advInfo != null) {
			if (TextUtils.isEmpty(advInfo.CU)) {
				ad_more.setVisibility(View.GONE);
			} else {
				if (AdForward.YOUKU_VIDEO == advInfo.CUF) {
					ad_more.setText(R.string.playersdk_ad_descrip_play_youku);
				} else {
					ad_more.setText(R.string.playersdk_ad_descrip_youku);
				}
				ad_more.setVisibility(View.VISIBLE);
			}
		}
	}

	@Override
	public void onPluginAdded() {
		super.onPluginAdded();
		if (mediaPlayerDelegate.isFullScreen) {
			if (MediaPlayerConfiguration.getInstance().getPlatform() == Plantform.TUDOU) {
				mSwitchPlayer.setImageResource(R.drawable.plugin_ad_gosmall_tudou);
			} else {
				mSwitchPlayer.setImageResource(R.drawable.plugin_ad_gosmall_youku);
			}
		} else {
			if (MediaPlayerConfiguration.getInstance().getPlatform() == Plantform.TUDOU) {
				if (PlayerUtil.isYoukuTablet(mActivity)) {
					mSwitchPlayer.setImageResource(R.drawable.plugin_ad_gofull_tudou_pad);
				} else {
					mSwitchPlayer.setImageResource(R.drawable.plugin_ad_gofull_tudou);
				}
			} else {
				mSwitchPlayer.setImageResource(R.drawable.plugin_ad_gofull_youku);
			}
		}

        PlayerUtil.hideSystemUI(mActivity, this, mediaPlayerDelegate.isFullScreen);
        mPlayerUiControl.setPluginHolderPaddingZero();
	}

	/**
	 * 获取广告信息
	 * 
	 * @return
	 */
	protected abstract AdvInfo getAdvInfo();

	/**
	 * 获取VideoAdvInfo对象
	 * 
	 * @return
	 */
	protected abstract VideoAdvInfo getVideoAdvInfo();

	/**
	 * 删除当前贴片advInfo
	 * 
	 */
	protected abstract void removeCurrentAdv();

	/**
	 * 发送广告统计信息
	 * 
	 * @param stat
	 */
	private void sendStat(Stat stat) {
		new DisposableHttpTask(stat.U).start();
	}

	private void initSeekLoading() {
		if (null == seekLoadingContainerView)
			return;
		playLoadingBar = (SeekBar) seekLoadingContainerView
				.findViewById(R.id.loading_seekbar);
		if (null != playLoadingBar)
			playLoadingBar
					.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

						@Override
						public void onStopTrackingTouch(SeekBar seekBar) {

						}

						@Override
						public void onStartTrackingTouch(SeekBar seekBar) {

						}

						@Override
						public void onProgressChanged(SeekBar seekBar,
								int progress, boolean fromUser) {
							if (fromUser) {
								//Track.setTrackPlayLoading(false);
								return;
							} else {
								seekBar.setProgress(progress);
							}

						}
					});
	}

	private int seekcount = 0;

	public void showLoading() {

		if (null != seekLoadingContainerView) {
			if (seekLoadingContainerView.getVisibility() == View.GONE) {
				seekLoadingContainerView.setVisibility(View.VISIBLE);
				seekcount = 0;
				seekHandler.sendEmptyMessageDelayed(0, 50);

			}
			if (null != mMediaPlayerDelegate
					&& mMediaPlayerDelegate.getCurrentPosition() > 1000) {
				seekendHandler.sendEmptyMessageDelayed(0, 50);
				seekLoadingContainerView.setBackgroundResource(0);
			} else {
				seekLoadingContainerView
						.setBackgroundResource(R.drawable.bg_play);
			}
		}
	}

	public void hideLoading() {
        mActivity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (null != seekLoadingContainerView) {
					seekLoadingContainerView.setVisibility(View.GONE);
					playLoadingBar.setProgress(0);
				}
				if (null != seekHandler)
					seekHandler.removeCallbacksAndMessages(null);
			}
		});
	}

	private Handler seekHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (seekcount < 50) {
				seekcount++;
				playLoadingBar.setProgress(seekcount);
				Thread temp = new Thread(new Runnable() {

					@Override
					public void run() {
						seekHandler.sendEmptyMessageDelayed(0, 50);
					}
				});
				temp.run();
			} else {
				playLoadingBar.setProgress(50);
			}

		}

	};

	private SeekBar playLoadingBar;
	private Handler seekendHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			if (seekcount < 100) {
				seekcount++;
				playLoadingBar.setProgress(seekcount);
				Thread temp = new Thread(new Runnable() {

					@Override
					public void run() {
						seekHandler.sendEmptyMessageDelayed(0, 10);
					}
				});
				temp.run();
			}

		}

	};

	@Override
	public void onNotifyChangeVideoQuality() {

	}

	@Override
	public void onRealVideoStart() {
		Logger.d(LogTag.TAG_PLAYER, "PluginVideoAd -------> onRealVideoStart()");
		setVisible(false); // 暂时处理正片开播后，还会出现广告UI的问题
	}

	@Override
	public void onADplaying() {
	}

	@Override
	public void onRealVideoStarted() {

	}

	@Override
	public void onStart() {

	}

	@Override
	public void onClearUpDownFav() {

	}

	@Override
	public void onPause() {
		Logger.d(LogTag.TAG_PLAYER,"PluginViewAd ----> onPause()");
		getAdvInfo();
		if(mediaPlayerDelegate.videoInfo == null){
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mCountUpdateTextView.setText("");
					mCountUpdateTextView.setVisibility(View.GONE);
					ad_more.setVisibility(View.GONE);
					mAdSkip.setVisibility(View.GONE);
					mAdSkipBlank.setVisibility(View.GONE);
					if (Profile.PLANTFORM == Plantform.YOUKU) {
						mCountUpdateWrap.setVisibility(View.GONE);
						hideTrueViewAd();
					}
				}
			});
		}

	}

	public void showPlayIcon() {
		play_adButton.setVisibility(View.VISIBLE);
	}

	@Override
	public void back() {
	}

	@Override
	public void onPlayNoRightVideo(GoplayException e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlayReleateNoRightVideo() {
		// TODO Auto-generated method stub

	}

	public boolean isCountUpdateVisible() {
		if (mCountUpdateTextView != null) {
			return mCountUpdateTextView.getVisibility() == View.VISIBLE ? true
					: false;
		}
		return false;
	}

	public void setSkipVisible(boolean visible) {
		if (MediaPlayerConfiguration.getInstance().showSkipAdButton() && mAdSkip != null) {
			mAdSkip.setVisibility(visible ? View.VISIBLE : View.GONE);
			if (mAdSkipBlank != null) {
				mAdSkipBlank.setVisibility(visible ? View.VISIBLE : View.GONE);
			}
		}
	}

	/**
	 * 互动广告
	 * 
	 * @return
	 */
	public void startInteractiveAd(String brs, int count) {
		if (brs == null || brs.equalsIgnoreCase("")
				|| mInteractiveAdContainer == null
				|| !AdApplicationContext.support(IOpenAdContants.AdUnitType.HTML5)) {
			return;
		}

		if (mAdManager == null) {
			mAdManager = new AdManager();
		}
		mAdManager.setLocation(DetailUtil.getLocation(mActivity));
		mAdApplicationContext = mAdManager.getAdApplicationContext();
		// (REQUIRED) the container which used to host the html5 ad.
		mInteractiveAdContainer.removeAllViews();
		mAdApplicationContext
				.setWMHtml5AdViewContainer(mInteractiveAdContainer);
		// (REQUIRED)
		mAdApplicationContext.setActivity(mActivity);
		setInteractiveAdResource();
		// (REQUIRED)
		setupInteractiveAdData(brs, count);
		mAdApplicationContext.setAdData(mCurrentAdData);
		mAdApplicationContext.setTimeout(INTERACTIVE_AD_TIMEOUT);
		// (REQUIRED) register observer
		if (mInteractiveAdListener == null) {
			mInteractiveAdListener = new InteractiveAdListener();
		}
		mAdApplicationContext.addEventListener(
				IAdApplicationContext.AD_PREPARED, mInteractiveAdListener);
		mAdApplicationContext.addEventListener(IAdApplicationContext.AD_STOPED,
				mInteractiveAdListener);
		mAdApplicationContext.addEventListener(IAdApplicationContext.AD_ERROR,
				mInteractiveAdListener);
		mAdApplicationContext.addEventListener(
				IAdApplicationContext.AD_VIEW_MODE_CHANGE,
				mInteractiveAdListener);
		mAdApplicationContext.addEventListener(
				IAdApplicationContext.VIDEO_PAUSE, mInteractiveAdListener);
		mAdApplicationContext.addEventListener(
				IAdApplicationContext.VIDEO_RESUME, mInteractiveAdListener);
		if (getAdvInfo() != null) {
			mInteractiveAdVideoRs = getAdvInfo().RS;
		}
		try {
			mAdApplicationContext.load();
		} catch (Exception e) {
		}
		isInteractiveAdHide = false;
		Logger.d(LogTag.TAG_PLAYER, "start to show Interactive ad");
	}

	public void closeInteractiveAd() {
		if (isInteractiveAdShow) {
			if (mAdApplicationContext != null) {
				mAdApplicationContext.removeAllListeners();
				mAdApplicationContext.dispose();
				mAdApplicationContext = null;
			}
            mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (mInteractiveAdContainer != null) {
						mInteractiveAdContainer.removeAllViews();
						mInteractiveAdContainer.setVisibility(View.GONE);
					}
					if (mAdPageHolder != null) {
						mAdPageHolder.setVisibility(View.VISIBLE);
					}
					mInteractiveAdGoFull.setVisibility(View.GONE);
				}
			});
			isInteractiveAdShow = false;
			isInteractiveAdHide = false;
			if (getAdvInfo() != null) {
				getAdvInfo().RST = "video";
			}
			if (mediaPlayerDelegate.videoInfo != null
					&& !StaticsUtil.PLAY_TYPE_LOCAL
							.equals(mediaPlayerDelegate.videoInfo.getPlayType())
					&& !PlayerUtil.isYoukuTablet(mActivity)) {
                mPlayerUiControl.setOrientionEnable();
			}
			mInteractiveOpenCounts = 0;
		}
	}

	public void closeInteractiveAdNotIcludeUI() {
		if (isInteractiveAdShow) {
			if (mAdApplicationContext != null) {
				mAdApplicationContext.removeAllListeners();
			}

			isInteractiveAdShow = false;
			isInteractiveAdHide = false;
			if (mediaPlayerDelegate.videoInfo != null
					&& !StaticsUtil.PLAY_TYPE_LOCAL
							.equals(mediaPlayerDelegate.videoInfo.getPlayType())
					&& !PlayerUtil.isYoukuTablet(mActivity)) {
                mPlayerUiControl.setOrientionEnable();
			}
			mInteractiveOpenCounts = 0;
		}
	}

	private int mInteractiveOpenCounts = 0;//记录互动页面被滑出的次数
	protected class InteractiveAdListener implements IXYDEventListener {

		@Override
		public void run(IXYDEvent arg0) {
			String type = arg0.getType();
			if (type.equals(AdApplicationContext.AD_PREPARED)) {
				if (isInteractiveAdShow) {
					Logger.d(LogTag.TAG_PLAYER, "Interactive ad is ready");
				}
			} else if (type.equals(AdApplicationContext.AD_STOPED)) {
				isInteractiveAdShow = false;
				isInteractiveAdHide = false;
				mAdApplicationContext.removeAllListeners();
				mAdApplicationContext.dispose();
				DisposableStatsUtils.disposeSUE(mActivity.getApplicationContext(), getAdvInfo());
                mActivity.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						mInteractiveAdContainer.removeAllViews();
						mInteractiveAdContainer.setVisibility(View.GONE);
						mInteractiveAdGoFull.setVisibility(View.GONE);
						if (mAdPageHolder != null) {
							mAdPageHolder.setVisibility(View.VISIBLE);
						}
					}
				});

				if (mediaPlayerDelegate.videoInfo != null
						&& !StaticsUtil.PLAY_TYPE_LOCAL
								.equals(mediaPlayerDelegate.videoInfo
										.getPlayType())
						&& !PlayerUtil.isYoukuTablet(mActivity)) {
                    mPlayerUiControl.setOrientionEnable();
				}
				if (mMediaPlayerDelegate != null
						&& mMediaPlayerDelegate.mediaPlayer != null
						&& getAdvInfo() != null
						&& mInteractiveAdVideoRs
								.equalsIgnoreCase(getAdvInfo().RS)) {
					removeCurrentAdv();
					mMediaPlayerDelegate.mediaPlayer.skipCurPreAd();
                    mMediaPlayerDelegate.setAdPausedPosition(0);
				}

			} else if (type.equals(AdApplicationContext.AD_ERROR)) {
				Logger.e(LogTag.TAG_PLAYER, "PlugiADPlay: interactive ad error");
				closeInteractiveAd();
			} else if (type.equals(AdApplicationContext.AD_VIEW_MODE_CHANGE)) {
				String oldViewMode = (String) arg0.getData().get("oldViewMode");
				String newViewMode = (String) arg0.getData().get("newViewMode");

				if (IOpenAdContants.ViewMode.EXPAND.getValue().equals(oldViewMode)
						&& IOpenAdContants.ViewMode.THUMBNAIL.getValue().equals(
								newViewMode)) {
					isInteractiveAdHide = true;
					mAdApplicationContext.hide();
                    mActivity.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							mInteractiveAdContainer.setVisibility(View.GONE);
							if (mAdPageHolder != null) {
								mAdPageHolder.setVisibility(View.VISIBLE);
							}
							mInteractiveAdGoFull.setVisibility(View.VISIBLE);
						}
					});
					if (mediaPlayerDelegate.videoInfo != null
							&& !StaticsUtil.PLAY_TYPE_LOCAL
									.equals(mediaPlayerDelegate.videoInfo
											.getPlayType())
							&& !PlayerUtil.isYoukuTablet(mActivity)) {
                        mPlayerUiControl.setOrientionEnable();
					}
					if (mMediaPlayerDelegate != null) {
						mMediaPlayerDelegate.startByInteractiveAd();
					}
				}
				if (IOpenAdContants.ViewMode.EXPAND.getValue().equals(newViewMode)) {
					isInteractiveAdHide = false;
				}

				} else if (type.equals(IAdApplicationContext.VIDEO_PAUSE)) {
				Logger.d(LogTag.TAG_PLAYER, "-----> IAdApplicationContext.VIDEO_PAUSE");
				if (mMediaPlayerDelegate != null) {
					mMediaPlayerDelegate.pauseByInteractiveAd();
					mInteractiveOpenCounts ++;
					if(mInteractiveOpenCounts == 1){//第一次滑开发送shu曝光
						if (isInteractiveAdShow) {
							DisposableStatsUtils.disposeSHU(mActivity.getApplicationContext(), getAdvInfo());
						}
					}
				}
			} else if (type.equals(IAdApplicationContext.VIDEO_RESUME)) {
				Logger.d(LogTag.TAG_PLAYER, "-----> IAdApplicationContext.VIDEO_RESUME");
				if (mMediaPlayerDelegate != null) {
					mMediaPlayerDelegate.startByInteractiveAd();
				}
			}
		}
	}

	private void setupInteractiveAdData(String rs, int count) {
		mCurrentAdData = new org.json.JSONObject();
		try {
			mCurrentAdData.put("BRS", rs);
			mCurrentAdData.put("AL", count);
		} catch (Exception e) {
		}
	}

	private void setInteractiveAdResource() {
		if (mAdApplicationContext == null) {
			return;
		}
		XAdSDKResource resource = new XAdSDKResource();
		resource.ad_mini = R.drawable.xadsdk_ad_mini;
		resource.ad_close = R.drawable.xadsdk_ad_close;
		resource.browser_bkgrnd = R.drawable.xadsdk_browser_bkgrnd;
		resource.browser_leftarrow = R.drawable.xadsdk_browser_leftarrow;
		resource.browser_unleftarrow = R.drawable.xadsdk_browser_unleftarrow;
		resource.browser_rightarrow = R.drawable.xadsdk_browser_rightarrow;
		resource.browser_unrightarrow = R.drawable.xadsdk_browser_unrightarrow;
		resource.browser_refresh = R.drawable.xadsdk_browser_refresh;
		resource.browser_close = R.drawable.xadsdk_browser_close;
		mAdApplicationContext.setXAdSDKResource(resource);
	}

	public boolean isInteractiveAdShow() {
		return isInteractiveAdShow;
	}

	public boolean isInteractiveAdHide() {
		return isInteractiveAdHide;
	}

	public boolean isInteractiveAdVisible() {
		if (mInteractiveAdContainer == null) {
			return false;
		}
		return mInteractiveAdContainer.getVisibility() == View.VISIBLE;
	}

	public void setInteractiveAdVisible(boolean visible) {
		if (mInteractiveAdContainer == null) {
			return;
		}
		if (visible) {
			if (isInteractiveAdShow) {
				if (!isInteractiveAdHide) {
					mInteractiveAdContainer.setVisibility(View.VISIBLE);
					if (mAdPageHolder != null) {
						mAdPageHolder.setVisibility(View.GONE);
					}
					if (mAdApplicationContext != null) {
						mAdApplicationContext.show();
					}
                    mPlayerUiControl.goFullScreen();
                    mPlayerUiControl.setOrientionDisable();
				} else {
					mInteractiveAdGoFull.setVisibility(View.VISIBLE);
				}
			}
		} else {
            mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mInteractiveAdContainer.setVisibility(View.GONE);
					if (mAdPageHolder != null) {
						mAdPageHolder.setVisibility(View.VISIBLE);
					}
					mInteractiveAdGoFull.setVisibility(View.GONE);
				}
			});
			if (mediaPlayerDelegate.videoInfo != null
					&& !StaticsUtil.PLAY_TYPE_LOCAL
							.equals(mediaPlayerDelegate.videoInfo.getPlayType())
					&& !PlayerUtil.isYoukuTablet(mActivity)) {
                mPlayerUiControl.setOrientionEnable();
			}
		}
	}

	public void showInteractiveAd() {
		if (mInteractiveAdContainer == null
				|| !AdApplicationContext.support(IOpenAdContants.AdUnitType.HTML5)) {
			return;
		}
		isInteractiveAdShow = true;
		if (mediaPlayerDelegate.isPause || mPlayerUiControl.isOnPause()) {
			return;
		}
        mPlayerUiControl.goFullScreen();
        mPlayerUiControl.setOrientionDisable();
		if (mAdPageHolder != null) {
			mAdPageHolder.setVisibility(View.GONE);
		}
		mInteractiveAdContainer.setVisibility(View.VISIBLE);
	}

	public void pauseInteractiveAd() {
		if (mInteractiveAdContainer == null) {
			return;
		}
		if (isInteractiveAdShow && mAdApplicationContext != null) {
			mAdApplicationContext.onPause();
		}
	}

    private YpYoukuDialog downLoadDialog = null;

    public void dismissDownloadDialog(){
        if(downLoadDialog != null && downLoadDialog.isShowing()){
            downLoadDialog.dismiss();
            if(mediaPlayerDelegate.pauseDuringSeek){
                mediaPlayerDelegate.start();    // 当弹窗出现时候，进行了pause操作，进入正片后会暂停
            }
        }
    }
    /**
     *  当在广告点击的url为app下载地址，让用户选择是否下载
     *  FIXME 添加App下载提示相关代码
     */
    protected void creatSelectDownloadDialog(final Activity activity, boolean isWifi,final String url,final AdvInfo advInfo) {
        if(downLoadDialog != null && downLoadDialog.isShowing()){//防止连续点击弹出多个提示框
            return;
        }
        downLoadDialog = new YpYoukuDialog(activity);
        downLoadDialog.setNormalPositiveBtn(
                R.string.youku_ad_dialog_selectdownload_cancel,
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mediaPlayerDelegate != null && play_adButton.getVisibility() != View.VISIBLE) {
                            mediaPlayerDelegate.start();
                        }
                        downLoadDialog.dismiss();
                    }
                });
        downLoadDialog.setNormalNegtiveBtn(
                R.string.youku_ad_dialog_selectdownload_ok,
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
						if (!Util.hasInternet()) {
							Toast.makeText(mActivity.getApplicationContext(), "当前无网络连接", Toast.LENGTH_SHORT)
									.show();
						} else {
                        mPlayerAdControl.onMoreInfoClicked(url, advInfo);
						}
                        if (mediaPlayerDelegate != null && play_adButton.getVisibility() != View.VISIBLE) {
                            mediaPlayerDelegate.start();
                        }
                        downLoadDialog.dismiss();
                    }
                });
        downLoadDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (mediaPlayerDelegate != null && play_adButton.getVisibility() != View.VISIBLE) {
                    mediaPlayerDelegate.start();
                }
            }
        });
        downLoadDialog.setMessage(isWifi ? R.string.youku_ad_dialog_selectdownload_message_wifi : R.string.youku_ad_dialog_selectdownload_message_3g);
        downLoadDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
                    if (mediaPlayerDelegate != null && play_adButton.getVisibility() != View.VISIBLE) {
                        mediaPlayerDelegate.start();
                    }
                    downLoadDialog.dismiss();
                }
                return true;
            }
        });
        downLoadDialog.setCanceledOnTouchOutside(false);
        if (activity.isFinishing()) {
            return;
        }
        downLoadDialog.show();
		mPlayerAdControl.onDownloadDialogShow(advInfo);
        if(mediaPlayerDelegate != null && !mediaPlayerDelegate.isAdvShowFinished()){
            mediaPlayerDelegate.pause();
        }
    }
	
		/**
	 * 当播放带有互动广告的前贴时，将播放时间传给互动sdk
	 * @param position  当前这一只视频广告播放头时间 单位：秒
	 * @param duration	当前这一只视频广告的时长 单位：秒
	 */
	protected void setInteractiveAdPlayheadTime(int position, int duration){
		if(mAdApplicationContext != null){
			Logger.d(LogTag.TAG_PLAYER, "setInteractiveAdPlayheadTime -------> position : " + position + "/ duration : " + duration);
			mAdApplicationContext.setVideoAdPlayheadTime(position);
			mAdApplicationContext.setVideoAdDuration(duration);
		}
	}

	/**
	 * 是否带互动广告
	 * @param advInfo
	 */
	protected boolean isInteractiveAd(AdvInfo advInfo) {
		if (advInfo != null && advInfo.RST != null && advInfo.RST.equals("hvideo")) {
			return true;
		}
		return false;
	}
	

    private void setMuteButtonState(){
        if(mVoice == 0)
            mMuteButton.setImageResource(R.drawable.plugin_ad_silent_tudou);
        else
            mMuteButton.setImageResource(R.drawable.plugin_ad_silent_off_tudou);





}


    @Override
    public boolean onAdStart(int index) {
        if (mMuteButton != null) {
            mMuteButton.setVisibility(View.VISIBLE);
            mediaPlayerDelegate.enableVoice(mVoice);
        }
        return false;
    }

    @Override
    public boolean onAdEnd(int index) {
        return false;
    }

    @Override
    public void onResume() {
        if (mMediaPlayerDelegate != null && mMuteButton != null && isADPluginShowing) {
            int volume = ((AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE)).getStreamVolume(AudioManager.STREAM_MUSIC);
            if (volume == 0)
                mVoice = 0;
            setMuteButtonState();
        }
    }

    public void initMuteButton() {
        if (mMediaPlayerDelegate != null && mMuteButton != null) {
            int volume = ((AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE)).getStreamVolume(AudioManager.STREAM_MUSIC);
            Logger.d("PlayFlow", "initMuteButton:" + volume);
            if (volume == 0)
                mVoice = 0;
            else
                mVoice = 1;
            setMuteButtonState();
        }
    }

	/**
	 * 是否为TrueView广告
	 * @param advInfo
	 */
	protected boolean isTrueViewAd(AdvInfo advInfo){
		if(advInfo != null && advInfo.EM != null){
			return true;
		}
		return false;
	}

	/**
	 * 显示trueview
	 * @param advInfo
	 */
	protected void showTrueViewAd(AdvInfo advInfo){
		if(isTrueViewAd(advInfo) &&  advInfo.EM.VIEW != null
				&& !TextUtils.isEmpty(advInfo.EM.VIEW.CU)
				&& !TextUtils.isEmpty(advInfo.EM.VIEW.TX)){
			mAdTrueViewPlay.setText(advInfo.EM.VIEW.TX);
			mAdTrueViewPlay.setVisibility(View.VISIBLE);
		} else {
			mAdTrueViewPlay.setVisibility(View.GONE);
		}
		Logger.d(LogTag.TAG_TRUE_VIEW , "------> show TrueView Ad.");
	}

	/**
	 * 隐藏TrueView广告
	 */
	public void hideTrueViewAd(){
		Logger.d(LogTag.TAG_TRUE_VIEW , "------> hide TrueView Ad.");
		canSkipTrueViewAd = false;
		if(mAdTrueViewPlay != null){
			mAdTrueViewPlay.setVisibility(View.GONE);
		}
		if(mAdTrueViewSkipLayout != null){
			mAdTrueViewSkipLayout.setVisibility(View.GONE);
		}

	}

	/**
	 * 更新trueview跳过广告按钮的倒计时（需要根据EM.SKIP.TX1 中的 “|” 来确定时间显示的位置）
	 * @param count 当前广告播放时间
	 */
	protected void notifyTrueViewSkipTime(int count, AdvInfo advInfo){
		if(advInfo == null || advInfo.EM == null || advInfo.EM.SKIP == null){
			return;
		}
		if(mAdTrueViewSkipLayout != null){
			int T = advInfo.EM.SKIP.T;
			int notifyCount = T - (count);
			String notifyCountStr = String.valueOf(notifyCount);
			if(notifyCount > 0){
				String tx1 = advInfo.EM.SKIP.TX1;
				String str = tx1.replace("|", notifyCountStr);
				try {
					int start = tx1.indexOf("|");
					int end = start + notifyCountStr.length();
					int textColor = mActivity.getResources().getColor(R.color.yp_ad_skip_trueview_text_color_youku);
					int fontSize;
					if (MediaPlayerConfiguration.getInstance().getPlatform() == Plantform.YOUKU) {
						fontSize = mActivity.getResources().getDimensionPixelSize(R.dimen.player_ad_trueview_count_text_size_youku);
					} else {
						fontSize = mActivity.getResources().getDimensionPixelSize(R.dimen.player_ad_count_text_size_tudou);
					}
					SpannableString span = new SpannableString(str);
					span.setSpan(new ForegroundColorSpan(textColor), start, end,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					span.setSpan(new AbsoluteSizeSpan(fontSize), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					mAdTrueViewSkipTipTV.setText(span);
				}catch (Exception e){
					mAdTrueViewSkipTipTV.setText(str);
				}
				canSkipTrueViewAd = false;
			} else {
				mAdTrueViewSkipTipTV.setText(advInfo.EM.SKIP.TX2);
				canSkipTrueViewAd = true;
			}

			if (MediaPlayerConfiguration.getInstance().getPlatform() == Plantform.YOUKU) {
				RelativeLayout.LayoutParams params =  (RelativeLayout.LayoutParams)mAdTrueViewSkipLayout.getLayoutParams();
				if(mediaPlayerDelegate.isPlayLocalType()){ // 播放缓存视频和在线视频的宽度不一样
					params.width = mActivity.getResources().getDimensionPixelOffset(R.dimen.player_ad_trueview_ship_offline_width_youku);
				} else {
					params.width = mActivity.getResources().getDimensionPixelOffset(R.dimen.player_ad_trueview_ship_width_youku);
				}
				mAdTrueViewSkipLayout.setLayoutParams(params);
			}

			mAdTrueViewSkipTipTV.setVisibility(View.VISIBLE);
			mAdTrueViewSkipLayout.setVisibility(View.VISIBLE);
		}
	}


	/**
	 * 设置广告倒计时(大于120秒，使用 xx分xx秒，否则使用xx秒)
	 */
	private void setCountUpdateText(int count){
		if(mCountUpdateMinuteTextView == null && mCountUpdateTextView != null){
			mCountUpdateTextView.setText(String.valueOf(count));
			mCountUpdateTextView.setVisibility(View.VISIBLE);
			return;
		}
		if(count <= 120){
			mCountUpdateTextView.setText(String.valueOf(count));
			mCountUpdateTextView.setVisibility(View.VISIBLE);
			mCountUpdateMinuteTextView.setVisibility(View.GONE);
			mCountUpdateDescripMinuteTextView.setVisibility(View.GONE);
		} else {
			int m = count / 60;
			int s = count % 60;
			mCountUpdateMinuteTextView.setText(String.valueOf(m));
			mCountUpdateTextView.setText(String.valueOf(s));
			mCountUpdateTextView.setVisibility(View.VISIBLE);
			mCountUpdateMinuteTextView.setVisibility(View.VISIBLE);
			mCountUpdateDescripMinuteTextView.setVisibility(View.VISIBLE);
		}
	}


	/**
	 * 更新广告中的返回按钮状态（横屏显示，竖屏隐藏）
	 * （目前只有土豆支持）
	 */
	public void updateBackBtn(){
		if (Profile.PLANTFORM == Plantform.YOUKU || mBackButton == null) {
			return;
		}
		if(mMediaPlayerDelegate.isFullScreen){
			mBackButton.setVisibility(View.VISIBLE);
		} else {
			mBackButton.setVisibility(View.GONE);
		}
	}

	/**
	 * 是否显示广告相关控件
	 */
	protected void showAdView (boolean isShow) {
		if(play_controller_header != null && bottom_text_layout != null) {
			if (isShow){
				play_controller_header.setVisibility(View.VISIBLE);
				bottom_text_layout.setVisibility(View.VISIBLE);
			} else {
				play_controller_header.setVisibility(View.INVISIBLE);
				bottom_text_layout.setVisibility(View.INVISIBLE);
				hideTrueViewAd();
			}
		}

	}

}
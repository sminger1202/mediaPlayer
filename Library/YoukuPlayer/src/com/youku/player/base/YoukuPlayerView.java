package com.youku.player.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.text.Spanned;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baseproject.utils.Logger;
import com.baseproject.utils.Util;
import com.youku.android.player.R;
import com.youku.player.LogTag;
import com.youku.player.NewSurfaceView;
import com.youku.player.Track;
import com.youku.player.apiservice.IPlayerUiControl;
import com.youku.player.apiservice.OnInitializedListener;
import com.youku.player.config.MediaPlayerConfiguration;
import com.youku.player.danmaku.DanmakuStatics;
import com.youku.player.danmaku.DanmakuUtils;
import com.youku.player.goplay.GoplayException;
import com.youku.player.goplay.Profile;
import com.youku.player.plugin.MediaPlayerDelegate;
import com.youku.player.plugin.MediaPlayerObserver;
import com.youku.player.plugin.PluginOverlay;
import com.youku.player.service.DisposableHttpCookieTask;
import com.youku.player.subtitle.StrokeTextView;
import com.youku.player.util.AnalyticsWrapper;
import com.youku.player.util.DetailMessage;
import com.youku.player.util.URLContainer;
import com.youku.statistics.PlayerStatistics;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import master.flame.danmaku.controller.DrawHandler.Callback;
import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.loader.ILoader;
import master.flame.danmaku.danmaku.loader.IllegalDataException;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.LiveDanmakuInfo;
import master.flame.danmaku.danmaku.model.android.BaseCacheStuffer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.SpannedCacheStuffer;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.parser.IDataSource;
import master.flame.danmaku.ui.widget.DanmakuSurfaceView;

/**
 * 播放器界面,不需要计算播放器高度 xml <com.youku.player.base.YoukuPlayerView
 * android:id="@+id/full_holder" android:layout_width="fill_parent"
 * android:layout_height="wrap_content" >
 * </com.youku.player.base.YoukuPlayerView>
 * 
 * @author longfan
 * @time 2013年5月7日10:28:42
 */

@SuppressLint("InlinedApi")
public class YoukuPlayerView extends PluginOverlay implements DetailMessage,
		MediaPlayerObserver {

	private Context mContext;
	private FragmentActivity mActivity;
	private static final String TAG = LogTag.TAG_PREFIX + YoukuPlayerView.class.getSimpleName();
	private static final float STANDARD_VIDEOSIZE_RATIO = 0.5625f;
	/**水印相关 **/
	int waterMarkWidth;
	int waterMarkHeight;
	int tudouWaterMarkWidth;
	int tudouWaterMarkHeight;
	int waterMarkMarginTop;
	int waterMarkMarginRight;
	int fullwaterMarkMarginTop;
	int fullwaterMarkMarginRight;
	private ImageView waterMark;
	private ImageView tudouWaterMark;
	private AnimationDrawable waterMarkAnimation;
	private static final int FIRST_FRAME = 0;
	private static final int LAST_FRAME = 1;
	private static final int ANIMATION = 2;

	ViewGroup.LayoutParams layoutParams;
	ViewGroup.LayoutParams tudouLayoutParams;
	ViewGroup.LayoutParams danmakuLayoutParams;
	private DanmakuContext danmakuContext;
	public NewSurfaceView surfaceView;
	View surfaceBlack;
	TextView playerDebugView;
	private YoukuPlayer player;
	public boolean firstOnloaded = false;
	public boolean realVideoStart = false;
	
	private int lastMeasuredWidth;
	private int lastMeasuredHeight;
	
	int mVideoWidth;
	int mVideoHeight;
	public boolean isFullscreen = false;

	private int mMinWidth;//PlayerView缩小到最小时的宽度
	private int mMaxWidth;//PlayerView放大到最小时的宽度

	private IDanmakuView danmakuView;
	private BaseDanmakuParser mParser;
	public long currMillisecond;
	private DanmakuUtils danmakuUtils;
    private boolean isTudouPadDanmaku;
    private int smallViewHeight;
	private Drawable defaultDrawable;
	private FrameLayout pluginHolder;

	// 字幕显示控件
	StrokeTextView singleSubtitle;
	
	StrokeTextView firstSubtitle;

	StrokeTextView secondSubtitle;

    private IPlayerUiControl mPlayerController;

	public YoukuPlayerView(Context context) {
		super(context);
		init(context);
	}

	public YoukuPlayerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public YoukuPlayerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public void setPlayerViewMinMaxWidth(int minW, int maxW)
	{
		mMinWidth = minW;
		mMaxWidth = maxW;
	}

	private LayoutInflater inflater;

	/**
	 * 找到播放器界面的layout并初始化
	 * 
	 * @param context
	 */
	private void init(Context context) {
		mContext = context;

		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		danmakuUtils = MediaPlayerConfiguration.getInstance().getDanmakuUtils();
		View view = inflater.inflate(R.layout.yp_player_view, null);
		this.addView(view);
		initLayout();
	}

	/**
	 * 初始化surface 界面的debug信息
	 */
	private void initLayout() {
		surfaceView = (NewSurfaceView) findViewById(R.id.surface_view);
		danmakuView = (DanmakuSurfaceView) findViewById(R.id.danmaku);
		waterMark = (ImageView) findViewById(R.id.water_mark);
		tudouWaterMark = (ImageView) findViewById(R.id.tudou_water_mark);
		playerDebugView = (TextView) findViewById(R.id.surface_view_debug);
		surfaceBlack = findViewById(R.id.surface_black);
		pluginHolder = (FrameLayout) findViewById(R.id.player_holder_all);

		singleSubtitle = (StrokeTextView) findViewById(R.id.single_subtitle);
		
		firstSubtitle = (StrokeTextView) findViewById(R.id.subtitle_first);

		secondSubtitle = (StrokeTextView) findViewById(R.id.subtitle_second);

		Resources res = getResources();
		waterMarkWidth = (int) res.getDimension(R.dimen.water_mark_width);
		waterMarkHeight = (int) res.getDimension(R.dimen.water_mark_height);
		tudouWaterMarkWidth = (int) res
				.getDimension(R.dimen.tudou_water_mark_width);
		tudouWaterMarkHeight = (int) res
				.getDimension(R.dimen.tudou_water_mark_height);
		waterMarkMarginTop = (int) res
				.getDimension(R.dimen.tudou_water_mark_margin_top);
		waterMarkMarginRight = (int) res
				.getDimension(R.dimen.tudou_water_mark_margin_right);
		fullwaterMarkMarginTop = (int) res
				.getDimension(R.dimen.tudou_full_water_mark_margin_top);
		fullwaterMarkMarginRight = (int) res
				.getDimension(R.dimen.tudou_full_water_mark_margin_right);
		layoutParams = waterMark.getLayoutParams();
		tudouLayoutParams = tudouWaterMark.getLayoutParams();
		danmakuLayoutParams = ((DanmakuSurfaceView) danmakuView)
				.getLayoutParams();
        isTudouPadDanmaku = MediaPlayerConfiguration.getInstance().showTudouPadDanmaku();
		initDanmakuLayout();
		surfaceView.setLayoutChangeListener(new LayoutChangeListener() {
			@Override
			public void onLayoutChange() {
				if (mMediaPlayerDelegate.mediaPlayer != null) {
					int width = surfaceView.getMeasuredWidth();
					int height = surfaceView.getMeasuredHeight();
					if (lastMeasuredWidth != width
							|| lastMeasuredHeight != height) {
						Logger.d(LogTag.TAG_PLAYER, "onLayoutChange:" + width + " "
								+ height);
						mMediaPlayerDelegate.mediaPlayer.changeVideoSize(width,
								height);
						lastMeasuredWidth = width;
						lastMeasuredHeight = height;
					}
				}
			}
		});
		if (MediaPlayerConfiguration.getInstance().hideDanmaku()) {
			if (danmakuView != null) {
				Logger.d(LogTag.TAG_DANMAKU, "设置弹幕不可见状态");
				danmakuView.setVisibility(View.INVISIBLE);
			}
		}
	}

	private static final boolean DEBUG = false;

	/**
	 * 设置debug信息
	 * 
	 * @param debug
	 *            需要显示到播放器界面上的信息
	 */
	public void setDebugText(final String debug) {
		if (!DEBUG)
			return;
		if (null != playerDebugView) {
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					playerDebugView.append("\n" + debug);
				}
			});
		}
	}

	@Override
	public void onBufferingUpdateListener(final int percent) {
	}

	@Override
	public void onCompletionListener() {
		setDebugText("播放完成onCompletionListener");
		setPlayerBlack();
	}

	@Override
	public boolean onErrorListener(int what, int extra) {
		// onerror时也将firstOnloaded置为false，防止出错重试时挂起后返回一直显示loading界面
		firstOnloaded = false;
		return false;
	}

	@Override
	public void OnPreparedListener() {
	}

	@Override
	public void OnSeekCompleteListener() {
		setDebugText("seek完成OnSeekCompleteListener");
	}

	@Override
	public void OnVideoSizeChangedListener(final int width, final int height) {
		if (mVideoHeight == height && mVideoWidth == width) {
			return;
		}
		mVideoWidth = width;
		mVideoHeight = height;
		if (width != 0 && height != 0) {
			mActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					surfaceView.setVideoSize(width, height);
				}
			});
		}
	}

	@Override
	public void OnTimeoutListener() {
		setDebugText("超时 OnTimeoutListener");
		firstOnloaded = false;
	}

	@Override
	public void OnCurrentPositionChangeListener(int currentPosition) {
		if (mMediaPlayerDelegate != null
				&& mMediaPlayerDelegate.videoInfo != null) {
			if (!mMediaPlayerDelegate.isComplete && !mMediaPlayerDelegate.isSeeking)
				mMediaPlayerDelegate.videoInfo.setProgress(currentPosition);
			// 付费视频需要在10分钟时候发送统计
			if (mMediaPlayerDelegate.videoInfo.paid
					&& !mMediaPlayerDelegate.videoInfo.paidSended
					&& currentPosition / 1000 == 600) {
				new DisposableHttpCookieTask(URLContainer.PLAY_LOG_URL
						+ "?vid=" + mMediaPlayerDelegate.videoInfo.getVid())
						.start();
				mMediaPlayerDelegate.videoInfo.paidSended = true;
			}
			if (mMediaPlayerDelegate.isTrialOver(currentPosition)) {
                mMediaPlayerDelegate.needPay();
			}
		}
		setPlayerBlackGone();
	}

	@Override
	public void onLoadedListener() {
		setDebugText("缓冲完成onLoadedListener");
		if (!firstOnloaded) {
			firstOnloaded = true;
		}
		if (surfaceBlack.getVisibility() == View.VISIBLE) {
			setPlayerBlackGone();
		}
		// Track.onRealVideoFirstLoadEnd();
		Track.onChangVideoQualityEnd(mActivity);
		if (Track.mIsChangingLanguage) {
			Track.mIsChangingLanguage = false;
		}
	}

	@Override
	public void onLoadingListener() {
		setDebugText("缓冲中onLoadingListener");
	}

	int videoSize = MediaPlayerDelegate.PLAY_100;

	private SharedPreferences sp;

	/**
	 * 调整播放画面的宽高比
	 * 
	 * @param force
	 *            是否强制刷新播放器宽高
	 */
	public void resizeMediaPlayer(boolean force) {
		if (mMediaPlayerDelegate != null) {
			if (mMediaPlayerDelegate.isFullScreen) {
				videoSize = sp.getInt("video_size", 100);
			} else {
				videoSize = 100;
			}
			resizeVideoView(videoSize, force);
		}
	}

	/**
	 * 全屏的时候设置全屏
	 */
	public void setFullscreenBack() {
		Logger.d(LogTag.TAG_PLAYER, "setFullscreenBack");
		this.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));
		isFullscreen = true;
		surfaceView.isFullScreen = true;
		this.setBackgroundColor(getResources().getColor(R.color.black));
	}

	/**
	 * 重新调整视频的画面
	 * 
	 * @param percent
	 *            画面百分比
	 * @param force
	 *            是否强制刷新
	 */
	public void resizeVideoView(int percent, boolean force) {
		videoSize = percent;
		surfaceView.setViewPercent(percent);
		surfaceView.requestLayout();
	}

	/**
	 * pad横屏时候播放器界面占总宽度的比例
	 */
	private final static float WIDTH_RATIO = 0.6625f;

	/**
	 * 设置哼屏幕布局
	 */
	public void setHorizontalLayout()// 设置横屏布局pad
	{
		isFullscreen = false;
		surfaceView.isFullScreen = false;
		Display getOrient = mActivity.getWindowManager().getDefaultDisplay();
		int playWidth = (int) ((int) getOrient.getWidth() * WIDTH_RATIO);
		this.setLayoutParams(new RelativeLayout.LayoutParams(playWidth,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
	}

	public void setHorizontalLayout(int height) {
		isFullscreen = false;
		surfaceView.isFullScreen = false;
		this.setLayoutParams(new RelativeLayout.LayoutParams(height * 16 / 9,
				height));
        smallViewHeight = height;
	}

	/**
	 * 设置竖屏布局
	 */
	public void setVerticalLayout()// 设置竖屏布局
	{
		Logger.d(LogTag.TAG_PLAYER, "setVerticalLayout");
		int width = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
				.getDefaultDisplay().getWidth();
		int height = (int) Math.ceil(width * 9 / 16f);
		this.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, height));
		isFullscreen = false;
		surfaceView.isFullScreen = false;
	}

	/**
	 * 初始化接口
	 * 
	 * @param mYoukuBaseActivity
	 * @param platformId
	 * @see {@link com.youku.player.base.Plantform}
	 * @param pid
	 *            各平台注册
	 * @param useSystemPlayer
	 *            强制硬解接口，使用这个参数将只能够播放m3u8
	 */
	public void initialize(YoukuBasePlayerActivity mYoukuBaseActivity,
			int platformId, String pid, String verName, String userAgent,
			boolean useSystemPlayer) {

		initialize(mYoukuBaseActivity, platformId, pid, verName, userAgent,
				useSystemPlayer, null, null);
	}

	/**
	 * 初始化接口
	 * 
	 * @param activity
	 * @param platformId
	 * @see {@link com.youku.player.base.Plantform}
	 * @param pid
	 *            各平台注册
	 * @param useSystemPlayer
	 *            强制硬解接口，使用这个参数将只能够播放m3u8
	 * @param timeStamp
	 *            时间戳
	 * @param secret
	 *            密匙
	 */
	public void initialize(FragmentActivity activity,
			int platformId, String pid, String verName, String userAgent,
			boolean useSystemPlayer, Long timeStamp, String secret) {

		long begin = SystemClock.elapsedRealtime();
		mActivity = activity;
		Profile.USE_SYSTEM_PLAYER = useSystemPlayer;
		player = new YoukuPlayer(activity,this);
        mPlayerController = player.getPlayerUiControl();
        mPlayerController.initLayoutView();
		Profile.PLANTFORM = platformId;
		Profile.pid = pid;
		Profile.USER_AGENT = userAgent;
		Util.TIME_STAMP = timeStamp;
		Util.SECRET = secret;
		URLContainer.verName = verName;
		URLContainer.getStatisticsParameter();
		MediaPlayerConfiguration.getInstance();
        mPlayerController.onCreate();
        ((OnInitializedListener)activity).onInitializationSuccess(player);
		trackPlayerLoad(SystemClock.elapsedRealtime() - begin);
	}

	/**
	 * 从播放器初始化到请求视频或广告文件片之前时间
	 * 
	 * @param duration
	 */
	private void trackPlayerLoad(long duration) {
		long currentTime = System.currentTimeMillis();
		HashMap<String, String> extend = new HashMap<String, String>();
		extend.put("pltype", "playerload");
		extend.put("s", duration + "");
		extend.put("st", (currentTime - duration) + "");
		extend.put("et", currentTime + "");
		AnalyticsWrapper.trackExtendCustomEvent(mContext,
                PlayerStatistics.PALYER_LOAD, PlayerStatistics.PAGE_NAME, null,
                extend);
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
		setDebugText("新视频newVideo");
	}

	@Override
	public void onVolumnUp() {
		setDebugText("音量调大onVolumnUp");
	}

	@Override
	public void onVolumnDown() {
		setDebugText("音量调小onVolumnDown");
	}

	@Override
	public void onMute(boolean mute) {
		setDebugText("静音onMute");
	}

	@Override
	public void onVideoChange() {
		setDebugText("获取信息中onVideoChange");
		firstOnloaded = false;
		realVideoStart = false;
	}

	@Override
	public void onVideoInfoGetting() {
		setDebugText("获取信息中onVideoInfoGetting");
		firstOnloaded = false;
		setPlayerBlack();
		realVideoStart = false;
        surfaceView.setOrientation(0);
	}

	@Override
	public void onVideoInfoGetted() {
	}

	@Override
	public void onVideoInfoGetFail(boolean needRetry) {
		setDebugText("获取信息失败onVideoInfoGetFail");
	}

	@Override
	public void setVisible(boolean visible) {
	}

	/**
	 * 播放完成
	 */
	protected void playComplete() {
		Track.setplayCompleted(true);
		if (mMediaPlayerDelegate != null) {
			mMediaPlayerDelegate.release();
			mMediaPlayerDelegate.videoInfo.setProgress(0);
		}
	}

	@Override
	public void onNotifyChangeVideoQuality() {
		setDebugText("播放清晰度变化onNotifyChangeVideoQuality");
	}

	@Override
	public void onRealVideoStart() {
		setDebugText("正片开始播放 onRealVideoStart");
		realVideoStart = true;
		if (surfaceView.getOrientation() != 0
				&& mMediaPlayerDelegate.mediaPlayer != null)
			mMediaPlayerDelegate.mediaPlayer.setVideoOrientation(surfaceView.getOrientation());
		setupWaterMark();
	}

	@Override
	public void onADplaying() {
		setDebugText("广告正在播放 onADplaying");

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

	}

	@Override
	public void back() {
	}

	/**
	 * 设置播放器画面为黑色
	 */
	public void setPlayerBlack() {
		if (surfaceBlack != null) {
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					surfaceBlack.setVisibility(View.VISIBLE);
				}
			});
		}
	}

	/**
	 * 去掉播放器的黑色
	 */
	public void setPlayerBlackGone() {
		if (surfaceBlack != null) {
			mActivity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    surfaceBlack.setVisibility(View.GONE);
                }
            });
		}
	}

	@Override
	public void onPlayNoRightVideo(GoplayException e) {

	}

	@Override
	public void onPlayReleateNoRightVideo() {

	}

    public void recreateSurfaceHolder() {
		Logger.d(LogTag.TAG_PLAYER, "recreateSurfaceHolder");
		if (surfaceView != null && mMediaPlayerDelegate != null)
			surfaceView.recreateSurfaceHolder();
		setWaterMarkVisible(false);
	}

	public void setWaterMarkVisible(boolean visible) {
		if (waterMark != null) {
			waterMark.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
		}
	}

	public void setTudouWaterMarkInvisible() {
		if (tudouWaterMark != null) {
			tudouWaterMark.setVisibility(View.INVISIBLE);
		}
	}

	public void setTudouWaterMarkFrameType(int type) {
		if (tudouWaterMark == null) {
			return;
		}
		switch (type) {
		case FIRST_FRAME:
			tudouWaterMark.setVisibility(View.VISIBLE);
			tudouWaterMark.setImageResource(R.drawable.play_mark_01);
			break;
		case LAST_FRAME:
			tudouWaterMark.setVisibility(View.VISIBLE);
			tudouWaterMark.setImageResource(R.drawable.play_mark_12);
			break;
		case ANIMATION:
			tudouWaterMark.setVisibility(View.VISIBLE);
			tudouWaterMark
					.setImageResource(R.drawable.yp_tudou_water_mark_anim);
			waterMarkAnimation = (AnimationDrawable) tudouWaterMark
					.getDrawable();
			waterMarkAnimation.stop();
			waterMarkAnimation.start();
			break;
		default:
			break;
		}
	}

	public void zoomWaterMark(int scaleParamA, int scaleParmB) {
		if (waterMark != null) {
			if (layoutParams != null) {
				layoutParams.width = waterMarkWidth * scaleParamA / scaleParmB;
				layoutParams.height = waterMarkHeight * scaleParamA
						/ scaleParmB;
				waterMark.setLayoutParams(layoutParams);
			}
		}
	}

	public void zoomTudouWaterMark() {
		if (tudouWaterMark != null) {
			if (tudouLayoutParams != null) {
				if (isFullscreen) {
					((MarginLayoutParams) tudouLayoutParams)
							.setMargins(0, fullwaterMarkMarginTop,
									fullwaterMarkMarginRight, 0);
					tudouLayoutParams.width = tudouWaterMarkWidth;
					tudouLayoutParams.height = tudouWaterMarkHeight;
				} else {
					((MarginLayoutParams) tudouLayoutParams).setMargins(0,
							waterMarkMarginTop, waterMarkMarginRight, 0);
					tudouLayoutParams.width = tudouWaterMarkWidth * 7 / 10;
					tudouLayoutParams.height = tudouWaterMarkHeight * 7 / 10;
				}
				tudouWaterMark.setLayoutParams(tudouLayoutParams);
			}
		}
	}

	public void zoomDanmakuView() {
		if (danmakuView != null && !MediaPlayerConfiguration.getInstance().hideDanmaku()) {
			if (danmakuLayoutParams != null) {
				danmakuLayoutParams.height = mActivity.getWindowManager()
						.getDefaultDisplay().getHeight() * 4 / 5;
				((DanmakuSurfaceView) danmakuView)
						.setLayoutParams(danmakuLayoutParams);
			}
		}
	}

    public void zoomDanmakuSmallView() {
//        if (!isTudouPadDanmaku) {
//            return;
//        }
        if (danmakuView != null) {
            if (danmakuLayoutParams != null) {
                danmakuLayoutParams.height = this.getMeasuredHeight() * 4 / 5;
                ((DanmakuSurfaceView) danmakuView)
                        .setLayoutParams(danmakuLayoutParams);
            }
        }
    }

	/**
	 * 设置字幕信息
	 * 
	 * @param subtitle
	 *            要显示的字幕信息
	 */
	public void setFirstSubtitle(final String subtitle) {
		if (null != firstSubtitle) {
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					firstSubtitle.setVisibility(View.GONE);
					firstSubtitle.setVisibility(View.VISIBLE);
					firstSubtitle.setText(subtitle);
				}
			});
		}
	}

	public void dismissFirstSubtitle() {
		if (null != firstSubtitle) {
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					firstSubtitle.setVisibility(View.GONE);
				}
			});
		}
	}

	public void setSecondSubtitle(final String subtitle) {
		if (null != secondSubtitle) {
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					secondSubtitle.setVisibility(View.GONE);
					secondSubtitle.setVisibility(View.VISIBLE);
					secondSubtitle.setText(subtitle);
				}
			});
		}
	}

	public void dismissSecondSubtitle() {
		if (null != secondSubtitle) {
			mActivity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    secondSubtitle.setVisibility(View.GONE);
                }
            });
		}
	}
	
	public void setSingleSubtitle(final String subtitle) {
		if (null != singleSubtitle) {
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					singleSubtitle.setVisibility(View.GONE);
					singleSubtitle.setVisibility(View.VISIBLE);
					singleSubtitle.setText(subtitle);
				}
			});
		}
	}

	public void dismissSingleSubtitle() {
		if (null != singleSubtitle) {
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					singleSubtitle.setVisibility(View.GONE);
				}
			});
		}
	}

	@Override
	public void onRelease() {
		super.onRelease();
		
		singleSubtitle.setText(null);
		
		firstSubtitle.setText(null);

		secondSubtitle.setText(null);
	}

	public void beginDanmaku(String jsonArray, final long beginTime) {
		if (danmakuView != null && danmakuUtils != null) {
			mParser = createParser(jsonArray);
            danmakuUtils.setTextSize(mParser, mContext);
			danmakuUtils.setDanmakuContextAndDrawable(danmakuContext, defaultDrawable);
			danmakuView.setCallback(new Callback() {

				@Override
				public void updateTimer(DanmakuTimer timer) {
					currMillisecond = timer.currMillisecond;
				}

				@Override
				public void drawingFinished() {

				}

				@Override
				public void prepared() {
					danmakuView.start(beginTime);
				}
			});
			danmakuView.prepare(mParser, danmakuContext);
		}
	}

	public void layoutSurfaceViewAndPlugins(int parentWidth, int parentHeight, float videoSizeRatio)
	{
		RelativeLayout.LayoutParams ps = (RelativeLayout.LayoutParams)pluginHolder.getLayoutParams();
		ps.width = parentWidth; ps.height = parentHeight;
		pluginHolder.setLayoutParams(ps);
		pluginHolder.layout(0, 0, parentWidth, parentHeight);

		int realVideoW = 0, realVideoH = 0;
		int left = 0, top = 0;
		if(videoSizeRatio==0.0f || videoSizeRatio == STANDARD_VIDEOSIZE_RATIO) {
			left = top = 0;
			realVideoW = parentWidth;
			realVideoH = parentHeight;
		} else if(videoSizeRatio < STANDARD_VIDEOSIZE_RATIO) {
			realVideoW = parentWidth;
			realVideoH = (int) (parentWidth * videoSizeRatio);
			left = 0;
			top = (parentHeight - realVideoH)/2;
		}else {
			realVideoH = parentHeight;
			realVideoW = (int) (realVideoH/videoSizeRatio);
			top = 0;
			left = (parentWidth - realVideoW)/2;
		}
		RelativeLayout.LayoutParams pms = (RelativeLayout.LayoutParams)surfaceView.getLayoutParams();
		pms.width = realVideoW; pms.height = realVideoH;
		surfaceView.setLayoutParams(pms);
		surfaceView.layout(left, top, left + realVideoW, top + realVideoH);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Logger.d(
				TAG,
				"YoukuPlayerViewonMeasure:"
						+ MeasureSpec.toString(widthMeasureSpec) + "  "
						+ MeasureSpec.toString(heightMeasureSpec));
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
        height = width * 9 / 16;
		Logger.d(TAG, "YoukuPlayerViewnow:" + width + " " + height);
		surfaceView.setParentSize(width, height);

		//为了解决拖动过快而导致某些帧changeSize失效问题
		if(mMinWidth == width || mMaxWidth-300<=width)
		{
			layoutSurfaceViewAndPlugins(width, height, surfaceView.getSizeRatio());
		}

		zoomTudouWaterMark();
		if (!isFullscreen) {
            zoomDanmakuSmallView();
			zoomWaterMark(4, 5);
            if (mPlayerController.getDanmakuManager() != null) {
                mPlayerController.getDanmakuManager().setDanmakuTextScale(false);
                if ((!Profile.getDanmakuSwith(mContext) && !mPlayerController
                        .getDanmakuManager().isPaused())
                        || (!Profile.getLiveDanmakuSwith(mContext) && mPlayerController
                        .getDanmakuManager().isHls())) {
                    //小屏不关闭弹幕
                    //mPlayerController.getDanmakuManager().hideDanmakuWhenRotate();
                }
            }
        } else {
            zoomDanmakuView();
            if (mPlayerController.getDanmakuManager() != null) {
                mPlayerController.getDanmakuManager().setDanmakuTextScale(true);
                if ((!Profile.getDanmakuSwith(mContext) && !mPlayerController
                        .getDanmakuManager().isPaused())
                        || (!Profile.getLiveDanmakuSwith(mContext) && mPlayerController
                        .getDanmakuManager().isHls())) {
//                    mPlayerController.getDanmakuManager().showDanmakuWhenRotate();
                }
            }
            if (videoSize == MediaPlayerDelegate.PLAY_FULL
					|| videoSize == MediaPlayerDelegate.PLAY_100) {
				zoomWaterMark(1, 1);
			} else if (videoSize == MediaPlayerDelegate.PLAY_50) {
				zoomWaterMark(1, 2);
			} else if (videoSize == MediaPlayerDelegate.PLAY_75) {
				zoomWaterMark(3, 4);
			}
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//	    setMeasuredDimension(width, height);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//if(changed){
//    Log.d("", "");
//}
//int statusHeight = 0;
//        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
//        if (resourceId > 0) {
//            statusHeight = getResources().getDimensionPixelSize(resourceId);
//        }

        super.onLayout(changed, l, t, r, b);
        int parentLeft = this.getLeft();
        int parentTop = this.getTop();
        int parentRight = this.getRight();
        int parentBottom = this.getBottom();
        int parentWidth = parentRight - parentLeft;
        int parentHeight = parentWidth * 9 / 16;
        int width = surfaceView.getMeasuredWidth();// mPlayerController.getMediaPlayerDelegate().getVideoWidth();//surfaceView.getMeasuredWidth();
        int height = surfaceView.getMeasuredHeight();// mPlayerController.getMediaPlayerDelegate().getVideoHeight();//surfaceView.getMeasuredHeight();//width * 9 / 16;

        int marginLeft = (parentWidth - width) / 2;
//        int marginTop = ((parentBottom - parentTop) - height) / 2; //这样计算不准确
        int marginTop = (parentHeight - height) / 2;
        int childLeft = parentLeft + surfaceView.getPaddingLeft() + this.getPaddingLeft();
        int childTop = parentTop + surfaceView.getPaddingTop() + getPaddingTop();

        Log.d("smingerPlayerYP", "********************************");
//        Log.d("smingerPlayerYP", "statusHeight:" + statusHeight);
        Log.d("smingerPlayerYP", "videoWidth:" + width);
        Log.d("smingerPlayerYP", "videoHeight:" + height);
        Log.d("smingerPlayerYP", "parentwidth:" + (parentRight - parentLeft));
        Log.d("smingerPlayerYP", "parentheight:" + (parentBottom - parentTop));
        Log.d("smingerPlayerYP", "parentratio:" + 1.0f * (parentBottom - parentTop) / (parentRight - parentLeft));
        Log.d("smingerPlayerYP", "parentLeft:" + parentLeft);
        Log.d("smingerPlayerYP", "parentTop:" + parentTop);
        Log.d("smingerPlayerYP", "parentRight:" + parentRight);
        Log.d("smingerPlayerYP", "parentBottom:" + parentBottom);
//        surfaceView.layout(childLeft + marginLeft, childTop + marginTop, childLeft + marginLeft + width, childTop + marginTop + height);

//        surfaceView.layout(parentLeft, parentTop, parentRight, parentBottom);
        Log.d("smingerPlayerYP", "width:" + (r - l));
        Log.d("smingerPlayerYP", "height:" + (b - t));
        Log.d("smingerPlayerYP", "ratio:" + 1.0f * (b - t) / (r - l));
        Log.d("smingerPlayerYP", "l:" + l);
        Log.d("smingerPlayerYP", "t:" + t);
        Log.d("smingerPlayerYP", "r:" + r);
        Log.d("smingerPlayerYP", "b:" + b);
        Log.d("smingerPlayerYP", "********************************");


    }

	public interface LayoutChangeListener {
		public void onLayoutChange();
	}

	private BaseDanmakuParser createParser(String jsonArray) {
		if (jsonArray == null) {
			Logger.d(LogTag.TAG_DANMAKU, "开始弹幕，但使用假数据");
			jsonArray = danmakuUtils.getFakeJSONArray();
		}

		ILoader loader = danmakuUtils.createDanmakuLoader();

		try {
			loader.load(jsonArray);
		} catch (IllegalDataException e) {
			e.printStackTrace();
		}
		BaseDanmakuParser parser = danmakuUtils.createDanmakuParser();
		IDataSource<?> dataSource = loader.getDataSource();
		parser.load(dataSource);
		return parser;

	}

	public void addDanmaku(String json, ArrayList<LiveDanmakuInfo> liveDanmakuInfos) {
		try {
			if (danmakuView != null && mParser != null && danmakuUtils != null) {
				danmakuUtils.addDanmaku(new JSONObject(json), danmakuView,
						mParser, danmakuView.getCurrentTime(), liveDanmakuInfos);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public IDanmakuView getDanmakuSurfaceView() {
		return danmakuView;
	}

	public BaseDanmaku sendDanmaku(int size, int position, int color,
			CharSequence content) {
		if (danmakuView != null && mParser != null && danmakuUtils != null) {
			BaseDanmaku danmaku = danmakuContext.mDanmakuFactory.createDanmaku(position);
			if (danmaku != null) {
				danmaku.text = content;
				danmaku.priority = 1;
				if (mParser.getTimer() != null) {
					danmaku.time = danmakuUtils.getCurrentMillisecond(mParser, danmakuView.getCurrentTime()) + 100;
				}
				if (mParser.getDisplayer() != null) {
					danmaku.textSize = danmakuUtils.getTextSize() * (mParser.getDisplayer()
							.getDensity() - 0.6f);
				}
				danmaku.textColor = color | 0xFF000000;
				if (content instanceof Spanned) {
					danmaku.textShadowColor = 0;
				} else {
					danmaku.textShadowColor = danmaku.textColor <= Color.BLACK ? Color.WHITE
							: Color.BLACK;
					danmaku.borderColor = danmakuUtils.getDanmakuSendColor(color);
				}
				danmakuView.addDanmaku(danmaku);
			}
			return danmaku;
		}
		return null;
	}

	// 目前在暂停按钮和onPause时会调用，onPause不会暂停，按暂停按钮，虽然会暂停，但时间点有误。
	public void pauseDanmaku() {
		if (danmakuView != null) {
			danmakuView.pause();
		}
	}

	// 目前在开始播放按钮调用
	public void resumeDanmaku() {
		if (danmakuView != null) {
			danmakuView.resume();
		}
	}

	// 前后seek，注意seek时要清空danmukuList，已修改
	public void seekToDanmaku(Long ms) {
		if (danmakuView != null) {
			danmakuView.seekTo(ms);
		}
	}

	public void showDanmaku() {
		if (danmakuView != null && !isNotAtTop) {
			danmakuView.show();
		}
	}

	public void hideDanmaku() {
		if (danmakuView != null) {
			danmakuView.hide();
		}
	}

	public void releaseDanmaku() {
		if (danmakuView != null) {
			danmakuView.release();
		}
	}
	
	public void setOrientation(int orientation){
		surfaceView.setOrientation(orientation);
	}
	
	public void setDanmakuVisibleWhenLive() {
		if (danmakuView != null) {
			danmakuView.setVisibility(View.VISIBLE);
		}
	}
	
	public void setDanmakuPosition(int position) {
		int scale = DanmakuStatics.DANMAKU_FULL_SCREEN_SCALE;
		switch (position) {
		case DanmakuStatics.DANMAKU_FULL_SCREEN:
			scale = DanmakuStatics.DANMAKU_FULL_SCREEN_SCALE;
			break;
		case DanmakuStatics.DANMAKU_TOP_SCREEN:
			scale = DanmakuStatics.DANMAKU_TOP_SCREEN_SCALE;
			break;
		default:
			break;
		}
        Logger.d(LogTag.TAG_DANMAKU, "position=" + position);
		if (danmakuView != null) {
			if (danmakuLayoutParams != null && mActivity != null) {
                danmakuLayoutParams.height = isFullscreen ? mActivity.getWindowManager()
                        .getDefaultDisplay().getHeight() * scale / 3 :
                        mActivity.getWindowManager().getDefaultDisplay().getWidth() * scale / 3;
                Logger.d(LogTag.TAG_DANMAKU, "height=" + danmakuLayoutParams.height);
                ((DanmakuSurfaceView) danmakuView)
						.setLayoutParams(danmakuLayoutParams);
			}
		}
	}
	
	public void setDanmakuEffect(int effect) {
		if (effect == DanmakuStatics.DANMAKU_EFFECT_INTENSIVE) {
			danmakuContext
					.setScrollSpeedFactor(DanmakuStatics.DANMAKUFASTRATE);
		} else if (effect == DanmakuStatics.DANMAKU_EFFECT_COMFORTTABLE) {
			danmakuContext
					.setScrollSpeedFactor(DanmakuStatics.DANMAKURATE);
		}
	}
	
	public void initDanmakuLayout() {
		danmakuContext = DanmakuContext.create();
		danmakuContext.setCacheStuffer(new SpannedCacheStuffer(), mCacheStufferAdapter);
		danmakuContext.setDanmakuStyle(
				IDisplayer.DANMAKU_STYLE_SHADOW, 3);
		defaultDrawable = mContext.getResources().getDrawable(R.drawable.default_danmu);
		if (!MediaPlayerConfiguration.getInstance().hideDanmaku()) {
			setDanmakuEffect(DanmakuStatics.DANMAKU_EFFECT_COMFORTTABLE);
		} else {
			setDanmakuEffect(Profile.getDanmakuEffect(mContext));
		}
	}

	private BaseCacheStuffer.Proxy mCacheStufferAdapter = new BaseCacheStuffer.Proxy() {


		@Override
		public void prepareDrawing(final BaseDanmaku danmaku, boolean fromWorkerThread) {
			if(danmaku.isStar && !danmaku.isStarAdded) {
				danmaku.isStarAdded = true;
				Logger.d("star", "prepareDrawing:" + danmaku.time);
				danmakuUtils.requestStarImage(danmaku, danmakuView);
			}
		}

		@Override
		public void releaseResource(BaseDanmaku danmaku) {
			// TODO 重要:清理含有ImageSpan的text中的一些占用内存的资源 例如drawable
		}
	};
	
	public  void setupWaterMark() {
		final int currentQuality = mMediaPlayerDelegate.videoInfo
				.getCurrentQuality();
		String quality = null;
		String isHardWaterMark = null;
		String isLocalHardWaterMark = null;
		Logger.d(LogTag.TAG_WATERMARK, "当前品质是" + currentQuality);
		Logger.d(LogTag.TAG_WATERMARK, "当前显示类型"
				+ mMediaPlayerDelegate.videoInfo.waterMarkType[currentQuality]);
		for (int i = 0; i < 5; i++) {
			if (i == 3)
				continue;
			switch (i) {
			case 0:
				quality = "超清视频";
				break;
			case 1:
				quality = "高清视频";
				break;
			case 2:
				quality = "标清视频";
				break;
			case 4:
				quality = "其他视频";
			default:
				break;
			}
			if (mMediaPlayerDelegate.videoInfo.isWaterMark[i] != 0) {
				isHardWaterMark = ",有硬水印";
			} else {
				isHardWaterMark = ",没有硬水印";
			}
			Logger.d(LogTag.TAG_WATERMARK, quality + isHardWaterMark);
		}
		if (mMediaPlayerDelegate.videoInfo.isLocalWaterMark) {
			isLocalHardWaterMark = ",有硬水印";
		} else {
			isLocalHardWaterMark = ",没有硬水印";
		}
		Logger.d(LogTag.TAG_WATERMARK, "缓存视频" + isLocalHardWaterMark);
		if (mMediaPlayerDelegate.videoInfo.isHLS
				|| mMediaPlayerDelegate.videoInfo.isExternalVideo
				|| mMediaPlayerDelegate.videoInfo.isLocalWaterMark
				|| (mMediaPlayerDelegate.videoInfo.isWaterMark[currentQuality] != 0)) {
			mActivity.runOnUiThread(new Runnable() {
				public void run() {
					MediaPlayerConfiguration.getInstance().mPlantformController
							.setWaterMarkInvisible(YoukuPlayerView.this);
				}
			});
		} else {
			mActivity.runOnUiThread(new Runnable() {
				public void run() {
					MediaPlayerConfiguration.getInstance().mPlantformController
							.setWaterMarkVisible(
									YoukuPlayerView.this,
									mMediaPlayerDelegate.videoInfo.waterMarkType[currentQuality]);
				}
			});
		}
	}

    private boolean isNotAtTop = false;
    @Override
    public void viewPositionChange(View view, float offset) {
        isNotAtTop = offset > 0.01;
        if(mMediaPlayerDelegate != null && !mMediaPlayerDelegate.isFullScreen) {
            if (isNotAtTop) {
                mMediaPlayerDelegate.setOrientionDisable();
                //播放加密视频的时候，如果滑动播放器，则关闭加密输入密码对话框
                MediaPlayerConfiguration.getInstance().mPlantformController
                        .disMissEncryptDialog();
            } else {
                mMediaPlayerDelegate.setOrientionEnable();
            }
        }
    }
}

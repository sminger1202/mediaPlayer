package com.youku.player.danmaku;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import com.baseproject.utils.Logger;
import com.youku.player.LogTag;
import com.youku.player.base.YoukuPlayerView;
import com.youku.player.config.MediaPlayerConfiguration;
import com.youku.player.goplay.Profile;
import com.youku.player.plugin.MediaPlayerDelegate;

import java.util.ArrayList;

import master.flame.danmaku.danmaku.model.LiveDanmakuInfo;

public class DanmakuManager implements IDanmakuManager {

	private YoukuPlayerView mYoukuPlayerView;
	private Context context;
	private MediaPlayerDelegate mMediaPlayerDelegate;
	public String currentVid;
	private int currentCid;
	public DanmakuUtils danmakuUtils;

	public boolean isGetDanmakuStatus;
	public String danmakuStatus;
	public ArrayList<Integer> danmakuRequest = new ArrayList<Integer>();
	public int danmakuFailedCount;
	public boolean danmakuSwith;

	// 弹幕相关参数
	public int prePosition = -1;
	public int currentGolbalPosition;
	public int currentPosition;
	public int danmakuRequestTimes;
	public String danmakuJsonArray;
	public int danmakuProcessStatus;
	public int danmakuNoDataStatus;
	public int beginTime = -1;
	public boolean isDanmakuNoData = false;
	public boolean isFirstOpen = false;
	public boolean isPaused = false;
	public boolean isDanmakuShow = false;
	public boolean isDanmakuHide = false;
	public boolean isUserShutUp = false;
	public ArrayList<String> starUids;
	public boolean isHLS = false;
	public static final int DANMAKUOPEN = 0x1;
	public static final int DANMAKUPLAY = 0x2;
	public static final int DANMAKUPAUSE = 0x4;
	public static final int DANMAKUCLOSE = 0;
    public boolean isFullScreenDanmaku;
    public boolean isSmallScreenDanmaku;
	public int lastSeekTime = -1;

	public static Handler danmakuHandler = new Handler() {};

	public DanmakuManager(Context context, YoukuPlayerView view,
			MediaPlayerDelegate mediaPlayerDelegate, String vid, int cid) {
		this.context = context;
		mYoukuPlayerView = view;
		mMediaPlayerDelegate = mediaPlayerDelegate;
		currentVid = vid;
		currentCid = cid;
		danmakuUtils = MediaPlayerConfiguration.getInstance().getDanmakuUtils();
	}

	@Override
	public void setVid(String vid, int cid) {
		currentVid = vid;
		currentCid = cid;
	}

	@Override
	public void handleDanmakuInfo(final String iid, final int minute_at,
								  final int minute_count) {
		if (MediaPlayerConfiguration.getInstance().hideDanmaku()
				|| danmakuRequest.contains(minute_at)) {
			Logger.d(LogTag.TAG_DANMAKU, "第" + minute_at + "分钟的数据已经被添加，无需再次添加");
			return;
		}
		final MyGetDanmakuManager myGetDanmakuManager = new MyGetDanmakuManager();
		if (isGetDanmakuStatus == true) {
			if (danmakuStatus != null && minute_at < danmakuStatus.length()
					&& danmakuStatus.charAt(minute_at) != '0') {
				handleSuccessfullGetDanmakuStatus(iid, minute_at, minute_count,
						myGetDanmakuManager);
			} else {
				Logger.d(LogTag.TAG_DANMAKU, "本分钟内没有弹幕");
				danmakuRequest.add(minute_at);
				if (danmakuJsonArray == null && danmakuStatus != null
						&& (minute_at + 1) < danmakuStatus.length()) {
					Logger.d(LogTag.TAG_DANMAKU, "由于视频没有弹幕，正在请求第" + (minute_at + 1)
							+ "分钟数据");
					handleDanmakuInfo(iid, minute_at + 1, minute_count);
				}
			}
			return;
		}
		myGetDanmakuManager.goGetDanmakuStatus(iid, currentCid,
				new IDanmakuInfoCallBack() {

					@Override
					public void onSuccess(String danmakuInfo,
							boolean danmakuEnable, boolean isUserShutUp, ArrayList<String> starUids) {
						isGetDanmakuStatus = true;
						DanmakuManager.this.isUserShutUp = isUserShutUp;
						DanmakuManager.this.starUids = starUids;
						danmakuStatus = danmakuInfo;
						Logger.d(LogTag.TAG_DANMAKU, "返回状态是" + danmakuStatus
								+ ",danmakuEnable=" + danmakuEnable);
						danmakuProcessStatus |= DANMAKUOPEN;
                        handleDanmakuEnable(danmakuEnable);
						if (!danmakuEnable
								|| (danmakuInfo != null && danmakuInfo
										.equals("0"))) {
							if (danmakuProcessStatus < DANMAKUPLAY) {
								beginDanmaku(null, currentPosition);
							}
							danmakuProcessStatus = DANMAKUCLOSE;
							isDanmakuNoData = true;
							if (((danmakuProcessStatus & DANMAKUPAUSE) != 0)) {
								danmakuNoDataStatus = DANMAKUPAUSE;
							} else {
								danmakuNoDataStatus = DANMAKUPLAY;
							}
							if (mMediaPlayerDelegate !=null && mMediaPlayerDelegate.isFullScreen || MediaPlayerConfiguration.getInstance().showTudouPadDanmaku()) {
								if (danmakuHandler != null) {
									danmakuHandler.postDelayed(new Runnable() {

										@Override
										public void run() {
											showDanmaku();
										}
									}, 100);
								}
							}
							return;
						}
						if (danmakuStatus != null
								&& minute_at < danmakuStatus.length()
								&& danmakuStatus.charAt(minute_at) != '0') {
							handleSuccessfullGetDanmakuStatus(iid, minute_at,
									minute_count, myGetDanmakuManager);
						} else {
							Logger.d(LogTag.TAG_DANMAKU, "本分钟内没有弹幕");
							danmakuRequest.add(minute_at);
							if (danmakuJsonArray == null
									&& danmakuStatus != null
									&& (minute_at + 1) < danmakuStatus.length()) {
								Logger.d(LogTag.TAG_DANMAKU, "由于视频没有弹幕，正在请求第"
										+ (minute_at + 1) + "分钟数据");
								handleDanmakuInfo(iid, minute_at + 1,
										minute_count);
							}
						}
					}

					@Override
					public void onFailed() {
						Logger.d(LogTag.TAG_DANMAKU, "获取弹幕状态失败");
						isGetDanmakuStatus = false;
						handleSuccessfullGetDanmakuStatus(iid, minute_at,
								minute_count, myGetDanmakuManager);
					}
				});
	}

	private void handleSuccessfullGetDanmakuStatus(final String iid,
			final int minute_at, final int minute_count,
			MyGetDanmakuManager myGetDanmakuManager) {
		myGetDanmakuManager.goGetDanmakuUrl(iid, minute_at, minute_count,
				new IDanmakuInfoCallBack() {

					@Override
					public void onSuccess(String danmakuInfo,
							boolean danmakuEnable, boolean isUserShutUp, ArrayList<String> starUids) {
						if (danmakuInfo == null) {
							return;
						}
						Logger.d(LogTag.TAG_DANMAKU,
								"返回数据是" + danmakuInfo.substring(0, 30));
						danmakuProcessStatus |= DANMAKUOPEN;
						if (danmakuRequest.contains(minute_at)
								|| getDanmakuCount(danmakuInfo) == 0) {
							return;
						}
						if (danmakuRequestTimes >= 1) {
							Logger.d(LogTag.TAG_DANMAKU, "正片播放中，增加弹幕");
							addDanmaku(danmakuInfo);
						} else if (danmakuRequestTimes == 0) {
							danmakuJsonArray = danmakuInfo;
							Logger.d(LogTag.TAG_DANMAKU, "第一次添加弹幕");
						}
						danmakuRequest.add(minute_at);
						for (int i = 0; i < danmakuRequest.size(); i++) {
							Logger.d(LogTag.TAG_DANMAKU,
									"目前已经添加的数据是第" + danmakuRequest.get(i)
											+ "分钟");
						}
					}

					@Override
					public void onFailed() {
						Logger.d(LogTag.TAG_DANMAKU, "获取第" + minute_at + "分钟弹幕数据失败");
						if (danmakuRequest.contains(minute_at)) {
							return;
						}
						if (danmakuFailedCount++ > 30) {
							Logger.d(LogTag.TAG_DANMAKU, "弹幕失败次数大于30次，不继续请求");
                            danmakuFailedCount = 0;
							return;
						}
						handleDanmakuInfo(currentVid, minute_at, minute_count);
					}
				});
	}

	@Override
	public void sendDanmaku(int size, int position, int color, String content) {
		if (danmakuUtils != null) {
			danmakuUtils.sendDanmaku(size, position, color, content,
					mMediaPlayerDelegate, mYoukuPlayerView, context, this);
		}
	}

	@Override
	public void sendDanmaku(int color, String content) {
		sendDanmaku(25, 1, color, content);
	}

	@Override
	public void beginDanmaku(String jsonArray, int beginTime) {
		if (danmakuUtils != null) {
			danmakuUtils.beginDanmaku(jsonArray, beginTime, this,
					mYoukuPlayerView);
		}
	}

	@Override
	public void addDanmaku(String json, ArrayList<LiveDanmakuInfo> liveDanmakuInfos) {
		if (danmakuUtils != null) {
			if (json == null) {
				json = danmakuUtils.getFakeJSONArray();
			}
			danmakuUtils.addDanmaku(json, mYoukuPlayerView, this, liveDanmakuInfos);
		}
	}
	
	@Override
	public void addDanmaku(String json) {
		addDanmaku(json, null);
	}
	
	@Override
	public void setDanmakuPosition(int position) {
		if (mYoukuPlayerView != null) {
			mYoukuPlayerView.setDanmakuPosition(position);
		}
	}

	@Override
	public void setDanmakuEffect(int effect) {
		if (mYoukuPlayerView != null) {
			mYoukuPlayerView.setDanmakuEffect(effect);
		}
	}
	
	@Override
	public void pauseDanmaku() {
		if (isDanmakuNoData) {
			if ((danmakuNoDataStatus & DANMAKUPLAY) != 0) {
				if (mYoukuPlayerView != null) {
					danmakuNoDataStatus = DANMAKUPAUSE;
					mYoukuPlayerView.pauseDanmaku();
					Logger.d(LogTag.TAG_DANMAKU, "暂停弹幕");
				}
			}
			return;
		}
		if (((danmakuProcessStatus & DANMAKUOPEN) != 0)
				&& ((danmakuProcessStatus & DANMAKUPLAY) != 0)) {
			if (mYoukuPlayerView != null) {
				danmakuProcessStatus = DANMAKUPAUSE | DANMAKUOPEN;
				mYoukuPlayerView.pauseDanmaku();
				Logger.d(LogTag.TAG_DANMAKU, "暂停弹幕");
			}
		}
	}

	@Override
	public void resumeDanmaku() {
		if (isPaused) {
			return;
		}
		if (isDanmakuNoData) {
			if ((danmakuNoDataStatus & DANMAKUPAUSE) != 0) {
				if (mYoukuPlayerView != null) {
					danmakuNoDataStatus = DANMAKUPLAY;
					mYoukuPlayerView.resumeDanmaku();
					Logger.d(LogTag.TAG_DANMAKU, "继续播放弹幕");
				}
			}
			return;
		}
		if (((danmakuProcessStatus & DANMAKUOPEN) != 0)
				&& ((danmakuProcessStatus & DANMAKUPAUSE) != 0)) {
			if (mYoukuPlayerView != null) {
				danmakuProcessStatus = DANMAKUPLAY | DANMAKUOPEN;
				mYoukuPlayerView.resumeDanmaku();
				Logger.d(LogTag.TAG_DANMAKU, "继续播放弹幕");
			}
		}
	}

	@Override
	public void seekToDanmaku(final int ms) {
		if ((danmakuProcessStatus & DANMAKUOPEN) != 0) {
			if (mYoukuPlayerView != null && mMediaPlayerDelegate != null) {
				Logger.d(LogTag.TAG_DANMAKU, "Seek到" + ms / 60000 + "分"
						+ (ms / 1000 % 60) + "秒");
				Logger.d(LogTag.TAG_DANMAKU, "Seek，正在请求第" + ms / 60000 + "分钟数据");
				handleDanmakuInfo(currentVid, ms / 60000, 1);
				new seekDanmakuRunnable(ms).run();
			}
		}
	}

	class seekDanmakuRunnable implements Runnable {
		int ms;

		public seekDanmakuRunnable(int ms) {
			this.ms = ms;
		}

		@Override
		public void run() {
			if (danmakuProcessStatus > DANMAKUOPEN) {
				if(ms == lastSeekTime) {
					Logger.d(LogTag.TAG_DANMAKU, "不能连续seek相同时间点");
					return;
				}
				if (beginTime > 0 && (ms <= beginTime + 3000 && ms >= beginTime - 3000)) {
					Logger.d(LogTag.TAG_DANMAKU, "此时无需seek");
					beginTime = -1;
					return;
				}
				lastSeekTime = ms;
				mYoukuPlayerView.seekToDanmaku((long) ms);
				danmakuProcessStatus |= DANMAKUPLAY;
				Logger.d(LogTag.TAG_DANMAKU, "弹幕基准时间点已变,ms=" + ms);
			} else {
				Logger.d(LogTag.TAG_DANMAKU, "弹幕没有开始，隔300毫秒再试");
				if (danmakuHandler != null) {
					danmakuHandler
							.postDelayed(new seekDanmakuRunnable(ms), 300);
				}
			}
		}
	}

	@Override
	public void openDanmaku() {
		if (danmakuUtils != null) {
            Logger.d(LogTag.TAG_DANMAKU, "打开弹幕"  + Log.getStackTraceString(new Throwable()));
			danmakuUtils.openDanmaku(context, this, mMediaPlayerDelegate, currentVid,
					currentGolbalPosition);
		}
	}

	@Override
	public void closeDanmaku() {
		if (danmakuUtils != null) {
            Logger.d(LogTag.TAG_DANMAKU, "关闭弹幕"  + Log.getStackTraceString(new Throwable()));
			danmakuUtils.closeDanmaku(context, this);
		}
	}

	@Override
	public void closeCMSDanmaku() {
		if (!Profile.getDanmakuSwith(context)) {
			Logger.d(LogTag.TAG_DANMAKU, "CMS关闭弹幕");
			setDanmakuPreferences(true, "danmakuSwith");
		}
	}

	@Override
	public boolean isDanmakuClosed() {
		return Profile.getDanmakuSwith(context);
	}

	@Override
	public void showDanmaku() {
		Logger.d(LogTag.TAG_DANMAKU, "show:" + Log.getStackTraceString(new Throwable()));
		if (danmakuUtils != null && !isDanmakuClosed()) {
			danmakuUtils.showDanmaku(mYoukuPlayerView, this);
		}
	}

	@Override
	public void hideDanmaku() {
		Logger.d(LogTag.TAG_DANMAKU, "hide:" + Log.getStackTraceString(new Throwable()));
		if (danmakuUtils != null) {
			danmakuUtils.hideDanmaku(mYoukuPlayerView, this);
		}
	}

	@Override
	public void releaseDanmaku() {
		if (danmakuUtils != null) {
			danmakuUtils.releaseDanmaku(mYoukuPlayerView, mMediaPlayerDelegate);
		}
	}

	@Override
	public void setDanmakuPreferences(boolean danmakuSwith, String key) {
		Editor editor = PreferenceManager.getDefaultSharedPreferences(context)
				.edit();
		editor.putBoolean(key, danmakuSwith);
		editor.commit();
	}

	@Override
	public void startLiveDanmaku() {
		if (mMediaPlayerDelegate != null
				&& mMediaPlayerDelegate.videoInfo != null
				&& mMediaPlayerDelegate.videoInfo.isHLS) {
			if (mYoukuPlayerView != null) {
				mYoukuPlayerView.setDanmakuPosition(Profile
						.getDanmakuPosition(context));
			}
			beginDanmaku(null, 0);
			isHLS = true;
			if (mMediaPlayerDelegate.isFullScreen && !Profile.getLiveDanmakuSwith(context)) {
				if (danmakuHandler != null) {
					danmakuHandler.postDelayed(new Runnable() {

						@Override
						public void run() {
							showDanmaku();
						}
					}, 100);
				}
			} else {
				hideDanmaku();
			}
		}
	}

	@Override
	public void setDanmakuVisibleWhenLive() {
		if (mYoukuPlayerView != null) {
			Logger.d(LogTag.TAG_DANMAKU, "设置弹幕可见");
			mYoukuPlayerView.setDanmakuVisibleWhenLive();
		}
	}

	@Override
	public void showDanmakuWhenRotate() {
		if (danmakuUtils != null) {
			danmakuUtils.showDanmakuWhenRotate(context, this);
		}
	}

	@Override
	public void hideDanmakuWhenRotate() {
		if (danmakuUtils != null) {
			danmakuUtils.hideDanmakuWhenRotate(context, this);
		}
	}

	@Override
	public int getDanmakuCount(String danmakuInfo) {
		int danmakuCount = 0;
		try {
			int beginIndex = danmakuInfo.indexOf("count");
			int middleIndex = danmakuInfo.indexOf(":", beginIndex);
			int endIndex = danmakuInfo.indexOf(",", middleIndex);
			danmakuCount = Integer.parseInt(danmakuInfo.substring(
					middleIndex + 1, endIndex).trim());
		} catch (Exception e) {

		}
		return danmakuCount;
	}

	@Override
	public void hideDanmakuWhenOpen() {
		if (!isDanmakuClosed()) {
			isPaused = true;
			hideDanmaku();
            pauseDanmaku();
		}
	}

	@Override
	public void continueDanmaku() {
		if (isPaused) {
			isPaused = false;
			if (!Profile.getDanmakuSwith(context)) {
				showDanmaku();
			}
			resumeDanmaku();
		}
	}

	@Override
	public void hideDanmakuAgain() {
		if (isPaused) {
			if (mYoukuPlayerView != null) {
				Logger.d(LogTag.TAG_DANMAKU, "切出去，再回来时，多设置一次隐藏弹幕，防止意外发生");
				mYoukuPlayerView.hideDanmaku();
			}
		}
	}

	@Override
	public void resetDanmakuInfo() {
		isGetDanmakuStatus = false;
		danmakuStatus = null;
		danmakuRequest = new ArrayList<Integer>();
		danmakuFailedCount = 0;
		danmakuSwith = false;
		prePosition = -1;
		beginTime = -1;
		currentGolbalPosition = 0;
		currentPosition = 0;
		danmakuRequestTimes = 0;
		danmakuJsonArray = null;
		danmakuProcessStatus = 0;
		isDanmakuNoData = false;
		isFirstOpen = false;
		isPaused = false;
		isDanmakuHide = false;
		isDanmakuShow = false;
		isUserShutUp = false;
		isHLS = false;
		danmakuNoDataStatus = 0;
        isFullScreenDanmaku = false;
        isSmallScreenDanmaku = false;
		lastSeekTime = -1;
	}
	
	@Override
	public void resetAndReleaseDanmakuInfo() {
		if (danmakuUtils != null) {
			danmakuUtils.resetAndReleaseDanmakuInfo(this, isHLS);
		}
	}

	@Override
	public void onPositionChanged(final int currentPosition) {
		currentGolbalPosition = currentPosition / 60000;
		this.currentPosition = currentPosition;
		if ((danmakuProcessStatus & DANMAKUOPEN) == 0) {
			return;
		}
		if (prePosition != currentGolbalPosition) {
			Logger.d(LogTag.TAG_DANMAKU, "onCurrentPositionUpdate="
					+ currentGolbalPosition);
			danmakuRequestTimes += 1;
			prePosition = currentGolbalPosition;
			if (danmakuRequestTimes == 1) {
				Logger.d(LogTag.TAG_DANMAKU, "开始弹幕,currentPosition=" + currentPosition);
				beginDanmaku(danmakuJsonArray, currentPosition);
				if (mMediaPlayerDelegate != null
						/*&& mMediaPlayerDelegate.isFullScreen*/
                        || MediaPlayerConfiguration
                        .getInstance()
                        .showTudouPadDanmaku()) {
					if (danmakuHandler != null) {
						danmakuHandler.postDelayed(new Runnable() {

							@Override
							public void run() {
								danmakuProcessStatus |= DANMAKUPLAY;
                                if (Profile.getDanmakuSwith(context)) {
                                    hideDanmaku();
                                } else {
                                    showDanmaku();
                                }
							}
						}, 100);
					}
				} else if (mMediaPlayerDelegate != null
						&& !mMediaPlayerDelegate.isFullScreen) {
					danmakuProcessStatus |= DANMAKUPLAY;
//					hideDanmaku();
				}
			}
			Logger.d(LogTag.TAG_DANMAKU, "正片播放中，正在请求第" + (currentGolbalPosition + 1)
					+ "分钟数据");
			handleDanmakuInfo(currentVid, currentGolbalPosition + 1, 1);
		}
	}
	
	@Override
	public void releaseDanmakuWhenDestroy() {
		if (!MediaPlayerConfiguration.getInstance().hideDanmaku()) {
			releaseDanmaku();
			if (danmakuUtils != null) {
				if (danmakuUtils instanceof TudouDanmakuUtils) {
					((TudouDanmakuUtils) danmakuUtils).starHandler.removeCallbacksAndMessages(null);
					((TudouDanmakuUtils) danmakuUtils).imgUrlHashMap.clear();
				}
			}
		}
	}

    @Override
	public void setDanmakuTextScale(boolean isFullScreenPlay) {
        if (danmakuUtils != null) {
            if (danmakuUtils instanceof TudouDanmakuUtils) {
                ((TudouDanmakuUtils) danmakuUtils).setDanmakuTextScale(isFullScreenPlay, this);
            }
        }
    }

    @Override
	public void handleDanmakuEnable(boolean danmakuEnable) {
        if (mMediaPlayerDelegate != null && mMediaPlayerDelegate.mIDanmakuEnable != null) {
            mMediaPlayerDelegate.mIDanmakuEnable.handleDanmakuEnable(danmakuEnable);
        }
    }

	@Override
	public boolean isPaused() {
		return isPaused;
	}

	@Override
	public boolean isHls() {
		return isHLS;
	}
}

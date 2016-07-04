package com.youku.player.ad;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.baseproject.utils.Logger;
import com.baseproject.utils.Util;
import com.unicom.iap.sdk.IWoVideoSDKCallBack;
import com.unicom.iap.sdk.WoVideoSDK;
import com.youku.player.LogTag;
import com.youku.player.apiservice.IPlayerAdControl;
import com.youku.player.goplay.AdvInfo;
import com.youku.player.goplay.GetVideoAdvService;
import com.youku.player.goplay.GoplayException;
import com.youku.player.goplay.IGetAdvCallBack;
import com.youku.player.goplay.Profile;
import com.youku.player.goplay.StaticsUtil;
import com.youku.player.goplay.VideoAdvInfo;
import com.youku.player.plugin.MediaPlayerDelegate;
import com.youku.player.unicom.ChinaUnicomConstant;
import com.youku.player.unicom.ChinaUnicomFreeFlowUtil;
import com.youku.player.unicom.ChinaUnicomManager;
import com.youku.player.util.AdUtil;
import com.youku.player.util.DisposableStatsUtils;
import com.youku.player.util.SessionUnitil;
import com.youku.player.util.URLContainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressLint("UseSparseArrays")
public class MidAdModel {
	private static String TAG = "MidAd";
	private Activity mActivity;
	private MediaPlayerDelegate mMediaPlayerDelegate;
    private IPlayerAdControl mPlayerAdControl;

	public static int COUNTDOWN_TIME = 3 * 60 * 1000; // ms
	private static final int TIME_BEFORE_GET_URL = 3 * 60 * 1000; // ms
	private static final int TIME_BEFORE_SHOW_TIPS = 10 * 1000; // ms
	private static final int TIME_AFTER_PLAY_POINT = 30 * 1000; // ms
	private static final int APPLY_MAX_FAILED_TIMES = 3;// 中插广告重新请求次数
	private static final int APPLY_CONTENT_AD_MAX_FATLED_TIMES = 0; // 标版广告重新请求次数

	private boolean mIsGettingAdv;
	public boolean isAfterEndNoSeek;
	private ArrayList<Integer> mMidList;// 用于记录总的中插点
	private ArrayList<Integer> mMidAdList; // 用于记录中插广告的ps
	private ArrayList<Integer> mMidSdList; // 用于记录标版广告的ps;
	private Map<Integer, VideoAdvInfo> mMidAdvInfoMap;
	private Map<Integer, Boolean> mMidAdApplyMap;
	private Map<Integer, Integer> mApplyFailTimesMap;
	private Map<Integer, String> mMidAdTypeMap;

	private int mCurrentMidAd;
	private boolean mIsPlaying;
	private int mLoadingAd;

	private Toast mTips;
	// timer related
	private MidAdCountDownTimer mTimer;
	private int mSavedCount = 0;
	private boolean mIsAfterShowed;

	//测试指定广告使用
	private String mTestAd = null;

    /**
     * 记录广告数量标志
     */
    private int mWoVideoFlag = 0;

	public MidAdModel(Activity context, MediaPlayerDelegate mediaPlayerDelegate,
                        IPlayerAdControl playerAdControl) {
		mActivity = context;
		mMediaPlayerDelegate = mediaPlayerDelegate;
        mPlayerAdControl = playerAdControl;
		mMidList = new ArrayList<Integer>();
		mMidAdList = new ArrayList<Integer>();
		mMidSdList = new ArrayList<Integer>();
		mMidAdvInfoMap = Collections
				.synchronizedMap(new HashMap<Integer, VideoAdvInfo>());
		mMidAdApplyMap = Collections
				.synchronizedMap(new HashMap<Integer, Boolean>());
		mApplyFailTimesMap = Collections
				.synchronizedMap(new HashMap<Integer, Integer>());
		mMidAdTypeMap = Collections
				.synchronizedMap(new HashMap<Integer, String>());
		mIsAfterShowed = false;
		mIsGettingAdv = false;
		mCurrentMidAd = 0;
		mLoadingAd = -1;
		mIsPlaying = false;
		mTips = null;
		isAfterEndNoSeek = false;
		mSavedCount = 0;
		mTestAd = null;
		mNeedBufferStartContentAd = false;
		hasStartTimeStamp = false;
		mIsGotStartContentAdUrl = false;
	}

	public MidAdModel(Activity context, MediaPlayerDelegate mediaPlayerDelegate,
                        IPlayerAdControl playerAdControl, String testAd) {
		mActivity = context;
		mMediaPlayerDelegate = mediaPlayerDelegate;
        mPlayerAdControl = playerAdControl;
		mMidList = new ArrayList<Integer>();
		mMidAdList = new ArrayList<Integer>();
		mMidSdList = new ArrayList<Integer>();
		mMidAdvInfoMap = Collections
				.synchronizedMap(new HashMap<Integer, VideoAdvInfo>());
		mMidAdApplyMap = Collections
				.synchronizedMap(new HashMap<Integer, Boolean>());
		mApplyFailTimesMap = Collections
				.synchronizedMap(new HashMap<Integer, Integer>());
		mMidAdTypeMap = Collections
				.synchronizedMap(new HashMap<Integer, String>());
		mIsAfterShowed = false;
		mIsGettingAdv = false;
		mCurrentMidAd = 0;
		mLoadingAd = -1;
		mIsPlaying = false;
		mTips = null;
		isAfterEndNoSeek = false;
		mSavedCount = 0;
		mTestAd = testAd;
		mNeedBufferStartContentAd = false;
		hasStartTimeStamp = false;
		mIsGotStartContentAdUrl = false;
	}

	public void clear() {
		if (hasMidPointAdNotShow()) {
			DisposableStatsUtils.disposeAdLoss(mActivity,
					URLContainer.AD_LOSS_STEP3,
					SessionUnitil.playEvent_session, URLContainer.AD_LOSS_MO);
		}
		if (mMidList != null) {
			mMidList.clear();
			mMidList = null;
		}
		if (mMidAdList != null) {
			mMidAdList.clear();
			mMidAdList = null;
		}
		if (mMidSdList != null) {
			mMidSdList.clear();
			mMidSdList = null;
		}
		if (mMidAdvInfoMap != null) {
			mMidAdvInfoMap.clear();
			mMidAdvInfoMap = null;
		}
		if (mMidAdApplyMap != null) {
			mMidAdApplyMap.clear();
			mMidAdApplyMap = null;
		}
		if (mApplyFailTimesMap != null) {
			mApplyFailTimesMap.clear();
			mApplyFailTimesMap = null;
		}
		if (mMidAdTypeMap != null) {
			mMidAdTypeMap.clear();
			mMidAdTypeMap = null;
		}
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
		mIsAfterShowed = false;
		mIsGettingAdv = false;
		mActivity = null;
		mMediaPlayerDelegate = null;
		mCurrentMidAd = 0;
		mLoadingAd = -1;
		mIsPlaying = false;
		mTips = null;
		isAfterEndNoSeek = false;
		mSavedCount = 0;
		mTestAd = null;
		mNeedBufferStartContentAd = false;
		hasStartTimeStamp = false;
		mIsGotStartContentAdUrl = false;
	}

    private boolean hasMidPointAdNotShow() {
        return !isAdvInfoEmpty() && !(mPlayerAdControl != null && mPlayerAdControl.isMidAdShowing()
                && mMidAdvInfoMap != null && (mMidAdvInfoMap.size() == 1));
    }

	public void addMidAdTimestamp(int timestamp) {
		Logger.d(TAG, "addMidAdTimestamp");
		mMidList.add(timestamp);
		mMidAdApplyMap.put(timestamp, false);
	}

	public void addMidAdTypes(int timestamp, String type){
		Logger.d(TAG, "addMidAdTypes ---> timestamp : " + timestamp + " / type : " + type);
		if(type.equals(Profile.CONTENTAD_POINT)){
			mMidSdList.add(timestamp);
			if(isStartTimeStamp(timestamp)){
				hasStartTimeStamp = true;
			}
		} else {
			mMidAdList.add(timestamp);
		}
		mMidAdTypeMap.put(timestamp, type);
	}

	public void addMidAdAdvInfo(int timestamp, VideoAdvInfo videoAdvInfo) {
		Logger.d(TAG, "addMidAdAdvInfo");
		/*
		 * VideoAdvInfo info = new VideoAdvInfo(); info.P = videoAdvInfo.P;
		 * info.JS = videoAdvInfo.JS; info.SKIP = videoAdvInfo.SKIP; info.VAL =
		 * videoAdvInfo.VAL; info.CREATOR.createFromParcel(videoAdvInfo);
		 */
		if (videoAdvInfo != null && mMidList.contains(timestamp)) {
			mMidAdvInfoMap.put(timestamp, videoAdvInfo);
		}
	}

	/**
	 * judge current timestamp is in mid ad interval or not
	 * 
	 * @param currentPosition
	 * @return mid ad point if true or -1 if not 
	 */
	public int isInMidAdInterval(int currentPosition) {
		if (mMidList != null && !mMidList.isEmpty()) {
			for (int i = 0; i < mMidList.size(); i++) {
				if (currentPosition >= (mMidList.get(i) - TIME_BEFORE_GET_URL)
						&& currentPosition < (mMidList.get(i) + TIME_AFTER_PLAY_POINT)) {
					return mMidList.get(i);
				}
			}
		}
		return -1;
	}
	/**
	 * get current timestamp need to get adv or not
	 * 
	 * @param currentPosition ��ǰʱ���
	 * @param midAdTime �в��
	 * @return  mid ad point to show ad
	 */
	public int needToGetMidAdvUrl(int currentPosition, int midAdTime) {
		if (!Util.hasInternet()) {
			return -1;
		}
		if (mMediaPlayerDelegate != null
				&& mMediaPlayerDelegate.videoInfo != null
				&& StaticsUtil.PLAY_TYPE_LOCAL
						.equals(mMediaPlayerDelegate.videoInfo.getPlayType())
				&& (!Util.isWifi())) {
			return -1;
		}
		if (midAdTime > 0 && mMidList != null && mMidList.contains(midAdTime)) {
			int dur = midAdTime - currentPosition;
			if (dur > 0 && dur <= TIME_BEFORE_GET_URL && !isAfterShowed(midAdTime)
					&& !mMidAdvInfoMap.containsKey(midAdTime)
					&& !mMidAdApplyMap.get(midAdTime)) {
				Logger.d(TAG, "needToGetMidAdvUrl time is " + midAdTime);
				return midAdTime;
			}
		}
		if (mMidList != null && !mMidList.isEmpty()) {
			for (int i = 0; i < mMidList.size(); i++) {
				int dur = mMidList.get(i) - currentPosition;
				if (dur > 0 && dur <= TIME_BEFORE_GET_URL && !isAfterShowed(midAdTime)
						&& !mMidAdvInfoMap.containsKey(mMidList.get(i))
						&& !mMidAdApplyMap.get(mMidList.get(i))) {
					Logger.d(TAG,
							"needToGetMidAdvUrl time is " + mMidList.get(i));
					return mMidList.get(i);
				}
			}
		}
		return -1;
	}

	public boolean needToBuffering(int currentPosition) {
		if (!Util.hasInternet()) {
			return false;
		}
		if (mMediaPlayerDelegate != null
				&& mMediaPlayerDelegate.videoInfo != null
				&& StaticsUtil.PLAY_TYPE_LOCAL
						.equals(mMediaPlayerDelegate.videoInfo.getPlayType())
				&& (!Util.isWifi())) {
			return false;
		}
		if (mMidList != null && !mMidList.isEmpty()) {
			for (int i = 0; i < mMidList.size(); i++) {
				int dur = mMidList.get(i) - currentPosition;
				if (dur > 0 && dur <= TIME_BEFORE_SHOW_TIPS && !isAfterShowed(mMidList.get(i))
						&& mMidAdvInfoMap.containsKey(mMidList.get(i))) {
					mCurrentMidAd = mMidList.get(i);
					Logger.d(TAG, "needToBuffering  true");
					return true;
				}
			}
		}
		return false;
	}

	public boolean needToPlayMidAD(int currentPosition) {
		if (!Util.hasInternet()) {
			return false;
		}
		if (mMediaPlayerDelegate != null
				&& mMediaPlayerDelegate.videoInfo != null
				&& StaticsUtil.PLAY_TYPE_LOCAL
						.equals(mMediaPlayerDelegate.videoInfo.getPlayType())
				&& (!Util.isWifi())) {
			return false;
		}
		if (mMidList != null && !mMidList.isEmpty()) {
			for (int i = 0; i < mMidList.size(); i++) {
				int dur = currentPosition - mMidList.get(i);
				if (dur > -1 && dur < TIME_AFTER_PLAY_POINT && !isAfterShowed(mMidList.get(i))
						&& mMidAdvInfoMap.containsKey(mMidList.get(i))
						&& !mIsPlaying) {
					if (mLoadingAd != mMidList.get(i)) {
						mCurrentMidAd = mMidList.get(i);
						setMidADUrl(mMidList.get(i));
					}
					Logger.d(TAG, "needToPlayMidAD  true");
					return true;
				}
			}
		}
		return false;
	}

	public boolean needToShowTips(int currentPosition) {
		if (mMidList != null && !mMidList.isEmpty()) {
			for (int i = 0; i < mMidList.size(); i++) {
				int dur = mMidList.get(i) - currentPosition;
				if (dur > TIME_BEFORE_SHOW_TIPS - 1000
						&& dur <= TIME_BEFORE_SHOW_TIPS && !isAfterShowed(mMidList.get(i))
						&& mMidAdvInfoMap.containsKey(mMidList.get(i))) {
					Logger.d(TAG, "needToShowTips  true");
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * setDataSource when player is on playing
	 * 
	 * @param position
	 *            return
	 */
	public void setMidADUrl(int position) {

		if (mLoadingAd == mCurrentMidAd) {
			return;
		}
		VideoAdvInfo  videoAdvInfo = null;
		videoAdvInfo = mMidAdvInfoMap.get(mCurrentMidAd);
		if (videoAdvInfo == null) {
			if (mMidList != null && !mMidList.isEmpty()) {
				for (int i = 0; i < mMidList.size(); i++) {
					int dur = position - mMidList.get(i);
					if (dur >= -TIME_BEFORE_SHOW_TIPS
							&& dur < TIME_AFTER_PLAY_POINT
							&& !isAfterShowed(mMidList.get(i))
							&& mMidAdvInfoMap.get(mMidList.get(i)) != null
							&& !mMidAdvInfoMap.get(mMidList.get(i)).VAL
									.isEmpty() && !mIsPlaying) {
						videoAdvInfo = mMidAdvInfoMap.get(mMidList.get(i));
					}
				}
			}
		}
        final Handler woVideoHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch(msg.what){
                    case 0:
                        break;
                    case ChinaUnicomConstant.HANDLE_FREEFLOW_MESSAGE_SUCCESS:
                        String url = (String) msg.obj;
                        if (url != null) {
                            mWoVideoFlag = 0;
                            mLoadingAd = mCurrentMidAd;
                            Logger.d(LogTag.TAG_WO_VIDEO, "MidADFinalURL=" + url);
                            mMediaPlayerDelegate.mediaPlayer.setMidADDataSource(url);
                            mMediaPlayerDelegate.mediaPlayer.prepareMidAD();
                        }
                        break;
                }
            }
        };

        if(ChinaUnicomFreeFlowUtil.isSatisfyChinaUnicomFreeFlow() && videoAdvInfo.VAL.size() != 0){
            for(int i=0;i<videoAdvInfo.VAL.size();i++){
                final int index = i;
                final VideoAdvInfo  woVideoAdvInfo = videoAdvInfo;
                Map<String, String> woVideoMidAdMap = ChinaUnicomManager.getChinaUnicomVideoInfo(woVideoAdvInfo.VAL.get(i).RS, "0");
                WoVideoSDK.identifyWoVideoSDK(com.baseproject.utils.Profile.mContext, woVideoMidAdMap,
                        new IWoVideoSDKCallBack() {

                            @Override
                            public void sdkCallback(boolean success, String resultCode,
                                                    String resultMessage, Object object, int intefaceType) {
                                String adurl = (String) object;
                                if(!TextUtils.isEmpty(adurl)){
                                    woVideoAdvInfo.VAL.get(index).RS = adurl;
                                    if(mWoVideoFlag == woVideoAdvInfo.VAL.size()-1){
                                        if (woVideoAdvInfo != null && mMediaPlayerDelegate != null
                                                && mMediaPlayerDelegate.mediaPlayer != null) {
                                            String url = makeMidADM3u8(woVideoAdvInfo);
                                            Message msg = new Message();
                                            msg.obj = url;
                                            msg.what = ChinaUnicomConstant.HANDLE_FREEFLOW_MESSAGE_SUCCESS;
                                            woVideoHandler.sendMessage(msg);
                                        }
                                    }
                                    mWoVideoFlag ++;
                                }
                            }
                        });
                }

            }
            else{
                if (videoAdvInfo != null && mMediaPlayerDelegate != null
                        && mMediaPlayerDelegate.mediaPlayer != null) {
                    String url = makeMidADM3u8(videoAdvInfo);
                    Logger.d(TAG, "midad url is :" + url);
                    if (url != null) {
                        mLoadingAd = mCurrentMidAd;
                        mMediaPlayerDelegate.mediaPlayer.setMidADDataSource(url);
                        mMediaPlayerDelegate.mediaPlayer.prepareMidAD();
                    }
                }
            }

        mWoVideoFlag = 0;
	}

	public String getCurrentMidAdUrl() {
		VideoAdvInfo videoAdvInfo = mMidAdvInfoMap.get(mCurrentMidAd);
		if (videoAdvInfo != null) {
			return makeMidADM3u8(videoAdvInfo);
		}
		return null;
	}

	public String getCurrentAdType(){
		return getMidAdType(mCurrentMidAd);
	}

	/**
	 * 获取中插点的type
	 */
	public String getMidAdType(int timestamp){
		if (mMidAdTypeMap != null && !mMidAdTypeMap.isEmpty()){
			return mMidAdTypeMap.get(timestamp);
		}
		return "";
	}


	/**
	 * setDataSource when mid ad on pause
	 * 
	 * return
	 */
	/*
	 * public void setMidADUrl() { VideoAdvInfo videoAdvInfo =
	 * mMidAdvInfoMap.get(mCurrentMidAd); if (videoAdvInfo != null &&
	 * mMediaPlayerDelegate != null && mMediaPlayerDelegate.mediaPlayer != null)
	 * { String url = makeMidADM3u8(videoAdvInfo); Logger.d(TAG,
	 * "midad url is :" + url); if (url != null) { mLoadingAd = mCurrentMidAd;
	 * mMediaPlayerDelegate.mediaPlayer.setMidADDataSource(url);
	 * mMediaPlayerDelegate.mediaPlayer.prepareMidAD(); } } }
	 */
	public void playMidAD(int adTime) {
		if (mIsPlaying) {
			return;
		}
		if (mMediaPlayerDelegate != null
				&& mMediaPlayerDelegate.mediaPlayer != null) {
			int videoTime;
			if (mMediaPlayerDelegate.videoInfo != null
					&& mMediaPlayerDelegate.videoInfo.getProgress() > 1000) {
				videoTime = mMediaPlayerDelegate.videoInfo.getProgress();
			} else {
				videoTime = mCurrentMidAd;
			}
			mMediaPlayerDelegate.mediaPlayer
					.playMidADConfirm(videoTime, adTime);
			mIsPlaying = true;
		}
	}

	public void showTips() {
		if(getCurrentAdType().equals(Profile.STANDARD_POINT)){ // 只有中插广告才显示toast
			try {
				if (mTips == null) {
					mTips = Toast
							.makeText(
									mActivity,
									mActivity
											.getString(com.youku.android.player.R.string.playersdk_mid_ad_tips),
									Toast.LENGTH_SHORT);
				} else {
					mTips.setText(com.youku.android.player.R.string.playersdk_mid_ad_tips);
				}
				mTips.show();
			} catch (Exception e) {

			}
		}
	}


	public void startMidAD() {
		mIsPlaying = true;
	}

	public void endMidAD() {
		mIsPlaying = false;
		isAfterEndNoSeek = true;
	}

	public void resetAfterRelease() {
		mIsPlaying = false;
		mLoadingAd = -1;
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
		// isAfterEndNoSeek = false;
	}

	public void getMidAdvUrl(final int timeStamp, boolean isOfflineAd) {
		if (mMediaPlayerDelegate.isADShowing) {
			return;
		}
		if (mIsGettingAdv) {
			return;
		}
		if (mApplyFailTimesMap != null
				&& mApplyFailTimesMap.get(timeStamp) != null
				&& mApplyFailTimesMap.get(timeStamp) >= getApplyMaxFailedTimes(timeStamp)) {
			return;
		}
		mIsGettingAdv = true;
		String type = getMidAdType(timeStamp);
		int position = getMidPosition(type);
		int ps = 0;
		ArrayList<Integer> list;
		if (position == AdPosition.MID){ // 中插广告获取ps
			list = mMidAdList;
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i) == timeStamp) {
					ps = i;
				}
			}
			Logger.d(TAG, "midad position is  :" + ps);
		} else { // 标版广告获取ps
			list = mMidSdList;
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i) == timeStamp) {
					if (isStartTimeStamp(timeStamp)) { // 片头
						ps = 0;
						mIsGotStartContentAdUrl = true;
					} else if (isEndTimeStamp(timeStamp)){ // 片尾
						ps = 10;
					} else { // 片中
						ps = i;
						if(!hasStartTimeStamp){
							ps ++;
						}
					}
				}
			}
			Logger.d(TAG, "contentad position is  :" + ps);
		}
		double pt = ((double) timeStamp) / 1000;
		Logger.d(TAG, "midad timeStamp is  :" + pt);

		GetVideoAdvService getVideoAdvService = new GetVideoAdvService(null);
		if (!TextUtils.isEmpty(mMediaPlayerDelegate.videoInfo.getVid())) {
			AdGetInfo adGetInfo = new AdGetInfo(
					mMediaPlayerDelegate.videoInfo.getVid(), position,
					mMediaPlayerDelegate.isFullScreen, isOfflineAd, mMediaPlayerDelegate.videoInfo.playlistId,
					mMediaPlayerDelegate.videoInfo, ps, pt, mMediaPlayerDelegate.getPlayerUiControl().isVerticalFullScreen());
			if (mTestAd == null || mTestAd.length() == 0) {
				getVideoAdvService.getVideoAdv(adGetInfo, mActivity,
						new IGetAdvCallBack() {

							@Override
							public void onSuccess(VideoAdvInfo videoAdvInfo) {
								if (mActivity.isFinishing()) {
									DisposableStatsUtils.disposeAdLoss(
											mActivity,
											URLContainer.AD_LOSS_STEP1,
											SessionUnitil.playEvent_session,
											URLContainer.AD_LOSS_MO);
									return;
								}
								if (videoAdvInfo != null
										&& mMidAdvInfoMap != null) {
									int size = videoAdvInfo.VAL.size();
									if (size == 0 || (!AdUtil.isAdvVideoType(videoAdvInfo))) {
										Logger.d(TAG, "mid ad is empty");
									} else {
										mMidAdvInfoMap.put(timeStamp,
												videoAdvInfo);
									}
								}
								if (mMidAdApplyMap != null) {
									mMidAdApplyMap.put(timeStamp, true);
								}
								if (mApplyFailTimesMap != null
										&& mApplyFailTimesMap
												.containsKey(timeStamp)) {
									mApplyFailTimesMap.remove(timeStamp);
								}
								mIsGettingAdv = false;
							}

							@Override
							public void onFailed(GoplayException e) {
								Logger.d(TAG, "mid ad onFailed");
								if (mApplyFailTimesMap != null) {
									int times = 1;
									if (mApplyFailTimesMap.get(timeStamp) != null
											&& mApplyFailTimesMap
													.get(timeStamp) >= 1) {
										times = mApplyFailTimesMap
												.get(timeStamp) + 1;
									}
									mApplyFailTimesMap.put(timeStamp, times);
									if (mMidAdApplyMap != null
											&& times >= getApplyMaxFailedTimes(timeStamp)) {
										mMidAdApplyMap.put(timeStamp, true);
									}
								}
								mIsGettingAdv = false;
								DisposableStatsUtils.disposeAdLoss(mActivity,
										URLContainer.AD_LOSS_STEP2,
										SessionUnitil.playEvent_session,
										URLContainer.AD_LOSS_MO);
							}
						});
			} else {
				getVideoAdvService.getVideoAdv(adGetInfo, mActivity,
						mTestAd, new IGetAdvCallBack() {

							@Override
							public void onSuccess(VideoAdvInfo videoAdvInfo) {
								if (videoAdvInfo != null
										&& mMidAdvInfoMap != null) {
									int size = videoAdvInfo.VAL.size();
									if (size == 0 || (!AdUtil.isAdvVideoType(videoAdvInfo))) {
										Logger.d(TAG, "mid ad is empty");
									} else {
										mMidAdvInfoMap.put(timeStamp,
												videoAdvInfo);
									}
								}
								if (mMidAdApplyMap != null) {
									mMidAdApplyMap.put(timeStamp, true);
								}
								if (mApplyFailTimesMap != null
										&& mApplyFailTimesMap
												.containsKey(timeStamp)) {
									mApplyFailTimesMap.remove(timeStamp);
								}
								mIsGettingAdv = false;
							}

							@Override
							public void onFailed(GoplayException e) {
								Logger.d(TAG, "mid ad onFailed");
								if (mApplyFailTimesMap != null) {
									int times = 1;
									if (mApplyFailTimesMap.get(timeStamp) != null
											&& mApplyFailTimesMap
													.get(timeStamp) >= 1) {
										times = mApplyFailTimesMap
												.get(timeStamp) + 1;
									}
									mApplyFailTimesMap.put(timeStamp, times);
									if (mMidAdApplyMap != null
											&& times >= getApplyMaxFailedTimes(timeStamp)) {
										mMidAdApplyMap.put(timeStamp, true);
									}
								}
								mIsGettingAdv = false;
							}
						});
			}
		}

	}

	private String makeMidADM3u8(VideoAdvInfo videoAdvInfo) {
		StringBuffer str = new StringBuffer();
		str.append("#PLSEXTM3U\n").append("#EXT-X-TARGETDURATION:").append(100)
				.append("\n#EXT-X-VERSION:2\n#EXT-X-DISCONTINUITY\n");

		if (videoAdvInfo != null && videoAdvInfo.VAL != null
				&& videoAdvInfo.VAL.size() > 0) {
			for (int i = 0; i < videoAdvInfo.VAL.size(); i++) {
				AdvInfo advInfo = videoAdvInfo.VAL.get(i);

				// VT��2��ʱ��չʾ�ز�
				if (TextUtils.isEmpty(advInfo.RS) || "2".equals(advInfo.VT))
					continue;
				str.append("#EXTINF:").append(advInfo.AL).append(" MID_AD\n");
				str.append(advInfo.RS.trim()).append("\n");
			}
		} else {
			return null;
		}
		str.append("#EXT-X-ENDLIST\n");
		return str.toString();
	}

	public VideoAdvInfo getCurrentAdvInfo() {
		if (mMidAdvInfoMap != null) {
			return mMidAdvInfoMap.get(mCurrentMidAd);
		}
		return null;
	}

	public AdvInfo getCurrentAdv() {
		if (mMidAdvInfoMap != null && mMidAdvInfoMap.get(mCurrentMidAd) != null) {
			return mMidAdvInfoMap.get(mCurrentMidAd).VAL.get(0);
		}
		return null;
	}

	public void removeCurrentAdv() {
		if (mMidAdvInfoMap == null || mMidAdvInfoMap.get(mCurrentMidAd) == null) {
			return;
		}

		if (mMidAdvInfoMap.get(mCurrentMidAd).VAL == null
				|| mMidAdvInfoMap.get(mCurrentMidAd).VAL.size() == 0) {
			return;
		}
		mMidAdvInfoMap.get(mCurrentMidAd).VAL.remove(0);
		if (mMidAdvInfoMap.get(mCurrentMidAd).VAL.size() == 0) {
			mMidAdvInfoMap.remove(mCurrentMidAd);
			mMidAdApplyMap.put(mCurrentMidAd, false);
			mIsPlaying = false;
			mCurrentMidAd = 0;
			mLoadingAd = -1;
		}
	}

	public void removeCurrentAdvInfo() {
		if (mMidAdvInfoMap == null || mMidAdvInfoMap.get(mCurrentMidAd) == null) {
			return;
		}
		mMidAdvInfoMap.remove(mCurrentMidAd);
		mMidAdApplyMap.put(mCurrentMidAd, false);
		mIsPlaying = false;
		mCurrentMidAd = 0;
		mLoadingAd = -1;
	}

	public boolean isCurrentAdvEmpty() {
		if (mMidAdvInfoMap == null || mMidAdvInfoMap.get(mCurrentMidAd) == null
				|| mMidAdvInfoMap.get(mCurrentMidAd).VAL == null
				|| mMidAdvInfoMap.get(mCurrentMidAd).VAL.size() == 0) {
			return true;
		}

		return false;
	}

	public boolean isAdvInfoEmpty() {
		if (mMidList != null && !mMidList.isEmpty() && mMidAdvInfoMap != null
				&& !mMidAdvInfoMap.isEmpty()) {
			for (int i = 0; i < mMidList.size(); i++) {
				if (mMidAdvInfoMap.get(mMidList.get(i)) != null
						&& !mMidAdvInfoMap.get(mMidList.get(i)).VAL.isEmpty()) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * response on time stamp update
	 * 
	 * @param currentPosition
	 */
	public void onPositionUpdate(final int currentPosition) {
		if (mMediaPlayerDelegate == null) {
			return;
		}

		int midAdTime = isInMidAdInterval(currentPosition);
		if (midAdTime > 0) {
			int timeToGetUrl = needToGetMidAdvUrl(currentPosition, midAdTime);
			if (timeToGetUrl > 0) {
				getMidAdvUrl(timeToGetUrl,
						StaticsUtil.PLAY_TYPE_LOCAL
								.equals(mMediaPlayerDelegate.videoInfo
										.getPlayType()));
				return;
			}

			if (needToBuffering(currentPosition)) {
				setMidADUrl(currentPosition);
				if (needToShowTips(currentPosition)) {
					showTips();
				}
				return;
			}

			if (needToPlayMidAD(currentPosition)) {
				playMidAD(0);
			}
		}

	}

	public void startTimer() {
		Logger.d(TAG, " mid ad start timer");
		if (mTimer != null) {
			mTimer.cancel();
		}
		mTimer = new MidAdCountDownTimer(COUNTDOWN_TIME, 1000);
		mTimer.start();
		mIsAfterShowed = true;
	}

	public void timerPause() {
		if (mIsAfterShowed && mTimer != null && mSavedCount >= 1) {
			Logger.d(TAG, "timerPause(): " + mSavedCount);
			mTimer.cancel();
			mTimer.isPause = true;
		}
	}

	public void timerStart() {
		if (mIsAfterShowed && mSavedCount >= 1 && mTimer != null
				&& mTimer.isPause) {
			Logger.d(TAG, "timerStart(): " + mSavedCount);
			if (mActivity != null) {
				mActivity.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						mTimer = new MidAdCountDownTimer(mSavedCount * 1000,
								1000);
						mTimer.start();
					}
				});
			}
		}
	}

	private class MidAdCountDownTimer extends CountDownTimer {
		public boolean isPause = false;

		public MidAdCountDownTimer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
			isPause = false;
		}

		@Override
		public void onFinish() {
			mIsAfterShowed = false;
			mTimer = null;
		}

		@Override
		public void onTick(long millisUntilFinished) {
			if (isPause) {
				return;
			}
			float time = (float) millisUntilFinished / 1000;
			int count = Math.round(time);
			if (mSavedCount != count && count > 0) {
				mSavedCount = count;
				Logger.d(TAG, "中插点倒计时 剩余: " + mSavedCount);
			}

		}
	}

	private int getMidPosition(String type){
		if (type.equals(Profile.CONTENTAD_POINT)){
			return AdPosition.SD; // 标版广告
		} else {
			return AdPosition.MID; // 中插广告
		}
	}

	private static final int TIME_START_TIME_STAMP = 3 * 60 * 1000; //片头
	private static final int TIME_END_TIME_STAMP = 3 * 60 * 1000; //片尾
	private boolean hasStartTimeStamp; // 是否包含有片头点的标版广告


	/**
	 * 判断是否为片头
	 */
	private boolean isStartTimeStamp(int timeStamp){
		if(timeStamp > 0 && timeStamp < TIME_START_TIME_STAMP){
			return true;
		}
		return false;
	}

	/**
	 * 判断是否位片尾
	 */
	private boolean isEndTimeStamp (int timeStamp){
		if (mMediaPlayerDelegate != null && mMediaPlayerDelegate.videoInfo != null){
			int al = mMediaPlayerDelegate.videoInfo.getDurationMills();
			if(timeStamp < al && timeStamp > (al - TIME_END_TIME_STAMP)){
				return true;
			}
		}
		return false;
	}


	private boolean mNeedBufferStartContentAd; // 是否需要加载标版片头广告
	private boolean mIsGotStartContentAdUrl; // 是否已经获取到标版片头广告
	/**
	 * 判断是否需要显示标版片头
	 */
	private boolean isShowStartContentAd(int timeStamp){
		boolean can = (mNeedBufferStartContentAd && isStartTimeStamp(timeStamp));
		return can;
	}

	/**
	 * 判断是否已经显示过中插
	 * @return false :没有显示， true:已经显示
	 */
	private boolean isAfterShowed(int timeStamp){
		// 开始播放视频显示片头广告优先,只要播放过一次片头标版就遵循一般中插规则(3分钟倒计时)
		if (isShowStartContentAd(timeStamp)){
			return false;
		}
		return mIsAfterShowed;
	}

	/**
	 * 判断是否需要加载标版片头广告
	 */
	public void checkBufferStartContentAd(){
		mNeedBufferStartContentAd = false;
		if (hasStartTimeStamp && mMidSdList != null && !mMidSdList.isEmpty() && !mIsGotStartContentAdUrl
				&& mMediaPlayerDelegate != null && mMediaPlayerDelegate.videoInfo != null) {
			int startTimeStamp = mMidSdList.get(0); // 获取片头标版
			int playProgress = mMediaPlayerDelegate.videoInfo.getProgress();
			if (playProgress >= 0 && playProgress < startTimeStamp){
				mNeedBufferStartContentAd = true;
			}
			Logger.d(TAG, "checkBufferStartContentAd ----> canBuffer :" + mNeedBufferStartContentAd + " / startTimeStamp : " + startTimeStamp + " / playProgress : " + playProgress);
		}
	}

	/**
	 * 获取设定的出错重新请求次数
	 */
	private int getApplyMaxFailedTimes(int timestamp){
		if (!TextUtils.isEmpty(getMidAdType(timestamp)) && getMidAdType(timestamp).equals(Profile.CONTENTAD_POINT)){
			return APPLY_CONTENT_AD_MAX_FATLED_TIMES;
		} else {
			return APPLY_MAX_FAILED_TIMES;
		}
	}


}

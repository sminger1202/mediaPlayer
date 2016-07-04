package com.youku.player.config;

import android.text.TextUtils;
import android.util.Xml;

import com.baseproject.utils.Logger;
import com.baseproject.utils.Profile;
import com.youku.android.player.R;
import com.youku.player.LogTag;
import com.youku.player.apiservice.PlantformController;
import com.youku.player.base.Plantform;
import com.youku.player.danmaku.DanmakuUtils;
import com.youku.player.danmaku.TudouDanmakuUtils;
import com.youku.player.danmaku.YoukuDanmakuUtils;
import com.youku.player.grey.GreyConfigParser;
import com.youku.player.grey.GreyParam;
import com.youku.statistics.IRVideoWrapper;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class MediaPlayerConfiguration {
    // 灰度发布控制的功能列表
    public final static int FUN_REQUESTASYNC = 1;
    public final static int FUN_HWDECODE = 2;
	
	private int mAdvPlatForm;
	private boolean mShowPreAd;
	private boolean mShowPauseAd;
	private boolean mShowOfflineAd;
	private boolean mShowSkipAdButton;
	private boolean mTrackAd;
	private boolean mShowChangeQualityTip;
	private boolean mShowLoginDialog;
	private boolean mTrackPlayError;
	private boolean mShowAdWebView;
	private boolean mUseHardwareDecode;
	private boolean mUseP2P;
	private boolean mHideDanmaku;
	private int mP2PRetryTimes;
	private boolean mLivePortrait;
	private boolean mEnableOrientation;
	private boolean mTudouPadDanmaku;
	private boolean mUnicomFree;
    private boolean mH265;
	private boolean mEnhancedMode;
	private int mPlatform;
	private String mDetailActivityName;
	private boolean mDefaultAllow3G;
	private final String DETAILACTIVITY_NAME_YOUKU = "com.youku.ui.activity.DetailActivity";
	private final String DETAILACTIVITY_NAME_TUDOU = "com.tudou.ui.activity.DetailActivity";
    private boolean mRequestAsync;
    private String mVersionCode;

    public boolean isGreyControl(int feature) {
        return mGreyFeatureSet.contains(feature);
    }

    private Set<Integer> mGreyFeatureSet = new HashSet<Integer>(); // 灰度发布功能列表

    public PlantformController mPlantformController;
    public DanmakuUtils mDanmakuUtils;


    // 灰度发布使用
    public int mTestid = 0;
    public int mIstest = 0;

    public static MediaPlayerConfiguration getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        public static final MediaPlayerConfiguration INSTANCE = new MediaPlayerConfiguration();
    }

    private MediaPlayerConfiguration() {
        // setConfigFromXML();
        // 根据用包名判断不同平台的功能
        if ("com.tudou.android".equalsIgnoreCase(Profile.mContext.getPackageName())
                || "com.tudou.xoom.android".equalsIgnoreCase(Profile.mContext.getPackageName())
                || "com.tudoulite.android".equalsIgnoreCase(Profile.mContext .getPackageName())
                || "com.android.playerdemo".equalsIgnoreCase(Profile.mContext.getPackageName())) {
			mAdvPlatForm = 2;
            mShowPreAd = true;
            mShowPauseAd = true;
            mShowOfflineAd = false;
            mShowSkipAdButton = true;
            mTrackAd = false;
            mShowChangeQualityTip = false;
            mShowLoginDialog = false;
            mTrackPlayError = true;
            mShowAdWebView = true;
            mUseHardwareDecode = false;
            mUseP2P = false;
            mEnableOrientation = true;
            mP2PRetryTimes = 1;
            mHideDanmaku = false;
            setTudouPadDanmaku();
            mLivePortrait = false;
            mUnicomFree = false;
            mRequestAsync = false;
            mH265 = false;
			mDefaultAllow3G = false;
            mEnhancedMode = false;
			mPlatform = Plantform.TUDOU;
            com.youku.player.goplay.Profile.ctype = 64;
            mPlantformController = new TudouController();
            mDanmakuUtils = new TudouDanmakuUtils();
            mDetailActivityName = DETAILACTIVITY_NAME_TUDOU;
        } else {
			mAdvPlatForm = 1;
            mShowPreAd = true;
            mShowPauseAd = true;
            mShowOfflineAd = true;
            mShowSkipAdButton = true;
            mTrackAd = true;
            mShowChangeQualityTip = true;
            mShowLoginDialog = true;
            mTrackPlayError = false;
            mShowAdWebView = true;
            mUseHardwareDecode = true;
            mUseP2P = true;
            mP2PRetryTimes = 1;
            mHideDanmaku = true;
            mLivePortrait = true;
            mEnableOrientation = true;
            mUnicomFree = true;
			mDefaultAllow3G = true;
            mRequestAsync = false;
            mH265 = false;
            mEnhancedMode = false;
			mPlatform = Plantform.YOUKU;
            com.youku.player.goplay.Profile.ctype = 20;
            mPlantformController = new YoukuController();
            mDanmakuUtils = new YoukuDanmakuUtils();
            mDetailActivityName = DETAILACTIVITY_NAME_YOUKU;
        }
        mVersionCode = "5.0";
        IRVideoWrapper.clearVideoPlayInfo(Profile.mContext);
    }

    public void setTudouPadDanmaku() {
        if ("com.tudou.android".equalsIgnoreCase(Profile.mContext
                .getPackageName())) {
            mTudouPadDanmaku = false;
        } else if ("com.tudou.xoom.android".equalsIgnoreCase(Profile.mContext
                .getPackageName())) {
            mTudouPadDanmaku = true;
        }
    }

    public boolean showTudouPadDanmaku() {
        return mTudouPadDanmaku;
    }

    public boolean hideDanmaku() {
        return mHideDanmaku;
    }

    public boolean showPreAd() {
        return mShowPreAd;
    }

    public boolean showPauseAd() {
        return mShowPauseAd;
    }

    public boolean showOfflineAd() {
        return mShowOfflineAd;
    }

    public boolean showSkipAdButton() {
        return mShowSkipAdButton;
    }

    public boolean trackAd() {
        return mTrackAd;
    }

    public boolean showChangeQualityTip() {
        return mShowChangeQualityTip;
    }

    public boolean showLoginDialog() {
        return mShowLoginDialog;
    }

    public boolean trackPlayError() {
        return mTrackPlayError;
    }

    public boolean showAdWebView() {
        return mShowAdWebView;
    }

    public boolean useHardwareDecode() {
        return mUseHardwareDecode;
    }

    public boolean useP2P() {
        return mUseP2P;
    }

	public boolean defaultAllow3G() {
		return mDefaultAllow3G;
	}

    public boolean livePortrait() {
        return mLivePortrait;
    }

    public int getP2PRetryTimes() {
        return mP2PRetryTimes;
    }

    public boolean enableOrientation() {
        return mEnableOrientation;
    }

    public boolean unicomFree() {
        return mUnicomFree;
    }

    public boolean requestAsync() {
        return mRequestAsync;
    }

    public String getVersionCode() {
        return mVersionCode;
    }

    public boolean useH265() {
        return mH265;
    }

    public boolean useEnhancedMode() {
        return mEnhancedMode;
    }

    public MediaPlayerConfiguration setShowPreAd(boolean isShow) {
        mShowPreAd = isShow;
        return this;
    }
	
	    public MediaPlayerConfiguration setShowPauseAd(boolean isShow) {
        mShowPauseAd = isShow;
        return this;
    }

	public MediaPlayerConfiguration setAdvPlatform(int platform) {
		mAdvPlatForm = platform;
		return this;
	}

    public MediaPlayerConfiguration setShowOfflineAd(boolean isShow) {
        mShowOfflineAd = isShow;
        return this;
    }

    public MediaPlayerConfiguration setShowSkipAdButton(boolean isShow) {
        mShowSkipAdButton = isShow;
        return this;
    }

    public MediaPlayerConfiguration setTrackAd(boolean isTrack) {
        mTrackAd = isTrack;
        return this;
    }

    public MediaPlayerConfiguration setShowChangeQualityTip(boolean isShow) {
        mShowChangeQualityTip = isShow;
        return this;
    }

    public MediaPlayerConfiguration setShowLoginDialog(boolean isShow) {
        mShowLoginDialog = isShow;
        return this;
    }

    public MediaPlayerConfiguration setTrackPlayError(boolean isTrack) {
        mTrackPlayError = isTrack;
        return this;
    }

    public MediaPlayerConfiguration setShowAdWebView(boolean isShow) {
        mShowAdWebView = isShow;
        return this;
    }

	public int getAdvPlatform() {
		return mAdvPlatForm;
	}

    public void setGreyConfiguration(String json) {
        GreyParam param = GreyConfigParser.parseJson(json);
        if (param != null) {
            mTestid = param.hit_config_id;
            mIstest = param.hit_state;

            // 1 - 广告同时请求，2 - 硬解
            int feature = FUN_REQUESTASYNC;
            int state = param.isFeatureEnable(feature);
            boolean enable = false;
            if (state != GreyParam.FUN_NOTFOUND) {
                mGreyFeatureSet.add(feature);
                enable = (state == GreyParam.FUN_ON);
                Logger.d(LogTag.TAG_GREY, "setGreyConfiguration async request: " + enable);
                setRequestAsync(enable);
            }

            feature = FUN_HWDECODE;
            state = param.isFeatureEnable(feature);
            if (state != GreyParam.FUN_NOTFOUND) {
                mGreyFeatureSet.add(feature);
                enable = (state == GreyParam.FUN_ON);
                setUseHardwareDecode(enable);
                Logger.d(LogTag.TAG_GREY, "setGreyConfiguration hardwaredecode: " + enable);
            }
        }
    }

    public MediaPlayerConfiguration setPlatform(int platform) {
        mPlatform = platform;
        return this;
    }

    public MediaPlayerConfiguration setUseHardwareDecode(boolean isHw) {
        mUseHardwareDecode = isHw;
        return this;
    }

    public MediaPlayerConfiguration setUseP2P(boolean useP2P) {
        this.mUseP2P = useP2P;
        return this;
    }

    public MediaPlayerConfiguration setP2PRetryTimes(int retryTimes) {
        mP2PRetryTimes = retryTimes;
        return this;
    }

    public MediaPlayerConfiguration setLivePortrait(boolean livePortrait) {
        mLivePortrait = livePortrait;
        return this;
    }

    public MediaPlayerConfiguration setRequestAsync(boolean requestAsync) {
        mRequestAsync = requestAsync;
        return this;
    }

    public MediaPlayerConfiguration setUseH265(boolean h265) {
        mH265 = h265;
        return this;
    }

    public MediaPlayerConfiguration setUseEnhancedMode(boolean enhancedMode) {
        mEnhancedMode = enhancedMode;
        return this;
    }

    private void setConfigFromXML() {
        InputStream inputStream = Profile.mContext.getResources()
                .openRawResource(R.raw.mediaplayer_configuration);
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(inputStream, "utf-8");
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        Logger.d(LogTag.TAG_PLAYER, "init config");
                        break;
                    case XmlPullParser.START_TAG:
                        String tag = parser.getName();
                        if ("MediaplayerConfig".equalsIgnoreCase(tag))
                            break;
                        if ("platform".equalsIgnoreCase(tag)) {
                            mPlatform = Integer.parseInt(getText(parser, tag));
                        } else if ("preAd".equalsIgnoreCase(tag)) {
                            mShowPreAd = Boolean.parseBoolean(getText(parser, tag));
                        } else if ("pauseAd".equalsIgnoreCase(tag)) {
                            mShowPauseAd = Boolean
                                    .parseBoolean(getText(parser, tag));
                        } else if ("offlineAd".equalsIgnoreCase(tag)) {
                            mShowOfflineAd = Boolean.parseBoolean(getText(parser,
                                    tag));
                        } else if ("skipAdButton".equalsIgnoreCase(tag)) {
                            mShowSkipAdButton = Boolean.parseBoolean(getText(
                                    parser, tag));
                        } else if ("trackAd".equalsIgnoreCase(tag)) {
                            mTrackAd = Boolean.parseBoolean(getText(parser, tag));
                        } else if ("changeQualityTip".equalsIgnoreCase(tag)) {
                            mShowChangeQualityTip = Boolean.parseBoolean(getText(
                                    parser, tag));
                        } else if ("loginDialog".equalsIgnoreCase(tag)) {
                            mShowLoginDialog = Boolean.parseBoolean(getText(parser,
                                    tag));
                        } else if ("trackPlayError".equalsIgnoreCase(tag)) {
                            mTrackPlayError = Boolean.parseBoolean(getText(parser,
                                    tag));
                        } else if ("adWebView".equalsIgnoreCase(tag)) {
                            mShowAdWebView = Boolean.parseBoolean(getText(parser,
                                    tag));
                        } else if ("ctype".equalsIgnoreCase(tag)) {
                            com.youku.player.goplay.Profile.ctype = Integer
                                    .parseInt(getText(parser, tag));
                        } else if ("harewareDecode".equalsIgnoreCase(tag)) {
                            mUseHardwareDecode = Boolean.parseBoolean(getText(
                                    parser, tag));
                        } else if ("p2pPlay".equalsIgnoreCase(tag)) {
                            String retryTimes = parser.getAttributeValue(null,
                                    "retryTimes");
                            if (!TextUtils.isEmpty(retryTimes)) {
                                mP2PRetryTimes = Integer.parseInt(retryTimes);
                                Logger.d(LogTag.TAG_PLAYER, "retryTimes:" + mP2PRetryTimes);
                            }
                            mUseP2P = Boolean.parseBoolean(getText(parser, tag));
                        }
                    case XmlPullParser.END_TAG:
                        break;
                    default:
                        break;
                }
                eventType = parser.next();
            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getText(XmlPullParser parser, String tag)
            throws XmlPullParserException, IOException {
        String text = parser.nextText().trim();
        Logger.d(LogTag.TAG_PLAYER, tag + ":" + text);
        return text;
    }

    public DanmakuUtils getDanmakuUtils() {
        return mDanmakuUtils;
    }

	public int getPlatform(){
		return mPlatform;
	}

    public String getDetailActivityName() {
        return mDetailActivityName;
    }
}
package com.youku.player.module;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.text.Html;
import android.text.TextUtils;
import android.util.SparseArray;

import com.baseproject.utils.Logger;
import com.baseproject.utils.Util;
import com.youku.analytics.data.Device;
import com.youku.player.LogTag;
import com.youku.player.Track;
import com.youku.player.base.YoukuBasePlayerActivity;
import com.youku.player.config.MediaPlayerConfiguration;
import com.youku.player.drm.DRMServiceManager;
import com.youku.player.goplay.AdvInfo;
import com.youku.player.goplay.ItemSeg;
import com.youku.player.goplay.ItemSegs;
import com.youku.player.goplay.Language;
import com.youku.player.goplay.Point;
import com.youku.player.goplay.Profile;
import com.youku.player.goplay.StaticsUtil;
import com.youku.player.goplay.VideoAdvInfo;
import com.youku.player.p2p.P2pManager;
import com.youku.player.plugin.MediaPlayerDelegate;
import com.youku.player.subtitle.Attachment;
import com.youku.player.unicom.ChinaUnicomFreeFlowUtil;
import com.youku.player.unicom.ChinaUnicomManager;
import com.youku.player.util.AdUtil;
import com.youku.player.util.Constants;
import com.youku.player.util.PlayerUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 视频信息
 *
 * @author yuanfang
 */
public class VideoUrlInfo implements Parcelable {
	private String videoLanguage = null;
	private P2pManager p2pManager;
	public int isWaterMark[] = new int[5];
	public int waterMarkType[] = new int[5];
	public boolean isLocalWaterMark = false;
	// 解决加密视频系统播放器无法播放问题，本地mp4视频播放失败，从系统播放器切换到软解播放器
	public boolean isEncyptError;

    private Map<String, String> woVideoUrls = new ConcurrentHashMap<String, String>();
	private ChinaUnicomManager chinaUnicomManager = null;

	public String getVideoLanguage() {
		return videoLanguage;
	}

	public void setVideoLanguage(String mVideoLanguage) {
		videoLanguage = mVideoLanguage;
	}

	public String playType = StaticsUtil.PLAY_TYPE_NET;

	public static int YOUKU_TYPE = 1;

	public static int TUDOU_TYPE = 2;

	private int type = YOUKU_TYPE;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String vitural_type = "";

	public boolean need_mark = false;

    /**
     * 非分段视频地址
     */
	private String url = "";
    /**
     * 视频标题
     */
	private String mTitle = "";

    /**
     * 视频未知id
     */
	private String id;
    /**
     * 视频id
     */
	private String videoId;
    /**
     * 加密视频的password
     */
	public String password;
    /**
     * showid
     */
	private String showId;
    /**
     * 重试次数，直播使用
     */
	private int retryTimes;
    /**
     * 浏览器播放地址
     */
	private String weburl;
    /**
     * 视频长度 单位:毫秒
     */
	private int duration;
    /**
     * 视频播放进度 单位:毫秒
     */
	private int progress;
    /**
     * 服务器返回状态码
     */
	private int code;
	private boolean isAlbum = false;
    /**
     * 当前播放剧集中的位置
     */
	private int show_videoseq = -1;

    /**
     * 下一集id
     */
	public String nextVideoId;

    /**
     * 下一集title
     */
	public String nextVideoTitle;

    /**
     * playlistCode
     */
	public String playlistCode;

    /**
     * playlistId
     */
	public String playlistId;

    /**
     * albumID
     */
	public String albumID;

	// 缓存时的路径
	public String cachePath;

	//缓存时SD卡路径
	public String savePath;

	private int limit = -1;

	// 频道ID
	private int cid;

	public String videoIdPlay;

	// 是否显示Toast
	public String show_videostage_title;

	// 是否为外部视频（非客户端缓存的视频）
	public boolean isExternalVideo = false;

	// 视频来源 0:代表本地 1：代表百度 2：代表快播	
	public Source mSource = Source.YOUKU;

	/**
	 * 是否有试看
	 */
	private int look_ten;
	/**
	 * 付费信息
	 */
	public PayInfo mPayInfo;

	// 每一集简介
	private String mItemDesc;

	private String uid = "";
	private String siddecode = "";
	private boolean interact;

	public LiveInfo mLiveInfo;

	public String bps;
	public String channel;
	public String offset;

	public boolean isHLS;

	private VVPlayInfo vvPlayInfo;
	private String youkuRegisterNum;
    private boolean http4xxError;

	/**
	 * 当前视频清晰度
	 */
	private int mCurrentVideoQuality = Profile.videoQuality;

	private String viddecode;
	private String channelId;//ct
	private String schannelid;//cs
	private String piddecode;//piddecode
	private String playlistchannelid;//pct
	private String splaylistchannelid;//pcs
	private String showchannelid;//sct
	private String sshowchannelid;//scs
	private String paystate;
	private String copyright;
	private String trailers;

    // DRM相关参数
    private String drmType;
    private String marlinToken;

	// 是否是重播
	private boolean isReplay;

    // 独播标示
    private boolean exclusive_logo;

    public boolean isExclusiveLogo() {
        return exclusive_logo;
    }

    public void setExclusiveLogo(boolean exclusive_logo) {
        this.exclusive_logo = exclusive_logo;
    }

	public boolean isReplay() {
		return isReplay;
	}

	public void setReplay(boolean isReplay) {
		this.isReplay = isReplay;
	}

	public int getLimit() {
		return limit;
	}

	public void setShow_videostage_title(String show_videostage_title){
		this.show_videostage_title = show_videostage_title;
	}

	public String getShow_videostage_title(){
		return show_videostage_title;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	private String img;

	private String mTip;
    private String mTiping;
    private String mTipEnd;
	private int mVipError;

	/**
	 * 代表 200 * 112 横图
	 */
	private String item_img_16_9;

	private String mAlbumTitle;

	private String item_subtitle;

	private String episodemode;

	private String channel_name;

	private String title_new_dl_sub;
	private String title_new_dl;

	private boolean isTrailer;
	private String registerNum;
	private String licenseNum;

	private long itemPlayTimes;
	private String itemShortDesc;

	//用于分享
	private String vCode;

	public long getItemPlayTimes() {
		return itemPlayTimes;
	}

	public void setItemPlayTimes(long itemPlayTimes) {
		this.itemPlayTimes = itemPlayTimes;
	}

	public String getItemShortDesc() {
		return itemShortDesc;
	}

	public void setItemShortDesc(String itemShortDesc) {
		this.itemShortDesc = itemShortDesc;
	}

	public boolean isTrailer() {
		return isTrailer;
	}

	public void setIsTrailer(boolean isTrailer) {
		this.isTrailer = isTrailer;
	}

	public String getRegisterNum() {
		return registerNum;
	}

	public void setRegisterNum(String registerNum) {
		this.registerNum = registerNum;
	}

	public String getLicenseNum() {
		return licenseNum;
	}

	public void setLicenseNum(String licenseNum) {
		this.licenseNum = licenseNum;
	}

	public String getChannel_name() {
		return channel_name;
	}

	public void setChannel_name(String channel_name) {
		this.channel_name = channel_name;
	}

	/**
	 * 请求时的id
	 */
	private String requestId;

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}


	/**
	 * 是否关注
	 */
	private boolean isAttention;

	public void setAttention(boolean attention) {
		isAttention = attention;
	}

	public boolean getAttention(){
		return isAttention;
	}

	public void setimgurl(String img){
		this.img = img;
	}

	public String getimgUrl(){
		return img;
	}

    /**
     * 获取当前播放视频在剧集中的位置
     */
	public int getShow_videoseq() {
		return show_videoseq;
	}

    /**
     * 设置当前播放视频在剧集中的位置
     */
	public void setShow_videoseq(int mShow_videoseq) {
		show_videoseq = mShow_videoseq;
	}

    /**
     * 本地是否有下一集 1:有下一集 0：没有下一集
     */
	private int haveNext;

    /**
     * 获取当前是否有下一集
     */
	public int getHaveNext() {
		return haveNext;
	}

    /**
     * 设置是否有下一集
     */
	public void setHaveNext(int mHaveNext) {
		haveNext = mHaveNext;
	}

    /**
     * 当前视频是否是音乐类 1： 是音乐 0：否
     */
	private int isMusic;

    /**
     * 获取当前视频是否属于音乐类
     */
	public int getIsMusic() {
		return isMusic;
	}

    /**
     * 设置当前视频是否是音乐类
     */
	public void setIsMusic(int mIsMusic) {
		isMusic = mIsMusic;
	}

	public boolean isAlbum() {
		return isAlbum;
	}

	public void setAlbum(boolean isAlbum) {
		this.isAlbum = isAlbum;
	}

	/** 播放视频的类型 具体对应见com.youku.phone.detail.Constants */
	private int videoType = Constants.VIDEO_TYPE_FILM;


    /**
     * 当前视频类型
     */
	public int getVideoType() {
		return videoType;
	}

    /**
     * 设置当前视频类型
     */
	public void setVideoType(int mVideoType) {
		videoType = mVideoType;
	}

    /**
     * 本地是否有缓存
     */
	private boolean isCached;
	// use for parcel
	private int cached = 0;
    /**
     * 网络获取状态
     */
	private String netStatus;
    /**
     * 精彩点
     */
	private ArrayList<Point> pointArray = new ArrayList<Point>();
    /**
     * 中插点
     */
	private ArrayList<Point> adPointArray = new ArrayList<Point>();
    /**
     * 语言
     */
	private ArrayList<Language> languages = new ArrayList<Language>();
    /**
     * 网络获取状态
     */
	private int httpResponseCode;

	private int videoStage = 0;

    /**
     * 视频段ArrayListmp4
     */
    private ItemSegs vSegMp4; // 高清
    /**
     * 视频段ArrayListflv
     */
    private ItemSegs vSegFlv; // 标清
    /**
     * 视频段ArrayListhd2
     */
    private ItemSegs vSegHd2; // 超清
    private ItemSegs vSegHd3; // 1080p

    /**
     * 视频段ArrayList
     */
    private ItemSegs vSeg;
    /**
     * iku链接状态
     */
	private boolean ikuConnected = false;
	/** 视频允许能够下载 */
	// private boolean isAllowDownload = false;
    /**
     * 视频类型，用于控制右侧画中画是选集还是推荐
     */
	private boolean isSeries = false;

	// 剧集的标题
	public String serialTitle;
	// 剧集的评分
	public String reputation;

    /**
     * 是否从本地视频列表进入
     */
	private boolean nativePlay = false;

	private String errorMsg;
    /**
     * 是否已经收藏
     */
	public boolean isFaved = false;

	// 是否第一次加载成功
	public boolean isFirstLoaded = false;

	// public ArrayList<ItemVideo> relativeVideos = new
	// ArrayList<ItemVideo>();
	public boolean IsSendVV = false;
	public boolean isSendVVEnd = false;

	// 是否付费
	public boolean paid = false;

	// 付费的播放统计标志位
	public boolean paidSended = false;

	// 防盗链需要的几个参数
	public String oip;
	public String token;
	public String sid;

    /**
     * 土豆用来区分视频和音频
     */
		private String mMediaType;
	private SparseArray<Integer> mStreamMilliseconds = new SparseArray<Integer>(6);

	private boolean isFeeVideo;
	private boolean isFeeView;

    // 专辑内视频个数
    private int albumVideoCount;

    private boolean isVerticalVideo;

    public boolean isVerticalVideo() {
        return isVerticalVideo;
    }

    public void setVerticalVideo(boolean isVerticalVideo) {
        this.isVerticalVideo = isVerticalVideo;
    }

    public void setHttp4xxError(boolean http4xxError){
        this.http4xxError = http4xxError;
    }

    public boolean isHttp4xxError(){
        return http4xxError;
    }

    public int getAlbumVideoCount() {
        return albumVideoCount;
    }

    public void setAlbumVideoCount(int albumVideoCount) {
        this.albumVideoCount = albumVideoCount;
    }

	public String getAlbumTitle() {
		return mAlbumTitle;
	}

	public void setAlbumTitle(String mAlbumTitle) {
		this.mAlbumTitle = mAlbumTitle;
	}

	public boolean isFeeVideo() {
		return isFeeVideo;
	}

	public boolean isFeeView() {
		return isFeeView;
	}

	public void setFeeVideo(boolean isFeeVideo) {
		this.isFeeVideo = isFeeVideo;
	}

	public void setFeeView(boolean isFeeView) {
		this.isFeeView = isFeeView;
	}

	public boolean isNativePlay() {
		return nativePlay;
	}

	public void setNativePlay(boolean mNativePlay) {
		nativePlay = mNativePlay;
	}

    /**
     * 视频类型，用于控制右侧画中画是否为选集
     */
	public boolean isSeries() {
		return isSeries;
	}

	public void setSeries(boolean mSeries) {
		isSeries = mSeries;
	}

	public int getVideoStage() {
		return videoStage;
	}

	public void setVideoStage(int mVideoStage) {
		videoStage = mVideoStage;
	}

	public void setStatus(String u) {
		netStatus = u;
	}

	public String getStatus() {
		return netStatus;
	}

	public void setHttpResponseCode(int r) {
		httpResponseCode = r;
	}

	public int getHttpResponseCode() {
		return httpResponseCode;
	}

	public void setWeburl(String u) {
		weburl = u;
	}

	public String getWeburl() {
		return weburl;
	}

	public void setUrl(String u) {
		url = u;
	}

	public String getUrl() {
		if (Profile.from == Profile.PHONE_BROWSER) {
			return url;
		}

		if (isExternalVideo) {
			return getUrlForExternalVideo();
		}

		if (Profile.USE_SYSTEM_PLAYER) {
			mCurrentVideoQuality = Profile.VIDEO_QUALITY_SD;
			if (isCached())
				return getCacheUrl();
			return getM3u8SD();
		}

        if (isDRMVideo()) {
            return DRMServiceManager.getInstance().makeUrl(getM3u8Url());
        }

		if (isHLS){
			if(YoukuBasePlayerActivity.getCurrentFormat() == Profile.FORMAT_3GPHD)
				return "";
			else
				return PlayerUtil.getHlsFinnalUrl(Util.getHLSEncreptUrl(url,
						channel, token, oip, sid, bps, retryTimes++, MediaPlayerDelegate.is,
						MediaPlayerConfiguration.getInstance().mPlantformController
								.getLiveEncyptType()));
		}

		if ((Profile.getVideoFormat() == Profile.FORMAT_FLV_HD
				|| Profile.getVideoFormat() == Profile.FORMAT_MP4
				|| Profile.getVideoFormat() == Profile.FORMAT_HD2 || Profile
				.getVideoFormat() == Profile.FORMAT_HD3)
				&& Profile.playerType == Profile.PLAYER_OUR) {
			if (isCached()) {
				return getCacheUrl();
			} else {
				return makeM3u8();
			}
		}
		if (YoukuBasePlayerActivity.getCurrentFormat() == Profile.FORMAT_3GPHD) {
			mCurrentVideoQuality = Profile.VIDEO_QUALITY_SD;
			if (videoAdvInfo != null)
				videoAdvInfo.VAL = null;
			if (isCached()) {
				return cachePath;
			}
			if (MediaPlayerConfiguration.getInstance().useP2P())
				Track.trackP2PError(videoId, P2pManager.P2P_NOT_SUPPORT);
			return PlayerUtil.getFinnalUrl(MediaPlayerConfiguration
					.getInstance().mPlantformController.getEncreptUrl(url,
							fieldId, token, oip, sid, MediaPlayerDelegate.is,
							Device.gdid));
		}
		return url;
	}

    private String getM3u8Url() {
        mCurrentVideoQuality = Profile.videoQuality;
        if (Profile.videoQuality == Profile.VIDEO_QUALITY_SD) { // 标清
            return getM3u8SD();
        } else if (Profile.videoQuality == Profile.VIDEO_QUALITY_HD) { // 高清
            if (!TextUtils.isEmpty(m3u8HD))
                return getM3u8HD();
            else {
                mCurrentVideoQuality = Profile.VIDEO_QUALITY_SD;
                return getM3u8SD();
            }
        } else if (Profile.videoQuality == Profile.VIDEO_QUALITY_HD2) { // 超清
            if (!TextUtils.isEmpty(m3u8HD2))
                return getM3u8HD2();
            else if (!TextUtils.isEmpty(m3u8HD)) {
                mCurrentVideoQuality = Profile.VIDEO_QUALITY_HD;
                return getM3u8HD();
            } else {
                mCurrentVideoQuality = Profile.VIDEO_QUALITY_SD;
                return getM3u8SD();
            }
        }
        return getM3u8SD();
    }

	private String getUrlForExternalVideo() {
		if (isUseCachePath()) {
			// 高品质视频，m3u8格式会启动软解播放,有卡的现象。 故直接返回播放地址进行硬解播放
			return cachePath;
		}
        if (PlayerUtil.useUplayer(this))
			return makeM3u8ForExternalVideo();
		else
			return cachePath;
	}

	public boolean isUseCachePath() {
		if(cachePath == null){
			return false;
		}
		return cachePath.toLowerCase().endsWith(".mp4") && !isEncyptError;
	}

	public boolean isNeedLoadedNotify() {
        if (isDRMVideo())
            return true;
		if (!isExternalVideo) {
			return false;
		}
		if (isUseCachePath()) {
			return true;
		}
		return false;
	}

	private String makeM3u8ForExternalVideo() {

		if (cachePath == null) {
			return null;
		}
		StringBuffer str = new StringBuffer();

		str.append("#PLSEXTM3U\n").append("#EXT-X-TARGETDURATION:")
				.append(1)//XXX:由于时长未知，暂时使用值1
				.append("\n#EXT-X-VERSION:2\n#EXT-X-DISCONTINUITY\n");

		str.append("#EXTINF:").append(1)//XXX:由于时长未知，暂时使用值1
				.append("\n").append(cachePath).append("\n");
		str.append("#EXT-X-ENDLIST\n");
        Logger.d(LogTag.TAG_PLAYER, "构建m3u8列表");
		return str.toString();
	}

	public String getCacheUrl() {
		return cachePath;
	}

	/**
	 * 根据清晰度设置视频地址
	 */
	private void setPlaySegByQuality() {
		mCurrentVideoQuality = Profile.videoQuality;
		if (Profile.videoQuality == Profile.VIDEO_QUALITY_SD) { // 标清
			vSeg = vSegFlv;
		} else if (Profile.videoQuality == Profile.VIDEO_QUALITY_HD) { // 高清
			if (vSegMp4 != null && vSegMp4.size() > 0)
				vSeg = vSegMp4;
			else {
				vSeg = vSegFlv;
				mCurrentVideoQuality = Profile.VIDEO_QUALITY_SD;
			}
		} else if (Profile.videoQuality == Profile.VIDEO_QUALITY_HD2) { // 超清
			if (vSegHd2 != null && vSegHd2.size() > 0)
				vSeg = vSegHd2;
			else if (vSegMp4 != null && vSegMp4.size() > 0) {
				vSeg = vSegMp4;
				mCurrentVideoQuality = Profile.VIDEO_QUALITY_HD;
			} else {
				vSeg = vSegFlv;
				mCurrentVideoQuality = Profile.VIDEO_QUALITY_SD;
			}
		} else if (Profile.videoQuality == Profile.VIDEO_QUALITY_HD3) {
			if (vSegHd3 != null && vSegHd3.size() > 0) {
				vSeg = vSegHd3;
			} else if (vSegHd2 != null && vSegHd2.size() > 0) {
				vSeg = vSegHd2;
				mCurrentVideoQuality = Profile.VIDEO_QUALITY_HD2;
			} else if (vSegMp4 != null && vSegMp4.size() > 0) {
				vSeg = vSegMp4;
				mCurrentVideoQuality = Profile.VIDEO_QUALITY_HD;
			} else {
				vSeg = vSegFlv;
				mCurrentVideoQuality = Profile.VIDEO_QUALITY_SD;
			}
		}
	}

	/**
	 * 是否可以进行p2p播放
     *
	 * @return
	 */
	public boolean canPlayP2P() {
		return MediaPlayerConfiguration.getInstance().useP2P() && !isCached
				&& YoukuBasePlayerActivity.isHighEnd && !isExternalVideo
				&& !isHLS && p2pManager.canUseAcc();
	}

	private String makeM3u8() {

		setPlaySegByQuality();
		StringBuffer str = new StringBuffer();
		str.append("#PLSEXTM3U\n").append("#EXT-X-TARGETDURATION:")
				.append(getDurationMills())
				.append("\n#EXT-X-VERSION:2\n#EXT-X-DISCONTINUITY\n");
		// 广告测试播放 注意本地路径
		// str.append("#EXTINF:30 PRE_AD\n/storage/emulated/0/youku/offlinedata/640x480.flv\n");
		if (videoAdvInfo != null && videoAdvInfo.VAL != null
				&& videoAdvInfo.VAL.size() > 0) {
			for (int i = 0; i < videoAdvInfo.VAL.size(); i++) {
				AdvInfo advInfo = videoAdvInfo.VAL.get(i);

				// VT是2的时候不展示素材
				if (TextUtils.isEmpty(advInfo.RS) || "2".equals(advInfo.VT))
					continue;
                str.append("#EXTINF:").append(advInfo.AL).append(" PRE_AD");
                if (AdUtil.isTrueViewAd(advInfo))
                    str.append(" 0");
                str.append("\n");
				str.append(advInfo.RS.trim()).append("\n");
			}
		} else if (vSeg == null || vSeg.size() == 0) {
			return "";
		}
		boolean playP2P = p2pManager.checkPlayP2P(videoId);
		String port = "";
		if (playP2P)
			port = p2pManager.getAccPort();
        boolean chinaUnicomFree = !ChinaUnicomFreeFlowUtil.checkChinaUnicom3GWapNet(com.baseproject.utils.Profile.mContext)
                && ChinaUnicomFreeFlowUtil.isSatisfyChinaUnicomFreeFlow();
        if (chinaUnicomFree)
            chinaUnicomManager.getWoVideoUrls(videoId, vSeg.getSegs(), woVideoUrls, token, oip, sid);

		for (int i = 0; vSeg != null && i < vSeg.size(); ++i) {
			ItemSeg item = vSeg.get(i);
			str.append("#EXTINF:").append(item.get_Seconds());
			if (i == 0) {
				if (getProgress() > 1000) {
					str.append(" START_TIME ").append(getProgress());
				} else if (Profile.isSkipHeadAndTail() && isHasHead() && getHeadPosition() > 0) {
					str.append(" START_TIME ").append(getHeadPosition());
				}
			}
			String finalUrl = MediaPlayerConfiguration.getInstance().mPlantformController
					.getEncreptUrl(item.get_Url(), item.getFieldId(), token,
							oip, sid, MediaPlayerDelegate.is, Device.gdid);
			if (playP2P) {
				finalUrl = finalUrl.replace("http://", "http://127.0.0.1:"
						+ port + "/")
						+ "&ua=mp&st=vod";
			}
            if (chinaUnicomFree &&
            //用户使用3g网络且可以使用P2P播放且则采用联通3G免流量播放
			//1. 非WAP网络
			//2. 用户使用3g网络且可以使用P2P播放且则采用联通3G免流量播放
                    !TextUtils.isEmpty(woVideoUrls.get(item.get_Url()))) {
						finalUrl = woVideoUrls.get(item.get_Url());
					}
			str.append("\n").append(finalUrl).append("\n");
		}
		str.append("#EXT-X-ENDLIST\n");
        Logger.d(LogTag.TAG_PLAYER, "构建m3u8列表");
        return str.toString();
    }

	/**
	 * 在线播放缓存视频时，将广告播放列表添加到cachePath.
     *
	 * @return 是否成功添加
	 */
	public boolean addAdvToCachePathIfNecessary() {
		if (!isCached) {
			return false;
		}

		if (videoAdvInfo == null || videoAdvInfo.VAL == null
				|| videoAdvInfo.VAL.size() == 0) {
			return false;
		}

		addAdvToCachePath();
		return true;
	}

	private void addAdvToCachePath() {
		StringBuffer buf = new StringBuffer();
		String cacheInfo = cachePath.substring(cachePath.indexOf("#EXTINF"));
		buf.append("#PLSEXTM3U\n").append("#EXT-X-TARGETDURATION:")
				.append(getDurationMills())
				.append("\n#EXT-X-VERSION:2\n#EXT-X-DISCONTINUITY\n");
		for (AdvInfo advInfo : videoAdvInfo.VAL) {
			if (TextUtils.isEmpty(advInfo.RS))
				continue;
			buf.append("#EXTINF:").append(advInfo.AL).append(" PRE_AD\n");
			buf.append(advInfo.RS.trim()).append("\n");
		}
		buf.append(cacheInfo);
        Logger.d(LogTag.TAG_PLAYER, "addAdvToCachePath cache:" + buf.toString());
		cachePath = buf.toString();
	}

	public String getUrl_M3U8() {
		return url;
	}

	public boolean isCached() {
		return isCached;
	}

	public void setCached(boolean mIsCached) {
		isCached = mIsCached;
	}

	public void setTitle(String title) {
		if (title != null && title.trim().length() > 0) {
			try {
				mTitle = Html.fromHtml(title).toString().trim();
			} catch (Exception e) {
				mTitle = title.toString().trim();
			}
		}
	}

	public String getTitle() {
		return mTitle;
	}

	public void setVid(String vid) {
		videoId = vid;
	}

	public String getVid() {
		return videoId;
	}

	public void setid(String Id) {
		id = Id;
	}

	public String getId() {
		return id;
	}

	public void setShowId(String s) {
		showId = s;
	}

	public String getShowId() {
		return showId;
	}

	public void setProgress(int p) {
		progress = p;
	}

	public int getProgress() {
		// if (progress > 0)
		return progress;
		// else
		// return 0;
	}

	public void setCode(int c) {
		code = c;
	}

	public int getCode() {
		return code;
	}

	/**
	 * 获取所有精彩点数组
	 *
	 * @return 精彩点数组
	 * @author 孙浩斌
	 */
	public synchronized ArrayList<Point> getPoints() {
		if (pointArray == null)
			pointArray = new ArrayList<Point>();
		return pointArray;
	}

	/**
	 * 获取所有中插点数组
	 *
	 * @return 中插点数组
	 */
	public synchronized ArrayList<Point> getAdPoints() {
		if (adPointArray == null)
			adPointArray = new ArrayList<Point>();
		return adPointArray;
	}

	public void setAdPoints(ArrayList<Point> points) {
		if (points == null && points.size() == 0) {
			return;
		}
		if (adPointArray == null) {
			adPointArray = new ArrayList<Point>();
		} else {
			adPointArray.clear();
		}
		for (int i = 0; i < points.size(); i++) {
			if (points.get(i).type.equals(Profile.STANDARD_POINT)
					|| points.get(i).type.equals(Profile.CONTENTAD_POINT)) {
				Point p = new Point();
				p.start = points.get(i).start;
				p.type = points.get(i).type;
				p.title = points.get(i).title;
				p.desc = points.get(i).desc;
				adPointArray.add(p);
			}
		}
	}

	public void clear() {
		IsSendVV = false;
		isSendVVEnd = false;
		mTitle = null;
		id = null;
		password = null;
		videoId = null;
		showId = null;
		progress = 0;
		retryTimes = 0;
		code = 0;
		videoLanguage = null;
		weburl = null;
		duration = 0;
		isCached = false;
		savePath = "";
		netStatus = null;
		pointArray.clear();
		adPointArray.clear();
		languages.clear();
		httpResponseCode = 0;
		ikuConnected = false;
		isWaterMark = null;
		waterMarkType = null;
		isLocalWaterMark = false;
		url = null;
		vSeg = null;
		m3u8SD = null;
		m3u8HD = null;
		m3u8HD2 = null;
		vSegFlv = null;
		vSegMp4 = null;
		vSegHd2 = null;
		vSegHd3 = null;
		m3u8SDDuration = 0;
		m3u8HDDuration = 0;
		m3u8HD2Duration = 0;
		reputation = null;
		showId = null;
		serialTitle = null;
		nextVideoId = null;
		nextVideoTitle = null;
		playlistCode = null;
		playlistId = null;
		haveNext = 0;
		albumID = null;
		pointArray.clear();
		adPointArray.clear();
		hasHead = false;
		hasTail = false;
		tailPosition = 0;
		headPosition = 0;
		isAlbum = false;
		videoIdPlay = null;
		mVideoFetchTime = 0;
		paid = false;
		paidSended = false;
		show_videostage_title = null;
		isExternalVideo = false;
		mMediaType = null;
		videoAdvInfo = null;
		item_img_16_9 = null;
		episodemode = null;
		mPayInfo = null;
		mLiveInfo = null;
		isHLS = false;
		bps = null;
		channel = null;
		uid = "";
		siddecode = "";
		interact = false;
		p2pManager.setUsingP2P(false);
		viddecode = null;
		channelId = null;
		schannelid = null;
		piddecode = null;
		playlistchannelid = null;
		splaylistchannelid = null;
		showchannelid = null;
		sshowchannelid = null;
		paystate = null;
		copyright = null;
		trailers = null;
		isReplay = false;
		vitural_type = "";
		need_mark = false;
        drmType = "";
        marlinToken = "";
		mStreamMilliseconds.clear();
		videoType = Constants.VIDEO_TYPE_FILM;
		vvPlayInfo = null;
		isFeeVideo = true;
		isFeeView = true;
		mItemDesc = "";
		mAlbumTitle = "";
		img = "";
		mTip = "";
		mVipError = 0;
		itemPlayTimes = 0;
		itemShortDesc = null;
		isVerticalVideo = false;	
        http4xxError = false;
        exclusive_logo = false;
	}

	public boolean isUrlOK() {
		if (isCached()) {
			return !TextUtils.isEmpty(cachePath);
		}
		if (Profile.getVideoFormat() == Profile.FORMAT_MP4
				|| Profile.getVideoFormat() == Profile.FORMAT_FLV_HD
				|| Profile.getVideoFormat() == Profile.FORMAT_HD2
				|| Profile.getVideoFormat() == Profile.FORMAT_HD3) {
            List<ItemSeg> segs = getvSeg(Profile.getVideoFormat());
			if (segs != null && segs.size() > 0) {
				for (int i = 0; i < segs.size(); i++) {
					ItemSeg seg = segs.get(i);
					if (seg != null && seg.get_Url() != null
							&& seg.get_Url().trim().length() > 0
							&& seg.get_Seconds() > 0) {

					} else {
						return false;
					}
				}
				return true;
			}
			return false;
		} else if (Profile.getVideoFormat() == Profile.FORMAT_M3U8) {
			return hasM3u8SD() || hasM3u8HD() || hasM3u8HD2();
		} else if (Profile.getVideoFormat() == Profile.FORMAT_3GPHD
				&& isCached()) {
			return hasM3u8SD();
		}
		return url != null && url.trim().length() > 0;
    }


    /**
     * 添加视频段信息
     */

    /**
     * 添加视频段信息 V3.0
     */
    public void addSegments(List<ItemSeg> segments, int format, boolean h265) {
		if (Profile.FORMAT_FLV_HD == format) {
			if (vSegFlv == null) {
                vSegFlv = new ItemSegs();
			}
            vSegFlv.setSegs(segments, h265);
		} else if (Profile.FORMAT_MP4 == format) {
			if (vSegMp4 == null) {
                vSegMp4 = new ItemSegs();
			}
            vSegMp4.setSegs(segments, h265);
		} else if (Profile.FORMAT_HD2 == format) {
			if (vSegHd2 == null) {
                vSegHd2 = new ItemSegs();
			}
            vSegHd2.setSegs(segments, h265);
		} else if (Profile.FORMAT_HD3 == format) {
			if (vSegHd3 == null) {
                vSegHd3 = new ItemSegs();
			}
            vSegHd3.setSegs(segments, h265);
		}
	}

	/** 返回视频段 */
	// public ArrayList<ItemSeg> getvSeg() {
	// return vSeg;
	// }

    /**
     * 返回视频段 V2.4.1
     */
    public List<ItemSeg> getvSeg(int format) {
		if (Profile.FORMAT_FLV_HD == format) {
            return vSegFlv != null ? vSegFlv.getSegs() : null;
		} else if (Profile.FORMAT_MP4 == format) {
            return vSegMp4 != null ? vSegMp4.getSegs() : null;
		} else if (Profile.FORMAT_HD2 == format) {
            return vSegHd2 != null ? vSegHd2.getSegs() : null;
		} else if (Profile.FORMAT_HD3 == format) {
            return vSegHd3 != null ? vSegHd3.getSegs() : null;
		}
		return null;
	}




	// 获得是否有片头。
	public boolean isHasHead() {
		return hasHead;
	}

	// 设置是否有片头。
	public void setHasHead(boolean mHasHead) {
		hasHead = mHasHead;
		if (!hasHead)
			headPosition = -1;
	}

	// 获得是否有片尾。
	public boolean isHasTail() {
		return hasTail;
	}

	// 设置是否有片尾。
	public void setHasTail(boolean mHasTail) {
		hasTail = mHasTail;
		if (!hasTail)
			tailPosition = -1;
	}

	// 是否有片头。
	private boolean hasHead = false;

	// 是否有片尾。
	private boolean hasTail = false;

	// 片头时间点，单位毫秒。
	private int headPosition = 0;

	// 片尾时间点，单位毫秒。
	private int tailPosition = 0;

	// 获得片头时间点。
	public int getHeadPosition() {
		return headPosition;
	}

	// 设置片头时间点。
	public void setHeadPosition(int mHeadPosition) {
		headPosition = mHeadPosition;
	}

	// 获得片尾时间点。
	public int getTailPosition() {
		return tailPosition;
	}

	// 设置片尾时间点。
	public void setTailPosition(int mTailPosition) {
		tailPosition = mTailPosition;
	}

	// 视频标清版本的url。
	public String m3u8SD = null;

	// 视频高清版本的url。
	public String m3u8HD = null;

	// 视频超清版本的url。
	public String m3u8HD2 = null;

	// 低端机防盗链用
	public String fieldId = null;

	public String getM3u8HD2() {
		return MediaPlayerConfiguration.getInstance().mPlantformController
				.getEncreptUrl(m3u8HD2, videoIdPlay, token, oip, sid,
						MediaPlayerDelegate.is, Device.gdid);
	}

	public void setM3u8HD2(String m3u8hd2) {
		m3u8HD2 = m3u8hd2;
	}

	// 视频标清版本的时长。
	private int m3u8SDDuration = 0;

	// 视频高清版本的时长。
	private int m3u8HDDuration = 0;

	// 视频超清版本的时长。
	private int m3u8HD2Duration = 0;

	public int getM3u8HD2Duration() {
		return m3u8HD2Duration;
	}

	public void setM3u8HD2Duration(int m3u8hd2Duration) {
		m3u8HD2Duration = m3u8hd2Duration;
	}

	// 获得视频标清版本的url。
	public String getM3u8SD() {
		return MediaPlayerConfiguration.getInstance().mPlantformController
				.getEncreptUrl(m3u8SD, videoIdPlay, token, oip, sid,
						MediaPlayerDelegate.is, Device.gdid);
	}

	// 设置视频标清版本的url。
	public void setM3u8SD(String m3u8sd) {
		m3u8SD = m3u8sd;
	}

	// 获得视频高清版本的url。
	public String getM3u8HD() {
		return MediaPlayerConfiguration.getInstance().mPlantformController
				.getEncreptUrl(m3u8HD, videoIdPlay, token, oip, sid,
						MediaPlayerDelegate.is, Device.gdid);
	}

	// 设置视频高清版本的url。
	public void setM3u8HD(String m3u8hd) {
		m3u8HD = m3u8hd;
	}

	// 获得视频标清版本的时常。
	public int getM3u8SDDuration() {
		return m3u8SDDuration;
	}

	// 设置视频标清版本的时常。
	public void setM3u8SDDuration(int m3u8sdDuration) {
		m3u8SDDuration = m3u8sdDuration;
	}

	// 获得视频高清版本的时常。
	public int getM3u8HDDuration() {
		return m3u8HDDuration;
	}

	// 设置视频高清版本的时常。
	public void setM3u8HDDuration(int m3u8hdDuration) {
		m3u8HDDuration = m3u8hdDuration;
	}

	// 视频是否提提供标清版本。
	public boolean hasM3u8SD() {
		return !TextUtils.isEmpty(m3u8SD);
	}

	// 视频是否提供高清版本。
	public boolean hasM3u8HD() {
		return !TextUtils.isEmpty(m3u8HD);
	}

	public boolean hasM3u8HD2() {
		return !TextUtils.isEmpty(m3u8HD2);
	}

	public boolean hasM3u8HD3() {
		// return m3u8HD2 != null;
		return getvSeg(Profile.FORMAT_HD3) == null ? false : true;
	}

	// 获得当前视频提供的语言。
	public ArrayList<Language> getLanguage() {
		if (languages == null) {
			languages = new ArrayList<Language>();
		}
		return languages;
	}

	public int getCurrentLanguageID() {
		return getLanguageID(videoLanguage);
	}

	public int getLanguageID(String languageName) {
		int id = -1;
		if (languages != null && languages.size() != 0) {
			for (int i = 0; i < languages.size(); i++) {
				if (languageName.equals(languages.get(i).langCode)) {
					id = languages.get(i).id;
				}
			}
		}

		return id;
	}
	/***
	 * 是否有某种语言
	 *
	 * @author heyanxia
	 * @param languageName
	 * @return
	 */
	public boolean isContainLanguage(String languageName) {
		if (languages != null && languages.size() != 0) {
			for (int i = 0; i < languages.size(); i++) {
				if (languageName.equals(languages.get(i).lang)) {
					return true;
				}
			}
		}
		return false;
	}

	public void setWebViewUrl(String url) {
		webViewUrl = url;
	}

	private String webViewUrl = null;

	public String getWebViewUrl() {
		return webViewUrl;
	}

	/**
	 * 设置视频总时长，单位为秒。
	 */
	public void setDurationSecs(double durationSecs) {
		if (durationSecs <= 0)
			return;
		duration = (int) (durationSecs * 1000);
	}

	/**
	 * 设置视频总时长，单位为毫秒。
	 */
	public void setDurationMills(int durationMills) {
		if (durationMills <= 0)
			return;
		duration = durationMills;
	}

	/**
	 * 获得视屏时常，单位为毫秒。
	 */
	public int getDurationMills() {
		return duration;
	}

	public String getPlayType() {
		if (playType == null) {
			return StaticsUtil.PLAY_TYPE_LOCAL;
		}
		return playType;
	}

	public void setPlayType(String mPlayType) {
		playType = mPlayType;
	}

	/**
	 * Set the value of msg
	 *
	 * @param newVar
	 *            the new value of msg
	 */
	public void setErrorMsg(String newVar) {
		errorMsg = newVar;
	}

	/**
	 * Get the value of msg
	 *
	 * @return the value of msg
	 */
	public String getErrorMsg() {
		return errorMsg;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public VideoUrlInfo() {
		super();
		p2pManager = P2pManager.getInstance();
		chinaUnicomManager = ChinaUnicomManager.getInstance();
	}

	/**
	 * 视频广告信息
	 */
	public VideoAdvInfo videoAdvInfo;

	/**
	 * 获取 Video 成功的时间
	 */
	public long mVideoFetchTime = 0;
	public static final long _2_HOURS_MILLI_SECONDS = 2 * 60 * 60 * 1000;
	public static final long _1_MIN_MILLI_SECONDS = 1 * 60 * 1000;
	public static final long _119_MINS_MILLI_SECONDS = _2_HOURS_MILLI_SECONDS - _1_MIN_MILLI_SECONDS;

	public boolean isVideoUrlOutOfDate() {

		if (mVideoFetchTime <= 0) {
			return false;
		}

		final long currentTime = SystemClock.elapsedRealtime();
		if (currentTime - mVideoFetchTime < _119_MINS_MILLI_SECONDS) {
			return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public VideoUrlInfo(Parcel source) {
		show_videoseq = source.readInt();
		mTitle = source.readString();
		id = source.readString();
		password = source.readString();
		videoId = source.readString();
		showId = source.readString();
		progress = source.readInt();
		retryTimes = source.readInt();
		code = source.readInt();
		videoLanguage = source.readString();
		playType = source.readString();
		weburl = source.readString();
		duration = source.readInt();
		cached = source.readInt();
		netStatus = source.readString();
		languages = source.readArrayList(Language.class.getClassLoader());
		pointArray = source.readArrayList(Point.class.getClassLoader());
		httpResponseCode = source.readInt();
		url = source.readString();
		isCached = cached != 0;
		// vSeg = source.read
		m3u8SD = source.readString();
		m3u8HD = source.readString();
		m3u8HD2 = source.readString();
		m3u8SDDuration = source.readInt();
		m3u8HDDuration = source.readInt();
		m3u8HD2Duration = source.readInt();
		reputation = source.readString();
		showId = source.readString();
		serialTitle = source.readString();
		nextVideoId = source.readString();
		nextVideoTitle = source.readString();
		playlistCode = source.readString();
		playlistId = source.readString();
		albumID = source.readString();
		haveNext = source.readInt();
		hasHead = (source.readInt() == 1);
		hasTail = (source.readInt() == 1);
		headPosition = source.readInt();
		tailPosition = source.readInt();
		videoAdvInfo = source.readParcelable(VideoAdvInfo.class
				.getClassLoader());
		mVideoFetchTime = source.readLong();
		paid = source.readInt() == 1;
		videoIdPlay = source.readString();
		cid = source.readInt();
		show_videostage_title = source.readString();
		isExternalVideo = source.readInt() == 1;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(show_videoseq);
		dest.writeString(mTitle);
		dest.writeString(id);
		dest.writeString(password);
		dest.writeString(videoId);
		dest.writeString(showId);
		dest.writeInt(progress);
		dest.writeInt(retryTimes);
		dest.writeInt(code);
		dest.writeString(videoLanguage);
		dest.writeString(playType);
		dest.writeString(weburl);
		dest.writeInt(duration);
		if (isCached)
			cached = 1;
		else
			cached = 0;
		dest.writeInt(cached);
		dest.writeString(netStatus);
		dest.writeList(languages);
		dest.writeList(pointArray);
		dest.writeInt(httpResponseCode);
		// dest.writeBoolean(ikuConnected);
		dest.writeString(url);
		// dest.writeString(vSeg);
		dest.writeString(m3u8SD);
		dest.writeString(m3u8HD);
		dest.writeString(m3u8HD2);
		dest.writeInt(m3u8SDDuration);
		dest.writeInt(m3u8HDDuration);
		dest.writeInt(m3u8HD2Duration);
		dest.writeString(reputation);
		dest.writeString(showId);
		dest.writeString(serialTitle);
		dest.writeString(nextVideoId);
		dest.writeString(nextVideoTitle);
		dest.writeString(playlistCode);
		dest.writeString(playlistId);
		dest.writeString(albumID);
		dest.writeInt(haveNext);
		dest.writeInt(hasHead ? 1 : 0);
		dest.writeInt(hasTail ? 1 : 0);
		dest.writeInt(headPosition);
		dest.writeInt(tailPosition);
		dest.writeParcelable(videoAdvInfo, PARCELABLE_WRITE_RETURN_VALUE);
		dest.writeLong(mVideoFetchTime);
		dest.writeInt(paid ? 1 : 0);
		dest.writeString(videoIdPlay);
		dest.writeInt(cid);
		dest.writeString(show_videostage_title);
		dest.writeInt(isExternalVideo ? 1 : 0);
	}

	/**
	 * 通过删除容器中 position=0 的 Adv,来移除已经播放过的广告记录
	 */
	public void removePlayedAdv() {
		removeFirstAdvFromVAL();
	}

	private void removeFirstAdvFromVAL() {
		if (videoAdvInfo == null) {
			return;
		}

		if (videoAdvInfo.VAL == null || videoAdvInfo.VAL.size() == 0) {
			return;
		}

		videoAdvInfo.VAL.remove(0);
	}

	/**
	 * 当前Video的广告是否为空或已经播放完
	 */
	public boolean isAdvEmpty() {
		if (videoAdvInfo == null) {
			return true;
		}

		if (videoAdvInfo.VAL == null || videoAdvInfo.VAL.size() == 0) {
			return true;
		}
		return false;
	}

	/**
	 * 实现Parcelable接口的greator
	 */
	public static final Creator<VideoUrlInfo> CREATOR = new Creator<VideoUrlInfo>() {
		@Override
		public VideoUrlInfo createFromParcel(Parcel source) {
			return new VideoUrlInfo(source);
		}

		@Override
		public VideoUrlInfo[] newArray(int size) {
			return new VideoUrlInfo[size];
		}
	};

	public AdvInfo getCurrentAdvInfo() {
		if ((videoAdvInfo == null) || (videoAdvInfo.VAL == null)
				|| (videoAdvInfo.VAL.size() == 0)) {
			return null;
		}
		return videoAdvInfo.VAL.get(0);
	}

	/**
	 * Get the cid.
	 *
	 * @return the cid.
	 */
	public int getCid() {
		return cid;
	}

	/**
	 * Set the cid.
	 *
	 * @param cid The cid to set.
	 */
	public void setCid(int cid) {
		this.cid = cid;
	}

	public void setMediaType(String mediaType){
		mMediaType = mediaType;
	}

	public String getMediaType() {
		return mMediaType;
	}

	public void setItem_img_16_9(String img) {
		item_img_16_9 = img;
	}

	public String getItem_img_16_9() {
		return item_img_16_9;
	}

	public String getEpisodemode() {
		return episodemode;
	}

	public void setEpisodemode(String episodemode) {
		this.episodemode = episodemode;
	}

	public void setLookTen(int lookLen) {
		look_ten = lookLen;
	}

	public int getLookTen() {
		return look_ten;
	}

	public String getTitle_new_dl() {
		return title_new_dl;
	}

	public String getTitle_new_dl_sub() {
		return title_new_dl_sub;
	}

	public void setTitle_new_dl_sub(String title_new_dl_sub) {
		this.title_new_dl_sub = title_new_dl_sub;
	}

	public void setTitle_new_dl(String title_new_dl) {
		this.title_new_dl = title_new_dl;
	}

	public enum Source {
		LOCAL, BAIDU, KUAIBO, YOUKU
	}

	public int getCurrentQuality() {
		return mCurrentVideoQuality;
	}

	public void setItemSubtitle(String itemSubtitle) {
		item_subtitle = itemSubtitle;
	}

	public String getItemSubtitle(){
		return item_subtitle;
	}

	public void setCurrentVideoQuality(int format) {
		mCurrentVideoQuality = format;
	}

	public boolean isUrlEmpty() {
		String playUrl = url;
		if (Profile.from == Profile.PHONE_BROWSER) {
			playUrl = url;
		} else if (isExternalVideo) {
			playUrl = getUrlForExternalVideo();
		} else if (isCached) {
			playUrl = getCacheUrl();
		} else if (YoukuBasePlayerActivity.getCurrentFormat() == Profile.FORMAT_3GPHD)
			playUrl = url;
		else {
			if ((Profile.getVideoFormat() == Profile.FORMAT_FLV_HD
					|| Profile.getVideoFormat() == Profile.FORMAT_MP4
					|| Profile.getVideoFormat() == Profile.FORMAT_HD2 || Profile
					.getVideoFormat() == Profile.FORMAT_HD3)
					&& Profile.playerType == Profile.PLAYER_OUR) {
				if (vSeg == null || vSeg.size() == 0)
					return true;
				else
					return false;
			}
		}
		if (TextUtils.isEmpty(playUrl))
			return true;
		else
			return false;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getSiddecode() {
		return siddecode;
	}

	public void setSiddecode(String siddecode) {
		this.siddecode = siddecode;
	}

	public boolean isInteract() {
		return interact;
	}

	public void setInteract(boolean interact) {
		this.interact = interact;
	}

	public boolean hasSegSD() {
		return getvSeg(Profile.FORMAT_FLV_HD) != null
				&& getvSeg(Profile.FORMAT_FLV_HD).size() > 0;
	}

	public boolean hasSegHD() {
		return getvSeg(Profile.FORMAT_MP4) != null
				&& getvSeg(Profile.FORMAT_MP4).size() > 0;
	}

	public boolean hasSegHD2() {
		return getvSeg(Profile.FORMAT_HD2) != null
				&& getvSeg(Profile.FORMAT_HD2).size() > 0;
	}

	public boolean hasSegHD3() {
		return getvSeg(Profile.FORMAT_HD3) != null
				&& getvSeg(Profile.FORMAT_HD3).size() > 0;
	}

	//视频的附件信息
	private List<Attachment> mAttachments;

	public void setAttachments(List<Attachment> attachments) {
		if (mAttachments != null) {
			mAttachments.clear();
		}

		mAttachments = attachments;
	}

	public List<Attachment> getAttachments() {
		return mAttachments;
	}

	public String getViddecode() {
		return viddecode;
	}

	public String getChannelId() {
		return channelId;
	}

	public String getSchannelid() {
		return schannelid;
	}

	public String getPiddecode() {
		return piddecode;
	}

	public String getPlaylistchannelid() {
		return playlistchannelid;
	}

	public String getSplaylistchannelid() {
		return splaylistchannelid;
	}

	public String getShowchannelid() {
		return showchannelid;
	}

	public String getSshowchannelid() {
		return sshowchannelid;
	}

	public String getPaystate() {
		return paystate;
	}

	public String getCopyright() {
		return copyright;
	}

	public String getTrailers() {
		return trailers;
	}

	public void setViddecode(String viddecode) {
		this.viddecode = viddecode;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public void setSchannelid(String schannelid) {
		this.schannelid = schannelid;
	}

	public void setPiddecode(String piddecode) {
		this.piddecode = piddecode;
	}

	public void setPlaylistchannelid(String playlistchannelid) {
		this.playlistchannelid = playlistchannelid;
	}

	public void setSplaylistchannelid(String splaylistchannelid) {
		this.splaylistchannelid = splaylistchannelid;
	}

	public void setShowchannelid(String showchannelid) {
		this.showchannelid = showchannelid;
	}

	public void setSshowchannelid(String sshowchannelid) {
		this.sshowchannelid = sshowchannelid;
	}

	public void setPaystate(String paystate) {
		this.paystate = paystate;
	}

	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	public void setTrailers(String trailers) {
		this.trailers = trailers;
	}

    public String getDrmType() {
        return drmType;
    }

    public String getMarlinToken() {
        return marlinToken;
    }

    public void setDrmType(String drmType) {
        this.drmType = drmType;
    }

    public void setMarlinToken(String marlinToken) {
        this.marlinToken = marlinToken;
    }

    public boolean isDRMVideo() {
        if ("marlin".equals(drmType) && !TextUtils.isEmpty(marlinToken))
            return true;
        else
            return false;
    }

	public void addStreamMilliseconds(int quality, int duration) {
		mStreamMilliseconds.put(quality, duration);
	}

	public int getStreamMilliseconds(int quality) {
		return mStreamMilliseconds.get(quality, 0);
	}

	public boolean trialByTime() {
		return mPayInfo != null && mPayInfo.trail != null && ("time".equalsIgnoreCase(mPayInfo.trail.type));
	}

	public VVPlayInfo getVVPlayInfo() {
		return vvPlayInfo;
	}

	public void setVVPlayInfo(VVPlayInfo vvPlayInfo) {
		this.vvPlayInfo = vvPlayInfo;
	}

	public String getItemDesc() {
		return mItemDesc;
	}

	public void setItemDesc(String itemDesc) {
		this.mItemDesc = itemDesc;
	}
	
	public void setYoukuRegisterNum(String youkuRegisterNum) {
        this.youkuRegisterNum = youkuRegisterNum;
    }
	
	public String getYoukuRegisterNum() {
        return this.youkuRegisterNum;
    }

	public String getTip() {
		return mTip;
	}

    public String getTiping() {
        return mTiping;
    }

    public String getTipend(){
        return mTipEnd;
    }

	public void setTip(String tip) {
		this.mTip = tip;
	}

	public void setTiping(String tiping) {
		this.mTiping = tiping;
	}

	public void setTipEnd(String tipend) {
		this.mTipEnd = tipend;
	}

	public int getVipError() {
		return mVipError;
	}

	public void setVipError(int vipError) {
		this.mVipError = vipError;
	}

	public boolean playH265Segs() {
        return vSeg != null && vSeg.h265();
    }

	public void setVcode(String vcode) {
		vCode = vcode;
	}

    /**
     * 只限分享使用，其他勿用
     */
	public String getVcode(){
		return vCode;
	}

}
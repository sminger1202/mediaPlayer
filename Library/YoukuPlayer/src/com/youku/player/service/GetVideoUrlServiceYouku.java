package com.youku.player.service;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Base64;

import com.baseproject.utils.Logger;
import com.youku.android.player.R;
import com.youku.player.LogTag;
import com.youku.player.Track;
import com.youku.player.apiservice.ICacheInfo;
import com.youku.player.base.YoukuBasePlayerActivity;
import com.youku.player.config.MediaPlayerConfiguration;
import com.youku.player.goplay.GoplayException;
import com.youku.player.goplay.IVideoInfoCallBack;
import com.youku.player.goplay.ItemSeg;
import com.youku.player.goplay.Language;
import com.youku.player.goplay.Point;
import com.youku.player.goplay.Profile;
import com.youku.player.goplay.TaskGetVideoUrl;
import com.youku.player.goplay.VideoInfoReasult;
import com.youku.player.module.PayInfo;
import com.youku.player.module.PayInfo.Trial;
import com.youku.player.module.VideoCacheInfo;
import com.youku.player.module.VideoUrlInfo;
import com.youku.player.plugin.MediaPlayerDelegate;
import com.youku.player.subtitle.Attachment;
import com.youku.player.subtitle.SubtitleManager;
import com.youku.player.util.PlayerUtil;
import com.youku.player.util.URLContainer;
import com.youku.uplayer.MediaPlayerProxy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class GetVideoUrlService
 */
public class GetVideoUrlServiceYouku implements NetService {

	private Context mContext;
	// 回调函数的监听变量
	private IVideoInfoCallBack mListener;
	private VideoUrlInfo mVideoUrlInfo;
	private boolean resetProgress;
	private String format;
	private boolean h265;

	/**
	 * 开始获取视频地址的时间
	 */
	private long mGetPlayListTime = 0;

	// 构造函数
	public GetVideoUrlServiceYouku(Context mContext) {
		this.mContext = mContext;
	};

	/**
	 * @return VideoUrlInfo
	 * @param vid
	 * @param languageCode
	 * @param videostage
	 * @param format
	 * @param resetProgress
	 * @param local_history
	 */
	public void getVideoUrl(String vid, String languageCode, int videostage,
			int format, VideoUrlInfo mVideoUrlInfo, boolean resetProgress,
			String local_vid, String local_time, String local_history,
			IVideoInfoCallBack mListener) {
		String url = null;
		this.resetProgress = resetProgress;
		languageCode = URLEncoder(languageCode);
		if (Profile.USE_SYSTEM_PLAYER) {
			this.format = "6";
		} else
		// 软解时取标清、高清、超清
		if (Profile.FORMAT_FLV_HD == format) {
			this.format = "1,5,6,7,8";
		} else {
			this.format = "4";
		}

		final String password = mVideoUrlInfo.password;
		final String playlistId = mVideoUrlInfo.playlistId;

		if (videostage != 0) {// 多集url
			String mUrlMoreYouku = URLContainer.getMutilPayVideoPlayUrl(vid, password,
					local_time, videostage, local_vid, languageCode,
					this.format, local_history, playlistId);

			/*
			 * URLContainer.getMutilVideoUrlYouku(vid, languageCode, videostage,
			 * this.format);
			 */
			url = mUrlMoreYouku;
		} else {// 单集url
			String mUrlOneYouku = URLContainer.getOnePayVideoPlayUrl(vid, password,
					local_time, local_vid, languageCode, this.format,
					local_history, playlistId);
			/*
			 * URLContainer.getOneVideoUrlYouku(vid, languageCode, this.format);
			 */
			url = mUrlOneYouku;
		}

		h265 = MediaPlayerConfiguration.getInstance().useH265() && MediaPlayerProxy.supportH265();
		if (h265)
			url += "&h265=1";
		this.mListener = mListener;
		this.mVideoUrlInfo = mVideoUrlInfo;
		Logger.d(LogTag.TAG_PLAYER, "请求播放地址 GetVideoUrlServiceYouku getVideoUrl:"
				+ url);
		mGetPlayListTime = SystemClock.elapsedRealtime();
		TaskGetVideoUrl taskGetVideoUrl = new TaskGetVideoUrl(url);
		taskGetVideoUrl.setSuccess(SUCCESS);
		taskGetVideoUrl.setFail(FAIL);
		taskGetVideoUrl.execute(handler);
	}
	
	private void sendTrack() {
		String vid = "";
		if (mVideoUrlInfo != null)
			vid = mVideoUrlInfo.getVid();
		Track.trackGetPlayList(mContext, mGetPlayListTime, vid);
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case SUCCESS:
					// 服务器返回空值容错
					if (TextUtils.isEmpty(VideoInfoReasult.getResponseString())) {
						GoplayException mException = new GoplayException();
						setVideoUrlFailReason(mException);
						Logger.d(LogTag.TAG_PLAYER, "获取正片信息 失败, 返回数据为空");
						mListener.onFailed(mException);
						return;
					}
					setVideoUrlInfo(mVideoUrlInfo);
					sendTrack();
					Logger.d(LogTag.TAG_PLAYER, "获取正片信息 成功");
					mListener.onSuccess(mVideoUrlInfo);
					break;
				case FAIL:
					// 获取失败，检查并设置本地是否已经缓存了视频
					if (getCachedVideoSuccessfully(mVideoUrlInfo)) {
						Logger.d(LogTag.TAG_PLAYER, "获取网络正片信息失败，播放本地视频信息 ");
						mListener.onSuccess(mVideoUrlInfo);
						return;
					}

					GoplayException mException = new GoplayException();
					setVideoUrlFailReason(mException);
					Logger.d(LogTag.TAG_PLAYER, "获取正片信息 失败");
					mListener.onFailed(mException);
					break;
			}
		}
	};

	public static String URLEncoder(String s) {
		if (s == null || "".equals(s))
			return "";
		try {
			s = URLEncoder.encode(s.toLowerCase(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
		} catch (NullPointerException e) {
		}
		return s;
	}

	/**
	 * 联网获取Url成功时，解析结果并赋值给VideoUrlInfo各变量
	 * 
	 * @param mResult
	 *            ：存储解析结果的VideoUrlInfo对象 msg：联网获取的数据结果
	 * 
	 * @return 无返回值
	 * 
	 * */
	public void setVideoUrlInfo(VideoUrlInfo mResult) {
		try {
			
			JSONObject json = new JSONObject(
					VideoInfoReasult.getResponseString());
			String data =  json.getString("data");
			byte[] bytes = Base64.decode(data.getBytes(), Base64.DEFAULT);
			String decrypt = new String(PlayerUtil.decrypt(bytes, "qwer3as2jin4fdsa"));
			JSONObject object = new JSONObject(decrypt);
			// 目前是优酷Url地址，则只设置优酷相关字段信息
			Logger.d(LogTag.TAG_PLAYER, "解析服务器返回的视频信息 setVideoUrlInfo");
			setVideoUrlInfoFromJson(object);
		} catch (JSONException e) {
			Logger.e(LogTag.TAG_PLAYER, "解析服务器返回的视频信息 setVideoUrlInfo 出错", e);
		}
	}
	
	private void setVideoUrlInfoFromJson(JSONObject object)
			throws JSONException {
		int response = -1;
		String status = PlayerUtil.getJsonValue(object, "status");
		JSONObject sidData = object.getJSONObject("sid_data");
		String token = sidData.getString("token");
		String oip = sidData.getString("oip");
		String sid = sidData.getString("sid");
		mVideoUrlInfo.token = token;
		mVideoUrlInfo.oip = oip;
		mVideoUrlInfo.sid = sid;
		mVideoUrlInfo.setStatus(status);
		int code = PlayerUtil.getJsonInit(object, "code", 0);
		mVideoUrlInfo.setCode(code);
		mVideoUrlInfo.setHttpResponseCode(response);
		String title = object.optString("title");
		if (title != null && title.trim().length() > 0) {
			mVideoUrlInfo.setTitle(title);
		}

		if (object.has("streamlogos")) {
			JSONObject streamlogos = object.getJSONObject("streamlogos");
			if (streamlogos != null) {
				int isMp4WaterMark = PlayerUtil.getJsonInit(streamlogos, "mp4",
						0);
				int isHd3WaterMark = PlayerUtil.getJsonInit(streamlogos, "hd3",
						0);
				int isHd2WaterMark = PlayerUtil.getJsonInit(streamlogos, "hd2",
						0);
				int isFlvWaterMark = PlayerUtil.getJsonInit(streamlogos,
						"flvhd", 0)
						+ PlayerUtil.getJsonInit(streamlogos, "flv", 0);
				if (isFlvWaterMark == 0 && streamlogos.has("3gphd")) {
					isFlvWaterMark = PlayerUtil.getJsonInit(streamlogos, "3gphd", 0);
				}
				mVideoUrlInfo.isWaterMark[0] = isHd2WaterMark;
				mVideoUrlInfo.isWaterMark[1] = isMp4WaterMark;
				mVideoUrlInfo.isWaterMark[2] = isFlvWaterMark;
				mVideoUrlInfo.isWaterMark[4] = isHd3WaterMark;
			}
		}

		mVideoUrlInfo
				.setSiddecode(PlayerUtil.getJsonValue(object, "siddecode"));
		mVideoUrlInfo.setUid(PlayerUtil.getJsonValue(object, "uid"));
		mVideoUrlInfo.setInteract(object.optBoolean("interact"));
		mVideoUrlInfo.setVideoType(object.optInt("video_type"));
		
		mVideoUrlInfo.setViddecode(PlayerUtil.getJsonValue(object, "viddecode"));
		mVideoUrlInfo.setChannelId(PlayerUtil.getJsonValue(object, "ct"));
		mVideoUrlInfo.setSchannelid(PlayerUtil.getJsonValue(object, "cs"));
		mVideoUrlInfo.setPiddecode(PlayerUtil.getJsonValue(object, "piddecode"));
		mVideoUrlInfo.setPlaylistchannelid(PlayerUtil.getJsonValue(object, "pct"));
		mVideoUrlInfo.setSplaylistchannelid(PlayerUtil.getJsonValue(object, "pcs"));
		mVideoUrlInfo.setShowchannelid(PlayerUtil.getJsonValue(object, "sct"));
		mVideoUrlInfo.setSshowchannelid(PlayerUtil.getJsonValue(object, "scs"));
		mVideoUrlInfo.setPaystate(PlayerUtil.getJsonValue(object, "paystate"));
		mVideoUrlInfo.setCopyright(PlayerUtil.getJsonValue(object, "copyright"));
		mVideoUrlInfo.setTrailers(PlayerUtil.getJsonValue(object, "trailers"));

		int lookten = object.optInt("look_ten");
		if (lookten == 1) {
			mVideoUrlInfo.setLookTen(lookten);
		}
		mVideoUrlInfo.mPayInfo = new PayInfo();
		setPayInfo(mVideoUrlInfo.mPayInfo, object);

		String showid = PlayerUtil.getJsonValue(object, "showid");
		if (!PlayerUtil.isNull(showid)) {
			mVideoUrlInfo.setShowId(showid);
		}
		// weburl
		String weburl = PlayerUtil.getJsonValue(object, "weburl");
		if (!PlayerUtil.isNull(weburl))
			mVideoUrlInfo.setWeburl(weburl);
		String img = PlayerUtil.getJsonValue(object, "img_hd");
		if (!PlayerUtil.isNull(img))
			mVideoUrlInfo.setimgurl(img);
		mVideoUrlInfo.setVideoLanguage(object.optString("lang"));
		// videoid
		String videoid = PlayerUtil.getJsonValue(object, "videoid");
		if (!PlayerUtil.isNull(videoid)) {
			mVideoUrlInfo.setVid(videoid);
		}
		// Duration
		mVideoUrlInfo.setDurationSecs(object.optInt("totalseconds"));
		// point
		int point = PlayerUtil.getJsonInit(object, "point", 0);
		int show_videoseq = PlayerUtil.getJsonInit(object, "show_videoseq", -1);
		mVideoUrlInfo.setShow_videoseq(show_videoseq);
		mVideoUrlInfo.setAlbumVideoCount(object.optInt("album_video_count"));
		mVideoUrlInfo.setVerticalVideo("1".equals(object.optString("is_phone_stream")) ? true : false);
		if (mVideoUrlInfo.playlistId == null) {
			if (null != object && object.has("next_video")) {
				mVideoUrlInfo.setHaveNext(1);
				JSONObject next = object.getJSONObject("next_video");
				mVideoUrlInfo.nextVideoId = next.getString("videoid");
			} else {
				mVideoUrlInfo.setHaveNext(0);
			}
		} else {
			if (object.has("playlist_next_video")) {
				JSONObject playlistNextVideoObject = object
						.getJSONObject("playlist_next_video");
				mVideoUrlInfo.nextVideoId = playlistNextVideoObject
						.optString("next_videoid");
				mVideoUrlInfo.nextVideoTitle = playlistNextVideoObject
						.optString("title");
				mVideoUrlInfo.setHaveNext(1);
			} else {
				mVideoUrlInfo.setHaveNext(0);
			}
		}
		if (-1 == mVideoUrlInfo.getProgress()) {
			mVideoUrlInfo.setProgress(0);
		} else {
//			if (resetProgress) {// point==0 true//其他页 读取本地历史
				if (point > 0) {
					mVideoUrlInfo.setProgress(point * 1000);
				}
				VideoUrlInfo tempInfo = MediaPlayerDelegate.getRecordFromLocal(mVideoUrlInfo);
				if (tempInfo != null)
					mVideoUrlInfo = tempInfo;
//			}
			}
		// 播放历史还差1分钟的时候重播
		if (mVideoUrlInfo.getDurationMills() - mVideoUrlInfo.getProgress() <= 60000) {
			mVideoUrlInfo.setProgress(0);
		}

		mVideoUrlInfo.setWebViewUrl(object.optString("webviewurl"));
		mVideoUrlInfo.paid = object.optInt("paid") == 1;

		mVideoUrlInfo.videoIdPlay = object.optString("videoid_play");
		mVideoUrlInfo.setDrmType(object.optString("drm_type"));
		if ("marlin".equals(mVideoUrlInfo.getDrmType())) {
			JSONObject drmToken = object.optJSONObject("drm_token");
			if (drmToken != null)
				mVideoUrlInfo.setMarlinToken(drmToken.optString("malin"));
		}

		getPointInfo(object);
		// FIXME
		ICacheInfo download = MediaPlayerDelegate.mICacheInfo;
		if (download != null) {
			if (download.isDownloadFinished(videoid)) {

				VideoCacheInfo downloadInfo = download
						.getDownloadInfo(mVideoUrlInfo.getVid());
				if (YoukuBasePlayerActivity.isHighEnd) {
					String cacheM3u8 = PlayerUtil
							.getM3u8File(downloadInfo.savePath + "youku.m3u8");
					// 读取m3u8失败后，直接onfail
					if (TextUtils.isEmpty(cacheM3u8)) {
						getLanguageInfo(object);
						if (object.has("results"))
							parseVideoInfo(object.getJSONObject("results"));
						return;
					}

					mVideoUrlInfo.setCached(true);
					mVideoUrlInfo.cachePath = cacheM3u8;
				} else {
					mVideoUrlInfo.setCached(true);
					mVideoUrlInfo.cachePath = downloadInfo.savePath + "1.3gp";
				}
				mVideoUrlInfo.setCurrentVideoQuality(downloadInfo.quality);
			} else {
				getLanguageInfo(object);
				if (object.has("results"))
					parseVideoInfo(object.getJSONObject("results"));
			}
		} else {
			getLanguageInfo(object);
			if (object.has("results"))
				parseVideoInfo(object.getJSONObject("results"));
		}

		if (object.has("attachment"))
			parseAttachment(object.optJSONArray("attachment"));

        if (object.has("stream_milliseconds")) {
            JSONObject streamJson = object.optJSONObject("stream_milliseconds");
            Logger.d(LogTag.TAG_PLAYER, "streamJson:" + streamJson);
            int flvDuration = streamJson.optInt("flvhd");
            if (flvDuration != 0)
                mVideoUrlInfo.addStreamMilliseconds(Profile.VIDEO_QUALITY_SD, flvDuration);
            int mp4Duration = streamJson.optInt("mp4");
            if (mp4Duration != 0)
                mVideoUrlInfo.addStreamMilliseconds(Profile.VIDEO_QUALITY_HD, mp4Duration);
            int hd2Duration = streamJson.optInt("hd2");
            if (hd2Duration != 0)
                mVideoUrlInfo.addStreamMilliseconds(Profile.VIDEO_QUALITY_HD2, hd2Duration);
            int currentQualityDuration = mVideoUrlInfo.getStreamMilliseconds(mVideoUrlInfo.getCurrentQuality());
            if (currentQualityDuration != 0)
                mVideoUrlInfo.setDurationMills(currentQualityDuration);
        }

		// 登记证号
		String youku_register_num = PlayerUtil.getJsonValue(object, "youku_register_num");
		if(!TextUtils.isEmpty(youku_register_num)){
			mVideoUrlInfo.setYoukuRegisterNum(youku_register_num);
		}

		// 许可证号
		String license_num = PlayerUtil.getJsonValue(object, "license_num");
		if(!TextUtils.isEmpty(license_num)){
			mVideoUrlInfo.setLicenseNum(license_num);
		}

	}
	
	private boolean getCachedVideoSuccessfully(VideoUrlInfo videoUrlInfo) {

		ICacheInfo download = MediaPlayerDelegate.mICacheInfo;

		String vid = videoUrlInfo.getVid();

		if (download == null || !download.isDownloadFinished(vid)) {
			return false;
		}

		String cachePath;
		VideoCacheInfo downloadInfo = download.getDownloadInfo(vid);

		if (YoukuBasePlayerActivity.isHighEnd) {

			String cachedM3u8 = PlayerUtil.getM3u8File(downloadInfo.savePath
					+ "youku.m3u8");

			if (TextUtils.isEmpty(cachedM3u8)) {
				return false;
			}

			cachePath = cachedM3u8;

		} else {
			cachePath = downloadInfo.savePath + "1.3gp";
		}

		getVideoUrlInfoFromDownloadInfo(videoUrlInfo, downloadInfo, cachePath);

		return true;
	}

	private void getVideoUrlInfoFromDownloadInfo(VideoUrlInfo videoUrlInfo,
			VideoCacheInfo downloadInfo, String cachePath) {
		videoUrlInfo.setVid(downloadInfo.videoid);
		videoUrlInfo.setTitle(downloadInfo.title);
		videoUrlInfo.setCached(true);
		videoUrlInfo.cachePath = cachePath;
		videoUrlInfo.setVideoLanguage(downloadInfo.language);
		videoUrlInfo.setProgress(downloadInfo.playTime);
		videoUrlInfo.setShowId(downloadInfo.showid);
		videoUrlInfo.serialTitle = downloadInfo.showname;
		videoUrlInfo.setShow_videoseq(downloadInfo.show_videoseq);
		videoUrlInfo.setDurationSecs(downloadInfo.seconds);
		videoUrlInfo.nextVideoId = downloadInfo.nextVid;
		videoUrlInfo.setCurrentVideoQuality(downloadInfo.quality);
	}

	/**
	 * 获取Url失败时设置失败原因
	 * 
	 * @param mResult
	 *            ：存储解析结果的GoplayException对象 msg：联网获取的数据结果
	 * 
	 * @return 无返回值
	 * 
	 * */
	protected void setVideoUrlFailReason(GoplayException mResult) {
		try {
			int code = 0;
			if (VideoInfoReasult.getResponseString() != null) {
				JSONObject object = new JSONObject(
						VideoInfoReasult.getResponseString());
				code = PlayerUtil.getJsonInit(object, "code", 0);
				mVideoUrlInfo.setCode(code);
				mResult.webUrl = object.optString("webviewurl");
				mResult.setErrorCode(code);
				mResult.setErrorInfo(object.optString("err_desc"));
				if (code == -104) {
					if (object.has("streamtypes")) {
						JSONArray streamtypes = object
								.getJSONArray("streamtypes");
						if (streamtypes != null)
							for (int i = 0; i < streamtypes.length(); i++) {
								if ("3gphd".equals(streamtypes.get(i))) {
									mResult.webUrl = object
											.optString("webviewurl");
								}
							}
					}
				} else if (code == -112) {
					mResult.payInfo = new PayInfo();
					setPayInfo(mResult.payInfo, object);
				}
			}

			if (mVideoUrlInfo.isCached()) {
				mResult.setErrorInfo(mContext.getText(
						R.string.player_error_native).toString());
			} else if (TextUtils.isEmpty(mResult.getErrorInfo())) {
				mResult.setErrorInfo(mContext.getText(
						R.string.Player_error_f100).toString());
			}
		} catch (JSONException e) {
			mResult.setErrorInfo(mContext.getText(R.string.Player_error_f100)
					.toString());
			Logger.e(LogTag.TAG_PLAYER, e);
		}
	}
	
	private void setPayInfo(PayInfo payInfo, JSONObject object)
			throws JSONException {
		JSONObject trial_info = object.optJSONObject("trial");
        if (trial_info != null) {
            payInfo.trail = new Trial();
            payInfo.trail.episodes = trial_info.optInt("episodes");
            payInfo.trail.time = trial_info.optInt("time");
            payInfo.trail.type = trial_info.optString("type");
            payInfo.trail.trialStr = object.optString("trial_str");
            Logger.d(LogTag.TAG_PLAYER, "payInfo.trail.episodes:" + payInfo.trail.episodes + " payInfo.trail.time:" + payInfo.trail.time + " payInfo.trail.type:" + payInfo.trail.type + " payInfo.trail.trialStr:" + payInfo.trail.trialStr);
        }
        if (object.has("pay_info")) {
			JSONObject pay_info = object.getJSONObject("pay_info");
			payInfo.duration = pay_info.getString("duration");
			payInfo.oriprice = pay_info.getString("oriprice");
			payInfo.coprice = pay_info.getString("coprice");
			payInfo.play = pay_info.getBoolean("play");
			JSONArray paytype = pay_info.getJSONArray("paytype");
			payInfo.payType = new ArrayList<String>();
			for (int i = 0; i < paytype.length(); i++) {
				payInfo.payType.add(paytype.getString(i));
			}
		}
		if (object.has("showid")) {
			payInfo.showid = object.getString("showid");
		}
		if (object.has("showname")) {
			payInfo.showname = object.getString("showname");
		}
		if (object.has("vip")) {
			payInfo.vip = object.getString("vip");
		}
	}

	private void parseVideoInfo(JSONObject object) {
		if (Profile.USE_SYSTEM_PLAYER) {
			parseM3U8(object);
		}
		if (Profile.getVideoFormatName().equals("flv_hd")
				|| Profile.getVideoFormatName().equals("mp4")
				|| Profile.getVideoFormatName().equals("hd2")) {
			String formatSD = "flvhd";
			String formatHD = "mp4";
			String formatHD2 = "hd2";
			String formatHD3 = "hd3";
			if (h265) {
				String formatSDH265 = "mp5sd";
				String formatHDH265 = "mp5hd";
				String formatHD2H265 = "mp5hd2";
				String formatHD3H265 = "mp5hd3";
				if (!parseSeg(object, formatSDH265, Profile.FORMAT_FLV_HD, true))
					parseSeg(object, formatSD, Profile.FORMAT_FLV_HD, false);
				if (!parseSeg(object, formatHDH265, Profile.FORMAT_MP4, true))
					parseSeg(object, formatHD, Profile.FORMAT_MP4, false);
				if (!parseSeg(object, formatHD2H265, Profile.FORMAT_HD2, true))
					parseSeg(object, formatHD2, Profile.FORMAT_HD2, false);
				if (!parseSeg(object, formatHD3H265, Profile.FORMAT_HD3, true))
					parseSeg(object, formatHD3, Profile.FORMAT_HD3, false);
			} else {
				parseSeg(object, formatSD, Profile.FORMAT_FLV_HD, false);
				parseSeg(object, formatHD, Profile.FORMAT_MP4, false);
				parseSeg(object, formatHD2, Profile.FORMAT_HD2, false);
				parseSeg(object, formatHD3, Profile.FORMAT_HD3, false);
			}
			parseM3U8(object);
		} else if (Profile.getVideoFormatName().equals("m3u8")) {
			parseM3U8(object);
		} else
			parseOther(object, Profile.getVideoFormatName());
	}


	private boolean parseSeg(JSONObject object, String type, int format, boolean h265) {
		JSONArray segsArray = object.optJSONArray(type);
		if (segsArray != null && segsArray.length() > 0) {
			List<ItemSeg> segs = new ArrayList<>();
			for (int i = 0; i < segsArray.length(); i++) {
				JSONObject segObject = segsArray.optJSONObject(i);
				if (segObject != null)
					segs.add(new ItemSeg(segObject.optString("id"),
							null, segObject.optString("seconds"),
							getVideoUrl(segObject.optString("url")),
									segObject.optString("fileid")));
			}
			mVideoUrlInfo.addSegments(segs, format, h265);
		return true;
	}
		return false;
	}

	private boolean parseOther(JSONObject object, String videotpye) {
		JSONArray array = object.optJSONArray(videotpye);
		if (array != null && array.length() > 0) {
			JSONObject videoObject = array.optJSONObject(0);
			if (videoObject != null) {
				mVideoUrlInfo.setUrl(getVideoUrl(videoObject.optString("url")));
				mVideoUrlInfo.setCached(false);
				mVideoUrlInfo.setDurationSecs(videoObject.optInt("seconds"));
				mVideoUrlInfo.fieldId = videoObject.optString("fileid");
			}
		}
		return true;
	}

	// 获取m3u8格式的播放地址。
	private boolean parseM3U8(JSONObject object) {
		if (object.has("m3u8_flv")) {
			JSONArray M3U8FLV = object.optJSONArray("m3u8_flv");
			if (M3U8FLV != null && M3U8FLV.length() > 0) {
				JSONObject M3U8FLVObject = M3U8FLV.optJSONObject(0);
				if (M3U8FLVObject != null) {
					mVideoUrlInfo.setM3u8SD(M3U8FLVObject.optString("url"));
					mVideoUrlInfo.setM3u8SDDuration(M3U8FLVObject
							.optInt("seconds"));
				}
			}
		}
		if (object.has("m3u8_mp4")) {
			JSONArray M3U8MP4 = object.optJSONArray("m3u8_mp4");
			if (M3U8MP4 != null && M3U8MP4.length() > 0) {
				JSONObject M3U8MP4Object = M3U8MP4.optJSONObject(0);
				if (M3U8MP4Object != null) {
					mVideoUrlInfo.setM3u8HD(M3U8MP4Object.optString("url"));
					mVideoUrlInfo.setM3u8HDDuration(M3U8MP4Object
							.optInt("seconds"));
				}
			}
		}
		if (object.has("m3u8_hd")) {
			JSONArray M3U8HD = object.optJSONArray("m3u8_hd");
			if (M3U8HD != null && M3U8HD.length() > 0) {
				JSONObject M3U8HDObject = M3U8HD.optJSONObject(0);
				if (M3U8HDObject != null) {
					mVideoUrlInfo.setM3u8HD2(M3U8HDObject.optString("url"));
					mVideoUrlInfo.setM3u8HD2Duration(M3U8HDObject
							.optInt("seconds"));
				}
			}
		}
		return true;
	}

	private void getPointInfo(JSONObject object) {
        mVideoUrlInfo.setHasHead(false);
        mVideoUrlInfo.setHasTail(false);
        mVideoUrlInfo.getPoints().clear();
        JSONArray pointArray = object.optJSONArray("points");
        if (pointArray != null)
            for (int i = 0; i < pointArray.length(); i++) {
                JSONObject point = pointArray.optJSONObject(i);
                if (point != null) {
                    Point p = new Point();
                    p.start = point.optDouble("start") * 1000;
                    p.type = point.optString("type");
                    if (Profile.HEAD_POINT.equals(p.type)) {
                        mVideoUrlInfo.setHasHead(true);
                        mVideoUrlInfo.setHeadPosition((int) p.start);
                    }
                    if (Profile.TAIL_POINT.equals(p.type)) {
                        mVideoUrlInfo.setHasTail(true);
                        mVideoUrlInfo.setTailPosition((int) p.start);
                    }
                    p.title = point.optString("title");
                    p.desc = point.optString("desc");
                    if (!p.type.equals(Profile.STANDARD_POINT)
							&& !p.type.equals(Profile.CONTENTAD_POINT)) {// 不是广告点。
                        mVideoUrlInfo.getPoints().add(p);
                    } else {
                        mVideoUrlInfo.getAdPoints().add(p);
                    }
                }
            }
    }

	private void getLanguageInfo(JSONObject object) {
		mVideoUrlInfo.getLanguage().clear();
		JSONArray pointArray = object.optJSONArray("audiolang");
		if (pointArray != null)
			for (int i = 0; i < pointArray.length(); i++) {
				JSONObject languageObj = pointArray.optJSONObject(i);
				if (languageObj != null) {
					Language lang = new Language();
					lang.lang = languageObj.optString("lang");
					lang.vid = languageObj.optString("videoid");
					lang.isDisplay = languageObj.optBoolean("isplay");
					lang.langCode = languageObj.optString("langcode");
					mVideoUrlInfo.getLanguage().add(lang);
				}
			}
	}

	/**
	 * 判断给出的视频url地址，如果已被iku加速，则返回302跳转后并本地加速的地址
	 * 
	 * @param url
	 * @return
	 */
	private String getVideoUrl(String url) {
		// jialei
		// if (!com.youku.phone.Youku.isHighEnd
		// || com.youku.phone.Youku.getIkuaccInstance().isAccelerated) {
		// return F.getFinnalUrl(url, exceptionString);
		// } else {
		// return url;
		// }
		return url;
	}
	
	/**
	 * 解析附件信息，如果是字幕类型附件怎保存
	 * 
	 * @param attachmentArray
	 */
	private void parseAttachment(JSONArray attachmentArray) {

		Logger.d(SubtitleManager.TAG, "parseAttachment()");
		
		Map<String, Attachment> attachmentMap = new HashMap<String, Attachment>();
		
		List<Attachment> attachments = new ArrayList<Attachment>();
		
		if (attachmentArray == null) {
			Logger.e(SubtitleManager.TAG, "parseResponse : dataArray == null");
			return;
		}
		
		for (int i = 0; i < attachmentArray.length(); i++) {
			JSONObject object = attachmentArray.optJSONObject(i);
			if (object == null) {
				continue;
			}
			
			String type = PlayerUtil.getJsonValue(object, "type");
			
			if (type.equals("subtitle")) {
				String lang = PlayerUtil.getJsonValue(object, "lang");
				String attrachmentUrl = PlayerUtil.getJsonValue(object, "attrachmenturl");
				
				if (!attachmentMap.containsKey(lang)) {
					attachmentMap.put(lang, new Attachment(lang, attrachmentUrl, type));
				}
			}
		}
		
		if (attachmentMap.size() <= 0)
			return;
		
		if (attachmentMap.containsKey("chs")) {
			attachments.add(attachmentMap.get("chs"));
		} 
		
		if (attachmentMap.containsKey("cht")) {
			attachments.add(attachmentMap.get("cht"));
		} 
		
		if (attachmentMap.containsKey("en")) {
			attachments.add(attachmentMap.get("en"));
		} 
		
		for (Attachment attachment : attachments) {
			Logger.d(SubtitleManager.TAG, "lang = " + attachment.lang + ", " + attachment.attrachmentUrl + ", type = " + attachment.type);
		}
		
		mVideoUrlInfo.setAttachments(attachments);
		
	}

}

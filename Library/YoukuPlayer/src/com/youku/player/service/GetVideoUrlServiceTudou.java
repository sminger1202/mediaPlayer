package com.youku.player.service;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.widget.Toast;

import com.baseproject.utils.Logger;
import com.youku.android.player.R;
import com.youku.player.LogTag;
import com.youku.player.Track;
import com.youku.player.apiservice.ICacheInfo;
import com.youku.player.base.YoukuBasePlayerActivity;
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
import com.youku.player.module.VVPlayInfo;
import com.youku.player.module.VideoCacheInfo;
import com.youku.player.module.VideoUrlInfo;
import com.youku.player.plugin.MediaPlayerDelegate;
import com.youku.player.util.PlayerUtil;
import com.youku.player.util.URLContainer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class GetVideoUrlServiceTudou implements NetService {

	private Context mContext;
	// 回调函数的监听变量
	private IVideoInfoCallBack mListener;
	private VideoUrlInfo mVideoUrlInfo;
	private boolean resetProgress;
	private String format;
	/**
	 *  开始获取视频地址的时间
	 */
	private long mGetPlayListTime = 0;

	// 构造函数
	public GetVideoUrlServiceTudou(Context mContext) {
		this.mContext = mContext;
	};

	/**
	 * @param vid
	 * @param languageCode
	 * @param videostage
	 * @param format
	 * @param mVideoUrlInfo
	 * @param resetProgress
	 * @param local_vid
	 * @param local_time
	 * @param local_history
	 * @param isTudouAlbum
	 * @param mListener
	 */
	public void getVideoUrl(String vid, String languageCode, int videostage,
							int format, VideoUrlInfo mVideoUrlInfo, boolean resetProgress,
							String local_vid, String local_time, String local_history,
							boolean isTudouAlbum, int tudouquality, IVideoInfoCallBack mListener) {
		String url = null;
		this.resetProgress = resetProgress;
		languageCode = URLEncoder(languageCode);
		// 软解时取标清、高清、超清
		if (Profile.FORMAT_FLV_HD == format) {
			this.format = "1,5,7,8";
		} else {
			this.format = "4";
		}

		final String password = mVideoUrlInfo.password;
		final String playlistCode = mVideoUrlInfo.playlistCode;
		final String albumID = mVideoUrlInfo.albumID;

		if (videostage > 0) {// 剧集类
			String mUrlMoreYouku = URLContainer.getMutilVideoPlayUrlTudou(vid,
					password, local_time, videostage, local_vid, languageCode,
					this.format, local_history, playlistCode, albumID);
			/*
			 * URLContainer.getMutilVideoUrlYouku(vid, languageCode, videostage,
			 * this.format);
			 */
			url = mUrlMoreYouku;
		} else {// 单集url

			if (isTudouAlbum)
				url = URLContainer.getMutilVideoPlayUrlTudou(vid, password,
						local_time, videostage, local_vid, languageCode,
						this.format, local_history, playlistCode, albumID);
			else
				url = URLContainer.getOneVideoPlayUrlTudou(vid, password,
						local_time, local_vid, languageCode, this.format,
						local_history, tudouquality, playlistCode, albumID);
			/*
			 * URLContainer.getOneVideoUrlYouku(vid, languageCode, this.format);
			 */
			// url = mUrlOneYouku;
		}
		this.mListener = mListener;
		this.mVideoUrlInfo = mVideoUrlInfo;
		Logger.d(LogTag.TAG_PLAYER, "请求播放地址 GetVideoUrlServiceTudou getVideoUrl:"
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
					setVideoUrlInfo(mVideoUrlInfo);
					sendTrack();
					mListener.onSuccess(mVideoUrlInfo);
					Logger.d(LogTag.TAG_PLAYER, "获取正片信息 成功");
					break;
				case FAIL:
					GoplayException mException = new GoplayException();
					setVideoUrlFailReason(mException);
					mListener.onFailed(mException);
					Logger.d(LogTag.TAG_PLAYER, "获取正片信息 失败");
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
			int response = -1;
			if (VideoInfoReasult.getResponseString() == null) {
				return;
			}
			JSONObject object = new JSONObject(VideoInfoReasult.getResponseString());
			// 目前是优酷Url地址，则只设置优酷相关字段信息
			Logger.d(LogTag.TAG_PLAYER, "解析服务器返回的视频信息 setVideoUrlInfo");

			String status = PlayerUtil.getJsonValue(object, "status");
			mVideoUrlInfo.setStatus(status);
			int code = PlayerUtil.getJsonInit(object, "code", 0);
			mVideoUrlInfo.setCode(code);
			mVideoUrlInfo.setHttpResponseCode(response);
			if (object.has("need_mark")) {
				mVideoUrlInfo.need_mark = object.optBoolean("need_mark");
			}
			if (object.has("vitural_type")) {
				mVideoUrlInfo.vitural_type = object.optString("vitural_type", "");
			}
			String title = object.optString("title_new");			
			if (title != null && title.trim().length() > 0) {
				mVideoUrlInfo.setTitle(title);
			}
			pasreWaterMarkInfo(object);
			// 用来区分视频和音频
			mVideoUrlInfo.setMediaType(object.optString("mediaType"));
			mVideoUrlInfo.setItem_img_16_9(object.optString("item_img_16_9"));
			mVideoUrlInfo.setAlbumTitle(object.optString("album_title"));
			mVideoUrlInfo.setItemSubtitle(object.optString("item_subtitle"));
			mVideoUrlInfo.setChannel_name(object.optString("channel_name"));
			mVideoUrlInfo.setTitle_new_dl(object.optString("title_new_dl"));
			mVideoUrlInfo.setTitle_new_dl_sub(object.optString("title_new_dl_sub"));
			// 土豆albumid 对应youku的showid
			String showid = PlayerUtil.getJsonValue(object, "albumid");
			if (!PlayerUtil.isNull(showid)) {
				mVideoUrlInfo.setShowId(showid);
				// mVideoUrlInfo.setAlbum(true);
			}
			// weburl
			String weburl = PlayerUtil.getJsonValue(object, "item_url");
			if (!PlayerUtil.isNull(weburl))
				mVideoUrlInfo.setWeburl(weburl);
			mVideoUrlInfo.setVideoLanguage(object.optString("lang"));
			mVideoUrlInfo.setLimit(object.optInt("limit"));
			mVideoUrlInfo.setimgurl(object.optString("item_img"));
			// 土豆的itemCode 对应优酷的videoid
			String videoid = PlayerUtil.getJsonValue(object, "itemCode");
			if (!PlayerUtil.isNull(videoid)) {
				mVideoUrlInfo.setVid(videoid);
				mVideoUrlInfo.videoIdPlay = videoid;
			}
			//添加vcode用于分享
			mVideoUrlInfo.setVcode(object.optString("videoid"));
			mVideoUrlInfo.setItemDesc(object.optString("item_desc"));
			String playlistCode = object.optString("playlist_code");
			if (!TextUtils.isEmpty(playlistCode))
				mVideoUrlInfo.playlistCode = playlistCode;
			mVideoUrlInfo.setShow_videostage_title(object
					.optString("show_videostage_title"));
			mVideoUrlInfo.mPayInfo = new PayInfo();
			setPayInfo(mVideoUrlInfo.mPayInfo, object);
			JSONObject vvPlayInfo = object.optJSONObject("vv_play_info");
			if (vvPlayInfo != null) {
				VVPlayInfo playInfo = new VVPlayInfo();
				playInfo.setAlbumID(vvPlayInfo.optString("album_id"));
				playInfo.setItemId(vvPlayInfo.optString("item_id"));
				playInfo.setAlbumItemLength(vvPlayInfo
						.optString("album_item_length"));
				mVideoUrlInfo.setVVPlayInfo(playInfo);
			}

			mVideoUrlInfo.setFeeVideo(object.optBoolean("is_fee_video"));
			mVideoUrlInfo.setFeeView(object.optBoolean("is_fee_view"));

			mVideoUrlInfo.setIsTrailer(object.optBoolean("is_trailer"));
			mVideoUrlInfo.setRegisterNum(object.optString("register_num"));
			mVideoUrlInfo.setLicenseNum(object.optString("license_num"));
			mVideoUrlInfo.setItemPlayTimes(object.optLong("item_playtimes"));
			mVideoUrlInfo.setItemShortDesc(object.optString("item_short_desc"));

			mVideoUrlInfo.setVerticalVideo(object.optInt("vertical_player") == 1);

            mVideoUrlInfo.setExclusiveLogo(object.optInt("exclusive_logo") == 1 ? true : false);


            if( null != object && object.has("paid_info")) {
                JSONObject paidInfo = object.getJSONObject("paid_info");
                if(paidInfo.has("trial")) {
                    JSONObject trial = paidInfo.getJSONObject("trial");
                    mVideoUrlInfo.setTiping(trial.optString("tip_ing"));
                    mVideoUrlInfo.setTipEnd(trial.optString("tip_end"));
                }
            }
			// Duration
			mVideoUrlInfo.setDurationSecs(object.optInt("totalseconds"));
			// point
			int point = PlayerUtil.getJsonInit(object, "point", 0);
			int show_videoseq = PlayerUtil.getJsonInit(object,
					"show_videostage", -1);
			mVideoUrlInfo.setShow_videoseq(show_videoseq);
			if (null != object && object.has("next_video")) {
				JSONObject next = object.getJSONObject("next_video");
				if (next.has("itemCode")) {
					mVideoUrlInfo.setHaveNext(1);
					mVideoUrlInfo.nextVideoId = next.getString("itemCode");
				} else
					mVideoUrlInfo.setHaveNext(0);

			} else {
				mVideoUrlInfo.setHaveNext(0);
			}
//			if (-1 == mVideoUrlInfo.getProgress()) {
//				mVideoUrlInfo.setProgress(0);
//			} else {
//				if (resetProgress) {// point==0 true//其他页 读取本地历史
//					if (point > 0) {
//						mVideoUrlInfo.setProgress(point * 1000);
//					}
//                    if(!MediaPlayerDelegate.mIUserInfo.isLogin()) {
//                        mVideoUrlInfo = MediaPlayerDelegate
//                                .getRecordFromLocal(mVideoUrlInfo);
//                    }
//				}
//			}

            if (resetProgress) {// point==0 true//其他页 读取本地历史
                if (point > 0) {
                    mVideoUrlInfo.setProgress(point);
                } else {//无论登陆与否，都读一下本地的,但初始时设置为0。
                    mVideoUrlInfo.setProgress(0);
                    mVideoUrlInfo = MediaPlayerDelegate
                            .getRecordFromLocal(mVideoUrlInfo);
                }
            }

			// 播放历史还差1分钟的时候重播
			if (mVideoUrlInfo.getDurationMills() - mVideoUrlInfo.getProgress() <= 60000) {
				mVideoUrlInfo.setProgress(0);
			}

			mVideoUrlInfo.setWebViewUrl(object.optString("webviewurl"));
			getPointInfo(object);
			mVideoUrlInfo.setCid(PlayerUtil.getJsonInit(object, "cid", 0));
			// FIXME
			ICacheInfo download = MediaPlayerDelegate.mICacheInfo;
			if (download != null) {
				//在线播放不走缓存
				if (false/*download.isDownloadFinished(videoid)*/) {
					mVideoUrlInfo.setCached(true);
					VideoCacheInfo downloadInfo = download
							.getDownloadInfo(mVideoUrlInfo.getVid());

					if (YoukuBasePlayerActivity.isHighEnd) {
						mVideoUrlInfo.cachePath = PlayerUtil
								.getM3u8File(downloadInfo.savePath
										+ "youku.m3u8");
					} else {
						mVideoUrlInfo.cachePath = downloadInfo.savePath
								+ "1.3gp";
					}
					mVideoUrlInfo.setCurrentVideoQuality(downloadInfo.quality);
					mVideoUrlInfo.setItem_img_16_9(downloadInfo.picUrl);
					mVideoUrlInfo.setEpisodemode(downloadInfo.episodemode);
					mVideoUrlInfo.setMediaType(downloadInfo.mMediaType);
					mVideoUrlInfo.setRegisterNum(downloadInfo.registerNum);
					mVideoUrlInfo.setLicenseNum(downloadInfo.licenseNum);
					mVideoUrlInfo.setVerticalVideo(downloadInfo.isVerticalVideo);
                    mVideoUrlInfo.setExclusiveLogo(downloadInfo.exclusiveLogo);
				} else {
					getLanguageInfo(object);
					parseVideoInfo(object);

				}
			} else {
				getLanguageInfo(object);
				parseVideoInfo(object);
			}

		} catch (JSONException e) {
			Logger.e(LogTag.TAG_PLAYER, "解析服务器返回的视频信息 setVideoUrlInfo 出错");
			Logger.e(LogTag.TAG_PLAYER, e);
		}
	}

	private void setPayInfo(PayInfo payInfo, JSONObject object) {
		JSONObject pay_info = object.optJSONObject("paid_info");
		if (pay_info != null) {
			payInfo.duration = pay_info.optString("duration");
			payInfo.oriprice = pay_info.optString("oriprice");
			payInfo.coprice = pay_info.optString("coprice");
			payInfo.play = pay_info.optBoolean("play");
			payInfo.showid = pay_info.optString("showid");
			JSONArray paytype = pay_info.optJSONArray("paid_type");
			payInfo.payType = new ArrayList<String>();
			if (paytype != null)
				for (int i = 0; i < paytype.length(); i++) {
					payInfo.payType.add(paytype.optString(i));
				}
			payInfo.paid_url = pay_info.optString("paid_url");
			payInfo.paid = pay_info.optInt("paid");
			payInfo.show_paid = pay_info.optInt("show_paid");
			JSONObject trial_info = pay_info.optJSONObject("trial");
			if (trial_info != null) {
				payInfo.trail = new Trial();
				payInfo.trail.episodes = trial_info.optInt("episodes");
				payInfo.trail.time = trial_info.optInt("time");
				payInfo.trail.type = trial_info.optString("type");
				payInfo.trail.tip = trial_info.optString("tip");
			}
		}
	}

	private void parseVideoInfo(JSONObject object) throws JSONException {
		if (object.has("results")) {
			if (object.getString("type") != null
					&& object.getString("type").equals("youku")) {
				mVideoUrlInfo.setType(VideoUrlInfo.YOUKU_TYPE);
				JSONObject sidData = object.getJSONObject("sid_data");
				String token = sidData.getString("token");
				String oip = sidData.getString("oip");
				String sid = sidData.getString("sid");
				mVideoUrlInfo.token = token;
				mVideoUrlInfo.oip = oip;
				mVideoUrlInfo.sid = sid;
				// 播放优酷视频时没有合适的播放地址进行提示
				if (!parseYoukuVideoInfo(object.getJSONObject("results")))
					Toast.makeText(mContext,
							R.string.player_error_url_is_nul_tudou,
							Toast.LENGTH_SHORT).show();
			} else {
				mVideoUrlInfo.setType(VideoUrlInfo.TUDOU_TYPE);
				if (YoukuBasePlayerActivity.isHighEnd) {
					parseTudouVideoInfo(object.getJSONObject("results"));
				} else
					Toast.makeText(mContext,
							R.string.player_error_url_is_nul_tudou,
							Toast.LENGTH_SHORT).show();

			}

		}
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
			JSONObject object = null;
			if (VideoInfoReasult.getResponseString() != null) {
				object = new JSONObject(
						VideoInfoReasult.getResponseString());
				code = PlayerUtil.getJsonInit(object, "error_code", 0);
				if (code == 0)
					code = PlayerUtil.getJsonInit(object, "code", 0);
				mVideoUrlInfo.setCode(code);
				mVideoUrlInfo.setWebViewUrl(object.optString("web_view_url"));
				mResult.itemCode = object.optString("itemCode");
				mResult.setErrorCode(code);
				mVideoUrlInfo.setVipError(object.optInt("vip_error"));
				mVideoUrlInfo.setVid(object.optString("item_code"));
				mVideoUrlInfo.setTip(object.optString("tip"));
				mVideoUrlInfo.setTiping(object.optString("tip_ing"));
				mVideoUrlInfo.setTipEnd(object.optString("tip_end"));

				mVideoUrlInfo.mPayInfo = new PayInfo();
				setPayInfo(mVideoUrlInfo.mPayInfo, object);
				mResult.setVideoUrlInfo(mVideoUrlInfo);
				if (code == -104) {
					mResult.webUrl = object.optString("web_view_url");
				}
			}

			if (mVideoUrlInfo.isCached()) {
				mResult.setErrorInfo(mContext.getText(
						R.string.player_error_native).toString());
			} else {
				switch (code) {
					case -101:
						mResult.setErrorInfo(mContext.getText(
								R.string.player_error_f101).toString());
						break;
					case -102:
						mResult.setErrorInfo(mContext.getText(
								R.string.player_error_f102).toString());
						break;
					case -104:
						mResult.setErrorInfo(object.optString("note"));
						break;
					case -105:
						// TODO:土豆加密视频的播放需要等待服务器端接口调试，此时暂时提示用户去看其他视频
						mResult.setErrorInfo(mContext.getText(
								R.string.player_error_f105).toString());
						break;
					case -106:
						if (object != null && object.optInt("vip_error") == -306)
							mResult.setErrorInfo(object.optString("note"));
						else
							mResult.setErrorInfo(mContext.getText(
									R.string.player_error_f106).toString());
						break;
					case -107:
						mResult.setErrorInfo(mContext.getText(
								R.string.player_error_f107).toString());
						break;
					case -202:
						mResult.setErrorInfo(mContext.getText(
								R.string.player_error_url_is_nul).toString());
						break;
					case -100:
						mResult.setErrorInfo(mContext.getText(
								R.string.Player_error_f100).toString());
						break;
					case -112:
						if (mVideoUrlInfo.getVipError() == -301 || mVideoUrlInfo.getVipError() == -307) {
							mResult.setShowTip(false);
						} else
							mResult.setErrorInfo(mContext.getText(
									R.string.player_error_no_pay).toString());
						break;
					default:
						mResult.setErrorInfo(mContext.getText(
								R.string.Player_error_timeout).toString());
						break;
				}
			}
		} catch (JSONException e) {
			// 在非登录的cmcc网络下返回html页面导致json解析失败，使用网络错误提示
			mResult.setErrorInfo(mContext.getText(
					R.string.Player_error_timeout).toString());
			Logger.e(LogTag.TAG_PLAYER, e);
		}
	}

	private boolean parseYoukuVideoInfo(JSONObject object) {
		if (Profile.USE_SYSTEM_PLAYER) {
			return parseM3U8(object);
		}
		if (Profile.getVideoFormatName().equals("flv_hd")
				|| Profile.getVideoFormatName().equals("mp4")
				|| Profile.getVideoFormatName().equals("hd2")) {
			boolean parseFLV = parseSeg(object, "flvhd", Profile.FORMAT_FLV_HD);
			boolean parseMP4 = parseSeg(object, "mp4", Profile.FORMAT_MP4);
			boolean parseHD2 = parseSeg(object, "hd2", Profile.FORMAT_HD2);
			boolean parseHD3 = parseSeg(object, "hd3", Profile.FORMAT_HD3);
			parseM3U8(object);
			return parseFLV || parseMP4 || parseHD2 || parseHD3;
		} else if (Profile.getVideoFormatName().equals("m3u8")) {
			return parseM3U8(object);
		} else
			return parseOther(object, Profile.getVideoFormatName());
	}

	private boolean parseTudouVideoInfo(JSONObject object) {
		if (Profile.getVideoFormatName().equals("flv_hd")
				|| Profile.getVideoFormatName().equals("mp4")
				|| Profile.getVideoFormatName().equals("hd2")) {
			boolean parseF4V256P = parseSeg(object,"f4v_2",Profile.FORMAT_TUDOU_STANDARD);
			boolean parseF4V480P = parseSeg(object, "f4v_4", Profile.FORMAT_TUDOU_SUPER);
			boolean parseF4V360P = parseSeg(object,"f4v_3",Profile.FORMAT_TUDOU_HIGH);

			boolean parseF4V720P = false;
			if (!mVideoUrlInfo.hasM3u8HD2()) {// 480，720都是超清,我们只解析其中
				parseF4V720P = parseSeg(object,"f4v_5",Profile.FORMAT_TUDOU_SUPER);
			}

			boolean parseF4VORIGINAL = parseSeg(object, "f4v_99", Profile.FORMAT_TUDOU_1080P);
			boolean parseF4V3GP = parseSeg(object, "3gp", Profile.FORMAT_TUDOU_STANDARD);
			boolean parseF4VMP4 = parseSeg(object,"mp4",Profile.FORMAT_TUDOU_HIGH);
			boolean parseF4VFLV = parseSeg(object,"flv",Profile.FORMAT_TUDOU_STANDARD);
			return parseF4V3GP || parseF4VMP4 || parseF4VFLV || parseF4V256P
					|| parseF4V360P || parseF4V480P || parseF4V720P
					|| parseF4VORIGINAL;
		} else if (Profile.getVideoFormatName().equals("m3u8")) {
			return parseM3U8(object);
		} else {
			Toast.makeText(mContext, R.string.player_error_url_is_nul_tudou,
					Toast.LENGTH_SHORT).show();
			return parseOther(object, Profile.getVideoFormatName());
		}

	}

	private boolean parseSeg(JSONObject object, String type, int format) {
		JSONObject segsobject = object.optJSONObject(type);
		if (segsobject == null)
			return false;
		JSONArray segsArray = segsobject.optJSONArray("segs");
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
			mVideoUrlInfo.addSegments(segs, format, false);
		return true;
	}
			return false;
			}

	private boolean parseOther(JSONObject object, String videotpye) {
		JSONObject otherObject = object.optJSONObject(videotpye);
		if (otherObject == null)
			return false;
		JSONArray array = otherObject.optJSONArray("segs");
		if (array != null && array.length() > 0) {
			JSONObject videoObject = array.optJSONObject(0);
			if (videoObject != null) {
				mVideoUrlInfo.setUrl(videoObject.optString("url"));
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
                    lang.id = languageObj.optInt("langid");
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

	private void pasreWaterMarkInfo(JSONObject object) {
		try {
			int isWaterMark[] = new int[5];
			int waterMarkType[] = new int[5];
			if (object.has("streamlogos")) {
				JSONObject streamlogos = object.getJSONObject("streamlogos");
				if (streamlogos != null) {
					if (streamlogos.has("hd2")) {
						JSONObject hd2Info = streamlogos.getJSONObject("hd2");
						if (hd2Info != null) {
							isWaterMark[0] = PlayerUtil.getJsonInit(hd2Info,
									"logo", 0);
							waterMarkType[0] = PlayerUtil.getJsonInit(hd2Info,
									"type", 0);
						}
					}

					if (streamlogos.has("mp4")) {
						JSONObject mp4Info = streamlogos.getJSONObject("mp4");
						if (mp4Info != null) {
							isWaterMark[1] = PlayerUtil.getJsonInit(mp4Info,
									"logo", 0);
							waterMarkType[1] = PlayerUtil.getJsonInit(mp4Info,
									"type", 0);
						}
					}

					if (streamlogos.has("flvhd")) {
						JSONObject flvhdInfo = streamlogos
								.getJSONObject("flvhd");
						if (flvhdInfo != null) {
							isWaterMark[3] = PlayerUtil.getJsonInit(flvhdInfo,
									"logo", 0);
							waterMarkType[3] = PlayerUtil.getJsonInit(
									flvhdInfo, "type", 0);
						}
					}

					if (streamlogos.has("flv")) {
						JSONObject flvInfo = streamlogos.getJSONObject("flv");
						if (flvInfo != null) {
							isWaterMark[2] = PlayerUtil.getJsonInit(flvInfo,
									"logo", 0) + isWaterMark[3];
							waterMarkType[2] = PlayerUtil.getJsonInit(flvInfo,
									"type", 0);
						}
					}

					if (streamlogos.has("hd3")) {
						JSONObject hd3Info = streamlogos.getJSONObject("hd3");
						if (hd3Info != null) {
							isWaterMark[4] = PlayerUtil.getJsonInit(hd3Info,
									"logo", 0);
							waterMarkType[4] = PlayerUtil.getJsonInit(hd3Info,
									"type", 0);
						}
					}

					for (int i = 0; i < 5; i++) {
						mVideoUrlInfo.isWaterMark[i] = isWaterMark[i];
						mVideoUrlInfo.waterMarkType[i] = waterMarkType[i];
					}
				}
			}
		} catch (JSONException e) {
		}
	}
}

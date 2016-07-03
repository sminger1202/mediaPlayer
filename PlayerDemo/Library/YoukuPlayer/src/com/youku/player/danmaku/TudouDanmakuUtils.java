package com.youku.player.danmaku;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import com.baseproject.utils.Logger;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.youku.android.player.R;
import com.youku.player.LogTag;
import com.youku.player.base.YoukuPlayerView;
import com.youku.player.config.MediaPlayerConfiguration;
import com.youku.player.goplay.Profile;
import com.youku.player.plugin.MediaPlayerDelegate;
import com.youku.player.util.DensityUtil;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.loader.ILoader;
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.LiveDanmakuInfo;
import master.flame.danmaku.danmaku.model.VerticalImageSpan;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.parser.android.TudouDanmakuParser;

public class TudouDanmakuUtils implements DanmakuUtils {
    private int textSize = 25;
	private DanmakuContext danmakuContext;
	private Drawable defaultDrawable;
	public Handler starHandler;
	private Looper starLooper;
	private Context mContext;
	private BaseDanmakuParser mParser;
	public HashMap<String, Drawable> imgUrlHashMap = new HashMap<String, Drawable>();

	public TudouDanmakuUtils() {
		HandlerThread thread = new HandlerThread("starHandler",
				Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();
		starLooper = thread.getLooper();
		starHandler = new Handler(starLooper);
	}

	@Override
	public String getFakeJSONArray() {
		return "{ \"count\": 1,\"msg\": \"success\",\"result\": [{\"playat\": -1,\"content\": \"ceshi\", \"propertis\": \"\"}]}";
	}

	@Override
	public ILoader createDanmakuLoader() {
		ILoader loader = DanmakuLoaderFactory
				.create(DanmakuLoaderFactory.TAG_TUDOU);
		return loader;
	}

	@Override
	public BaseDanmakuParser createDanmakuParser() {
		return new TudouDanmakuParser();
	}

	@Override
	public void addDanmaku(JSONObject jsonObject, IDanmakuView danmakuView,
			BaseDanmakuParser parser, long currMillisecond, ArrayList<LiveDanmakuInfo> liveDanmakuInfos) {
		BaseDanmaku item = null;
		Danmakus danmakus = null;
		if (jsonObject.has("result")) {
			danmakus = parser.doParse(jsonObject.optJSONArray("result"));
		}
		Collection<BaseDanmaku> items = danmakus.items;
		Iterator<BaseDanmaku> itr = items.iterator();
		while (itr.hasNext()) {
			item = itr.next();
			if (item.isStar) {
				item.isStarAdded = true;
				danmakuView.addDanmaku(item);
				starHandler.post(new starUrlRunnable(item, danmakuView));
			} else {
				danmakuView.addDanmaku(item);
			}
		}
	}

	private class starUrlRunnable implements Runnable {
		BaseDanmaku item;
		IDanmakuView danmakuView;
		public starUrlRunnable(BaseDanmaku item, IDanmakuView danmakuView){
			this.item = item;
			this.danmakuView = danmakuView;
		}

		@Override
		public void run() {
			if (!imgUrlHashMap.containsKey(item.starUrl)) {
				loadImage(item, danmakuView);
			} else {
				item.text = createSpannable(imgUrlHashMap.get(item.starUrl), item.starName, item.content, item.textColor);
				danmakuView.invalidateDanmaku(item, false);
			}
		}
	}

	private void loadImage(final BaseDanmaku item, final IDanmakuView danmakuView) {
		InputStream inputStream = null;
		Bitmap bitmap = null;
		Drawable drawable = null;
		int drawblelength =  DensityUtil.dip2px(mContext, 32);

		try {
			URLConnection urlConnection = new URL(item.starUrl).openConnection();
			urlConnection.setConnectTimeout(5000);
			urlConnection.setReadTimeout(5000);
			urlConnection.setDoInput(true);
			inputStream = urlConnection.getInputStream();
			bitmap = BitmapFactory.decodeStream(inputStream);
			Logger.d("star", "onLoadingComplete:" + item.starUrl + ",drawable:" + (bitmap == null));
		} catch (Exception e) {
			Logger.d("star", "onLoadingFailed:" + item.starUrl + ",drawable:" + (bitmap == null));
			e.printStackTrace();
		} finally {
			try {
				if (inputStream != null)
					inputStream.close();
			} catch (IOException ignore) {
			}
		}

		if (bitmap == null) {
			drawable = defaultDrawable;
		} else {
			drawable = new CircleImageDrawable(bitmap, drawblelength);
		}
		item.text = createSpannable(drawable, item.starName, item.content, item.textColor);
		danmakuView.invalidateDanmaku(item, false);
		if(drawable != defaultDrawable) {
			imgUrlHashMap.put(item.starUrl, drawable);
		}
	}

	private Drawable bitMapToDrawble(Bitmap loadedImage) {
		Drawable d = null;
		if(mContext != null) {
			d = new BitmapDrawable(mContext.getResources(), loadedImage);
		}
		if(d == null) {
			d = defaultDrawable;
		}
		return  d;
	}

	private SpannableStringBuilder createSpannable(Drawable drawable, String starName, String content ,int color) {
		if(drawable instanceof CircleImageDrawable) {
			drawable.setBounds(0, 0, ((CircleImageDrawable)drawable).mWidth, ((CircleImageDrawable)drawable).mWidth);
		} else {
			int drawblelength = DensityUtil.dip2px(mContext, 32);
			drawable.setBounds(0, 0, drawblelength, drawblelength);
		}
		String text = "bitmap";
		SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);
		VerticalImageSpan span = new VerticalImageSpan(drawable);
		spannableStringBuilder.setSpan(span, 0, text.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
		spannableStringBuilder.append(starName);
		spannableStringBuilder.setSpan(new ForegroundColorSpan(Color.parseColor("#FF612A")), 0, spannableStringBuilder.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		int length = spannableStringBuilder.length();
		spannableStringBuilder.append(content);
		spannableStringBuilder.setSpan(new ForegroundColorSpan(color), length, spannableStringBuilder.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		return spannableStringBuilder;
	}

	@Override
	public long getCurrentMillisecond(BaseDanmakuParser parser,
			long currMillisecond) {
		return parser.getTimer().currMillisecond;
	}

	@Override
	public void openDanmaku(Context ctx, final DanmakuManager danmakuManager,
			MediaPlayerDelegate mediaPlayerDelegate, String currentVid,
			int currentGolbalPosition) {
        if (danmakuManager == null || ctx == null) {
            return;
        }
		if (Profile.getDanmakuSwith(ctx)) {
            danmakuManager.setDanmakuPreferences(false, "danmakuSwith");
			if (!danmakuManager.isFirstOpen) {
				Logger.d(LogTag.TAG_DANMAKU, "开启弹幕");
                if (!danmakuManager.isPaused) {
                    danmakuManager.showDanmaku();
                }
                return;
			}
			Logger.d(LogTag.TAG_DANMAKU, "第一次开启弹幕");
            danmakuManager.isFirstOpen = false;
			if (mediaPlayerDelegate != null
					&& !mediaPlayerDelegate.videoInfo.isCached()) {
                danmakuManager.handleDanmakuInfo(currentVid,
						currentGolbalPosition, 1);
			}
		}
	}

	@Override
	public void closeDanmaku(Context ctx, final IDanmakuManager danmakuManager) {
        if (danmakuManager == null || ctx == null) {
            return;
        }
		if (!Profile.getDanmakuSwith(ctx)) {
            danmakuManager.setDanmakuPreferences(true, "danmakuSwith");
            danmakuManager.hideDanmaku();
		}
	}

	@Override
	public void showDanmaku(YoukuPlayerView youkuPlayerView,
                            DanmakuManager danmakuManager) {
		if (danmakuManager == null) {
			return;
		}
		if (danmakuManager.danmakuProcessStatus > DanmakuManager.DANMAKUOPEN
				|| danmakuManager.isDanmakuNoData) {
			if (youkuPlayerView != null) {
				if ((!danmakuManager.isDanmakuShow && !danmakuManager.isDanmakuHide)
						|| (!danmakuManager.isDanmakuShow && danmakuManager.isDanmakuHide)) {
					Logger.d(LogTag.TAG_DANMAKU, "展示弹幕");
					youkuPlayerView.showDanmaku();
					danmakuManager.isDanmakuShow = true;
					danmakuManager.isDanmakuHide = false;
				}
			}
		}

	}

	@Override
	public void hideDanmaku(YoukuPlayerView youkuPlayerView,
                            DanmakuManager danmakuManager) {
		if (danmakuManager == null) {
			return;
		}
		if (danmakuManager.danmakuProcessStatus > DanmakuManager.DANMAKUOPEN
				|| danmakuManager.isDanmakuNoData) {
			if (youkuPlayerView != null) {
				if ((!danmakuManager.isDanmakuShow && !danmakuManager.isDanmakuHide)
						|| (danmakuManager.isDanmakuShow && !danmakuManager.isDanmakuHide)) {
					Logger.d(LogTag.TAG_DANMAKU, "隐藏弹幕");
					youkuPlayerView.hideDanmaku();
					danmakuManager.isDanmakuShow = false;
					danmakuManager.isDanmakuHide = true;
				}
			}
		}

	}

	@Override
	public void releaseDanmaku(YoukuPlayerView youkuPlayerView,
			MediaPlayerDelegate mediaPlayerDelegate) {
		if (youkuPlayerView != null) {
			Logger.d(LogTag.TAG_DANMAKU, "释放弹幕");
			youkuPlayerView.releaseDanmaku();
		}
	}

	@Override
	public void addDanmaku(String json, YoukuPlayerView youkuPlayerView,
                           DanmakuManager danmakuManager, ArrayList<LiveDanmakuInfo> liveDanmakuInfos) {
		if (danmakuManager == null) {
			return;
		}
		if ((danmakuManager.danmakuProcessStatus & DanmakuManager.DANMAKUOPEN) != 0) {
			if (youkuPlayerView != null) {
				youkuPlayerView.addDanmaku(json, liveDanmakuInfos);
			}
		}
	}

	@Override
	public void showDanmakuWhenRotate(Context ctx, DanmakuManager danmakuManager) {
		if (danmakuManager == null || ctx == null) {
			return;
		}
		if (!Profile.getDanmakuSwith(ctx) && !danmakuManager.isPaused) {
			danmakuManager.showDanmaku();
		}
	}

	@Override
	public void hideDanmakuWhenRotate(Context ctx, DanmakuManager danmakuManager) {
		if (danmakuManager == null || ctx == null) {
			return;
		}
        if (MediaPlayerConfiguration.getInstance().showTudouPadDanmaku()) {
            danmakuManager.showDanmaku();
            return;
        }
        if (!Profile.getDanmakuSwith(ctx) && !danmakuManager.isPaused) {
			danmakuManager.hideDanmaku();
		}
	}

	@Override
	public void beginDanmaku(String jsonArray, int beginTime,
                             DanmakuManager danmakuManager, YoukuPlayerView youkuPlayerView) {
		if (danmakuManager == null) {
			return;
		}
		if ((danmakuManager.danmakuProcessStatus & DanmakuManager.DANMAKUOPEN) != 0) {
			if (youkuPlayerView != null) {
				danmakuManager.beginTime = beginTime;
				youkuPlayerView.beginDanmaku(jsonArray, beginTime);
			}
		}
	}

	@Override
	public void sendDanmaku(int size, int position, int color, String content,
			MediaPlayerDelegate mediaPlayerDelegate,
			YoukuPlayerView youkuPlayerView, Context ctx, DanmakuManager danmakuManager) {
		if (danmakuManager == null || danmakuManager.isDanmakuClosed()) {
			return;
		}
		if (youkuPlayerView != null && mediaPlayerDelegate != null) {
			BaseDanmaku baseDanmaku = null;
			if (danmakuManager.starUids != null && danmakuManager.starUids.contains(Profile.getPreferences("danmuUserid", mContext))) {
				CharSequence text = createSpannable(defaultDrawable, Profile.getPreferences("danmuNickName", mContext), content, color);
				baseDanmaku = youkuPlayerView.sendDanmaku(size,
						position, color, text);
				baseDanmaku.starUrl = Profile.getPreferences("danmuUrl", mContext);
				baseDanmaku.starName = Profile.getPreferences("danmuNickName", mContext);
				baseDanmaku.content = content;
				starHandler.post(new starUrlRunnable(baseDanmaku, youkuPlayerView.getDanmakuSurfaceView()));
				Logger.d(LogTag.TAG_DANMAKU, "发送明星弹幕");
			} else {
				baseDanmaku = youkuPlayerView.sendDanmaku(size,
						position, color, content);
				Logger.d(LogTag.TAG_DANMAKU, "发送普通弹幕");
			}
			if (danmakuManager.isUserShutUp) {
				Logger.d(LogTag.TAG_DANMAKU, "用户禁言，禁止发送弹幕");
				return;
			}
			if (baseDanmaku != null) {
				String propertis = "{\"pos\":"
						+ Profile.getDanmakuPosition(position)
						+ ",\"alpha\":1,\"size\":"
						+ Profile.getDanmakuTextSize(size)
						+ ",\"effect\":0,\"color\":"
						+ Profile.getUnsignedInt(color) + "}";
				content = Profile.replaceSpaceWithPlus(content);
				mediaPlayerDelegate.submitDanmaku("1", danmakuManager.currentVid, ""
						+ baseDanmaku.time, propertis, content);
			}
		}
	}

	@Override
	public void resetAndReleaseDanmakuInfo(final IDanmakuManager danmakuManager, Boolean isHLS) {
		if (danmakuManager == null) {
			return;
		}
		danmakuManager.hideDanmaku();
		danmakuManager.resetDanmakuInfo();
		starHandler.removeCallbacksAndMessages(null);
		imgUrlHashMap.clear();
		if (DanmakuManager.danmakuHandler != null) {
			DanmakuManager.danmakuHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					danmakuManager.releaseDanmaku();
				}
			}, 100);
		}
	}

    @Override
    public int getDanmakuSendColor(int color) {
        return 0xFFFF9900;
    }

    @Override
    public void setTextSize(BaseDanmakuParser parser, Context ctx) {
        if (parser == null || ctx == null) {
            return;
        }
		mParser = parser;
		mContext = ctx;
        if (MediaPlayerConfiguration.getInstance().showTudouPadDanmaku()) {
            textSize = ctx.getResources().getDimensionPixelSize(R.dimen.danmaku_text_size);
            parser.setTextSize(textSize);
        } else {
            parser.setTextSize(25);
            textSize = 25;
        }
    }

    @Override
    public int getTextSize() {
        return textSize;
    }

	@Override
	public void setDanmakuContextAndDrawable(DanmakuContext danmakuContext, Drawable drawable) {
		this.danmakuContext = danmakuContext;
		this.defaultDrawable = drawable;
		mParser.setDefaultDrawale(drawable, mContext);
	}

	@Override
	public void requestStarImage(BaseDanmaku item, IDanmakuView danmakuView) {
		starHandler.post(new starUrlRunnable(item, danmakuView));
	}

	public void setDanmakuTextScale(boolean isFullScreenPlay, DanmakuManager danmakuManager) {
        if (danmakuManager == null || !MediaPlayerConfiguration.getInstance().showTudouPadDanmaku()) {
            return;
        }
        if (isFullScreenPlay) {
            if ((!danmakuManager.isFullScreenDanmaku && !danmakuManager.isSmallScreenDanmaku)
                    || (danmakuManager.isFullScreenDanmaku && !danmakuManager.isSmallScreenDanmaku)) {
				danmakuContext.setScaleTextSize(1.0F);
                danmakuManager.isFullScreenDanmaku = false;
                danmakuManager.isSmallScreenDanmaku = true;
                Logger.d(LogTag.TAG_DANMAKU, "设置字体为1.0F");
            }
        } else {
            if ((!danmakuManager.isFullScreenDanmaku && !danmakuManager.isSmallScreenDanmaku)
                    || (!danmakuManager.isFullScreenDanmaku && danmakuManager.isSmallScreenDanmaku)) {
                danmakuContext.setScaleTextSize(0.7F);
                danmakuManager.isFullScreenDanmaku = true;
                danmakuManager.isSmallScreenDanmaku = false;
                Logger.d(LogTag.TAG_DANMAKU, "设置字体为0.7F");
            }
        }
    }
}

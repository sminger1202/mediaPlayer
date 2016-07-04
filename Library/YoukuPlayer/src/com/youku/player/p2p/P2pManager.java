package com.youku.player.p2p;

import android.content.Context;
import android.text.TextUtils;

import com.baseproject.utils.Logger;
import com.baseproject.utils.Util;
import com.youku.analytics.data.Device;
import com.youku.player.LogTag;
import com.youku.player.Track;
import com.youku.player.config.MediaPlayerConfiguration;
import com.youku.player.goplay.ItemSeg;
import com.youku.player.plugin.MediaPlayerDelegate;
import com.youku.player.util.PlayerUtil;
import com.youku.service.acc.AcceleraterManager;
import com.youku.service.acc.AcceleraterServiceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class P2pManager {
	private static P2pManager instance;
	private AcceleraterManager acceleraterManager;

	public static final String P2P_NOT_ENABLE_ERROR = "0-优先方式为cdn";
	public static final String P2P_RETRY_ERROR = "1-重试次数超过限定";
	public static final String P2P_NOT_AVAILABLE_ERROR = "2-未启动";
	public static final String P2P_NOT_SUPPORT = "3-cpu未满足软解要求";
	public static final String P2P_SWITCH_OFF = "4-P2P播放开关关闭";

	private ExecutorService service = Executors.newCachedThreadPool();

	private boolean isUsingP2P = false;
	public int mRetryTimes;
	public String mLastPlayVid;

	public synchronized static P2pManager getInstance() {
		if (instance == null) {
			instance = new P2pManager(com.baseproject.utils.Profile.mContext);
		}
		return instance;
	}

	private P2pManager(Context context) {
		acceleraterManager = AcceleraterManager.getInstance(context);
		acceleraterManager.bindService();
	}

	public String getAccPort() {
		int port = acceleraterManager.getHttpProxyPort();
		if (port != -1)
			return port + "";
		else
			return "";
	}

	public boolean canUseAcc() {
		Logger.d(LogTag.TAG_PLAYER, "canUseAcc()");

		if (acceleraterManager.canPlayWithP2P() && getAccPort().length() != 0
				&& Util.hasInternet() && Util.isWifi()) {
			Logger.d(LogTag.TAG_PLAYER, "can play with p2p!");
			return true;
		}

		Logger.d(LogTag.TAG_PLAYER, "cann't play with p2p!");
		return false;
	}

	public void reset(String vid) {
		if (mLastPlayVid == null || (vid != null && !vid.equals(mLastPlayVid))) {
			Logger.d(LogTag.TAG_PLAYER, "p2pmanager reset");
			mLastPlayVid = vid;
			mRetryTimes = 0;
		}
	}

	public boolean isUsingP2P() {
		return isUsingP2P;
	}

	public void setUsingP2P(boolean useP2P) {
		isUsingP2P = useP2P;
	}

	/**
	 * 获取p2p播放地址，会阻塞调用的线程
	 * 
	 * @param vSeg
	 * @param p2pUrls
	 * @param token
	 * @param oip
	 * @param sid
	 */
	public void getP2PUrls(final ArrayList<ItemSeg> vSeg,
			final HashMap<String, String> p2pUrls, final String token,
			final String oip, final String sid) {
		if (vSeg == null || vSeg.isEmpty())
			return;
		final AtomicInteger atomicInteger = new AtomicInteger(0);
		final CountDownLatch countDownLatch = new CountDownLatch(vSeg.size());
		int size = vSeg.size() > 8 ? 8 : vSeg.size();
		for (int i = 0; i < size && atomicInteger.get() < vSeg.size(); i++) {
			service.execute(new Runnable() {

				@Override
				public void run() {
					getP2PUrl(countDownLatch, atomicInteger, vSeg, p2pUrls,
							token, oip, sid);
				}
			});
		}
		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void getP2PUrl(CountDownLatch countDownLatch,
			AtomicInteger atomicInteger, ArrayList<ItemSeg> vSeg,
			HashMap<String, String> p2pUrls, String token, String oip,
			String sid) {
		int i = 0;
		if ((i = atomicInteger.getAndIncrement()) >= vSeg.size()) {
			return;
		}
		ItemSeg item = vSeg.get(i);
		String p2pUrl = p2pUrls.get(item.get_Url());
		if (TextUtils.isEmpty(p2pUrl)) {
			String encyptUrl = MediaPlayerConfiguration.getInstance().mPlantformController
					.getEncreptUrl(item.get_Url(), item.getFieldId(), token,
							oip, sid, MediaPlayerDelegate.is, Device.gdid);
			p2pUrl = PlayerUtil.getFinnalUrl(encyptUrl + getAccPort());
			if (!TextUtils.isEmpty(p2pUrl)) {
				p2pUrl += "?ua=mp&st=vod";
				p2pUrls.put(item.get_Url(), p2pUrl);
				Logger.d(LogTag.TAG_PLAYER, "getP2P url:" + p2pUrl);
			} else
				Logger.e(LogTag.TAG_PLAYER, "getP2P url failed");
		} else {
			Logger.d(LogTag.TAG_PLAYER, "getP2P url from map");
		}
		countDownLatch.countDown();
		if (atomicInteger.get() < vSeg.size())
			getP2PUrl(countDownLatch, atomicInteger, vSeg, p2pUrls, token, oip,
					sid);
	}

	/**
	 * 检查是否可以使用P2P播放
	 * 
	 * @return
	 */
	public boolean checkPlayP2P(String videoId) {
		String p2pError = null;
		if (MediaPlayerConfiguration.getInstance().useP2P()) {
			if (canUseAcc()) {
				if (mRetryTimes <= MediaPlayerConfiguration.getInstance()
						.getP2PRetryTimes()) {
					setUsingP2P(true);
					Track.setP2P(true);
					return true;
				} else {
					p2pError = P2pManager.P2P_RETRY_ERROR;
				}
			} else {
				if (!acceleraterManager.getPlaySwitch())
					p2pError = P2P_SWITCH_OFF;
				else
					p2pError = P2P_NOT_AVAILABLE_ERROR;
			}
		} else {
			p2pError = P2pManager.P2P_NOT_ENABLE_ERROR;
		}
		setUsingP2P(false);
		Track.trackP2PError(videoId, p2pError);
		return false;
	}

	public String getP2PVersion() {
		String versionName = AcceleraterServiceManager.getAccVersionName();
		if (TextUtils.isEmpty(versionName)) {
			versionName = "0.0.0.0";
		}
		return versionName;
	}

}

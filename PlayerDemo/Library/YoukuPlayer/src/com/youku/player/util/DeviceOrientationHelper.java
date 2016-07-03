package com.youku.player.util;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;
import android.provider.Settings;
import android.util.Log;
import android.view.OrientationEventListener;

import com.baseproject.utils.Logger;
import com.youku.player.LogTag;
import com.youku.player.config.MediaPlayerConfiguration;

/**
 * 播放器横竖屏翻转 传感器 代替系统sensor
 * 
 * @author hail_lelouch
 * 
 */
public class DeviceOrientationHelper extends OrientationEventListener {

	private OrientationChangeCallback callback;
	private DeviceOrientation localOrientation = DeviceOrientation.UNKONWN;

	private static final String TAG = LogTag.TAG_PREFIX + DeviceOrientation.class.getSimpleName();

	// 判断是否为用户点击放大缩小按钮 竖屏点击放大时判断
	public boolean fromUser = false;

	private boolean fromComplete = false;
	private Context mAppContext;
	private boolean mEnable = false;

	// 设备方向
	public static enum DeviceOrientation {

		UNKONWN,

		ORIENTATION_PORTRAIT,

		ORIENTATION_REVERSE_LANDSCAPE,

		ORIENTATION_REVERSE_PORTRAIT,

		ORIENTATION_LANDSCAPE
	}

	public DeviceOrientationHelper(Activity ctxt,
			OrientationChangeCallback callback) {

		super(ctxt, SensorManager.SENSOR_DELAY_NORMAL);
		this.callback = callback;
		mAppContext = ctxt;
	}

	private void initLocalOrientation(int orientation) {

		if ((orientation >= 0 && orientation <= 30)
				|| (orientation >= 330 && orientation <= 360)) {

			localOrientation = DeviceOrientation.ORIENTATION_PORTRAIT;

		} else if ((orientation >= 60) && orientation <= 120) {

			localOrientation = DeviceOrientation.ORIENTATION_REVERSE_LANDSCAPE;

		} else if (orientation >= 150 && orientation <= 210) {

			localOrientation = DeviceOrientation.ORIENTATION_REVERSE_PORTRAIT;

		} else if (orientation >= 240 && orientation <= 300) {

			localOrientation = DeviceOrientation.ORIENTATION_LANDSCAPE;

		}
	}

	@Override
	public void onOrientationChanged(int orientation) {

		if (localOrientation == DeviceOrientation.UNKONWN) {
			initLocalOrientation(orientation);
		}
		if (Settings.System.getInt(mAppContext.getContentResolver(),
				Settings.System.ACCELEROMETER_ROTATION, 0) == 0) {
			return;
		}
//		Logger.d(TAG, "onOrientationChanged:" + orientation
//				+ " fromUser:" + fromUser + "  localOrientation:"
//				+ localOrientation + " fromComplete:" + fromComplete);
		if ((orientation >= 0 && orientation <= 30)
				|| (orientation >= 330 && orientation <= 360)) {

			if (!fromUser
					&& localOrientation != DeviceOrientation.ORIENTATION_PORTRAIT
					&& (localOrientation == DeviceOrientation.ORIENTATION_REVERSE_LANDSCAPE || localOrientation == DeviceOrientation.ORIENTATION_LANDSCAPE)
					&& !fromComplete) {
				if (callback != null) {
					callback.land2Port();
				}
			} else if (fromUser) {
				fromUser = false;
			} else if (fromComplete) {
				if (callback != null) {
					callback.onFullScreenPlayComplete();
					fromComplete = false;
				}
			}
			localOrientation = DeviceOrientation.ORIENTATION_PORTRAIT;

		} else if ((orientation >= 60) && orientation <= 120) {

			if (localOrientation != DeviceOrientation.ORIENTATION_REVERSE_LANDSCAPE) {
				if (callback != null) {
					callback.reverseLand();
				}
			}

			localOrientation = DeviceOrientation.ORIENTATION_REVERSE_LANDSCAPE;

		} else if (orientation >= 150 && orientation <= 210) {
			if (localOrientation != DeviceOrientation.ORIENTATION_REVERSE_PORTRAIT) {
				if (callback != null) {
					callback.reversePort();
				}
			}

			localOrientation = DeviceOrientation.ORIENTATION_REVERSE_PORTRAIT;

		} else if (orientation >= 240 && orientation <= 300) {

			if (!fromUser
					&& localOrientation != DeviceOrientation.ORIENTATION_LANDSCAPE) {
				if (callback != null) {
					callback.port2Land();
				}
			} else if (fromUser) {
				fromUser = false;
			}

			localOrientation = DeviceOrientation.ORIENTATION_LANDSCAPE;

		}

	}

	public interface OrientationChangeCallback {

		public void land2Port();

		public void port2Land();

		// 点击放大按钮后 旋转180%
		public void reverseLand();

		public void reversePort();

		public void onFullScreenPlayComplete();

	}

	public boolean isOrientionEnable()  {
		return mEnable;
	}
	public void enableListener() {
//        Logger.d(TAG, Log.getStackTraceString(new Exception("enableListener")));
		if (this.canDetectOrientation() && MediaPlayerConfiguration.getInstance().enableOrientation()) {
			this.enable();
			mEnable = true;
		}
	}

	public void disableListener() {
//		Logger.d(TAG, Log.getStackTraceString(new Exception("disableListener")));
		this.disable();
		localOrientation = DeviceOrientation.UNKONWN;
		mEnable = false;

	}

	public void isFromUser() {
		fromUser = true;
	}

	public void isFromComplete() {
		fromComplete = true;
	}

	public DeviceOrientation getLocalOrientation() {

		return localOrientation;

	}

	public void setCallback(OrientationChangeCallback callback) {
		this.callback = callback;
	}

}

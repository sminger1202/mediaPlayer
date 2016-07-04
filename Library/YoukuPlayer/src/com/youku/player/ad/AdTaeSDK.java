package com.youku.player.ad;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.baseproject.utils.Logger;
//import com.taobao.tae.sdk.TaeSDK;
//import com.taobao.tae.sdk.callback.InitResultCallback;
//import com.taobao.tae.sdk.callback.TradeProcessCallback;
//import com.taobao.tae.sdk.model.TradeResult;

/**
 * TaeSDK 相关
 */
public class AdTaeSDK {

	public static String TAG = "AdTaeSDK";

	/**
	 * 判断是否已经初始化TaeSDK
	 */
	public static boolean isInitTaeSDK = false;

	/**
	 * 判断TaeSDK是否正在初始化
	 */
	private static boolean isInitializing = false;

	/**
	 * 初始化TaeSDK
	 */
	public static void initTaeSDK(Context context) {
//		if (isInitTaeSDK) {
//			Logger.d(TAG, "TaeSDK has been initialized.");
//			return;
//		}
//		if (!isInitializing) {
//			isInitializing = true;
//			Logger.d(TAG, "TaeSDK is initializing...");
//			TaeSDK.asyncInit(context, new InitResultCallback() {
//				@Override
//				public void onFailure(int code, String msg) {
//					Logger.d(TAG, "TaeSDK asynInit failure , code : " + code
//							+ " , msg : " + msg);
//					isInitTaeSDK = false;
//					isInitializing = false;
//				}
//
//				@Override
//				public void onSuccess() {
//					Logger.d(TAG, "TaeSDK asynInit success!");
//					isInitTaeSDK = true;
//					isInitializing = false;
//				}
//			});
//		} else {
//			Logger.d(TAG, "TaeSDK is initializing...");
//		}

	}

	/**
	 * show landing page
	 */
	public static void showPage(final Activity context, String url) {
//		try {
//			TaeSDK.showPage(context, new TradeProcessCallback() {
//				@Override
//				public void onFailure(int code, String msg) {
//					if (code == 10002) {// TaeSDK初始化异常
//						isInitTaeSDK = false;
//					} else if (code == 10014) {// NETWORK_NOT_AVAILABLE
//						Toast.makeText(context, "当前无网络连接", Toast.LENGTH_SHORT)
//								.show();
//					}
//					Logger.d(TAG, "TaeSDK showPage failure , code : " + code
//							+ ", msg : " + msg);
//				}
//
//				@Override
//				public void onPaySuccess(TradeResult arg0) {
//				}
//			}, null, url);
//		} catch (Exception e) {
//		}
	}
}

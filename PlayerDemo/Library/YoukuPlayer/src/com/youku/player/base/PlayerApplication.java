package com.youku.player.base;


import android.app.Application;

public class PlayerApplication extends Application {

	private static PlayerApplication mApplication;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	private PlayerApplication() {
		mApplication = this;
	}

	public static PlayerApplication getPlayerApplicationInstance() {
		if (mApplication == null) {
			mApplication = new PlayerApplication();
			return mApplication;
		}
		return mApplication;
	}
}

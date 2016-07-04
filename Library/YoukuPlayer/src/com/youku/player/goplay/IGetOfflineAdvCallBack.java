package com.youku.player.goplay;


import com.youdo.vo.XAdInstance;

public interface IGetOfflineAdvCallBack {

	public void onSuccess(VideoAdvInfo videoAdvInfo, XAdInstance instance);

	public void onFailed(GoplayException e);
}

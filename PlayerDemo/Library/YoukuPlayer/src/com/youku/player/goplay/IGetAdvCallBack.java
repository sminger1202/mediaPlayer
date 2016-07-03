package com.youku.player.goplay;


public interface IGetAdvCallBack {

	public void onSuccess(VideoAdvInfo videoAdvInfo);

	public void onFailed(GoplayException e);
}

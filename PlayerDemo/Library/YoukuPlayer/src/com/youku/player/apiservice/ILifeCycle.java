package com.youku.player.apiservice;

/** plugin等响应activity声明周期变化接口**/
public interface ILifeCycle {

	/** activity onResume **/
	public void onBaseResume();

	/** activity onConfigurationChanged **/
	public void onBaseConfigurationChanged();
}

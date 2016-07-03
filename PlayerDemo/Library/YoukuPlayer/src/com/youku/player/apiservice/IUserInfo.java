package com.youku.player.apiservice;

/**
 * 
 * @author yuanfang
 *
 */
public interface IUserInfo {
	
	/**
	 * 是否登录
	 * @return
	 */
	public boolean isLogin();
	
	/**
	 * 获得登录ua
	 * @return
	 */
	public String getUserAgent();
	
	/**
	 * 获得登录cookie
	 * @return
	 */
	public String getCookie();

	/**
	 * 获得登录用户ID
	 * @return
	 */
	public String getUserID();
	
	/**
	 * 获取数字形式的userid
	 * @return
	 */
	public String getNumUserID();
	
	/**
	 * 是否是vip
	 * @return
	 */
	public boolean isVip();
}

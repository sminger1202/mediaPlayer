package com.youku.libmanager;

import com.youku.player.util.URLContainer;

public class UrlUtils {

	public static final String OFFICIAL_YOUKU_DOMAIN = "http://api.mobile.youku.com";
	
	public static final String TEST_YOUKU_DOMAIN = "http://test2.api.3g.youku.com";
	
	public static final String OFFICIAL_TUDOU_DOMAIN = "http://api.3g.tudou.com";

	public static final String TEST_TUDOU_DOMAIN = URLContainer.TEST_TUDOU_DOMAIN;
	
	public static final String YOUKU_REQUEST_PATH = "/common/dl/updateinfo";
	
	public static final String TUDOU_REQUEST_PATH = "/client/dl/updateinfo";
	
	public static String getYoukuRequestUrl() {
		if (com.baseproject.utils.Profile.DEBUG) {
			return TEST_YOUKU_DOMAIN + URLContainer.getStatisticsParameter("GET", YOUKU_REQUEST_PATH);
		} else {
			return OFFICIAL_YOUKU_DOMAIN + URLContainer.getStatisticsParameter("GET", YOUKU_REQUEST_PATH);
		}
		
	}
	
	public static String getTudouRequestUrl() {
		
		if (com.baseproject.utils.Profile.DEBUG) {
			return TEST_TUDOU_DOMAIN + URLContainer.getStatisticsParameter("GET", TUDOU_REQUEST_PATH);
		} else {
			return OFFICIAL_TUDOU_DOMAIN + URLContainer.getStatisticsParameter("GET", TUDOU_REQUEST_PATH);
		}
		
	}
	
	public static String getRequestUrl(String url, String product, String os, String arch, String pid, String guid) {
		StringBuffer requestUrl = new StringBuffer();
		requestUrl.append(url + "&product=" + product + "&os=" + os
				+ "&arch=" + arch +"&pid=" + pid + "&guid=" + guid);
		
		return requestUrl.toString();
	}
	
	public static String getRequestUrl(String url, String name, String versionName, String product, String os, 
			String arch, String pid, String guid) {
		StringBuffer requestUrl = new StringBuffer();
		
		requestUrl.append(url + "&name=" + name + "&product=" + product + "&os=" + os
				+ "&arch=" + arch + "&pid=" + pid + "&guid=" + guid); 
		
		if (versionName != null) {
			requestUrl.append("&versionName=" + versionName);
		}
		
		return requestUrl.toString();
	}
	

}

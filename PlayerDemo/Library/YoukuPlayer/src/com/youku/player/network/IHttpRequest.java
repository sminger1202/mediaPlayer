package com.youku.player.network;
/**
 *   
 *   @author          张宇 
 *   @create-time     Oct 15, 2012   10:22:46 AM   
 *   @version         $Id
 *
 */
public interface IHttpRequest {
	
	public void request(HttpIntent httpIntent, IHttpRequestCallBack callBack);
	
	public <T> T parse(T dataObject);
	
	/**
	 * 获得接口未解析时的数据String
	 * @return
	 */
	public String getDataString();
	
	public void cancel();
	
	
	public interface IHttpRequestCallBack{
		
		public void onSuccess(HttpRequestManager httpRequestManager);
		
		public void onFailed(String failReason);
		
//		public void onCancel();
	}

}

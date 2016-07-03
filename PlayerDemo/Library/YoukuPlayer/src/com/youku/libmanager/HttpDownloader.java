/**
 * 文件名：HttpDownloader
 * 功能：基于HTTP协议的下载工具类
 * 作者：于俊锋
 * 创建时间：2012-09-04 
 */
package com.youku.libmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.baseproject.utils.Logger;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

public class HttpDownloader {

	private URL url = null;
	// 从主activity中传来的Context
	private Context mContext;
	// 从主activity中传来的handler
	//private Handler handler;
	private ConnectivityManager manager = null;
	
	public static final int OK = 200;
	
	public static final String TAG = "SoUpgradeService";
	
	private long mCurrentLength = -1L;
	
	private long mContentLength = -1L;
	

//	public HttpDownloader(Context context, Handler handler) {
//		this.mContext = context;
//		this.handler = handler;
//	}

	public HttpDownloader(Context context) {
		this.mContext = context;
	}

	/**
	 * 根据URL下载文件，前提是这个文件当中的内容是文本，函数的返回值就是文件当中的内容
	 * 
	 * @param 文本文件存放地址url
	 * @return 文本文件内容
	 */
	public String download(String urlStr) {
		StringBuffer sb = new StringBuffer();
		String line = null;
		BufferedReader buffer = null;
		
	/*	//检查网络状态，如果没有联网，直接返回
		boolean hasNet = CheckNetworkState();
		
		if (hasNet == false) {
			return null;
		}*/

		try {
			url = new URL(urlStr);
			HttpURLConnection urlConn = null;
			urlConn = (HttpURLConnection) url.openConnection();
			// 设置建立HTTP连接超时时间为4秒，防止ANR
			urlConn.setConnectTimeout(4000);
			// 设置HTTP连接读超时时间为30秒，与主客户端一致
			urlConn.setReadTimeout(30000);
			// 不设缓存
			urlConn.setUseCaches(false);
			buffer = new BufferedReader(new InputStreamReader(
					urlConn.getInputStream()));
			while ((line = buffer.readLine()) != null) {
				sb.append(line);
			}
		} catch (ConnectException e) {
			Logger.e(TAG, e);
			// 断网情况下更新，提示无法连接服务器
			//Toast.makeText(mContext, R.string.connect_failed, Toast.LENGTH_SHORT).show();
			sb = null;
		} catch (Exception e) {
			Logger.e(TAG, e);
			sb = null;
		} finally {
			try {
				buffer.close();
			} catch (NullPointerException e) {
				Logger.e(TAG, e);
			} catch (Exception e) {
				Logger.e(TAG, e);
			}
		}

		if (sb == null) {
			return null;
		} else {
			return sb.toString();
		}
	}
	
	/** 
     * 根据提供的URL地址下载文件，文件可以是任意的文件。 
     * @param urlStr 代表需要下载文件的url字符串 
     * @param path 将下载的文件存放的目录 
     * @param fileName 下载的文件的名字 
     * @return 返回-1：代表文件下载出错；
     *         返回0：代表文件下载成功； 
     */ 
	public int downloadFile(String urlStr, String path, String fileName) {  
		Logger.d(TAG, "downloadFile");
		Logger.d(TAG, "downloadFile");
		Logger.d(TAG, "urlStr = " + urlStr);
		Logger.d(TAG, "path = " + path);
		Logger.d(TAG, "fileName = " + fileName);
		int flag = 0;
		long size = -1L;
		int prevProgress = -1;
		
		//检查网络状态，如果没有联网，直接返回
		/*boolean hasNet = CheckNetworkState();
		if (hasNet == false) {
			return -1;
		}*/
		
		if (!path.endsWith("/")) {
			path += "/";
		}
		
		try {
			size = getInputSize(urlStr);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        
        File downloadFile = new File(path + fileName);
    	String fullPath = downloadFile.getAbsolutePath();
    	long httpLength = 0;
    	
    	if ((downloadFile.exists()) && (downloadFile.length() == size)) {
    		Logger.d(TAG, "文件已存在");
    		return 0;
    	}
    	
        try {  
        	fileOutputStream = new FileOutputStream(fullPath, true);
        	
        	URL url = new URL(urlStr);
			HttpURLConnection httpURLConnection = (HttpURLConnection) url
					.openConnection();
			httpURLConnection.setRequestMethod("GET");
			httpURLConnection.setRequestProperty("Accept-Encoding",
					"identity");
			httpURLConnection
					.addRequestProperty("Connection", "keep-alive");
			httpURLConnection.setConnectTimeout(5000);
			httpURLConnection.setReadTimeout(10000);
			
			if ((downloadFile.exists()) && (downloadFile.length() > 0L)) {
				httpURLConnection.setRequestProperty("Range", "bytes="
						+ downloadFile.length() + "-");
			}
			httpURLConnection.connect();
			inputStream = httpURLConnection.getInputStream();
        	
			mCurrentLength = downloadFile.length();
			httpLength = httpURLConnection.getContentLength();
			mContentLength = httpLength + mCurrentLength;
			
			byte[] arrayOfByte = new byte[4096];
			int readLength = 0;
        	
			while ((readLength = inputStream.read(arrayOfByte)) > 0) {
				fileOutputStream.write(arrayOfByte, 0, readLength);
				mCurrentLength += readLength;
				int progress = (int) ((float) mCurrentLength * 100.0F / (float) mContentLength);
				if (progress % 10 == 0)
				Logger.d(TAG, "progress = " + progress + "%");
			}
			
        } catch (MalformedURLException e) {  
            Logger.e(TAG, e);  
            flag = -1;  
        } catch (IOException e) {  
            Logger.e(TAG, e);  
            flag = -1;  
        } finally {  
            try {  
                if(inputStream != null) {  
                	inputStream.close();  
                	inputStream = null;  
                }    
                if(fileOutputStream != null) {
                	fileOutputStream.close();
                	fileOutputStream = null;
                }
                
                return flag;
            } catch (IOException e) {  
                Logger.e(TAG, e);  
            }  
        }  
        
        return flag;  
    }  

	/**
	 * 根据URL得到HTTP输入流
	 * 
	 * @param 文件存放地址Url
	 * @return inputStream
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public InputStream getInputStreamFromUrl(String urlStr)
			throws MalformedURLException, IOException {
		
		url = new URL(urlStr);
		HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
		// 设置建立HTTP连接超时时间为4秒，防止ANR
		urlConn.setConnectTimeout(4000);
		// 设置HTTP连接读超时时间为30秒，与主客户端一致
		urlConn.setReadTimeout(30000);
		// 不设缓存
		
		urlConn.setUseCaches(false);
		urlConn.connect();
		InputStream inputStream = urlConn.getInputStream();
		return inputStream;
	}

	/**
	 * 根据URL得到输入流文件大小
	 * 
	 * @param 文件存放地址Url
	 * @return 文件大小（bytes）
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public int getInputSize(String urlStr) throws MalformedURLException,
			IOException {
		url = new URL(urlStr);
		HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
		// 设置建立HTTP连接超时时间为4秒，防止ANR
		urlConn.setConnectTimeout(4000);
		// 设置HTTP连接读超时时间为30秒，与主客户端一致
		urlConn.setReadTimeout(30000);
		// 不设缓存
		urlConn.setUseCaches(false);
		urlConn.connect();
		if (urlConn.getResponseCode() != OK) {
			Logger.d(TAG, "responsecode = " + urlConn.getResponseCode() + ", message = " + urlConn.getResponseMessage());
			return -1;
		}
		
		int size = urlConn.getContentLength();
		return size;
	}

	/**
	 * 检查网络连接状况，包括wifi和2G/3G网络
	 * 
	 * @param 
	 * @return 是否有网
	 */
	public boolean CheckNetworkState() {
		manager = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		android.net.NetworkInfo.State mobile = manager.getNetworkInfo(
				ConnectivityManager.TYPE_MOBILE).getState();
		android.net.NetworkInfo.State wifi = manager.getNetworkInfo(
				ConnectivityManager.TYPE_WIFI).getState();
		// 如果3G、wifi、2G等网络状态是连接或者正在连接的，则返回true，否则返回false
		if ((mobile == android.net.NetworkInfo.State.CONNECTED || mobile == android.net.NetworkInfo.State.CONNECTING)
				|| (wifi == android.net.NetworkInfo.State.CONNECTED || wifi == android.net.NetworkInfo.State.CONNECTING))
			return true;
		else
			return false;
	}
}

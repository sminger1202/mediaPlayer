package com.youku.player.ad;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baseproject.utils.Logger;
import com.baseproject.utils.Util;
import com.youku.android.player.R;
import com.youku.player.LogTag;
import com.youku.player.util.PlayerUtil;

public class AdWebViewActivity extends FragmentActivity {

	private final static String TAG = LogTag.TAG_PREFIX + AdWebViewActivity.class.getSimpleName();
	private WebView mWebView;
	private ProgressBar mprogreBar;// 上方的进度细条
	private Context mContext;
	private String url;
	private int countNumber = 0;// 加载次数

	private TextView mTitle = null;
	private RelativeLayout mImageBackWrap = null;
	private RelativeLayout mImageUpdateWrap = null;
	private RelativeLayout mContainer = null;
	private boolean mIsYouku = true;

	/**
	 * intent -------string类型的 url, boolean类型的getCookie
	 */
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.yp_ad_activity_webview);
		mContext = this;

		mContainer = (RelativeLayout) findViewById(R.id.webview_container);
		mWebView = (WebView) findViewById(R.id.webView);
		mprogreBar = (ProgressBar) findViewById(R.id.progress);

		Intent intent = getIntent();
		url = intent.getStringExtra("url");
		mIsYouku = intent.getBooleanExtra("isYouku", true);
		final boolean getCookie = intent.getBooleanExtra("getCookie", false);
		if (url != null && !"".equals(url) && URLUtil.isNetworkUrl(url)) {
			// 有网
			if (Util.hasInternet()) {
				if (!Util.isWifi()) {// 3g
					PlayerUtil.showTips(mContext
							.getString(R.string.player_tips_use_3g));
				}

				mImageBackWrap = (RelativeLayout) findViewById(R.id.webview_custom_back_wrap);
				mImageBackWrap.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						onBackPressed();
					}

				});
				mImageUpdateWrap = (RelativeLayout) findViewById(R.id.webview_custom_update_wrap);
				mImageUpdateWrap.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (!Util.hasInternet()) {
							PlayerUtil.showTips(mContext
									.getString(R.string.player_tips_no_network));
						}
						if (mWebView != null) {
							mWebView.reload();
						}
					}

				});
				// 可缩放
				mWebView.getSettings().setBuiltInZoomControls(true);
				if (android.os.Build.VERSION.SDK_INT >= 11) {
					mWebView.getSettings().setDisplayZoomControls(false);
				}
				mWebView.getSettings().setSupportZoom(true);
				mWebView.getSettings().setUseWideViewPort(true);
				mWebView.getSettings().setLoadWithOverviewMode(true);
				mWebView.getSettings()
						.setJavaScriptCanOpenWindowsAutomatically(true);
				// 客串文件
				mWebView.getSettings().setAllowFileAccess(true);
				// mWebView.getSettings().setDefaultZoom(ZoomDensity.CLOSE);
				if (Build.VERSION.SDK_INT >= 11) {
					mWebView.getSettings().setEnableSmoothTransition(true);
				}
				// 可解析js
				mWebView.getSettings().setJavaScriptEnabled(true);
				mWebView.getSettings().setPluginState(PluginState.ON);
				mWebView.getSettings().setDomStorageEnabled(true);
				// 展示进度以及连接不成功的提示语
				mWebView.setWebViewClient(new WebViewClient() {
					@Override
					public void onPageFinished(WebView view, String url) {
						Logger.d(TAG, "onPageFinished");
						mprogreBar.setVisibility(View.GONE);
						super.onPageFinished(view, url);
					}

					@Override
					public void onReceivedError(WebView view, int errorCode,
							String description, String failingUrl) {
						mprogreBar.setVisibility(View.GONE);
						PlayerUtil.showTips(mContext
								.getString(R.string.player_tips_not_responding));

						Logger.d(TAG, "onReceivedError->code:"
								+ errorCode + "->description:" + description
								+ "->failingUrl:" + failingUrl);
						// if errorcode is -10, it is a invalid url for webview
						if (errorCode == -10) {
							try {
								final Uri uri = Uri.parse(failingUrl);
								final Intent it = new Intent(
										Intent.ACTION_VIEW, uri);
								startActivity(it);
							} catch (Exception e) {
							}
						} else {
							super.onReceivedError(view, errorCode, description,
									failingUrl);
						}
					}

					@Override
					// 接收任何证书 用于2.2以前接收https， ssl需要认证的地址，直接不认证了
					public void onReceivedSslError(WebView view,
							SslErrorHandler handler, SslError error) {
						handler.proceed();
						// super.onReceivedSslError(view, handler, error);
					}

					@Override
					public boolean shouldOverrideUrlLoading(WebView view,
							String url) {
						if (url == null || "".equals(url)) {
							return true;
						}
						// 发送邮箱
						if (url.startsWith("mailto:")) {
							try {
								Intent intent = new Intent(
										Intent.ACTION_SENDTO, Uri.parse(url));
								startActivity(intent);
							} catch (ActivityNotFoundException e) {

								PlayerUtil.showTips(mContext
										.getString(R.string.player_webview_mail_app_not_found));

							}
							return true;
						} else if (url.startsWith("tel:")) { // 打电话
							try {
								Intent intent = new Intent(Intent.ACTION_DIAL,
										Uri.parse(url));
								startActivity(intent);
							} catch (ActivityNotFoundException e) {
								PlayerUtil.showTips("您没有安装电话");
							}
							return true;
						}

						// 如果传来的url是apk结尾的，就启动下载
						if (url.substring(url.length() - 4).equals(".apk")
								&& countNumber == 0) {
							countNumber++;
							Uri uri = Uri.parse(url);
							Intent intent = new Intent(Intent.ACTION_VIEW, uri);
							startActivity(intent);
							// finish();
						}
						mprogreBar.setVisibility(View.VISIBLE);
						return super.shouldOverrideUrlLoading(view, url);
					}

				});
				mWebView.setWebChromeClient(new WebChromeClient() {

					@Override
					public void onProgressChanged(WebView view, int newProgress) {
						mprogreBar.setProgress(newProgress);
						if (newProgress == 100) {
							mprogreBar.setVisibility(View.GONE);
							Logger.d(TAG, "onProgressChanged 100");
						}
					}

					@Override
					public void onReceivedTitle(WebView view, String title) {
						super.onReceivedTitle(view, title);
						if (!TextUtils.isEmpty(title)) {
							setWebViewTitle(title);
							Logger.d(TAG, "onReceivedTitle");
						}
					}

				});
				final Handler handler = new Handler(new Callback() {

					@Override
					public boolean handleMessage(Message msg) {
                        if (mWebView != null) {
                        	mWebView.loadUrl(url);
                        }
						return false;
					}
				});
				Thread temp = new Thread(new Runnable() {

					@Override
					public void run() {
						CookieSyncManager.createInstance(mContext);
						CookieManager cookieManager = CookieManager
								.getInstance();
						//cookieManager.removeAllCookie();
						if (getCookie) {/*
										 * // cookie加入头里
										 * 
										 * if (Youku.COOKIE != null &&
										 * !"".equals(Youku.COOKIE)) { String[]
										 * cookies = Youku.COOKIE.split(";");
										 * for (int i = 0; i < cookies.length;
										 * i++) { cookieManager.setCookie(url,
										 * cookies[i]); } }
										 * CookieSyncManager.getInstance
										 * ().sync();
										 */
						}
						handler.sendEmptyMessageDelayed(0, 50);
					}
				});
				temp.run();
				PlayerUtil.showTips(mContext
						.getString(R.string.player_webview_tip));
			} else {// 无网
				PlayerUtil.showTips(mContext
						.getString(R.string.player_tips_no_network));
				finish();
			}
		} else {// 地址不是http
			PlayerUtil.showTips(mContext
					.getString(R.string.player_webview_wrong_address));
			finish();
		}
	}

	// 返回的时候返回到上一层页面而不是退出
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		/*
		 * if (mWebView.canGoBack() && event.getKeyCode() ==
		 * KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
		 * mWebView.goBack(); return true; }
		 */
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onBackPressed() {
		if (mWebView != null && mWebView.canGoBack()) {
			mWebView.goBack();
			return;
		}
		super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
		if (mWebView != null && mContainer != null) {
			mContainer.removeView(mWebView);
			mWebView.removeAllViews();
			mWebView.destroy();
			mWebView = null;
		}
		mContainer = null;
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mWebView != null) {
			mWebView.onResume();
		}
		countNumber = 0;// 修复会进行两次apk地址请求bug
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (mWebView != null) {
			mWebView.onPause();
		}
	}

	public String getPageName() {
		return "webview页";
	}

	private void setWebViewTitle(String title) {
		mTitle = (TextView) findViewById(R.id.custom_title);
		if (mTitle != null) {
			mTitle.setVisibility(View.VISIBLE);
			mTitle.setText(title);
		}
	}

}

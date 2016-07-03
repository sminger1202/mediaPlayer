package com.youku.player.ui.widget;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.baseproject.utils.Logger;
import com.youku.android.player.R;

/**
 * 调用方法：
 * 1：初始化
 * 2：initial()
 * 3: load()
 *
 */
 
public class InteractionWebView extends FrameLayout{
    public static final String TAG = "InteractionWebView";
	private boolean isWebViewShown = false;
    private FragmentActivity mActivity;
    private int mWidth;
    private Fragment mFragment;
    private FragmentTransaction transaction;


    private FragmentManager mFragmentManager = null;

    public InteractionWebView(FragmentActivity activity, int width, Fragment fragment){
        super(activity);
        this.mActivity = activity;
        this.mFragment = fragment;
        this.mWidth = width;

        initData(activity,width);
    }

    private void initData(FragmentActivity activity, int width){
        LayoutInflater.from(activity).inflate(R.layout.yp_interaction_webview, this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, RelativeLayout.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.RIGHT;
        this.setLayoutParams(params);
    }

    /**
     * 为互动娱乐添加Fragment
     */
    public void addInteractionFragment(){
        mFragmentManager =  mActivity.getSupportFragmentManager();;
        transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.container,mFragment);
        transaction.commit();
    }

	public boolean isWebViewShown(){
		Logger.d(TAG,"get webview state: " + isWebViewShown);
		return isWebViewShown;
	}

	public void setVisiable(){
		this.setVisibility(View.VISIBLE);
		isWebViewShown = true;
	}

	/**
	 * 隐藏WebView
	 */
	@SuppressWarnings("deprecation")
	public void hideWebView(){
		Logger.d(TAG,"hide webview");
        transaction = mFragmentManager.beginTransaction();
        transaction.remove(mFragment).commit();
        this.setVisibility(View.INVISIBLE);
        isWebViewShown = false;
	}
}

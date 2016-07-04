package com.youku.uplayer;

/**
 * 302跳转时间
 */
public interface OnHttp302DelayListener {
    void onVideo302Delay(int time);

    void onAd302Delay(int time);
}

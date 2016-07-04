package com.youku.uplayer;

/**
 * 播放延迟统计
 */
public interface OnConnectDelayListener {
    void onVideoConnectDelay(int time);

    void onAdConnectDelay(int time);

}

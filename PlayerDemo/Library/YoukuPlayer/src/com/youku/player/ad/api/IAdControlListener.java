package com.youku.player.ad.api;

/**
 * 广告监听
 */
public interface IAdControlListener {
    /**
     * 通知上层前贴广告获取是否成功
     */
    public void onAdvInfoGetted(boolean hasAd);

    /**
     * 通知上层中插广告播放开始loading
     */
    public void onMidAdLoadingStartListener();

    /**
     * 通知上层中插广告播放结束loading
     */
    public void onMidAdLoadingEndListener();

    /**
     * 通知上层跳过广告
     */
    public void onSkipAdClicked();

    /**
     * 更新plugin方法
     */
    public void updatePlugin(int pluginId);
}

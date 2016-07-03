package com.youku.player.apiservice;

/**
 * user callbacks implemented by high level module.
 */
public interface IUserCallback {
    /**
     * 平滑清晰度切换开始
     */
    void onQualitySmoothChangeStart(int quality);

    /**
     * 清晰度平滑切换结束
     */
    void onQualitySmoothChangeEnd(int quality);
}

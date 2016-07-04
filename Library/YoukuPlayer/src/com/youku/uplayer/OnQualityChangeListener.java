package com.youku.uplayer;

/**
 * 清晰度切换.
 */
public interface OnQualityChangeListener {
    /**
     * 清晰度切换成功
     */
    void onQualityChangeSuccess();

    /**
     * 清晰度平滑切换失败
     */
    void onQualitySmoothChangeFail();
}

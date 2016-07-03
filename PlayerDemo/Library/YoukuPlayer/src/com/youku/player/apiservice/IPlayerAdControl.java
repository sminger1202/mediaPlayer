package com.youku.player.apiservice;

import com.youku.player.ad.AdState;
import com.youku.player.ad.MidAdModel;
import com.youku.player.ad.api.IAdControlListener;
import com.youku.player.goplay.AdvInfo;
import com.youku.player.goplay.Point;
import com.youku.player.goplay.VideoAdvInfo;
import com.youku.player.module.PlayVideoInfo;

import java.util.ArrayList;

/**
 * activity implement this to control ad show
 */
public interface IPlayerAdControl {
    /**
     * 前贴广告信息获取是否成功回调
     */
    public void onAdvInfoGetted(boolean hasAd);

    /**
     * 前贴广告播放一定次数后，提示用户登录
     */
    public void creatDialogToLogin(final PlayVideoInfo playVideoInfo);

    /*****************************全屏广告 start *************************/
    /**
     * 全屏广告是否正在显示
     */
    public boolean isImageAdShowing();
    /**
     * 是否发起全屏广告显示操作
     */
    public boolean isImageAdStartToShow();
    /**
     * 设置全屏广告是否正在显示
     *
     * @return
     */
    public void setImageAdShowing(boolean isShowing);

    public void showImageAD(VideoAdvInfo videoAdvInfo);

    public void dismissImageAD();
    /*****************************全屏广告 end *************************/

    /*****************************中插广告 start *************************/
    /**
     * 初始化中插广告，设置中插点
     */
    public void setMidADInfo(ArrayList<Point> list, String adTest);
    /**
     * 清除中插广告信息
     */
    public void clearMidAD();
    /**
     * 中插广告(标版广告)是否正在显示
     */
    public boolean isMidAdShowing();
    /**
     * 获取中插广告控制器
     *
     * @return MidAdModel
     */
    public MidAdModel getMidAdModel();
    /**
     * loading mid ad start
     */
    public void onMidAdLoadingStartListener();
    /**
     * loading mid ad end
     */
    public void onMidAdLoadingEndListener();

    /**
     * 中插广告play的回调
     */
    public void onMidAdPlay();
    /*****************************中插广告 end *************************/

    /*****************************调查问卷 start *************************/
    /**
     * 初始调查问卷状态
     */
    public void initInvestigate(VideoAdvInfo videoAdvInfo);

    /**
     * 显示调查问卷
     */
    public void showInvestigate();

    /**
     * 释放调查问卷
     */
    public void releaseInvestigate();
    /*****************************调查问卷 end *************************/

    /*****************************暂停广告 start *************************/
    /**
     * 显示暂停广告
     */
    public void showPauseAD();

    public void dismissPauseAD();
    /**
     * 设置暂停广告预览ID
     */
    public void setPauseTestAd(String adext);
    /*****************************暂停广告 end *************************/
    /**
     * 关闭互动广告
     */
    public void dismissInteractiveAD();

    /**
     * set activity ad state
     */
    public void setAdState(AdState state);

    /**
     * get activity current ad state
     *
     * @return AdState
     */
    public AdState getAdState();

    /**
     * skip ad on clicked callback
     */
    public void onSkipAdClicked();

    /**
     * 查看详情点击 callback
     */
    public void onMoreInfoClicked(String url, AdvInfo advInfo);

    /**
     * apk下载对话框显示
     * @param advInfo
     */
    void onDownloadDialogShow(AdvInfo advInfo);

    public void interuptAD();

    public boolean isImageADShowingAndNoSave();

    /**
     * 设置 监听器
     * @param listener
     */
    public void setListener(IAdControlListener listener);

    /**
     * 暂停广告是否正在显示
     */
    boolean isPauseAdVisible();
}

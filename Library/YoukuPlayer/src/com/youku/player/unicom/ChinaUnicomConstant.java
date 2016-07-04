package com.youku.player.unicom;

/**
 * Created by malijie on 2015/3/13.
 */
public class ChinaUnicomConstant {

    /**免流地址转换成功提示*/
    public static final String NORMAL_TRANSFORM_FREEFLOW_SUCCESS = "当前使用联通免流量播放";

    /**免流地址转换失败对话框提示*/
    public static final String NORMAL_TRANSFORM_FREEFLOW_FAILED = "联通免流量地址获取失败，继续播放将消耗套餐流量。";

    /**播放视频时判断为WAP接入点时提示*/
    public static final String WAP_TANSFORM_TFREEFLOW_FAILED = "WAP接入点将导致联通免流量服务失效，请切换为NET接入点。";

    /**联通免流订购类型，1为包月*/
    public static final int FREEFLOW_ORDER_TYPE_MONTH = 1;

    /**联通免流订购状态，0为订购*/
    public static final int FREEFLOW_ORDER_STAUS_SUBSCRIBED = 0;

    /**联通免流订购状态，1为退订，但包月用户有效*/
    public static final int FREEFLOW_ORDER_STAUS_UNSBSCRIBED = 1;

    /**Handler标志,满足条件为真*/
    public static final int HANDLE_FREEFLOW_MESSAGE_SUCCESS = 1;

    /**
     * 对话框按钮文字
     */
    public static final String BUTTON_CONTINE_WATCH = "继续播放";
    public static final String BUTTON_CANCEL_WATCH = "取消播放";

}

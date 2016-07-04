package com.youku.player.apiservice;

import com.youku.player.module.PayInfo;

/**
 * 付费视频的回调接口
 * @author yuanfang
 *
 */
public interface IPayCallBack {
	/**
	 * 需要支付
	 * @param vid 视频id
	 * @param payInfo 支付信息
	 */
	public void needPay(String vid, PayInfo payInfo);
}

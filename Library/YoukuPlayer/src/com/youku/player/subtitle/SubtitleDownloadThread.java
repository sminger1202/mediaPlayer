/**
 * @brief 字幕下载线程
 * 		  只下载chs(简体中文)、cht(繁体中文)、en(英文)三种字幕
 */
package com.youku.player.subtitle;

import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.baseproject.utils.Logger;
import com.youku.libmanager.HttpDownloader;

public class SubtitleDownloadThread extends Thread {

	public static final int TRY_TIME = 3;

	public static final int SUBTITLE_DOWNLOAD_SUCCESS = 10001;

	public static final int SUBTITLE_DOWNLOAD_FAIL = 10002;

	private boolean stopFlag = false;

	Context context;

	String name;

	Handler handler;

	List<Attachment> attachments;

	public void stopSelf() {
		stopFlag = true;
	}

	public SubtitleDownloadThread(Context context, Handler handler, String name) {
		super();
		this.context = context;
		this.handler = handler;
		this.name = name;
	}
	
	public void setTask(List<Attachment> attachments) {
		this.attachments = attachments;
	}
	
	private void handleDownloadSuccess(Attachment attachment, int mode) {
		DownloadedSubtitle subtitle = new DownloadedSubtitle(name, attachment.lang, "", mode);
		
		SubtitleManager.addSubtitle(subtitle);
		
		Message msg = handler.obtainMessage();
		msg.what = SUBTITLE_DOWNLOAD_SUCCESS;
		msg.obj = subtitle;

		handler.sendMessage(msg);
	}

	@Override
	public void run() {

		SubtitleManager.clearAllSubtitle();

		HttpDownloader httpDownloader = new HttpDownloader(context);

		for (Attachment attachment : attachments) {
			if (attachment.lang.equals("chs") || attachment.lang.equals("en")
					|| attachment.lang.equals("cht")) {
				for (int i = 0; i < TRY_TIME; i++) {
					if (stopFlag)
						return;

					if (attachment.lang.equals("chs")) {
						Logger.d(SubtitleManager.TAG, "download chs");
						SubtitleManager.sChsContent = httpDownloader
								.download(attachment.attrachmentUrl);
						if (SubtitleManager.sChsContent != null && !stopFlag) {
							handleDownloadSuccess(attachment, SubtitleManager.SIMPLIFIED_CHINESE);
							break;
						}
					}

					if (attachment.lang.equals("cht")) {
						Logger.d(SubtitleManager.TAG, "download cht");
						SubtitleManager.sChtContent = httpDownloader
								.download(attachment.attrachmentUrl);
						if (SubtitleManager.sChtContent != null && !stopFlag) {
							handleDownloadSuccess(attachment, SubtitleManager.TRADITIONAL_CHINESE);
							break;
						}
					}

					if (attachment.lang.equals("en")) {
						Logger.d(SubtitleManager.TAG, "download en");
						SubtitleManager.sEnContent = httpDownloader
								.download(attachment.attrachmentUrl);
						if (SubtitleManager.sEnContent != null && !stopFlag) {
							handleDownloadSuccess(attachment, SubtitleManager.ENGLISH);
							break;
						}

					}

				}
			}
		}

	}

}

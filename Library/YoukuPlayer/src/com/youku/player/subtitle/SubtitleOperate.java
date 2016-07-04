package com.youku.player.subtitle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.baseproject.utils.Logger;
import com.youku.player.base.YoukuPlayerView;
import com.youku.player.plugin.PluginManager;

public class SubtitleOperate {
	// 保存当前将要显示的字幕
	private Subtitle mCurChsSubtitle;

	private Subtitle mCurEnSubtitle;

	private Subtitle mCurChtSubtitle;

	// 简体中文
	private SubtitleManager mChsSubtitleManager;

	// 繁体中文
	private SubtitleManager mChtSubtitleManager;

	// 英文
	private SubtitleManager mEnSubtitleManager;

	private YoukuPlayerView mYoukuPlayerView;
	private PluginManager mPluginManager;

	public SubtitleOperate(YoukuPlayerView youkuPlayerView,
			PluginManager pluginManager) {
		mYoukuPlayerView = youkuPlayerView;
		mPluginManager = pluginManager;
	}

	private boolean prepareChsSubtitle(DownloadedSubtitle subtitle, int type) {
		if (mChsSubtitleManager != null) {
			mChsSubtitleManager = null;
		}

		mChsSubtitleManager = new SubtitleManager();
		mChsSubtitleManager.init();

		if (type == SubtitleManager.ONLINE_TYPE) {
			return mChsSubtitleManager.prepare("chs");
		} else {
			return mChsSubtitleManager.prepare(subtitle.path, subtitle.name
					+ "_chs");
		}
	}

	private boolean prepareChtSubtitle(DownloadedSubtitle subtitle, int type) {
		if (mChtSubtitleManager != null) {
			mChtSubtitleManager = null;
		}

		mChtSubtitleManager = new SubtitleManager();
		mChtSubtitleManager.init();

		if (type == SubtitleManager.ONLINE_TYPE) {
			return mChtSubtitleManager.prepare("cht");
		} else {
			return mChtSubtitleManager.prepare(subtitle.path, subtitle.name
					+ "_cht");
		}
	}

	private boolean prepareEnSubtitle(DownloadedSubtitle subtitle, int type) {
		if (mEnSubtitleManager != null) {
			mEnSubtitleManager = null;
		}

		mEnSubtitleManager = new SubtitleManager();
		mEnSubtitleManager.init();

		if (type == SubtitleManager.ONLINE_TYPE) {
			return mEnSubtitleManager.prepare("en");
		} else {
			return mEnSubtitleManager.prepare(subtitle.path, subtitle.name
					+ "_en");
		}
	}

	public void clearSubtitle() {
		Logger.d(SubtitleManager.TAG, "clearSubtitle");

		if (mEnSubtitleManager != null) {
			mEnSubtitleManager = null;
		}

		if (mChtSubtitleManager != null) {
			mChtSubtitleManager = null;
		}

		if (mChsSubtitleManager != null) {
			mChsSubtitleManager = null;
		}

		SubtitleManager.clearAllSubtitle();

	}

	public void onDownloadSubtitle(DownloadedSubtitle subtitle, int type) {
		if (subtitle == null) {
			if (mPluginManager != null) {
				mPluginManager.onSubtitlePrepared();
			}
			return;
		} else if (subtitle.lang.equals("chs")) {
			if (!prepareChsSubtitle(subtitle, type)) {
				SubtitleManager.removeSubtitle("chs");
				return;
			}
		} else if (subtitle.lang.equals("cht")) {
			if (!prepareChtSubtitle(subtitle, type)) {
				SubtitleManager.removeSubtitle("cht");
				return;
			}
		} else if (subtitle.lang.equals("en")) {
			if (!prepareEnSubtitle(subtitle, type)) {
				SubtitleManager.removeSubtitle("en");
				return;
			}
		}

		SubtitleManager.setDefaultMode();

		if (mPluginManager != null) {
			mPluginManager.onSubtitlePrepared();
		}

	}

	public void showSubtitle(int currentPosition) {
		if (SubtitleManager.sMode == SubtitleManager.NO_SUBTITLE) {
			mYoukuPlayerView.dismissSingleSubtitle();
			mYoukuPlayerView.dismissFirstSubtitle();
			mYoukuPlayerView.dismissSecondSubtitle();
		} else if (SubtitleManager.sMode == SubtitleManager.SIMPLIFIED_CHINESE) {
			mYoukuPlayerView.dismissFirstSubtitle();
			mYoukuPlayerView.dismissSecondSubtitle();
			if (mChsSubtitleManager != null && mChsSubtitleManager.isReady()
					&& currentPosition > 0) {
				mCurChsSubtitle = mChsSubtitleManager
						.getSubtitle(currentPosition);

				if (currentPosition >= mCurChsSubtitle.start
						&& currentPosition <= mCurChsSubtitle.end) {
					// display subtitle
					mYoukuPlayerView.setSingleSubtitle(mCurChsSubtitle.text);
				} else {
					// dismiss subtitle
					mYoukuPlayerView.dismissSingleSubtitle();
				}
				mCurChsSubtitle = null;
			}
		} else if (SubtitleManager.sMode == SubtitleManager.TRADITIONAL_CHINESE) {
			mYoukuPlayerView.dismissFirstSubtitle();
			mYoukuPlayerView.dismissSecondSubtitle();
			if (mChtSubtitleManager != null && mChtSubtitleManager.isReady()
					&& currentPosition > 0) {
				mCurChtSubtitle = mChtSubtitleManager
						.getSubtitle(currentPosition);

				if (currentPosition >= mCurChtSubtitle.start
						&& currentPosition <= mCurChtSubtitle.end) {
					// display subtitle
					mYoukuPlayerView.setSingleSubtitle(mCurChtSubtitle.text);
				} else {
					// dismiss subtitle
					mYoukuPlayerView.dismissSingleSubtitle();
				}
				mCurChtSubtitle = null;
			}
		} else if (SubtitleManager.sMode == SubtitleManager.ENGLISH) {
			mYoukuPlayerView.dismissFirstSubtitle();
			mYoukuPlayerView.dismissSecondSubtitle();
			if (mEnSubtitleManager != null && mEnSubtitleManager.isReady()
					&& currentPosition > 0) {
				mCurEnSubtitle = mEnSubtitleManager
						.getSubtitle(currentPosition);

				if (currentPosition >= mCurEnSubtitle.start
						&& currentPosition <= mCurEnSubtitle.end) {
					// display subtitle
					mYoukuPlayerView.setSingleSubtitle(mCurEnSubtitle.text);
				} else {
					// dismiss subtitle
					mYoukuPlayerView.dismissSingleSubtitle();
				}
				mCurEnSubtitle = null;
			}
		} else if (SubtitleManager.sMode == SubtitleManager.SIMPLIFIED_AND_ENGLISH) {
			mYoukuPlayerView.dismissSingleSubtitle();
			if (mEnSubtitleManager != null && mEnSubtitleManager.isReady()
					&& currentPosition > 0) {
				mCurEnSubtitle = mEnSubtitleManager
						.getSubtitle(currentPosition);

				if (currentPosition >= mCurEnSubtitle.start
						&& currentPosition <= mCurEnSubtitle.end) {
					// display subtitle
					mYoukuPlayerView.setFirstSubtitle(mCurEnSubtitle.text);
				} else {
					// dismiss subtitle
					mYoukuPlayerView.dismissFirstSubtitle();
				}

				mCurEnSubtitle = null;
			}

			if (mChsSubtitleManager != null && mChsSubtitleManager.isReady()
					&& currentPosition > 0) {
				mCurChsSubtitle = mChsSubtitleManager
						.getSubtitle(currentPosition);

				if (currentPosition >= mCurChsSubtitle.start
						&& currentPosition <= mCurChsSubtitle.end) {
					// display subtitle
					mYoukuPlayerView.setSecondSubtitle(mCurChsSubtitle.text);
				} else {
					mYoukuPlayerView.dismissSecondSubtitle();
					// dismiss subtitle
				}
				mCurChsSubtitle = null;
			}
		} else if (SubtitleManager.sMode == SubtitleManager.TRADITIONAL_AND_ENGLISH) {
			mYoukuPlayerView.dismissSingleSubtitle();
			if (mEnSubtitleManager != null && mEnSubtitleManager.isReady()
					&& currentPosition > 0) {
				mCurEnSubtitle = mEnSubtitleManager
						.getSubtitle(currentPosition);

				if (currentPosition >= mCurEnSubtitle.start
						&& currentPosition <= mCurEnSubtitle.end) {
					// display subtitle
					mYoukuPlayerView.setFirstSubtitle(mCurEnSubtitle.text);
				} else {
					// dismiss subtitle
					mYoukuPlayerView.dismissFirstSubtitle();
				}

				mCurEnSubtitle = null;
			}

			if (mChtSubtitleManager != null && mChtSubtitleManager.isReady()
					&& currentPosition > 0) {
				mCurChtSubtitle = mChtSubtitleManager
						.getSubtitle(currentPosition);

				if (currentPosition >= mCurChtSubtitle.start
						&& currentPosition <= mCurChtSubtitle.end) {
					// display subtitle
					mYoukuPlayerView.setSecondSubtitle(mCurChtSubtitle.text);
				} else {
					// dismiss subtitle
					mYoukuPlayerView.dismissSecondSubtitle();
				}

				mCurChtSubtitle = null;
			}
		}
	}

	public void dismissSubtitle() {
		mYoukuPlayerView.dismissSingleSubtitle();
		mYoukuPlayerView.dismissFirstSubtitle();
		mYoukuPlayerView.dismissSecondSubtitle();
	}

	public static List<DownloadedSubtitle> getSubtitles(String path, String vid) {
		Logger.d(SubtitleManager.TAG, "getSubtitles() path = " + path
				+ ", vid = " + vid);
		List<DownloadedSubtitle> subtitles = new ArrayList<DownloadedSubtitle>();
		if (!path.endsWith("/")) {
			path += "/";
		}

		File file = new File(path + vid + "_chs");
		if (file.exists() && file.length() > 0) {
			Logger.d(SubtitleManager.TAG, file + " exist");
			subtitles.add(new DownloadedSubtitle(vid, "chs", path,
					SubtitleManager.SIMPLIFIED_CHINESE));
		}

		file = new File(path + vid + "_cht");
		if (file.exists() && file.length() > 0) {
			Logger.d(SubtitleManager.TAG, file + " exist");
			subtitles.add(new DownloadedSubtitle(vid, "cht", path,
					SubtitleManager.TRADITIONAL_CHINESE));
		}

		file = new File(path + vid + "_en");
		if (file.exists() && file.length() > 0) {
			Logger.d(SubtitleManager.TAG, file + " exist");
			subtitles.add(new DownloadedSubtitle(vid, "en", path,
					SubtitleManager.ENGLISH));
		}
		return subtitles;
	}
}

package com.youku.player.module;

import com.youku.player.base.PlayType;

import java.nio.Buffer;

/**
 * @class A PlayVideoInfo.
 * @Description: PlayVideo()需要的播放信息封装
 */
public class PlayVideoInfo {
    public String vid;
    public String password;
    public boolean isCache;
    public int point;
    public int videoStage;
    public boolean noAdv;
    public boolean isFromYouku;
    public boolean isTudouAlbum;
    public int tudouquality;
    public String playlistCode;
    public String playlistId;
    public String albumID;
    public String languageCode;
    private boolean isLocal;
    private PlayType mPlayType;
    private String mSource;
    private String adExt;
    private String adMid;
    private String adPause;
    public String url;
    private boolean isWaterMark;
    private int waterMarkType;
    private String title;
    // 是否是自动播放
    public int autoPlay;

    public Boolean mIsFullScreen;

    private PlayVideoInfo(Builder builder) {
        this.vid = builder.vid;
        this.password = builder.password;
        this.isCache = builder.isCache;
        this.point = builder.point;
        this.videoStage = builder.videoStage;
        this.noAdv = builder.noAdv;
        this.isFromYouku = builder.isFromYouku;
        this.isTudouAlbum = builder.isTudouAlbum;
        this.tudouquality = builder.tudouQuality;
        this.playlistCode = builder.playlistCode;
        this.playlistId = builder.playlistId;
        this.albumID = builder.albumID;
        this.languageCode = builder.languageCode;
        this.isLocal = builder.isLocal;
        mPlayType = builder.mPlayType;
        this.mSource = builder.mSource;
        this.adExt = builder.adExt;
        this.adMid = builder.adMid;
        this.adPause = builder.adPause;
        url = builder.url;
        isWaterMark = builder.isWaterMark;
        waterMarkType = builder.waterMarkType;
        title = builder.title;
        autoPlay = builder.autoPlay;
        this.mIsFullScreen = builder.mIsFullScreen;
    }

    public boolean isLocalPlay() {
        return isLocal;
    }

    public String getAdExt() {
        return adExt;
    }

    public String getAdMid() {
        return adMid;
    }

    public String getAdPause() {
        return adPause;
    }

    public String getSource() {
        return mSource;
    }

    public PlayType getPlayType() {
        return mPlayType;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public int getWaterMarkType() {
        return waterMarkType;
    }

    public boolean waterMark() {
        return isWaterMark;
    }

    public boolean IsFullScreenPlay() {
        return mIsFullScreen;
    }

    public static class Builder {
        private String vid;
        private String password;
        private boolean isCache;
        private int point;
        private int videoStage;
        private boolean noAdv;
        private boolean isFromYouku = true;
        private boolean isTudouAlbum;
        private int tudouQuality;
        private String playlistCode;
        private String playlistId;
        private String albumID;
        private String languageCode;
        private boolean isLocal;
        private PlayType mPlayType = PlayType.ONLINE;
        private String mSource;
        private String adExt;
        private String adMid;
        private String adPause;
        private String url;
        private boolean isWaterMark;
        private int waterMarkType;
        private String title;
        private int autoPlay;
        private boolean mIsFullScreen;

        public Builder(String vid) {
            this.vid = vid;
        }

        public Builder setId(String id) {
            this.vid = id;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder setCache(boolean isCache) {
            this.isCache = isCache;
            return this;
        }

        public Builder setPoint(int point) {
            this.point = point;
            return this;
        }

        public Builder setVideoStage(int videoStage) {
            this.videoStage = videoStage;
            return this;
        }

        public Builder setNoAdv(boolean noAdv) {
            this.noAdv = noAdv;
            return this;
        }

        public Builder setFromYouku(boolean isFromYouku) {
            this.isFromYouku = isFromYouku;
            return this;
        }

        public Builder setLocal(boolean isLocal) {
            this.isLocal = isLocal;
            return this;
        }

        public Builder setTudouAlbum(boolean isTudouAlbum) {
            this.isTudouAlbum = isTudouAlbum;
            return this;
        }

        public Builder setTudouQuality(int tudouQuality) {
            this.tudouQuality = tudouQuality;
            return this;
        }

        public Builder setPlaylistCode(String playlistCode) {
            this.playlistCode = playlistCode;
            return this;
        }

        public Builder setPlaylistId(String playlistId) {
            this.playlistId = playlistId;
            return this;
        }

        public Builder setAlbumID(String albumID) {
            this.albumID = albumID;
            return this;
        }

        public Builder setLanguageCode(String languageCode) {
            this.languageCode = languageCode;
            return this;
        }

        public Builder setPlayType(PlayType playType) {
            mPlayType = playType;
            return this;
        }

        public Builder setSource(String source) {
            mSource = source;
            return this;
        }

        public Builder setAdExt(String adExt) {
            this.adExt = adExt;
            return this;
        }

        public Builder setAdMid(String adMid) {
            this.adMid = adMid;
            return this;
        }

        public Builder setAdPause(String adPause) {
            this.adPause = adPause;
            return this;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setWaterMark(boolean waterMark) {
            this.isWaterMark = waterMark;
            return this;
        }

        public Builder setWaterMarkType(int waterMarkType) {
            this.waterMarkType = waterMarkType;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }


        public Builder setAutoPlay(int autoPlay){
            this.autoPlay = autoPlay;
            return this;
        }

        public Builder setFullScreen( boolean fullScreen) {
            this.mIsFullScreen = fullScreen;
            return this;
        }

        public PlayVideoInfo build() {
            return new PlayVideoInfo(this);
        }
    }
}

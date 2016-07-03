package com.youku.uplayer;

/**
 * 用于系统播放器长时间release而导致无法创建新的播放器
 */
public class ReleaseTimeoutException extends IllegalStateException{
    public ReleaseTimeoutException(String detailMessage) {
        super(detailMessage);
    }
}

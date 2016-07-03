package com.youku.player;

import com.youku.uplayer.NetCache;

public class PlayerNetCache {
    public static PlayerNetCache getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static final class SingletonHolder {
        public static final PlayerNetCache INSTANCE = new PlayerNetCache();
    }

    /**
     * 启动缓存
     * @param path 缓存目录
     * @param capacity 缓存大小
     * @return
     */
    public int start(String path, long capacity) {
        return NetCache.start(path, capacity);
    }

    public void stop() {
        NetCache.stop();
    }

    /**
     * 解析DNS
     */
    public void dnsPreParse() {
        NetCache.DNSPreParse();
    }

    /**
     * 设置ua
     * @param userAgent
     */
    public void setUserAgent(String userAgent) {
        NetCache.SetUserAgent(userAgent);
    }

    public void reset(String path, long capacity) {
        NetCache.stop();
        NetCache.start(path, capacity);
    }
}

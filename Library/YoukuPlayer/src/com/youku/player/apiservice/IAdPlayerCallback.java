package com.youku.player.apiservice;

/**
 * plugin video ad need to implement this callback, to do response for media player
 */
public interface IAdPlayerCallback {
    public boolean onAdStart(final int index);

    public boolean onAdEnd(final int index);

    public void onADCountUpdate(final int count);
}

package com.youku.player.ad.pausead;

/**
 * pause ad callback interface
 */
public interface IPauseAdCallback {
    public void onPauseAdClose();

    public void onPauseAdPresent(int request);

    public void onPauseAdClicked();

    public void onPauseAdFailed();

    public void onPauseAdDismiss();
}

package com.youku.player.ad.imagead;

/**
 * image ad callback.
 */
public interface IImageAdCallback {
    public void onAdClose();

    public void onAdPresent();

    public void onAdClicked();

    public void onAdFailed();

    public void onAdDismiss();
}

package com.youku.player.apiservice;


public interface ScreenChangeListener {

    public abstract void onFullscreenListener();

    public abstract void setPadHorizontalLayout();

    public abstract void onSmallscreenListener();

    /**
     * 在真正旋转前的回调；
     */
    abstract void onGoSmall();
    abstract void onGoFull();

}

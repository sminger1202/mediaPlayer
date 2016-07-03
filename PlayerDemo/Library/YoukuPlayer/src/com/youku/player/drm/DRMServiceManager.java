package com.youku.player.drm;

import com.baseproject.utils.Logger;
import com.intertrust.wasabi.ErrorCodeException;
import com.intertrust.wasabi.media.PlaylistProxy;
import com.intertrust.wasabi.media.PlaylistProxy.MediaSourceType;

public class DRMServiceManager {
    private static final String TAG = "drm";

    public static DRMServiceManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        public static final DRMServiceManager INSTANCE = new DRMServiceManager();
    }

    private DRMServiceManager() {

    }

    /**
     * Handler of incoming messages from service.
     */
    // class IncomingHandler extends Handler {
    // @Override
    // public void handleMessage(Message msg) {
    // switch (msg.what) {
    // case PlaylistProxyService.MSG_MAKE_URL:
    // try {
    // Bundle mBundle = msg.getData();
    // String url = mBundle.getString("url");
    // Log.d(TAG,
    // "MarlinVideoPlayer handleMessage MSG_MAKE_URL "
    // + url);
    // play(url);
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // break;
    // default:
    // super.handleMessage(msg);
    // }
    // }
    // }

    // void startPlayer(String url) {
    // Log.d(TAG, "startPlayer fileToPlay");
    //
    // Bundle mBundle = new Bundle();
    // // *for pptv test
    // // dontfang
    // // fileToPlay =
    // //
    // "http://atn-play.cloudapp.net/live/887d4d23ee894ceb8f5f457fa24f3e97.m3u8";
    // // taiguo
    // // fileToPlay =
    // //
    // "http://atn-play.cloudapp.net/live/c22409a2a818409ebb5e5a1c42c4eb63.m3u8";
    // // */
    // // fileToPlay =
    // //
    // "https://ms3.china.hostedmarlin.com:8443/hms/ms3/rights/?b=AA0AAwAAA4MAA3pjaoPUABDmLWXvk1aPUbaMjfVOwYT4AGBD7Nl-ZbZZ-N1GqxCjh_VQCNP43Ab4PGuE6sn1bFkJECBieupKIGkZqLtezA8cvh4nNmgh_W69Df3sv5otGIV259L4B0E4LYVq71kJLkm2sIjVA6wyXAiwckl2I-sZlqcAAAAUKmq6inOE8nxJSpqYTIlZX-Fy-hM#http%3A%2F%2F192.168.1.131%2Fcontent%2Fbunny-dash%2Fdash-cenc%2Fall.mpd";
    // // fileToPlay =
    // //
    // "https://ms3.china.hostedmarlin.com:8443/hms/ms3/rights/?b=AA0AAwAAA4MAA3pjaodUABCBESNreZPRv0JLTiPk8902AHCFIXJfWgcpwVlx8bCPGrgvj4MKTgZjNUNdlVS8BAcesFgBuA17mjh-_L-81oQ7M85ugPLaub8hOp9DOOU3k6NjRqs_0YZZpaWyySlH2HaQr7Hw7e6brQ_FrW9IQnU9CkqZZrw3XWu4llBLkxHRKUMRAAAAFOammn9crFGaIKJvw0pu-PQwOLwq#http%3A%2F%2F192.168.1.107%2Fcontent%2Fhbbtv%2Fbunny%2Foutput%2Fmpd.xml";
    // // fileToPlay =
    // //
    // "https://ms3.eval.hostedmarlin.com:8443/hms/ms3/rights/?b=AA0AAwAABIEAA3pjapF2ABDXBskyfdacbLmairV-tHwjAHD82rnXA1Mowo2GC_HOsEE-xuwJpVTVEShFq408kbLqQ8Vebocm-xLR7tItO4XSG6h27oTBNo6reSLam21e-ecrjNwa0PHbXXs3VFlw9mHcmQ_AN0VUHTp8-qCZ94P1gQT_xDutCC0w7WlZhzBENjMZAAAAFOEh-YYb_q4GLKbiJ6_6ynt4JtPL#http%3A%2F%2F192.168.1.107%2Fcontent%2Fhbbtv%2Fbunny%2Foutput%2Fmpd.xml";
    // mBundle.putString("FileToPlay", url); // pakage usr to a bundle
    //
    // mBundle.putInt("DurationSeconds", (int) movie_duration);
    // mBundle.putString("SourceContentType", movie_mimeType);
    // mBundle.putString("MediaSourceType", movie_sourceType);
    //
    // Message msg = Message.obtain(null, PlaylistProxyService.MSG_MAKE_URL);
    // msg.setData(mBundle);
    // msg.replyTo = mMessenger;
    // try {
    // mService.send(msg);
    // } catch (RemoteException e) {
    // e.printStackTrace();
    // }
    // }
    public String makeUrl(String fileToPlay) {
        String url = "";
        try {
            Logger.d(TAG, "PlaylistProxyService onStart");
            PlaylistProxy playlistProxy = new PlaylistProxy();
            playlistProxy.start();
            PlaylistProxy.MediaSourceParams params = new PlaylistProxy.MediaSourceParams();
            MediaSourceType st = MediaSourceType.HLS;
            Logger.d(TAG, "fileToPlay:" + fileToPlay);
            url = playlistProxy.makeUrl(fileToPlay, st, params);
        } catch (ErrorCodeException e) {
            e.printStackTrace();
        }
        return url;
    }

}

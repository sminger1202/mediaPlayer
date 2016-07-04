package com.youku.player.drm;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;

import com.baseproject.utils.Logger;
import com.intertrust.wasabi.Runtime;
import com.intertrust.wasabi.ErrorCodeException;
import com.intertrust.wasabi.Runtime.Property;
import com.intertrust.wasabi.drm.DateTime;
import com.intertrust.wasabi.drm.Engine;
import com.youku.libmanager.SoUpgradeStatics;

public class MarlinDrmManager {
    private static final String TAG = "drm";
    private Context m_context;

    static {
        String path = SoUpgradeStatics.getDRMSo(com.baseproject.utils.Profile.mContext);
        Logger.d(TAG, "path:" + path);
        System.load(path);
    }


    public MarlinDrmManager(Context context) {
        m_context = context;
        try {
            com.intertrust.wasabi.Runtime.setProperty(Property.ROOTED_OK, true);
            Runtime.initialize(m_context.getDir("wasabi", Context.MODE_PRIVATE)
                    .getAbsolutePath());
            Logger.d(
                    TAG,
                    "MarlinDrmManager "
                            + m_context.getDir("wasabi", Context.MODE_PRIVATE)
                            .getAbsolutePath());
            Engine m_engine = new Engine();
            MarlinBroadbandTransactionListener listener = new MarlinBroadbandTransactionListener();
            m_engine.addTransactionListener(listener);
            if (!m_engine.isPersonalized()) {
                m_engine.personalize(null);
                if (!listener.isSuccessed()) {
                    Logger.d(TAG, "personalize failed");
                    m_engine.personalize(null);
                }

            }
            DateTime date = m_engine.getTrustedTime();
            m_engine.destroy();
            Logger.d(TAG, "m_engine.getTrustedTime() TimeZone "
                    + date.toCalendar().getTimeZone().toString());
            Logger.d(
                    TAG,
                    "Date = year " + date.getYear() + ", month "
                            + date.getMonth() + ", day " + date.getDay() + " "
                            + date.getHours() + ":" + date.getMinutes() + ":"
                            + date.getSeconds());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean acquireLicense(String tokenUrl) {
        try {
            Engine m_engine = new Engine();
            MarlinBroadbandTransactionListener listener = new MarlinBroadbandTransactionListener();
            m_engine.addTransactionListener(listener);
            String token = tokenUrl;
            Logger.d(TAG, "token: " + token);
            m_engine.processServiceToken(token);
            m_engine.destroy();
            Logger.d(TAG,"processServiceToken over");
            return listener.isSuccessed();
        } catch (ErrorCodeException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String downloadRegisterToken(String serverAddr, String userName,
                                        String password) {
        String tokenUrl = "http://" + serverAddr
                + "/RegisterMobile.php?UserName=" + userName + "&Password="
                + password;
        return downloadToken(tokenUrl);
    }

    public String downloadRegisterToken(String tokenUrl) {
        return downloadToken(tokenUrl);
    }

    public String downloadLicenseToken(String serverAddr, String userName,
                                       String productId, String licenseType) {
        String tokenUrl = "http://" + serverAddr
                + "/BuyContentMobile.php?UserName=" + userName + "&ProductId="
                + productId + "&LicenseType=" + licenseType;
        return downloadToken(tokenUrl);
    }

    public String downloadLicenseToken(String tokenUrl) {
        return downloadToken(tokenUrl);
    }

    public String downloadToken(String tokenUrl) {

        HttpResponse rsp = null;
        HttpClient httpclient = new DefaultHttpClient();

        HttpGet httpget = new HttpGet(tokenUrl);
        try {
            rsp = httpclient.execute(httpget);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        if (rsp.getStatusLine().getStatusCode() == 200) {
            HttpEntity entity = rsp.getEntity();
            if (entity != null) {
                try {
                    return EntityUtils.toString(entity);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

}

package com.youku.player.reporter;

import com.baseproject.utils.Logger;
import com.youku.player.LogTag;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by liangji on 15/8/12.
 */
public class HttpUtilImpl implements IHttpUtil {
    protected void init(HttpURLConnection conn,String file)
    {
        conn.setDoOutput(true);
        conn.setChunkedStreamingMode(0);
        conn.setRequestProperty("connection", "Keep-Alive");
        conn.setRequestProperty("Charset", "UTF-8");
        conn.setRequestProperty("Content-Type","multipart/form-data;name=file");
        conn.setRequestProperty("filename", file);

    }
    protected boolean readStream(HttpURLConnection conn) throws IOException
    {
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        // 定义BufferedReader输入流来读取URL的响应
        String line = null;
        while ((line = in.readLine()) != null) {
            Logger.d(LogTag.TAG_PLAYER, "upload log return: " + line);
        }
        in.close();

        int code=conn.getResponseCode();
        if (code!=HttpURLConnection.HTTP_OK)
        {
            Logger.e(LogTag.TAG_PLAYER, "upload log error: " + code+" "+ conn.getResponseMessage());
            return false;
        }
        return true;
    }
    protected void writeStream(HttpURLConnection conn, File file) throws IOException
    {
        OutputStream out = new DataOutputStream(conn.getOutputStream());
        InputStream in = new DataInputStream(new FileInputStream(file));
        int bytes = 0;
        byte[] bufferOut = new byte[4*1024];
        while ((bytes = in.read(bufferOut)) != -1) {
            out.write(bufferOut, 0, bytes);
        }
        in.close();
        out.flush();
        out.close();
    }
    public boolean upload(String weburl, String file)
    {
        HttpURLConnection conn=null;
        boolean ret=false;
        try {
            File f=new File(file);
            if (!f.exists())
            {
                Logger.e(LogTag.TAG_PLAYER, "upload log error: " + file);
                return ret;
            }

            URL url = new URL(weburl);
            conn = (HttpURLConnection)url.openConnection();
            init(conn, file);

            writeStream(conn, f);

            ret=readStream(conn);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            Logger.e(LogTag.TAG_PLAYER, "upload log error: " + e.toString());
        }
        finally {
            if (conn!=null)
                conn.disconnect();
        }
        return ret;
    }
}

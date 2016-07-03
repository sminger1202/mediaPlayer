package com.youku.player.reporter;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.widget.Toast;

import com.baseproject.utils.Logger;
import com.youku.analytics.data.Device;
import com.youku.libmanager.FileUtils;
import com.youku.player.LogTag;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by liangji on 15/8/4.
 * 日志生成类，实现生产日志，压缩并上传
 */
public class LogWorker extends Thread{

    protected String mPath;
    protected String logName;
    protected long logTime;
    protected Context context=null;
    protected Handler mHandler= new Handler(){};

    protected String tag;
    protected String url;

    protected IHttpUtil httpUtil=null;

    /**
     * 构造函数
     *
     * @param path
     *            日志文件路径
     * @param log
     *            日志文件名
     * @param logtime
     *            日志记录时间
     * @param Tag
     *            日志TAG
     * @param url
     *            上传网网址
     * @param context
     *            上下文
     * @param http
     *            http上传类
     * */
    public LogWorker(String path, String log,long logtime,String Tag,String url,Context context,IHttpUtil http) {
        logName=log;
        mPath=path;
        logTime=logtime;
        this.context=context;
        tag=Tag;
        this.url=url;
        httpUtil=http;
    }

    @Override
    public void run() {
        super.run();
        startLog();
    }

    public void setExit()
    {
        interrupt();
    }
    private void startLog() {

        Process process = null;

        showToast(com.youku.android.player.R.string.start_log);
        FileUtils.creatDir(mPath);

        com.baseproject.utils.Logger.setDebugMode(true);

        String log = mPath + logName;
        Logger.d(LogTag.TAG_PLAYER, "start log " + log);

        writeInformation(log);


        try {
            process = Runtime.getRuntime().exec("logcat -f "+log+" -v time -s "+tag);
        } catch (IOException e) {
            Logger.e(LogTag.TAG_PLAYER, "error in startLog");
            com.baseproject.utils.Logger.setDebugMode(false);
            return;
        }

        try
        {
            Thread.sleep(logTime);
        }catch (InterruptedException e) {
            Logger.e(LogTag.TAG_PLAYER, "interrupt log");
        }

        process.destroy();

        Logger.d(LogTag.TAG_PLAYER, "stop log");


        String zipfile=log + ".zip";
        ZipUtil.zip(log, zipfile);

        Logger.d(LogTag.TAG_PLAYER, "zip complete");

        com.baseproject.utils.Logger.setDebugMode(false);

        Delete(log);

        showToast(com.youku.android.player.R.string.stop_log);

        if (httpUtil!=null) {
            httpUtil.upload(url, zipfile);
        }
    }


    protected void showToast(final int resId){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, context.getString(resId), Toast.LENGTH_SHORT).show();
            }
        });

    }

    protected void writeInformation(String log)
    {
        PrintStream out = null;

        try {

            out = new PrintStream(new File(log));
            out.println(Device.guid);
            out.println(getSWInfo());

            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            if (out!=null)
                out.close();
        }

    }
    protected String getSWInfo()
    {
        PackageManager manager = context.getPackageManager();
        String str="";

        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            if (info!=null)
                str = info.packageName+" "+info.versionName+" "+info.versionCode;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return str;

    }
    protected void Delete(String file)
    {
        File f=new File(file);
        f.delete();
    }
}

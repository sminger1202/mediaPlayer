package com.youku.player.reporter;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.baseproject.utils.Logger;
import com.youku.player.LogTag;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by liangji on 15/8/4.
 * 日志管理接口，实现日志的设置，创建及退出
 */
public class LogManager {
    private static LogManager INSTANCE = null;

    protected LogWorker worker=null;

    protected long logTime;
    protected String tag;
    protected String url="";

    public static LogManager getInstance()
    {
        if (INSTANCE == null)
        {
            INSTANCE = new LogManager();
        }
        return INSTANCE;
    }


    private LogManager()
    {
        tag = LogTag.TAG_PLAYER;
        logTime=1*60*1000;
    }

    /**
     * 设置参数
     *
     * @param logTag
     *            要记录的日志TAG
     * @param time
     *            记录时长
     */
    public static void setParameter(String logTag,long time,String url) {
        getInstance().tag = logTag;
        getInstance().logTime=time;
        getInstance().url=url;
    }

    public static void start(Context context)
    {
        String folderPath = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
            folderPath = context.getExternalFilesDir(null).getAbsolutePath() +  "/logs/";
        }
        else
        {
            folderPath = context.getFilesDir().getAbsolutePath() + "/logs/";
        }

        getInstance().init(context,folderPath);
    }


    public static void stop()
    {
        getInstance().destory();
    }

    public void init(Context context,String folderPath)
    {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd-HH#mm#ss");
        String log= sdf.format(new Date())+".log";
        if (worker!=null && worker.isAlive())
        {
            Logger.d(LogTag.TAG_PLAYER, "log already start");
            return;
        }
        worker=new LogWorker(folderPath,log,logTime,tag,url,
                context.getApplicationContext(),null);//new HttpUtilImpl());
        worker.start();
    }


    protected void destory()
    {
        if (worker != null)
        {
            if (worker.isAlive())
                worker.setExit();
            worker = null;
        }
    }
}

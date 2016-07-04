package com.youku.player.reporter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;

import com.baseproject.utils.Logger;
import com.youku.libmanager.FileUtils;
import com.youku.libmanager.SoUpgradeStatics;
import com.youku.player.LogTag;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liangji on 15/8/3.
 * 统计键盘计数
 */
public class KeyCounter {
    protected ArrayList<Integer> mKeyList=null;
    protected int[] mDefineKeyArray=null;
    protected long mLastTime=0;

    public final long EXPIRE_TIME=3000;

    public KeyCounter(int defineKeyArray[])
    {
        mLastTime=System.currentTimeMillis();
        mKeyList=new ArrayList<Integer>();
        mDefineKeyArray=new int[defineKeyArray.length];
        mDefineKeyArray=defineKeyArray.clone();
    }

    /**
     * 加入键值
     *
     * @param context
     *            上下文
     * @param keyCode
     *            键值
     * @return
     *        true - 达到激活条件，启动日志记录，false - 未达到激活条件
     */
    public boolean addKey(Context context,int keyCode)
    {
        long diff=System.currentTimeMillis()-mLastTime;
        Logger.d(LogTag.TAG_PLAYER, "KeyCounter addkey interval=" + diff);
        if (diff>EXPIRE_TIME)
            mKeyList.clear();

        mLastTime=System.currentTimeMillis();
        mKeyList.add(keyCode);

        if (checkKey())
        {
            mKeyList.clear();
            Start(context);
            return true;
        }
        else
            return false;

    }
    public void stop()
    {
        //LogManager.stop();
    }
    protected boolean checkKey()
    {
        if (mKeyList.size()!=mDefineKeyArray.length)
            return false;

        int i=0;
        for (Integer key:mKeyList)
        {
            if (key!=mDefineKeyArray[i++])
                return false;
        }
        return true;
    }
    protected void Start(Context context)
    {
        //设置参数
        //LogManager.setParameter(LogTag.TAG_PLAYER+" "+LogTag.TAG_ORIENTATION,2*60*1000);
        //LogManager.start(context);
    }

}

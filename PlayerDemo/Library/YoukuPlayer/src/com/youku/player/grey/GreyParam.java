package com.youku.player.grey;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by liangji on 15/8/25.
 */
public class GreyParam {
    public final static int FUN_NOTFOUND=-1;
    public final static int FUN_OFF=0;
    public final static int FUN_ON=1;
    public final static int FUN_HIT=2;

    Map<Integer,Integer> deliverMap=null;
    Set<Integer> hitSet=null;

    public int hit_config_id=0;
    public int hit_state=0;

    public GreyParam()
    {
        deliverMap=new HashMap<Integer,Integer>();
        hitSet=new HashSet<Integer>();
    }


    public void addDeliver(int feature_id, int state)
    {
        deliverMap.put(feature_id, state);
    }
    public void addHit(int feature_id)
    {
        hitSet.add(feature_id);
    }

    public int isFeatureEnable(int feature_id)
    {
        int state;
        if (deliverMap.containsKey(feature_id)) {
            state = deliverMap.get(feature_id);
            if (state == FUN_OFF || state == FUN_ON)
                return state;
        }
        if (hitSet.contains(feature_id)) {
            state = hit_state;
            if (state == FUN_OFF || state == FUN_ON)
                return state;

        }
        return FUN_NOTFOUND;
    }
}

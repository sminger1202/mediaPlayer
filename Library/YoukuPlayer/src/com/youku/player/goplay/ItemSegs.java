package com.youku.player.goplay;

import java.util.ArrayList;
import java.util.List;

public class ItemSegs {
    private List<ItemSeg> mSegs;
    private boolean h265;

    public void add(ItemSeg item) {
        if (mSegs == null)
            mSegs = new ArrayList<>();
        mSegs.add(item);
    }

    public void setSegs(List<ItemSeg> segs, boolean h265){
        mSegs = segs;
        this.h265 = h265;
    }

    public int size() {
        if (mSegs == null)
            return 0;
        return mSegs.size();
    }

    public ItemSeg get(int i) {
        if (mSegs == null)
            return null;
        return mSegs.get(i);
    }

    public boolean h265() {
        return h265;
    }

    public List<ItemSeg> getSegs() {
        return mSegs;
    }
}

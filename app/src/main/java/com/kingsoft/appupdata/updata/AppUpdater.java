package com.kingsoft.appupdata.updata;
/*
 * 所有的更新的操作都只能通过AppUpdater这个类
 * */

import com.kingsoft.appupdata.updata.net.KNetManager;
import com.kingsoft.appupdata.updata.net.OkHttpNetManager;

public class AppUpdater {
    // 使这个类变成一个单例的类
    private static AppUpdater sInstance = new AppUpdater();
    private KNetManager mNetManager = new OkHttpNetManager();

    // 交由使用者自己决定，当前的NetManager是哪一个Manager
    public void setNetManager(KNetManager mNetManager) {
        this.mNetManager = mNetManager;
    }
    // get KNetManager
    public KNetManager getNetManager()
    {
        return mNetManager;
    }


    //网络下载的能力
    public static AppUpdater getInstance()
    {
        return sInstance;
    }
}

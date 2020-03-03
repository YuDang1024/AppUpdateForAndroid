package com.kingsoft.appupdata.updata.net;

import java.io.File;

public interface KNetManager {

    //需要让服务器返回一段json数据，告诉我们要不要进行应用升级
    void get(String url,KNetGetCallback callback,Object tag);
    // 下载
    void  download(String url, File targetFile,KNetDownLoadCallback callback,Object tag);
    // 取消
    void cancel(Object tag);
}

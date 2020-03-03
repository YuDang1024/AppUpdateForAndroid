package com.kingsoft.appupdata.updata.net;

import java.io.File;

public interface KNetDownLoadCallback {
    // 下载成功应该返回一个文件，这个时候应该是一个apk文件
    void downloadSuccess(File apkFile);
    // 下载失败抛出异常
    void downloadfailed(Throwable throowable);
    // 下载的过程返回进度
    void downloadProgress(int progress);
}

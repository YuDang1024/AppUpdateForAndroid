package com.kingsoft.appupdata.updata.net;

public interface KNetGetCallback {
    // get成功的话应该返回一个json字符串
    void getSuccess(String reponse);
    //get失败抛出异常
    void getFailed(Throwable throeable);
}

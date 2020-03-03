package com.appupdata;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.appupdata.updata.AppUpdater;
import com.appupdata.updata.DownLoadBean.DownLoadBean;
import com.appupdata.updata.net.KNetDownLoadCallback;
import com.appupdata.updata.net.KNetGetCallback;
import com.appupdata.updata.net.OkHttpNetManager;
import com.appupdata.updata.ui.UpdateVersionShowDialog;
import com.appupdata.updata.utils.AppUtils;
import com.kingsoft.appupdata.R;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.CopyOnWriteArrayList;

public class MainActivity extends AppCompatActivity {
    private Button mBtnupdater;
    private static Context context;

    public static Context getAppContext()
    {
        return context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        OkHttpNetManager.handleSSLHandshake();

        mBtnupdater = findViewById(R.id.btn_updater);
        mBtnupdater.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                AppUpdater.getInstance().getNetManager().get("https://dev.ideacom.cn/static/UpdateJson.json", new KNetGetCallback() {
                    @Override
                    public void getSuccess(final String reponse) {
                        Log.d("Respon",reponse.toString());
                        // 1、成功的话，就会返回一个json数据，要解析json，做版本匹配，
                        // 2、如果需要更新，则弹窗，点击下载

                        DownLoadBean bean = DownLoadBean.parse(reponse);
                        if(bean == null)
                        {
                            Toast.makeText(MainActivity.this,"版本检测返回数据异常",Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // 检测弹窗(Ver sionCode的一个判断)
                        // 使用异常捕获，是因为有可能返回的versionCode可能不一定是long
                        try {
                            long versionCode = Long.parseLong(bean.versionCode);
                            if (versionCode <= AppUtils.getVersionCode(MainActivity.this))
                            {
                                //不需要更新
                                Toast.makeText(MainActivity.this,"已是最新版本",Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this,"版本检测返回的版本号异常",Toast.LENGTH_SHORT).show();
                        }

                        // 弹窗
                        UpdateVersionShowDialog.show(MainActivity.this,bean);
                    }

                    @Override
                    public void getFailed(Throwable throwable) {
                        throwable.printStackTrace();
                        Toast.makeText(MainActivity.this,"版本更新接口失败",Toast.LENGTH_SHORT).show();
                    }
                },MainActivity.this);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppUpdater.getInstance().getNetManager().cancel(MainActivity.this);
    }
}

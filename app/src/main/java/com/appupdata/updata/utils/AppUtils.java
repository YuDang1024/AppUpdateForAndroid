package com.appupdata.updata.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.content.FileProvider;

import com.kingsoft.appupdata.BuildConfig;
import com.meituan.android.walle.WalleChannelReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipFile;

public class AppUtils {
    public static long getVersionCode(Context context)
    {
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageArchiveInfo(context.getPackageCodePath(),PackageManager.GET_ACTIVITIES);

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            {
                return packageInfo.getLongVersionCode();
            }
            else
            {
                return packageInfo.versionCode;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  -1;
    }

    public static void installApk(Activity activity, File apkFile)
    {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            uri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID+".provider",apkFile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
        else
        {
            uri = Uri.fromFile(apkFile);
        }
        intent.setDataAndType(uri,"application/vnd.android.package-archive");
        activity.startActivity(intent);
    }

    public static String getFileMD5(File targetFile) {
        if (targetFile == null || !targetFile.isFile())
        {
            return null;
        }

        MessageDigest digest = null;
        FileInputStream in = null;
        byte[] buffer = new byte[1024];
        int length = 0;

        try {
            digest = MessageDigest.getInstance("md5");
            in = new FileInputStream(targetFile);
            while ((length = in.read(buffer))!=-1)
            {
                digest.update(buffer,0,length);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }finally {
            if (in != null)
            {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        byte[] result = digest.digest();
        BigInteger bigInt = new BigInteger(1,result);
        return bigInt.toString(16);
    }

    public static String getChannelData(Context context)
    {
        String channel = WalleChannelReader.getChannel(context);
        return channel;
    }
}

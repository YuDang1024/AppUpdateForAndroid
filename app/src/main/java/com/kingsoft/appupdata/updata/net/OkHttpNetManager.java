package com.kingsoft.appupdata.updata.net;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.kingsoft.appupdata.MainActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
//import java.util.logging.Handler;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class OkHttpNetManager implements KNetManager{

    private static OkHttpClient sOkHttpClient;

    private static Handler sHandler = new Handler(Looper.getMainLooper());

    static {
        /*
        // 使用http服务器
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(15, TimeUnit.SECONDS);
        sOkHttpClient = builder.build();
        */

        /*
        // 忽略https证书
        sOkHttpClient = builder.sslSocketFactory(sslContext.getSocketFactory())
                .hostnameVerifier(new TrustAllHostnameVerifier())
                .build();
        */

        // 使用https服务器
        /*
        * 需要设置https的证书*/
        sOkHttpClient = getOkHttpClient_newInterface();

    }

    @Override
    public void get(String url,final KNetGetCallback callback,Object tag)
    {
        // 通过request builder 获得一个request对象
        // 通过request获得一个call对象
        // 通过call对象去执行execute/enqueue
        Request.Builder builder =  new Request.Builder();
        // 返回一个request对象
        Request request = builder.url(url).get().tag(tag).build();
        Call call = sOkHttpClient.newCall(request);

        // 该接口会直接返回一个结果，类似与一个同步的操作
        //call.execute();

        // 类似于使用一个队列进行异步的操作
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                //不一定是UI线程，但是我们希望是UI线程，因为可能牵扯一些UI的更新
                sHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.getFailed(e);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    final String string = response.body().string();
                    sHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.getSuccess(string);
                        }
                    });
                }catch (Throwable e)
                {
                    e.printStackTrace();
                    callback.getFailed(e);
                }
            }
        });
    }

    @Override
    public void  download(String url, final File targetFile, final KNetDownLoadCallback callback,Object tag)
    {
        if (!targetFile.exists())
        {
            //创建文件夹
            targetFile.getParentFile().mkdirs();
        }

        Request.Builder builder = new Request.Builder();
        final Request request = builder.url(url).get().tag(tag).build();
        Call call = sOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                sHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.downloadfailed(e);
                    }
                });
            }

            @Override
            public void onResponse(final Call call, Response response) throws IOException {
                InputStream is = null;
                OutputStream os = null;
                try {

                    final long totalLength= response.body().contentLength();

                    is = response.body().byteStream();
                    os = new FileOutputStream(targetFile);

                    // 写文件的buffer
                    byte[] buffer = new byte[8 *1024];
                    // 当前写了多少个字节
                    long curLenngth = 0;
                    //
                    int bufferLength = 0;

                    while (!(call.isCanceled())  && (bufferLength = is.read(buffer)) !=-1)
                    {
                        os.write(buffer,0,bufferLength);
                        os.flush();
                        // 这个累加就是为了回调progress
                        curLenngth +=bufferLength;

                        final long finalCurLenngth = curLenngth;
                        sHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.downloadProgress((int)(finalCurLenngth * 1.0f / totalLength *100));
                            }
                        });
                    }

                    // 设置文件的可读可写可执行
                    try {
                        targetFile.setExecutable(true,false);
                        targetFile.setReadable(true,false);
                        targetFile.setWritable(true,false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (call.isCanceled())
                    {
                        return;
                    }

                    sHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.downloadSuccess(targetFile);
                        }
                    });
                } catch (final IOException e) {
                    if(call.isCanceled())
                    {
                        return;
                    }
                    e.printStackTrace();
                    sHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.downloadfailed(e);
                        }
                    });
                }finally {
                    if (is != null)
                    {
                        is.close();
                    }

                    if (os != null)
                    {
                        os.close();
                    }
                }
            }
        });
    }

    @Override
    public void cancel(Object tag)
    {
        // 获取等待的call
        List<Call> queuedCallList = sOkHttpClient.dispatcher().queuedCalls();
        if (queuedCallList != null)
        {
            for (Call call:queuedCallList)
            {
                if (tag.equals(call.request().tag()))
                {
                    call.cancel();
                }
            }
        }

        // 获取正在获取的call
        List<Call> runningCdallList = sOkHttpClient.dispatcher().runningCalls();
        if (runningCdallList != null)
        {
            for (Call call:runningCdallList)
            {
                if (tag.equals(call.request().tag()))
                {
                    call.cancel();
                }
            }
        }
    }

    private static OkHttpClient getOkHttpClient_newInterface()
    {
        try{
            SSLContext sslContext = null;
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            InputStream certificate = MainActivity.getAppContext().getAssets().open("GoodCloud.cer");
            String certificateAlias = Integer.toString(0);
            keyStore.load(null);
            keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificate));
            final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:"
                        + Arrays.toString(trustManagers));
            }
            X509TrustManager trustManager = (X509TrustManager) trustManagers[0];
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[] { trustManager }, new SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            OkHttpClient client = new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, trustManager)
                    .build();
            return client;
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void handleSSLHandshake() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};

            SSLContext sc = SSLContext.getInstance("TLS");
            // trustAllCerts信任所有的证书
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        } catch (Exception ignored)
        {
            ignored.printStackTrace();
        }
    }

    private static class TrustAllHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;//，无视hostName，直接返回true，表示信任所有主机,可跳过证书的验证
        }

    }



}

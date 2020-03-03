package com.appupdata.updata.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.appupdata.updata.AppUpdater;
import com.appupdata.updata.DownLoadBean.DownLoadBean;
import com.appupdata.updata.net.KNetDownLoadCallback;
import com.appupdata.updata.utils.AppUtils;
import com.kingsoft.appupdata.R;

import java.io.File;
import java.io.FileOutputStream;

public class UpdateVersionShowDialog extends DialogFragment {

    private static final String KEY_DOWN_LOAD_BEAN = "download_bean";
    private DownLoadBean mDownLoadBean;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null)
        {
            mDownLoadBean = (DownLoadBean)arguments.getSerializable(KEY_DOWN_LOAD_BEAN);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_updater,container,false);
        bindEvents(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void bindEvents(View view)
    {
        TextView tvTitle = view.findViewById(R.id.text_title);
        TextView tvContent = view.findViewById(R.id.text_content);
        final TextView tvUpdate = view.findViewById(R.id.text_update);

        tvTitle.setText(mDownLoadBean.title);
        tvContent.setText(mDownLoadBean.content);
        tvUpdate.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(final View view)
            {
                view.setEnabled(false);
                final File targetFile = new File(getActivity().getCacheDir(),"updateinstall.apk");

                AppUpdater.getInstance().getNetManager().download(mDownLoadBean.url, targetFile, new KNetDownLoadCallback() {
                    @Override
                    public void downloadSuccess(File apkFile) {
                        view.setEnabled(true);
                        Log.d("DY",apkFile.getAbsolutePath());

                        // 下载成功之后开始隐藏,然后安装
                        dismiss();

                        // check md5,然后安装
                        String fileMD5 = AppUtils.getFileMD5(targetFile);
                        Log.d("md5 = ",fileMD5);

                        if (fileMD5 != null && fileMD5.equals(mDownLoadBean.md5))
                        {
                            // 安装
                            AppUtils.installApk(getActivity(),apkFile);
                        }
                        else
                        {
                            Toast.makeText(getActivity(),"MD5检测失败，文件被损坏",Toast.LENGTH_SHORT).show();
                        }


                    }

                    @Override
                    public void downloadfailed(Throwable throowable) {
                        view.setEnabled(true);
                        Toast.makeText(getActivity(),"下载失败",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void downloadProgress(int progress) {
                        Log.d("DY","progress" + progress);
                        tvUpdate.setText(progress + "%");
                    }
                },UpdateVersionShowDialog.this);
            }
        });
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        AppUpdater.getInstance().getNetManager().cancel(this);
    }

    public static void show(FragmentActivity activity, DownLoadBean bean)
    {
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_DOWN_LOAD_BEAN,bean);
        UpdateVersionShowDialog dialog = new UpdateVersionShowDialog();
        dialog.setArguments(bundle);
        if (bean.updateLevel.equals("HIGH"))
        {
            dialog.setCancelable(false);
        }

        dialog.show(activity.getSupportFragmentManager(),"UpdateShowVersionDialog");
    }
}

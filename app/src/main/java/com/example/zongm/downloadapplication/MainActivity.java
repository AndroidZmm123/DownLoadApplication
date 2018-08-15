package com.example.zongm.downloadapplication;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity {

    private String url = "https://cdn.awsbj0.fds.api.mi-img.com/huami-watch-production/1533720247_BYah3yp6EGVw_a8cf53f3efa5a2bef1951e406fc9b8be.apk?GalaxyAccessKeyId=5961778546660&Expires=316893720247000&Signature=j3EJrqKJFSEgExVIuu08izr7xAg=";

    private DownloadReceiver mReceiver;


    public static final int REQUEST_CODE_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerDownLoadReceiver();
        findViewById(R.id.btn_down).setOnClickListener((view) -> {
            download();
        });
    }

    public void download() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission(REQUEST_CODE_PERMISSION);
            return;
        }
        downLoadApk();
    }


    /**
     * 检查权限
     *
     * @param requestCode
     */
    private void checkPermission(int requestCode) {

        boolean granted = PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
            Manifest.permission_group.CAMERA);
        if (granted) {//有权限
            downLoadApk();
            return;
        }
        //没有权限的要去申请权限
        //注意：如果是在Fragment中申请权限，不要使用ActivityCompat.requestPermissions,
        // 直接使用Fragment的requestPermissions方法，否则会回调到Activity的onRequestPermissionsResult
        ActivityCompat.requestPermissions(this, new String[]{Manifest
                .permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
            requestCode);

    }

    private void downLoadApk() {
        DownLoadManager.doloadApk(this, url);
    }

    private void registerDownLoadReceiver() {

        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        filter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
        mReceiver = new DownloadReceiver();
        registerReceiver(mReceiver, filter);

    }


    class DownloadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                installApk(context, id);
            } else if (intent.getAction().equals(DownloadManager.ACTION_NOTIFICATION_CLICKED)) {
                // DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                //获取所有下载任务Ids组
                //long[] ids = intent.getLongArrayExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS);
                ////点击通知栏取消所有下载
                //manager.remove(ids);
                //Toast.makeText(context, "下载任务已取消", Toast.LENGTH_SHORT).show();
                //处理 如果还未完成下载，用户点击Notification ，跳转到下载中心
                Intent viewDownloadIntent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
                viewDownloadIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(viewDownloadIntent);
            }
        }
    }

    private void installApk(Context context, long id) {

        DownloadManager dManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        //方法一：
        //Intent install = new Intent(Intent.ACTION_VIEW);
        //Uri downloadFileUri = dManager.getUriForDownloadedFile(id);
        //Log.e("zmm", "安装apk---------->" + downloadFileUri);
        ////content://downloads/all_downloads/689
        //String path1 = ImageUtils.getPath(this, downloadFileUri);//根据虚拟路径拿到对应的真实路径
        //Log.e("zmm", "真实路径--------->" + path1);
        /////storage/emulated/0/Download/1533720247_BYah3yp6EGVw_a8cf53f3efa5a2bef1951e406fc9b8be-2.apk
        // Uri downloaded = ImageUtils.getUri(this, path1);
        // 因为7.0及其以上需要用FileProvider。这里面是又把真实路径再转换成Uri.
        //Log.e("zmm", "虚拟路径Uri--------->" + downloaded);
        ////content://com.example.zongm.testapplication.provider/external_storage_root/Download/1533720247_BYah3yp6EGVw_a8cf53f3efa5a2bef1951e406fc9b8be-2.apk
        //Intent intent = new Intent(Intent.ACTION_VIEW);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        //    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //}
        //intent.setDataAndType(downloaded, "application/vnd.android.package-archive");
        //context.startActivity(intent);

        //方法二：
        Uri downloaded = null;
        Cursor q = null;
        try {
            q = dManager.query(new DownloadManager.Query().setFilterById(id));
            if (q != null && q.moveToFirst()) {
                //Android 7.0 或更高版本开发的应用在尝试访问DownloadManager.COLUMN_LOCAL_FILENAME时
                // 会触发java.lang.SecurityException。我们需要更换成DownloadManager.COLUMN_LOCAL_URI
                //String p = q.getString(q.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                String fileUri = q.getString(q.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                Log.e("zmm", "虚拟路径---------->" + fileUri);
                //file:///storage/emulated/0/Download/1533720247_BYah3yp6EGVw_a8cf53f3efa5a2bef1951e406fc9b8be-6.apk

                String path = ImageUtils.getPath(this, Uri.parse(fileUri));
                Log.e("zmm", "真实路径---------->" + path);
                // /storage/emulated/0/Download/1533720247_BYah3yp6EGVw_a8cf53f3efa5a2bef1951e406fc9b8be-6.apk
                if (!TextUtils.isEmpty(path)) {
                    downloaded = ImageUtils.getUri(this, path);
                    Log.e("zmm", "转换过后的虚拟路径------------->" + downloaded);
                    //content://com.example.zongm.downloadapplication.provider/external_storage_root/Download/1533720247_BYah3yp6EGVw_a8cf53f3efa5a2bef1951e406fc9b8be-6.apk
                }
                if (downloaded != Uri.EMPTY) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                    intent.setDataAndType(downloaded, "application/vnd.android.package-archive");
                    context.startActivity(intent);
                }
            } else {
                Log.w("zmm", "Unable to parse downloaded file!!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (q != null) {
                q.close();
            }
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            boolean flag = true;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PERMISSION_GRANTED) {
                    flag = false;
                    break;
                }
            }
            //权限通过以后。自动下载apk
            if (flag) {
                if (requestCode == REQUEST_CODE_PERMISSION) {
                    downLoadApk();
                }
            } else {
                Toast.makeText(this, "请开启权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

}

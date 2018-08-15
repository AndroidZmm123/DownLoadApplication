package com.example.zongm.downloadapplication;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

/**
 * @author zongm on 2018/8/15
 */
public class DownLoadManager {

    public static void doloadApk(Context context, String url) {
        Uri uri = Uri.parse(url);
        DownloadManager.Request req = new DownloadManager.Request(uri);
        //设置WIFI下进行更新
        req.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        //下载中和下载完后都显示通知栏
        req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        //使用系统默认的下载路径 此处为应用内 /android/data/packages ,所以兼容7.0
        //req.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, title);
        req.setAllowedOverRoaming(false);
        req.setVisibleInDownloadsUi(true);
        req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
            Uri.parse(url).getLastPathSegment());


        //通知栏标题
        req.setTitle("测试应用.apk");
        //通知栏描述信息
        req.setDescription("下载完成后，点击安装");
        //设置类型为.apk
        req.setMimeType("application/vnd.android.package-archive");
        //获取下载任务ID
        DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        long downloadId = dm.enqueue(req);
        if (downloadId != -1) {
            Log.d("zmm", "下载成功--------->" + downloadId);
            return;
        }
        Log.e("zmm", "下载失败--------->" + downloadId);
    }
}

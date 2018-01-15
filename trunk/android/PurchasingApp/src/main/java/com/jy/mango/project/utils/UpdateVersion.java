package com.jy.mango.project.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by mango on 2017/11/6.
 */

public class UpdateVersion {


    static String  nameChapter = "app-debug";
    Context context;

    public static void download(String url,final Context context) {
        HttpHandler<File> handlerDownload;
        // 判断是否有SD卡
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            long availableSize = availableSize();
            int downloadLength = downloadFileSize(url);
            //判断手机内存是否够下载文件
            if (availableSize >= downloadLength) {
                HttpUtils utils = new HttpUtils();
               final String target = Environment.getDataDirectory()
                        + "/data/com.jy.mango.project/download/" + nameChapter
                        + ".apk";
               handlerDownload = utils.download(url, target, true, true,
                        new RequestCallBack<File>() {
                            @Override
                            public void onStart() {
                                super.onStart();
                                T.showShort("开始下载");

                            }
                            @Override
                            public void onLoading(long total, long current,
                                                  boolean isUploading) {
                                super.onLoading(total, current, isUploading);
                                Log.i("TEST" , current + "/" + total);

                            }
                            @Override
                            public void onSuccess (ResponseInfo< File > arg0) {
                                T.showShort("成功");
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(Uri.fromFile(new File(target)), "application/vnd.android.package-archive");
                                context.startActivity(intent);

                            }

                            @Override
                            public void onFailure (HttpException arg0,
                                    String arg1){
                                T.showShort("失败");
                                Log.i("TEST" , "TEST" + arg1.toString()+arg1);
                            }
                            });
                        }
            } else {
                T.showShort("请插入SD卡");
            }
        }



    /**
     * 获取手机剩余内存大小
     *
     * @return 手机剩余内存(单位：byte)
     */
    public static long availableSize() {
        // 取得SD卡文件路径
        File file = Environment.getExternalStorageDirectory();
        StatFs fs = new StatFs(file.getPath());
        // 获取单个数据块的大小(Byte)
        int blockSize = fs.getBlockSize();
        // 空闲的数据块的数量
        long availableBlocks = fs.getAvailableBlocks();
        return blockSize * availableBlocks;
    }
    /**
     * 获取网络文件大小
     *
     * @param path
     *            文件链接
     * @return 文件大小
     */

    static  int length;
    public static int downloadFileSize(final String path) {

        new Thread() {
            public void run() {
                try {
                    URL url = new URL(path);     //创建url对象
                    HttpURLConnection conn = (HttpURLConnection) url
                            .openConnection();     //建立连接
                    conn.setRequestMethod("GET");    //设置请求方法
                    conn.setReadTimeout(5000);       //设置响应超时时间
                    conn.setConnectTimeout(5000);   //设置连接超时时间
                    conn.connect();   //发送请求
                    int responseCode = conn.getResponseCode();    //获取响应码
                    if (responseCode == 200) {   //响应码是200(固定值)就是连接成功，否者就连接失败
                        length = conn.getContentLength();    //获取文件的大小
                    } else {
                        T.showShort("连接失败");
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
        }.start();
        return length;
    }
}

package com.jy.mango.project.main;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;


import com.jy.mango.project.R;
import com.jy.mango.project.base.BaseFragmentActivity;
import com.jy.mango.project.bean.UtilsBean;
import com.jy.mango.project.home.LoginActivity;
import com.jy.mango.project.recevicer.ExampleUtil;
import com.jy.mango.project.service.UpdateService;
import com.jy.mango.project.utils.PreferenceUtils;
import com.jy.mango.project.utils.T;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


/**
 *  程序主入口
 */

public class MainActivity extends BaseFragmentActivity {

    //推送相关
    public static boolean isForeground = false;

    public com.jy.mango.project.view.FragmentTabHost fTabHost;
    private String[] tabTags = {"tab_home", "tab_today", "tab_history", "tab_mine"};
    private Class[] tabFragments = {HomeFragment.class, TodayFragment.class, HomeFragment3.class, HomeFragment4.class};
    private String[] tabTitles = { "首页", "今日采购", "历史采购", "我的"};
    private int[] tabIcons = {R.drawable.tab_home, R.drawable.tab_contact, R.drawable.tab_activ, R.drawable.tab_mine};

    private ImageView[] ivTabDot;
    private Context context;

    String serviceUrl;
    static ImageView textView;


    String HOST ="";
    private UpdateService.DownloadBinder mDownloadBinder;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mDownloadBinder = (UpdateService.DownloadBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mDownloadBinder = null;
        }
    };

    // 初始化 JPush。如果已经初始化，但没有登录成功，则执行重新登录。
    private void init(){
        JPushInterface.init(getApplicationContext());
        registerMessageReceiver();
        setAlias();
    }

    //权限申请
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context= this;
        textView = getView(R.id.tips);
        HOST = PreferenceUtils.getPrefString(context,"host","");
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        if(PreferenceUtils.getPrefString(context,"message","").equals("1")){
            textView.setVisibility(View.VISIBLE);
        }else {
            textView.setVisibility(View.GONE);
        }
        initTabHost();
//        if(PreferenceUtils.getPrefBoolean(context,"login_state",false)){
//            T.showShort("买好菜上今优");
//        }
        //
        init();
        Intent intent = new Intent(this, UpdateService.class);
        startService(intent);
        bindService(intent, mConnection, BIND_AUTO_CREATE);//绑定服务
        checkUpdate(context);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Long hqtime = PreferenceUtils.getPrefLong(context,"login_time",0);
        Long s = (System.currentTimeMillis() - hqtime) / (1000 * 60);
        if(s > 43200){
            Intent intent = new Intent(context, LoginActivity.class);
            intent.putExtra("data","exit");
            startActivity(intent);
            PreferenceUtils.setPrefBoolean(context,"login_state",false);
            JPushInterface.stopPush(getApplicationContext());
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {

        if (requestCode == MY_PERMISSIONS_REQUEST_CALL_PHONE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                if (mDownloadBinder != null) {
                    long downloadId = mDownloadBinder.startDownload(serviceUrl);
                    startCheckProgress(downloadId);
                }
            } else{
                T.showLong("该应用已被禁止存储权限" +
                        "\n请在设置>权限管理中授权");
                alertDialog.show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    /**
     * 更新框
     */
    AlertDialog alertDialog;
    private void showAlart() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("请升级至最新版本");
        builder.setTitle("升级提示");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which)
            {
                if(PreferenceUtils.getPrefString(context,"permission","").equals("true")){
                    if (mDownloadBinder != null) {
                        long downloadId = mDownloadBinder.startDownload(serviceUrl);
                        startCheckProgress(downloadId);
                        dialog.dismiss();
                    }
                }else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_CALL_PHONE);
                }
            }
        });

        alertDialog = builder.create();
        alertDialog.setCancelable(false);//设置这个对话框不能被用户按[返回键]而取消掉,但测试发现如果用户按了KeyEvent.KEYCODE_SEARCH,对话框还是会Dismiss掉
        //由于设置alertDialog.setCancelable(false); 发现如果用户按了KeyEvent.KEYCODE_SEARCH,对话框还是会Dismiss掉,这里的setOnKeyListener作用就是屏蔽用户按下KeyEvent.KEYCODE_SEARCH
        alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
            {
                if (keyCode == KeyEvent.KEYCODE_SEARCH){
                    return true;
                }else
                {
                    return false; //默认返回 false
                }
            }
        });
        alertDialog.show();
    }


    //开始监听进度
    private void startCheckProgress(long downloadId) {
        Observable
                .interval(100, 200, TimeUnit.MILLISECONDS, Schedulers.io())//无限轮询,准备查询进度,在io线程执行
                .filter(times -> mDownloadBinder != null)
                .map(i -> mDownloadBinder.getProgress(downloadId))//获得下载进度
                .takeUntil(progress -> progress >= 100)//返回true就停止了,当进度>=100就是下载完成了
                .distinct()//去重复
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ProgressObserver());
    }



    //观察者
    private class ProgressObserver implements Observer<Integer> {

        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onNext(Integer progress){//设置进度
        }

        @Override
        public void onError(Throwable throwable) {
            throwable.printStackTrace();
            Toast.makeText(MainActivity.this, "出错", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onComplete() {
            Toast.makeText(MainActivity.this, "下载完成", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 获取版本号
     * @return 当前应用的版本号
     */
    public String getVersion() {
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            String version = info.versionName;
            return  version ;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    protected void onResume() {
        JPushInterface.resumePush(getApplicationContext());
        isForeground = true;
        super.onResume();
    }


    @Override
    protected void onPause() {
        isForeground = false;
        super.onPause();
    }


    public void initTabHost() {
        fTabHost = (com.jy.mango.project.view.FragmentTabHost) findViewById(android.R.id.tabhost);
        fTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
        ivTabDot = new ImageView[tabFragments.length];
        for (int i = 0; i < tabFragments.length; i++) {
            TabHost.TabSpec spec = fTabHost.newTabSpec(tabTags[i]);
            spec.setIndicator(getTabView(i));
            fTabHost.addTab(spec, tabFragments[i], null);
        }

        fTabHost.getTabWidget().setShowDividers(LinearLayout.SHOW_DIVIDER_NONE);
        fTabHost.getTabWidget().getChildTabViewAt(0);

    }

    static ImageView tip;
    public View getTabView(int i ) {
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.view_tab_main, null);
        ivTabDot[i] = (ImageView) view.findViewById(R.id.ivMainTabDot);
        ImageView ivIcon = (ImageView) view.findViewById(R.id.ivMainTabIcon);
        TextView tvTitle = (TextView) view.findViewById(R.id.tvMainTabTitle);
        ivIcon.setImageResource(tabIcons[i]);
        tvTitle.setText(tabTitles[i]);
        return view;
    }


    public static class PushBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String name = intent.getExtras().getString("name");
            Log.i("Recevier1", "接收到:" + name);
            textView.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        unbindService(mConnection);
        finish();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        exitBy2Click();
    }

    private static boolean isExit = false;

    private void exitBy2Click() {
        if (!isExit) {
            isExit = true;
            T.showShort("再按一次退出程序");
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false;
                }
            }, 1500);
        } else {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode){
            case 1:
                Intent intent = new Intent(context, LoginActivity.class);
                intent.putExtra("data","exit");
                startActivity(intent);
                PreferenceUtils.setPrefBoolean(context,"login_state",false);
                JPushInterface.stopPush(getApplicationContext());
                finish();
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private static final String TAG = "JPush";
    private static final int MSG_SET_ALIAS = 1001;

    private void setAlias(){
        String alias = PreferenceUtils.getPrefString(context,"userId","");
        String set = alias.replace("-","");
        Log.i(TAG, "alias" +set);
        //调用JPush API设置Alias
        mHandler.sendMessage(mHandler.obtainMessage(MSG_SET_ALIAS, set));
    }

    private final TagAliasCallback mAliasCallback = new TagAliasCallback() {

        @Override
        public void gotResult(int code, String alias, Set<String> tags) {
            String logs ;
            switch (code) {
                case 0:
                    logs = "Set tag and alias success";
                    Log.i(TAG, logs);
                    break;
                case 6002:
                    logs = "Failed to set alias and tags due to timeout. Try again after 60s.";
                    Log.i(TAG, logs);
                    if (ExampleUtil.isConnected(getApplicationContext())) {
                        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_SET_ALIAS, alias), 1000 * 60);
                    } else {
                        Log.i(TAG, "No network");
                    }
                    break;
                default:
                    logs = "Failed with errorCode = " + code;
                    Log.e(TAG, logs);
            }
            //打印别名
        }
    };

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SET_ALIAS:
                    Log.d(TAG, "Set alias in handler.");
                    JPushInterface.setAliasAndTags(getApplicationContext(), (String) msg.obj, null, mAliasCallback);
                    break;
                default:
                    Log.i(TAG, "Unhandled msg - " + msg.what);
            }
        }
    };

    //for receive customer msg from jpush server
    private MessageReceiver mMessageReceiver;
    public static final String MESSAGE_RECEIVED_ACTION = "com.example.jpushdemo.MESSAGE_RECEIVED_ACTION";
    public static final String KEY_TITLE = "title";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_EXTRAS = "extras";

    public void registerMessageReceiver() {
        mMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        filter.addAction(MESSAGE_RECEIVED_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filter);
    }
    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (MESSAGE_RECEIVED_ACTION.equals(intent.getAction())) {
                    String messge = intent.getStringExtra(KEY_MESSAGE);
                    String extras = intent.getStringExtra(KEY_EXTRAS);
                    StringBuilder showMsg = new StringBuilder();
                    showMsg.append(KEY_MESSAGE + " : " + messge + "\n");
                    if (!ExampleUtil.isEmpty(extras)) {
                        showMsg.append(KEY_EXTRAS + " : " + extras + "\n");
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        //
    }

    public void checkUpdate(final Context context){
        final UtilsBean req = new UtilsBean();
        req.systemcode = "3";
        req.version = getVersion(context);
        req.serviceid = "";
        RequestParams params = new RequestParams();
        try {
            params.setHeader("Content-Type", "application/json; charset=utf-8");
            params.setBodyEntity(new StringEntity(req.toJson()));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        new HttpUtils().send(HttpRequest.HttpMethod.POST,
                "http://servicequery.jinyoufarm.com/api/ServicesQuery/CheckUpdate",
                params, new RequestCallBack<String>() {
                    @Override
                    public void onFailure(HttpException arg0, String arg1) {
                        T.showNetworkError(context);
                    }

                    @Override
                    public void onSuccess(ResponseInfo<String> resp) {
                        JSONObject jsonObject;
                        try {
                            jsonObject = new JSONObject(resp.result.toString());
                            serviceUrl = jsonObject.getString("data");
                            if(!serviceUrl.equals("")){
                                showAlart();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
         });
    }


    /**
     * 获取版本号
     * @return 当前应用的版本号
     */
    public static String getVersion(Context context) {
        String version ="";
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            version = info.versionName;
            return  version ;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return version;
    }

}
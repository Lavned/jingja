package com.jy.mango.project.home;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jy.mango.project.R;
import com.jy.mango.project.base.BaseTopActivity;
import com.jy.mango.project.bean.LoginBean;
import com.jy.mango.project.bean.UserInfo;
import com.jy.mango.project.main.GulidActivity;
import com.jy.mango.project.main.MainActivity;
import com.jy.mango.project.utils.CountDownTimerUtils;
import com.jy.mango.project.utils.PreferenceUtils;
import com.jy.mango.project.utils.ProgressDialogUtil;
import com.jy.mango.project.utils.T;
import com.jy.mango.project.view.MyProgressView;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class LoginActivity extends BaseTopActivity {

    private TextView login, getCode;
    Context context;
    private EditText account, pass;
    private TextView toggle;
    String type = "1";
    Boolean isClick = false;
    int count  = 5;

    String serviceUrl ="";

    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode
                (WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setContentView(R.layout.activity_login);

        context = this;
        serviceUrl = getIntent().getStringExtra("data");
        if(serviceUrl == null){
            T.showShort("服务异常");
        }else{
            switch (serviceUrl){
                case "exit" :
                    T.showShort("退出成功");
                    break;
//                case "" :
//                    T.showShort("服务异常");
//                    break;
            }
        }
        initView();
        getCode.setTextColor(Color.parseColor("#ffffff"));
        getCode.setBackgroundResource(R.drawable.gray_back);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_CALL_PHONE);
        } else{
            PreferenceUtils.setPrefString(context,"permission","true");
        }

        account.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,int count) { }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,  int after) {}
            @Override
            public void afterTextChanged(Editable s) {
                if(account.getText().length() > 11 || account.getText().toString().length() < 11){
                    getCode.setEnabled(false);
                    getCode.setTextColor(Color.parseColor("#ffffff"));
                    getCode.setBackgroundResource(R.drawable.gray_back);
                }else{
                    if(delayRun!=null){//每次editText有变化的时候，则移除上次发出的延迟线程
                        handler.removeCallbacks(delayRun);}
                    //延迟800ms，如果不再输入字符，则执行该线程的run方法
                    handler.postDelayed(delayRun, 200);
                }
            }
        });

    }


    private Handler handler = new Handler();
    /** 延迟线程，看是否还有下一个字符输入 */
    private Runnable delayRun = new Runnable() {

        @Override
        public void run() {
            getCode.setEnabled(true);
            getCode.setTextColor(Color.parseColor("#00A961"));
            getCode.setBackgroundResource(R.drawable.search_back);
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {

        if (requestCode == MY_PERMISSIONS_REQUEST_CALL_PHONE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                T.showShort("已授权");

            } else
            {
                T.showShort("Permission Denied");
                PreferenceUtils.setPrefString(context,"permission","false");
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //界面初始化
    private void initView() {
//        IM_login = getView(R.id.IM_login);
//        IM_login.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
////                builder.setIcon(R.mipmap.delete);
//                builder.setTitle("请选择");
//                final String[] items = {"开发环境", "测试环境"};
//                //    设置一个单项选择下拉框
//                builder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        T.showShort("当前环境为：" + items[which]);
//                        if(items[which].equals("发布环境")){
//                            testType = 0;
//                        }else if(items[which].equals("测试环境")) {
//                            testType = 1;
//                        }
//                        PreferenceUtils.setPrefInt(context,"testType",testType);
//                    }
//                });
//                builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
//                {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which)
//                    {
//                        startActivity(new Intent(context, GulidActivity.class));
//                        finish();
//                    }
//                });
//                builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
//                {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which)
//                    {
//
//                    }
//                });
//                builder.show();
//                return false;
//            }
//        });

        login = getView(R.id.TV_login);
        toggle = getView(R.id.toggle);
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pass.setText("");
                if(!isClick){
                    account.setHint("请输入用户名");
                    pass.setHint("请输入密码");
                    pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    type = "0";
                    getCode.setVisibility(View.GONE);
                    isClick = true;
                    toggle.setText("使用短信验证码登录");
                }
                else {
                    account.setHint("请输入手机号");
                    pass.setHint("请输入验证码");
                    pass.setInputType(InputType.TYPE_CLASS_TEXT);
                    getCode.setVisibility(View.VISIBLE);
                    type = "1";
                    isClick = false;
                    toggle.setText("使用密码登录");
                }

            }
        });
        account = getView(R.id.TV_name);
        pass = getView(R.id.TV_pass);
        getCode = getView(R.id.TV_getCode);
        getCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(account.getText().toString().trim().equals("")){
                    T.showShort("手机号不能为空!");
                }else {
                    if(isPhoneLegal(account.getText().toString().trim())) {
                        getLoginCode(account.getText().toString().trim());
                    }else {
                        T.showShort("您输入的手机号不正确，请重新输入");
                    }
                }

            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPhoneLegal(account.getText().toString().trim())){
                    if(pass.getText().toString().trim().isEmpty()){
                        T.showShort("验证码或密码不可为空！");
                    }else {
                        loginBtn();
                    }
                }else {
                    T.showShort("您输入的手机号不正确，请重新输入！");
                }

            }
        });
    }

    /**
     * 登录请求
     */
    public void loginBtn() {
        if(!LoginActivity.this.isFinishing()){
            MyProgressView.buildProgressDialog(LoginActivity.this,"正在登录...");
        }
        LoginBean req = new LoginBean();
        req.UserName = account.getText().toString();
        req.VerificationCode = pass.getText().toString();
        req.UserType = type;
        RequestParams params = new RequestParams();
        try {
            params.setHeader("Content-Type", "application/json; charset=utf-8");
            params.setBodyEntity(new StringEntity(req.toJson()));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        new HttpUtils().send(HttpRequest.HttpMethod.POST,
                PreferenceUtils.getPrefString(context,"host","") + "/UserLogin", params, new RequestCallBack<String>() {
            @Override
            public void onFailure(HttpException arg0, String arg1) {
                T.showNetworkError(context);
                if(!LoginActivity.this.isFinishing()){
                    MyProgressView.cancelProgressDialog();
                }
            }

            @Override
            public void onSuccess(ResponseInfo<String> resp) {
                if(!LoginActivity.this.isFinishing()){
                    MyProgressView.cancelProgressDialog();
                }
                try {
                    JSONObject dataJSON = new JSONObject(resp.result.toString());
                    Log.i("TESTS" , resp.result.toString());
                    if(dataJSON.getString("code").equals("0")){
                        Gson gson = new Gson();
                        UserInfo userInfo= gson.fromJson(dataJSON.getJSONObject("userInfoModel").toString(),UserInfo.class);
                        saveUserLogin(userInfo);
                        startActivity(new Intent(context, MainActivity.class));
                        finish();
                    }else if(dataJSON.getString("code").equals("2")){
                        count -=1;
                        T.showShort("验证码或密码错误，还可以登录" + count +"次");
                        if(count <= 0){
                            T.showShort("错误次数太多，请退出重试！");
                            login.setEnabled(false);
                            login.setBackgroundResource(R.drawable.gray_back);
                        }
                    }else if(dataJSON.getString("code").equals("3")){
                        T.showShort("没有权限，请联系管理员！");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 获取验证码
     */
    public String getLoginCode(String userkey) {
        if(!LoginActivity.this.isFinishing()){
            MyProgressView.buildProgressDialog(LoginActivity.this,"发送中...");
        }
        String result = null;
        final HttpUtils http = new HttpUtils();
        http.configCurrentHttpCacheExpiry(1000 * 10); //设置超时时间   10s
        http.send(HttpRequest.HttpMethod.GET,
                PreferenceUtils.getPrefString(context,"host","")  + "/SMSSend?username=" + userkey,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        if(!LoginActivity.this.isFinishing()){
                            MyProgressView.cancelProgressDialog();
                        }
                        try {
                            JSONObject jsonObject = new JSONObject(responseInfo.result.toString());
                            if(jsonObject.getString("state").equals("0")){
                                T.showShort("发送成功！");
                                CountDownTimerUtils mCountDownTimerUtils = new CountDownTimerUtils(getCode, 60000, 1000); //倒计时1分钟
                                mCountDownTimerUtils.start();
                            } else if(jsonObject.getString("state").equals("5")){
                                T.showShort("发送次数超出限制！");
                            }else if(jsonObject.getString("state").equals("7")) {
                                T.showShort("该号码未注册！");
                            }else {
                                T.showShort("请稍后重试！");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        T.showNetworkError(context);
                        if(!LoginActivity.this.isFinishing()){
                            MyProgressView.cancelProgressDialog();
                        }
                    }
                });
        return result;
    }


    /**
     * 存储用户登录信息
     * @param userInfo
     */
    public  void saveUserLogin(UserInfo userInfo){
        PreferenceUtils.setPrefBoolean(context,"login_state",true);
        PreferenceUtils.setPrefString(context,"nName",userInfo.nickname);
        PreferenceUtils.setPrefString(context,"sName",userInfo.storehousename);
        PreferenceUtils.setPrefString(context,"userId",userInfo.userID);
        PreferenceUtils.setPrefString(context,"contact",userInfo.contact);
        PreferenceUtils.setPrefString(context,"address",userInfo.address);
        PreferenceUtils.setPrefString(context,"storehouseID",userInfo.storehouseID);
        PreferenceUtils.setSettingLong(context,"login_time",System.currentTimeMillis());
    }

    /**
     * 大陆号码或香港号码均可
     */
    public boolean isPhoneLegal(String str)throws PatternSyntaxException {
        return isChinaPhoneLegal(str) || isHKPhoneLegal(str);
    }

    /**
     * 大陆手机号码11位数，匹配格式：前三位固定格式+后8位任意数
     * 此方法中前三位格式有：
     * 13+任意数
     * 15+除4的任意数
     * 18+除1和4的任意数
     * 17+除9的任意数
     * 147
     */
    public boolean isChinaPhoneLegal(String str) throws PatternSyntaxException {
        String regExp = "^((13[0-9])|(15[^4])|(18[0,1,2,3,4,5-9])|(17[0-8])|(147))\\d{8}$";
        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(str);
        return m.matches();
    }

    /**
     * 香港手机号码8位数，5|6|8|9开头+7位任意数
     */
    public boolean isHKPhoneLegal(String str)throws PatternSyntaxException {
        String regExp = "^(5|6|8|9)\\d{7}$";
        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(str);
        return m.matches();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

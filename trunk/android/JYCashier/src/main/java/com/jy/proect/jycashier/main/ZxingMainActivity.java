package com.jy.proect.jycashier.main;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jy.proect.jycashier.R;
import com.jy.proect.jycashier.bean.PayBean;
import com.jy.proect.jycashier.utils.T;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

public class ZxingMainActivity extends Activity{




	private String  payType ;
	private final static int SCANNIN_GREQUEST_CODE = 1;
	/**
	 * 显示扫描结果
	 */
	private TextView mTextView ;
	/**
	 * 显示扫描拍的图片
	 */
	private ImageView mImageView;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.zxing_activity_main);

		Intent intents = getIntent();
		payType = intents.getStringExtra("payType");
		Log.i("ttttttttttttttpayType",payType);

		mTextView = (TextView) findViewById(R.id.result);
		mImageView = (ImageView) findViewById(R.id.qrcode_bitmap);

		//点击按钮跳转到二维码扫描界面，这里用的是startActivityForResult跳转
		//扫描完了之后调到该界面
//		findViewById(R.id.btn_scan_qr).setOnClickListener(this);
		Intent intent = new Intent();
		intent.setClass(ZxingMainActivity.this, MipcaActivityCapture.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case SCANNIN_GREQUEST_CODE:
				if(resultCode == RESULT_OK){
					Bundle bundle = data.getExtras();
					//显示扫描到的内容
					mTextView.setText(bundle.getString("result"));
					//显示
					mImageView.setImageBitmap((Bitmap) data.getParcelableExtra("bitmap"));
					postJson(bundle.getString("result"));
				}
				break;
		}
	}

	private void postJson(String auth_code) {
//        DialogUIUtils.showLoadingHorizontal(this, "加载中...").show();
		Log.i("tttttttttttttt",auth_code+"paytype" +payType);
		RequestParams params = new RequestParams("http://pay.jinyoufarm.cn/api/payservice/v1/payment");
		final PayBean req = new PayBean();
		req.auth_code = auth_code;
		req.id = System.currentTimeMillis()+"";
		req.paytype = payType;
		req.subject = System.currentTimeMillis()+"";
		req.total_amount = "0.1";
		req.out_trade_no = System.currentTimeMillis()+"";
		req.store_id = "1";
		String json = JSON.toJSONString(req);
		params.setBodyContent(json);
		params.setAsJsonContent(true);
		params.setHeader("Content-Type", "application/json; charset=utf-8");
//        params.addBodyParameter("", json, "application/json");
		x.http().post(params, new Callback.CommonCallback<String>() {
			@Override
			public void onCancelled(CancelledException cex) {
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				Log.i("test", "----" + ex.getMessage() + "===" + ex.toString());
			}

			@Override
			public void onFinished() {
			}

			@Override
			public void onSuccess(String result) {
				//打印服务端返回结果
				T.showShort(result);
			}
		});
	}
}

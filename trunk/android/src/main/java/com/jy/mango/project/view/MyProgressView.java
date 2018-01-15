package com.jy.mango.project.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.TextView;

import com.jy.mango.project.R;
import com.jy.mango.project.base.BaseTopActivity;
import com.jy.mango.project.main.MainActivity;

public class MyProgressView extends Dialog {


	private static  Dialog progressDialog;

	public MyProgressView(@NonNull Context context) {
		super(context);

	}



	/**
	 * @Description: TODO 固定加载提示内容
	 * @param context
	 */
	public static void buildProgressDialog(Context context,String message) {
//		if (progressDialog == null) {
			progressDialog = new Dialog(context, R.style.progress_dialog);
//		}
		progressDialog.setContentView(R.layout.dialog_my);
		progressDialog.setCancelable(true);
		progressDialog.getWindow().setBackgroundDrawableResource(
				android.R.color.transparent);
		TextView msg =  progressDialog.findViewById(R.id.id_tv_loadingmsg);
		msg.setText(message);
		progressDialog.show();

	}

	/**
	 * @Description: TODO 取消加载框
	 */
	public static  void cancelProgressDialog() {
			progressDialog.dismiss();
	}

}

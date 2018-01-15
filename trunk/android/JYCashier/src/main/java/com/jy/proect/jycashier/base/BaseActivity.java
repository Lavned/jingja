package com.jy.proect.jycashier.base;

import android.app.Activity;
import android.app.ActivityGroup;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;

import com.jy.proect.jycashier.application.JYApplication;

/**
 *
 * @Description : activity基类
 * @version : 1.0 Create Date : 2016-1-27
 */
public class BaseActivity extends ActivityGroup
{
	private Handler mhanlder;


	public Context mContext;
	protected final String TAG = this.getClass().getName();
	public int mImageSize;



	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mContext = this;

		if (isAddToStack())
		{
			((JYApplication) getApplication()).addActivity(this);
		}
	}

	public int dpToPx(float dp, Resources resources)
	{
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				resources.getDisplayMetrics());
		return (int) px;
	}

	/** 获取最上层的Activity负责 弹dialog,防止出现 can't add dialog 出错 */
	protected Context getDialogContext()
	{
		Activity activity = this;
		while (activity.getParent() != null)
		{
			activity = activity.getParent();
		}
		Log.d("Dialog", "context:" + activity);
		return activity;
	}

	@Override
	public void finish()
	{
		if (isAddToStack())
		{
			((JYApplication) getApplication()).removeActivty(this);
		}
		super.finish();
	}
	/**
	 * 是否添加至堆栈
	 * 
	 * @return
	 */
	protected boolean isAddToStack()
	{
		return true;
	}
}

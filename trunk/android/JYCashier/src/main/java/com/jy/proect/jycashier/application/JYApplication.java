package com.jy.proect.jycashier.application;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.util.Log;


import com.jy.proect.jycashier.utils.T;

import org.xutils.x;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;


/**
 * Created by mango on 2017/10/28.
 */

public class JYApplication extends Application {

    public static Context applicationContext;
    private static JYApplication instance;

    private static final String TAG = "JYApplication";

    public static JYApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = this;
        instance = this;
        T.init(this);
        x.Ext.init(this);
//        x.Ext.setDebug(true);//是否输出Debug日志
//        CrashHandler crashHandler = CrashHandler.getInstance();
//        crashHandler.init(getApplicationContext());
    }


    private String getAppName(int pID) {
        String processName = null;
        ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List l = am.getRunningAppProcesses();
        Iterator i = l.iterator();
//		PackageManager pm = this.getPackageManager();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());
            try {
                if (info.pid == pID) {
//					CharSequence c = pm.getApplicationLabel(pm.getApplicationInfo(info.processName, PackageManager.GET_META_DATA));
                    // Log.d("Process", "Id: "+ info.pid +" ProcessName: "+
                    // info.processName +"  Label: "+c.toString());
                    // processName = c.toString();
                    processName = info.processName;
                    return processName;
                }
            } catch (Exception e) {
                // Log.d("Process", "Error>> :"+ e.toString());
            }
        }
        return processName;
    }


    private static  Stack<Activity> mActivityStack = new Stack<Activity>();

    /**
     * 添加Activity至堆栈
     *
     * @param activity
     */
    public synchronized void addActivity(Activity activity)
    {
        mActivityStack.add(activity);
    }

    /**
     * 删除堆栈中Activity
     *
     * @param activity
     */
    public synchronized void removeActivty(Activity activity)
    {
        mActivityStack.remove(activity);
    }

    /**
     * 清空堆栈
     */
    public synchronized void cleanStack()
    {
        mActivityStack.clear();
    }

    /**
     * 清除堆栈，忽略ignoreList中对应的Activity
     *
     * @param ignoreList
     */
    public synchronized void cleanStack(List<Class<?>> ignoreList)
    {
        final int size = mActivityStack.size();
        for (int i = size - 1; i >= 0; i--)
        {
            Activity activity = mActivityStack.get(i);
            for (Class<?> classz : ignoreList)
            {
                if (!classz.isInstance(activity))
                {
                    Log.d(TAG, "Finish Activity = "
                            + activity.getClass().getName());
                    mActivityStack.remove(activity);
                    activity.finish();
                }
            }
        }
    }

    /**
     * 清除堆栈，忽略ignoreList中对应的Activity
     *
     * @param
     */
    public synchronized void cleanStack(Class<?> ignored)
    {
        final int size = mActivityStack.size();
        for (int i = size - 1; i >= 0; i--)
        {
            Activity activity = mActivityStack.get(i);
            if (!ignored.isInstance(activity))
            {
                Log.e(TAG, "Finish Activity = " + activity.getClass().getName());
                mActivityStack.remove(activity);
                activity.finish();
            }
        }
    }

    /**
     * 清除堆栈，关闭所有activity
     *
     * @param
     */
    public static void finishStack()
    {
        final int size = mActivityStack.size();
        for (int i = size - 1; i >= 0; i--)
        {
            Activity activity = mActivityStack.get(i);
            Log.e(TAG, "Finish Activity = " + activity.getClass().getName());
            mActivityStack.remove(activity);
            activity.finish();
        }
    }

    /**
     * 获取指定的activity
     *
     * @param activityClass
     * @return
     */
    public Activity getActivity(Class<?> activityClass)
    {
        for (int i = 0; i < mActivityStack.size(); i++)
        {
            if (activityClass.isInstance(mActivityStack.get(i)))
            {
                return mActivityStack.get(i);
            }
        }
        return null;
    }
    /**
     * 获取当前activity
     *
     * @param
     * @return
     */
    public Activity getCurrentActivity()
    {
        return mActivityStack.lastElement();
    }

    /**
     * 获取上级activity
     * @return
     */
    public Activity getSuperActivity()
    {
        if(mActivityStack.size() >= 2)
        {
            return mActivityStack.get(mActivityStack.size() - 2);
        }
        return null;
    }

}


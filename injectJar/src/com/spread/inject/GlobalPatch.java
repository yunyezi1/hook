package com.spread.inject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.spread.hook.HookCallBack;
import com.spread.hook.Reflect;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * @author wushuai
 * @date 2015年5月11日 下午4:03:41
 * 
 */
public class GlobalPatch {
	private static final String TAG = "Inject";
	public static final String HAOSOU_PACKAGE="com.qihoo.haosou";
	public static final String HAOSOU_QUICK_SEARCH="com.qihoo.haosou.activity.QuickSearchActivity";

	public static void main() {
		// TODO Auto-generated method stub
		Log.d(TAG, "Hello World");
		final Activity curActivity = getActivity();
		if(curActivity != null){
			Log.d(TAG, "Get Activity!");
			Toast.makeText(curActivity, "Hello Activity!"+curActivity.getTitle(), Toast.LENGTH_LONG).show();
//			ExportRes.exportLayout(curActivity);
			List<View> views = getAllChildViews(curActivity);
			for (View view : views) {
				if(view.getId() == -1)
					continue;
				String name = null;
				try{
					name = curActivity.getResources().getResourceName(view.getId());
				}catch(Exception e){
					continue;
				}
				if(TextUtils.isEmpty(name))
					continue;
				Log.d(TAG, "View name:" + name + ";View Class:" + view.getClass());
				if(name.equals("com.spread.injectjar:id/button1")){
					view.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							Intent intent = new Intent();
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							intent.setClassName(HAOSOU_PACKAGE,	HAOSOU_QUICK_SEARCH);
							curActivity.startActivity(intent);
						}
					});
				}
				if(view.getClass() != null && view.getClass().getName().startsWith("android.widget.")){
					view.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							Intent intent = new Intent();
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							intent.setClassName(HAOSOU_PACKAGE,	HAOSOU_QUICK_SEARCH);
							curActivity.startActivity(intent);
						}
					});
				}
			}
		}
	}

	public static Activity getActivity() {
		try{
		    Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
		    Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
		    Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
		    activitiesField.setAccessible(true);
		    @SuppressWarnings("unchecked")
			Map<IBinder, Object> activities = (Map<IBinder, Object>) activitiesField.get(activityThread);
		    for (Object activityRecord : activities.values()) {
		    	Log.d(TAG, "get activities:" + activityRecord);
		        Class<? extends Object> activityRecordClass = activityRecord.getClass();
		        Field pausedField = activityRecordClass.getDeclaredField("paused");
		        pausedField.setAccessible(true);
		        if (!pausedField.getBoolean(activityRecord)) {
		        	Log.d(TAG, "activity is not paused!");
		            Field activityField = activityRecordClass.getDeclaredField("activity");
		            activityField.setAccessible(true);
		            Activity activity = (Activity) activityField.get(activityRecord);
		            return activity;
		        }else{
		        	Log.d(TAG, "activity is paused!");
		            Field activityField = activityRecordClass.getDeclaredField("activity");
		            activityField.setAccessible(true);
		            Activity activity = (Activity) activityField.get(activityRecord);
		            return activity;
		        }
		    }
		}catch(Exception e){
			Log.d(TAG, "Debug Exception!" + e.toString());
			return null;
		}
	    return null;
	}
	
	private static void hookActivity(){
		try {
			Object currentActivityThread = Reflect.on("android.app.ActivityThread").
					call("currentActivityThread").get();

			Handler localHandler = (Handler) Reflect.on(currentActivityThread)
					.field("mH").get();
			Callback oriCallback = (Callback) Reflect.on(localHandler)
					.field("mCallback").get();
			HookCallBack callBack = new HookCallBack(oriCallback);
			Reflect.on(localHandler).set("mCallback", callBack);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
     * @note 获取该activity所有view
     * @author liuh
     * */
    private static List<View> getAllChildViews(Activity activity) {
        View view = activity.getWindow().getDecorView();
        if(view == null)
        	return new ArrayList<View>();
        return getAllChildViews(view);
    }

    private static List<View> getAllChildViews(View view) {
        List<View> allchildren = new ArrayList<View>();
        if (view instanceof ViewGroup) {
            ViewGroup vp = (ViewGroup) view;
            for (int i = 0; i < vp.getChildCount(); i++) {
                View viewchild = vp.getChildAt(i);
                allchildren.add(viewchild);
                allchildren.addAll(getAllChildViews(viewchild));
            }
        }
        return allchildren;
    }
	
}

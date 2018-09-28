package com.spread.hook;

import android.app.Application;
import android.content.Context;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class HookCallBack implements Callback {
	private static final String TAG = "Inject";
	public static final int RESUME_ACTIVITY = 107;
	public static final int PAUSE_ACTIVITY = 101;

	private Callback mParentCallback;

	public HookCallBack(Callback parentCallback) {
		mParentCallback = parentCallback;
	}

	@Override
	public boolean handleMessage(Message msg) {
		Context context = null;
//		if(msg != null)
//			context = msg.
		switch (msg.what) {
		case RESUME_ACTIVITY:
			if(context != null)
				Toast.makeText(context, "On Resume!", Toast.LENGTH_LONG).show();
			Log.d(TAG, "hook activity resume!!!");
			break;
		case PAUSE_ACTIVITY:
			if(context != null)
				Toast.makeText(context, "On Pause!", Toast.LENGTH_LONG).show();
			Log.d(TAG, "hook activity pause!!!");
		default:
			Log.d(TAG, "hook a " + msg.what);
			break;
		}

		if (mParentCallback != null) {
			return mParentCallback.handleMessage(msg);
		} else {
			return false;
		}
	}

}
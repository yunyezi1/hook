package com.spread.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;

public class ExportRes{
	private static final String TAG = "Inject";
	public static void exportLayout(Context context){
		String pkgName = context.getPackageName();
		try{
			Class<?> cls = Class.forName(pkgName+".R");
			if(cls == null){
				Log.e(TAG, "class is empty!");
				return;
			}
			Class<?>[] subcls = cls.getDeclaredClasses();
			Class<?> layoutCls = null;
			for (Class<?> sub : subcls) {
				if(sub.getName().equals(pkgName+".R$layout")){
					Log.d(TAG, "get layout!");
					layoutCls = sub;
					break;
				}
			}
			if(layoutCls != null){
				Field[] lays = layoutCls.getDeclaredFields();
				for (Field lay : lays) {
					int resid = lay.getInt(null);
					Log.d(TAG, "layout id:" + resid);
					String name = resid+".xml";
					try{
						name = context.getResources().getResourceName(resid);
						name = name.substring(name.indexOf("/")+1)+".xml";
					}catch(Exception e){}
					XmlResourceParser parser = context.getResources().getLayout(resid);
					File dir = new File("/mnt/sdcard/"+context.getPackageName()+"/layout");
					if(!dir.exists())
						dir.mkdirs();
					File target = new File(dir.getAbsolutePath()+"/" + name);
					if(!target.exists()){
						try {
							target.createNewFile();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					Log.d(TAG, "layout xml name:" + name);
					DecodeXML.decodeToXML(context, parser, target);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			return;
		}
	}
}
package com.spread.export;

import java.io.File;
import java.io.FileOutputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.text.TextUtils;
import android.util.Xml;

public class DecodeXML{
	public static void decodeToXML(Context context, XmlResourceParser xpp,File outFile){
		if(xpp == null || outFile == null || !outFile.exists())
			return;
		FileOutputStream os = null;
    	try {
    		os = new FileOutputStream(outFile);
    		XmlSerializer serializer = Xml.newSerializer();
    		serializer.setOutput(os, "UTF-8");
    		int type = 0;
    		serializer.startDocument("UTF-8", true);
    		while((type=xpp.next()) != XmlPullParser.END_DOCUMENT){
    			if(type == XmlPullParser.START_TAG){
    				String tag = xpp.getName();
    				serializer.startTag("", tag);
    				int count = xpp.getAttributeCount();
    				for(int i=0;i<count;i++){
    					String name = xpp.getAttributeName(i);
    					String value = xpp.getAttributeValue(i);
    					if(name.equals("id") && value.startsWith("@")){
    						value = value.replaceFirst("@", "");
    						try{
	    						if(TextUtils.isDigitsOnly(value)){
	    							value = context.getResources().
	    									getResourceName(Integer.parseInt(value));
	    						}
    						}catch(Exception e){}
    					}
    					serializer.attribute("", name, value);
    				}
    			}else if(type == XmlPullParser.END_TAG){
    				String tag = xpp.getName();
    				serializer.endTag("", tag);
    			}
    		}
    		serializer.endDocument();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try{
				if(os != null)
					os.close();
			}catch(Exception e){}
		}
	}
}
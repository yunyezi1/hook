/**
 * 
 */

package com.spread.inject.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

/**
 * @author wushuai
 */
public class DoInject {

    private static final String TAG = "Inject";
    public interface InjectEvent{
    	public void onInjectSucc();
    	public void onInjectError();
    }
    public static final void tryStart(final Context context,final String procName,
    		final InjectEvent event) {
        new Thread() {
            @Override
            public void run() {
            	try{
            		String jarPath = NativeHelper.copyNativeLib(context, "inject.jar");
	            	String soPath = NativeHelper.copyNativeLib(context, "libso.so");
	                String inject = NativeHelper.copyNativeLib(context, "inject");
	                if (TextUtils.isEmpty(jarPath) || TextUtils.isEmpty(soPath) ||
	                		TextUtils.isEmpty(inject)) {
	                     return;
	                }
	              	Integer[] result = new Integer[]{0};
	              	try {
	              		String rst = runCommand(result, inject + " " + procName + " " 
								+ soPath + " " + jarPath + "@" + procName);
						if(event != null){
							if(!TextUtils.isEmpty(rst) && rst.equals("0\n")){
								event.onInjectSucc();
							}else{
								event.onInjectError();
							}
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
					}
            	}catch(Exception e){
            	}
            }
        }.start();
    }

    private static final String SU="su";
	public static Process getRootAccess(Integer[] result) {
        Process mProcess = null;
        int rootResult  = 0;
        try {
        	long begin = System.nanoTime();
            mProcess = Runtime.getRuntime().exec(SU);

            DataOutputStream os = new DataOutputStream(mProcess.getOutputStream());
            DataInputStream is = new DataInputStream(mProcess.getInputStream());

            if (os != null && is != null) {
                // Getting current user's UID to check for Root Access
                os.writeBytes("id\n");
                os.flush();

                String outputSTR = is.readLine();
                if (outputSTR == null) {
                	rootResult = 1;
                } else if (outputSTR.contains("uid=0")) {
                    //If is contains uid=0, It means Root Access is granted
                	rootResult = 0;
                } else {
                	rootResult = 2;
                }
                double duration = (System.nanoTime()-begin)/1000.0/1000.0/1000.0;
                if(duration > 1)
                	Thread.sleep(500);
                if(rootResult == 1 && duration <= 1)
                	rootResult = 2;
            }
        } catch (Exception e) {
        	rootResult = 3;
        }
        if(result != null && result.length > 0)
        	result[0] = rootResult;
        return mProcess;
    }
	public static String runCommand(Integer[] result,String command) throws IOException, InterruptedException{
		StringBuilder rtn=new StringBuilder();
		DataOutputStream out=null;
		DataInputStream in=null;
		try{
			Process process= getRootAccess(result);
			if(process == null)
				return null;
			out=new DataOutputStream(process.getOutputStream());
			in=new DataInputStream(process.getInputStream());
			out.writeBytes(command+"\n");
			out.flush();
			out.writeBytes("echo $?\n");
			out.flush();
			out.writeBytes("exit\n");
			out.flush();
			String line=null;
			while ((line=in.readLine())!=null) {
				rtn.append(line);
				rtn.append("\n");
			}
			int temp=process.waitFor();
			if(temp!=0){
				Log.d("sdk", "error:");
			}
		}catch(Throwable th){
			if(result != null && result.length > 0)
				result[0] = 3;
		}finally{
			try{
				if (in!=null) {
					in.close();
				}
				if (out!=null) {
					out.close();
				}
			}catch(Exception e){}
		}
		return rtn.toString();
	}
}

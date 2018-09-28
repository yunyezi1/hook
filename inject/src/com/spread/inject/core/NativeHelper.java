/*
 * 创建日期：2014年3月20日 下午2:58:26
 */

package com.spread.inject.core;

import android.content.Context;
import android.os.Build;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * @author ws
 * @version 1.0
 * @since JDK1.6
 */
public class NativeHelper {

    private static String getCpuAbi() {
        try {
            String abi1 = Build.CPU_ABI;
            if ((abi1 != null && abi1.contains("armeabi"))) {
                return "armeabi";
            } else if ((abi1 != null && abi1.contains("mips"))) {
                return "mips";
            } else if ((abi1 != null && abi1.contains("x86"))) {
                return "x86";
            } else {
                return "armeabi";
            }
        } catch (Exception e) {
            return "armeabi";
        }
    }

    public static String copyNativeLib(Context context, String name) {
        File dir = context.getDir("MyLibs", Context.MODE_PRIVATE);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String assetpath = String.format("library/%s/%s", getCpuAbi(), name);
        File dstFile = new File(dir, name);
        if (dstFile.exists()) {
           try {
                Process process = Runtime.getRuntime().exec("chmod 755 " + dstFile.getPath());
                process.waitFor();
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return dstFile.getPath();
        }
        if (copyAssetFile(context, assetpath, dstFile.getPath())) {
            try {
                Process process = Runtime.getRuntime().exec("chmod 755 " + dstFile.getPath());
                process.waitFor();
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return dstFile.getPath();
        } else {
            return null;
        }
    }

    private static boolean copyAssetFile(Context context, String name, String dstPath) {
        InputStream in = null;
        FileOutputStream out = null;
        try {
            in = context.getAssets().open(name);
            out = new FileOutputStream(dstPath);
            byte[] buffer = new byte[8192];
            int readed = 0;
            while ((readed = in.read(buffer)) != -1) {
                out.write(buffer, 0, readed);
            }
            return new File(dstPath).exists();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                }
            }
        }
        return false;
    }
}

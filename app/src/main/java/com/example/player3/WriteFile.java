package com.example.player3;


import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;

public class WriteFile {

    public static boolean isConn()
    {
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
            return true;
        }
        return false;
    }
    public static boolean write(byte b[],String path)
    {
        boolean flag=false;
        String name=path.substring(path.lastIndexOf("/")+1);
        File file=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), name);
        try
        {
            FileOutputStream fo=new FileOutputStream(file);
            fo.write(b);
            flag=true;
            fo.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return flag;

    }

}
package com.example.player3;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpData {

    public static boolean isConn(Context context)
    {
        ConnectivityManager m=(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = m.getActiveNetworkInfo();
        if(info!=null)
        {
            return true;
        }
        else
        {
            return false;
        }

    }
    public static byte[] getData(String path)
    {
        ByteArrayOutputStream bo=new ByteArrayOutputStream();
        try
        {
            URL url=new URL(path);
            HttpURLConnection con=(HttpURLConnection) url.openConnection();
            con.setConnectTimeout(5000);
            con.setDoInput(true);
            con.connect();
            if(con.getResponseCode()==200)
            {
                InputStream in = con.getInputStream();
                int count=0;
                byte b[]=new byte[1024];
                while((count=in.read(b))!=-1)
                {
                    bo.write(b, 0, count);
                    bo.flush();
                }
            }
            return bo.toByteArray();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;

    }
}

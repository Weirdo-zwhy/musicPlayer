package com.example.player3;


import android.app.IntentService;
import android.content.Intent;

/*
 * 如果一个线程能满足操作   就用IntentService
 * 如果一个线程不能满足操作   就用service
 *
 * 继承IntentService  耗时操作  在onHandleIntent 执行
 * 执行完毕之后  会执行ondestroy()
 */
public class MusicService extends IntentService{

    public MusicService(String name) {
        super(name);
    }
    public MusicService() {
        super("");
    }
    //底层封装了一个工作线程
    @Override
    protected void onHandleIntent(Intent intent) {
        String path = intent.getStringExtra("path");
        if(HttpData.isConn(MusicService.this))
        {
            byte b[]=HttpData.getData(path);
            if(b!=null&&b.length>0)
            {
                if(WriteFile.isConn())
                {
                    boolean r=WriteFile.write(b, path);
                    if(r)
                    {
                        System.out.println("写入成功");
                        stopSelf();//关闭服务
                    }
                    else
                    {
                        System.out.println("写入失败");
                    }
                }
                else
                {
                    System.out.println("SD卡不可用");
                }
            }
            else
            {
                System.out.println("音乐下载失败");
            }
        }
        else
        {
            System.out.println("网络异常请检查");
        }
    }
}
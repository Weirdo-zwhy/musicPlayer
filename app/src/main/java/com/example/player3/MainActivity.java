package com.example.player3;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private boolean isSeekBarChanging;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private List<Music> musicList = new ArrayList<>();//歌曲列表
    private List<File> musicFile = new ArrayList<>();//MP3文件列表
    private int cMusicId = 0;//当前播放的音乐ID
    private Button btnPlay;//播放暂停
    private Button btnPre;//上一首
    private Button btnNext;//下一首
    private SeekBar mSeekBar;//进度条
    private Timer timer = new Timer();
    private int currentTime = 0;

    TextView tv1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //初始化
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv1 = findViewById(R.id.tv1);
        btnPlay = (Button) findViewById(R.id.btnPlay);
        btnPre = (Button)findViewById(R.id.btnPre);
        btnNext = (Button)findViewById(R.id.btnNext);

        btnPlay.setOnClickListener(this);
        btnPre.setOnClickListener(this);
        btnNext.setOnClickListener(this);

        mSeekBar = (SeekBar)findViewById(R.id.mSeekbar);
        mSeekBar.setOnSeekBarChangeListener(this);

        //权限判断，如果没有权限就请求权限
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            getMusicList();//得到音乐歌曲列表
            initMediaPlayer("/mnt/sdcard/Download/再度重逢.mp3");//初始化播放器 MediaPlayer，默认播放第一首
            btnPlay.setText("暂停");//设置播放后的界面，播放/暂停按钮为暂停显示状态
        }
    }
    //对于handle
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    tv1.setText("正在播放："+musicList.get(cMusicId).getNameM());
                    break;
            }
        }
    };
    public void getMusicList(){
        File SdcardFile = new File("/mnt/sdcard/Download");//打开sdcard的下载路径
        getSDcardFile(SdcardFile);//得到所有文件列表
        musicList.clear();//清空原始列表
        for(int i=0;i<musicFile.size();i++){
            File c = musicFile.get(i);
            String path = c.getPath();//得到路径
            String name = c.getName();//得到歌曲名字
            name = name.substring(0, name.length()-4);//数据清洗，将后面的.mp3清洗掉
            Music music = new Music(name, path);
            musicList.add(music);
        }
        MusicAdapter adapter = new MusicAdapter(MainActivity.this, R.layout.music_item, musicList);//适配器
        ListView lv = findViewById(R.id.listWords);
        lv.setAdapter(adapter);//设置适配器
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {//点击事件
                String mpath = musicList.get(i).getPath();
                cMusicId=i;
                mediaPlayer.stop();//停止媒体播放器
                Log.d("path12121212",mpath);
                initMediaPlayer(mpath);
                mediaPlayer.start();//开始播放
                btnPlay.setText("暂停");
            }
        });
    }

    public void getSDcardFile(File path){
        File[] files = path.listFiles();//得到文件下所有子文件
        System.out.println(files.length);
        for(int i=0;i<=files.length-1;i++){
            musicFile.add(files[i]);
        }
    }

    private void initMediaPlayer(String path) {
        try {
            mediaPlayer.stop();
            mediaPlayer.reset();//重启播放器
            File file = new File(path);//判断文件夹是否存在,如果不存在则创建文件夹
            if (!file.exists()) {
                Intent intent=new Intent(MainActivity.this,MusicService.class);
                intent.putExtra("path",path);
                startService(intent);
            }else{
                mediaPlayer.setDataSource(path);//指定音频文件路径
            }
            mediaPlayer.setLooping(true);//设置为循环播放
            mediaPlayer.prepare();//初始化播放器MediaPlayer
            mediaPlayer.start();
            mSeekBar.setMax(mediaPlayer.getDuration());
            currentTime = 0;
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(!isSeekBarChanging&&mediaPlayer.isPlaying()){//如果进度条未改变，并且当前正在播放
                    mSeekBar.setProgress(mediaPlayer.getCurrentPosition());
                    Message msg = new Message();
                    msg.what = 1;
                    handler.sendMessage(msg);
                }
            }
        },0,1000);

    }

    @Override
    public void onProgressChanged(SeekBar mSeekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {//开始移动滚动条
        isSeekBarChanging = true;//开始移动
    }



    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {//结束移动滚动条
        isSeekBarChanging = false;//停止移动
        mediaPlayer.seekTo(mSeekBar.getProgress());//找到滚动条对应位置
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnPlay:
                //如果没在播放中，立刻开始播放。
                if(!mediaPlayer.isPlaying()){
                    mediaPlayer.start();
                    btnPlay.setText("暂停");
                }else{  //如果在播放中，立刻暂停。
                    mediaPlayer.pause();
                    btnPlay.setText("播放");
                    v.setTag(0);
                }
                break;

            case R.id.btnPre:
                try{
                    mediaPlayer.stop();//停止
                    btnPlay.setText("暂停");//设置播放/停止状态
                    cMusicId=(cMusicId+musicList.size()-1)%musicList.size();//上一首
                    initMediaPlayer(musicList.get(cMusicId).getPath());//得到路径，并初始化播放器
                    mediaPlayer.start();//开始播放
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case R.id.btnNext:
                try {
                    mediaPlayer.stop();//停止
                    btnPlay.setText("暂停");//设置播放/停止状态
                    cMusicId = (cMusicId + 1) % musicList.size();//下一首
                    Log.d("path",musicList.get(cMusicId).getPath());
                    initMediaPlayer(musicList.get(cMusicId).getPath());//得到路径，并初始化播放器
                    mediaPlayer.start();//开始播放
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }
    //结束时释放资源
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer != null){
            mediaPlayer.stop();//停止播放器
            mediaPlayer.release();//释放资源
        }
    }

    public static void DownLoad(final String url, final String filePath)
    {
        ConfirmFile(filePath);//初始化判断是否存在文件
        Executors.newCachedThreadPool().execute(new Runnable()
        {
            @Override
            public void run() {
                try {
                    URL webUrl = new URL(url);
                    URLConnection con = webUrl.openConnection();	// 打开连接
                    InputStream in = con.getInputStream();			// 获取InputStream
                    File f = new File(filePath);					// 创建文件输出流
                    FileOutputStream fo = new FileOutputStream(f);
                    byte[] buffer = new byte[1024 * 1024];
                    int len = 0;
                    while( (len = in.read(buffer)) > 0){		// 读取文件
                        fo.write(buffer, 0, len); 			// 写入文件
                    }
                    in.close();
                    fo.flush();
                    fo.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /** 创建目录和文件 */
    public static void ConfirmFile(String filePath)
    {
        try{
            File f = new File(filePath);
            File parent = f.getParentFile();//得到路径父目录
            if (!parent.exists()) //若目录不存在，则创建父目录
                parent.mkdirs();
            if (!f.exists()) //若该文件存在，则重新创建
                f.createNewFile();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}

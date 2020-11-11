package com.example.player3;


import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private boolean isSeekBarChanging;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private List<Music> musicList = new ArrayList<>();//歌曲列表
    private List<File> musicFile = new ArrayList<>();//MP3文件列表
    private int cMusicId = 0;//当前播放的音乐ID

    private SeekBar mSeekBar;
    private Timer timer = new Timer();
    private int currentTime = 0;

    TextView tv1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv1 = findViewById(R.id.tv1);
        Button btnPlay = (Button) findViewById(R.id.btnPlay);
        Button btnPause = (Button) findViewById(R.id.btnPause);
        Button btnPre = (Button)findViewById(R.id.btnPre);
        Button btnNext = (Button)findViewById(R.id.btnNext);

        btnPlay.setOnClickListener(this);
        btnPause.setOnClickListener(this);
        btnPre.setOnClickListener(this);
        btnNext.setOnClickListener(this);

        mSeekBar = (SeekBar)findViewById(R.id.mSeekbar);
        mSeekBar.setOnSeekBarChangeListener(this);




        //权限判断，如果没有权限就请求权限
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            getMusicList();
            initMediaPlayer("/sdcard/Music/1.mp3");//初始化播放器 MediaPlayer

        }
    }
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
        File SdcardFile = new File("/sdcard/Music");
        getSDcardFile(SdcardFile);
        musicList.clear();
        for(int i=0;i<musicFile.size();i++){
            File c = musicFile.get(i);
            String path = c.getPath();
            String name = c.getName();
            name = name.substring(0,name.length()-4);

            Music music = new Music(name,path);
            musicList.add(music);

        }
        MusicAdapter adapter = new MusicAdapter(MainActivity.this,R.layout.music_item,musicList);
        ListView lv = findViewById(R.id.listWords);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String mpath = musicList.get(i).getPath();
                cMusicId=i;
                mediaPlayer.stop();
                initMediaPlayer(mpath);
                mediaPlayer.start();
            }
        });


    }

    public void getSDcardFile(File path){
        File[] files = path.listFiles();
        for(int i=0;i<files.length-1;i++){
            musicFile.add(files[i]);
        }
    }
    private void initMediaPlayer(String path) {
        try {
            mediaPlayer.stop();
            mediaPlayer.setDataSource(path);//指定音频文件路径
            mediaPlayer.setLooping(true);//设置为循环播放
            mediaPlayer.prepare();//初始化播放器MediaPlayer
            mediaPlayer.start();
            mSeekBar.setMax(mediaPlayer.getDuration());
            currentTime = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(!isSeekBarChanging&&mediaPlayer.isPlaying()){//如果进度条未改变，并且当前正在播放
                    //tv1.append(""+mediaPlayer.getCurrentPosition());
                    mSeekBar.setProgress(mediaPlayer.getCurrentPosition());
                    //lrcShow(currentTime);

                    Message msg = new Message();
                    msg.what = 1;
                    handler.sendMessage(msg);


                }
            }
        },0,1000);

    }

    @Override
    public void onProgressChanged(SeekBar mSeekBar, int progress,
                                  boolean fromUser) {


    }

    @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            isSeekBarChanging = true;
        }



    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
            isSeekBarChanging = false;
            mediaPlayer.seekTo(mSeekBar.getProgress());
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnPlay:

                //如果没在播放中，立刻开始播放。
                if(!mediaPlayer.isPlaying()){
                    mediaPlayer.start();
                }
                break;
            case R.id.btnPause:
                //如果在播放中，立刻暂停。
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                }
                break;
            case R.id.btnPre:
                try{
                    mediaPlayer.stop();
                    cMusicId=(cMusicId+musicList.size()-1)%musicList.size();
                    initMediaPlayer(musicList.get(cMusicId).getPath());
                    mediaPlayer.start();
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case R.id.btnNext:
                try {
                    mediaPlayer.stop();
                    cMusicId = (cMusicId + 1) % musicList.size();
                    initMediaPlayer(musicList.get(cMusicId).getPath());
                    mediaPlayer.start();
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}

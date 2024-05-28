package com.example.musicplayer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class PlayerActivity extends AppCompatActivity {
    // MusicService 实例
    private MusicService musicService;
    // MusicService 是否已绑定的标志
    private boolean isBound = false;
    // 播放、暂停、停止按钮
    private Button playButton, pauseButton, stopButton;
    // 播放进度条
    private SeekBar seekBar;
    // 当前时间文本视图、总时间文本视图
    private TextView currentTimeTextView, totalTimeTextView;
    // 用于更新UI的Handler
    public static Handler handler;

    // 服务连接对象
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            isBound = true;
            initializePlayerUI();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // 获取各个控件
        playButton = findViewById(R.id.btn_play);
        pauseButton = findViewById(R.id.btn_pause);
        stopButton = findViewById(R.id.btn_stop);
        seekBar = findViewById(R.id.seekBar);
        currentTimeTextView = findViewById(R.id.current_time);
        totalTimeTextView = findViewById(R.id.total_time);

        // 获取从上一个界面传递过来的歌曲名
        String songName = getIntent().getStringExtra("songName");
        // 创建启动 MusicService 的 Intent，并传递歌曲名
        Intent intent = new Intent(this, MusicService.class);
        intent.putExtra("songName", songName);
        // 绑定到 MusicService
        bindService(intent, connection, BIND_AUTO_CREATE);

        // 设置按钮点击事件
        playButton.setOnClickListener(v -> playSong(songName));
        pauseButton.setOnClickListener(v -> pauseSong());
        stopButton.setOnClickListener(v -> stopSong());

        // 设置播放进度条的改变监听器
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 创建 Handler 对象，用于更新UI
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                // 从消息中获取播放时长和当前播放位置
                Bundle bundle = msg.getData();
                int duration = bundle.getInt("duration");
                int currentPosition = bundle.getInt("currentPosition");

                // 设置进度条的最大值和当前值
                seekBar.setMax(duration);
                seekBar.setProgress(currentPosition);

                // 设置当前时间和总时间的文本
                currentTimeTextView.setText(formatTime(currentPosition));
                totalTimeTextView.setText(formatTime(duration));
            }
        };
    }

    // 播放音乐
    private void playSong(String songName) {
        if (isBound) {
            musicService.playSong(songName);
            updateSeekBar();
        }
    }

    // 暂停音乐
    private void pauseSong() {
        if (isBound) {
            musicService.pauseSong();
        }
    }

    // 停止音乐
    private void stopSong() {
        if (isBound) {
            musicService.stopSong();
        }
    }

    // 跳转到指定播放位置
    private void seekTo(int progress) {
        if (isBound) {
            musicService.seekTo(progress);
        }
    }

    // 初始化播放界面UI
    private void initializePlayerUI() {
        if (isBound && musicService.isPlaying()) {
            // 设置进度条的最大值为音乐总时长
            seekBar.setMax(musicService.getDuration());
            // 设置总时间文本
            totalTimeTextView.setText(formatTime(musicService.getDuration()));
            // 更新进度条
            updateSeekBar();
        }
    }

    // 更新进度条
    private void updateSeekBar() {
        if (isBound && musicService.isPlaying()) {
            // 设置进度条的当前值为当前播放位置
            seekBar.setProgress(musicService.getCurrentPosition());
            // 设置当前时间文本
            currentTimeTextView.setText(formatTime(musicService.getCurrentPosition()));
            // 每隔一秒更新一次进度条
            handler.postDelayed(this::updateSeekBar, 1000);
        }
    }

    // 将毫秒格式化为"分:秒"格式
    private String formatTime(int milliseconds) {
        int minutes = (milliseconds / 1000) / 60;
        int seconds = (milliseconds / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 解绑 MusicService
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
    }
}

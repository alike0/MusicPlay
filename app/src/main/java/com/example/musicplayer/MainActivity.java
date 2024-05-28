package com.example.musicplayer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    // 歌曲数组
    private String[] songs = {"jay", "spacewalk", "takethejourney", "wildfire", "interstellarjourney"};
    // 播放状态文本视图
    private TextView playbackStatusText;
    // MusicService 实例
    private MusicService musicService;
    // MusicService 是否已绑定的标志
    private boolean isBound = false;

    // 服务连接对象
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 获取 MusicService 实例
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            isBound = true;
            // 更新播放状态
            updatePlaybackStatus();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    // 广播接收器用于接收播放状态变化的广播
    private final BroadcastReceiver statusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (MusicService.ACTION_STATUS_CHANGED.equals(intent.getAction())) {
                // 获取播放状态
                boolean isPlaying = intent.getBooleanExtra("isPlaying", false);
                // 更新播放状态文本视图
                playbackStatusText.setText("播放状态：" + (isPlaying ? "播放中" : "已停止"));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 获取歌曲列表视图并设置适配器
        ListView listView = findViewById(R.id.song_list_view);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, songs);
        listView.setAdapter(adapter);

        // 设置歌曲列表项点击事件，点击进入播放界面
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
            intent.putExtra("songName", songs[position]);
            startActivity(intent);
        });

        // 获取播放状态文本视图和播放按钮
        playbackStatusText = findViewById(R.id.playback_status_text);
        Button playbackButton = findViewById(R.id.playback_button);
        // 播放按钮点击事件，点击跳转到播放界面
        playbackButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
            startActivity(intent);
        });

        // 绑定到 MusicService
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
        // 注册广播接收器以接收播放状态变化的广播
        IntentFilter filter = new IntentFilter(MusicService.ACTION_STATUS_CHANGED);
        registerReceiver(statusReceiver, filter);
    }

    // 更新播放状态
    private void updatePlaybackStatus() {
        if (isBound && musicService != null) {
            // 获取当前播放状态并更新播放状态文本视图
            String status = musicService.isPlaying() ? "播放中" : "已停止";
            playbackStatusText.setText("播放状态：" + status);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 解绑服务和注销广播接收器
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
        unregisterReceiver(statusReceiver);
    }
}

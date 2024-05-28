package com.example.musicplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class PlaybackReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case "com.example.musicplayer.ACTION_PLAY":
                    // 处理播放事件
                    Toast.makeText(context, "Playback: Play", Toast.LENGTH_SHORT).show();
                    break;
                case "com.example.musicplayer.ACTION_PAUSE":
                    // 处理暂停事件
                    Toast.makeText(context, "Playback: Pause", Toast.LENGTH_SHORT).show();
                    break;
                case "com.example.musicplayer.ACTION_STOP":
                    // 处理停止事件
                    Toast.makeText(context, "Playback: Stop", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}

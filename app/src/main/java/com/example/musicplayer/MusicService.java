package com.example.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class MusicService extends Service {
    private MediaPlayer player;
    private Timer timer;
    // 定义广播动作常量，用于发送播放状态变化的广播
    public static final String ACTION_STATUS_CHANGED = "com.example.musicplayer.ACTION_STATUS_CHANGED";

    @Override
    public IBinder onBind(Intent intent) {
        return new MusicBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        player = new MediaPlayer();
    }

    // 添加定时器
    public void addTimer() {
        if (timer == null) {
            timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    if (player == null) return;
                    int duration = player.getDuration();
                    int currentPosition = player.getCurrentPosition();
                    sendMessageToActivity(duration, currentPosition);
                }
            };
            timer.schedule(task, 0, 500); // 每500毫秒执行一次
        }
    }

    // 发送消息给Activity，用于更新播放进度
    private void sendMessageToActivity(int duration, int currentPosition) {
        if (PlayerActivity.handler != null) {
            Message msg = PlayerActivity.handler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putInt("duration", duration);
            bundle.putInt("currentPosition", currentPosition);
            msg.setData(bundle);
            PlayerActivity.handler.sendMessage(msg);
        }
    }

    // 播放音乐
    public void playSong(String songName) {
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/raw/" + songName);
        try {
            player.reset();
            player.setDataSource(getApplicationContext(), uri);
            player.prepare();
            player.start();
            addTimer();
            notifyStatusChanged(true);
        } catch (Exception e) {
            Log.e("MusicService", "Error playing music", e);
        }
    }

    // 暂停音乐
    public void pauseSong() {
        if (player.isPlaying()) {
            player.pause();
            notifyStatusChanged(true);
        }
    }

    // 停止音乐
    public void stopSong() {
        if (player.isPlaying()) {
            player.stop();
            notifyStatusChanged(true);
        }
    }

    // 发送广播通知状态变化
    private void notifyStatusChanged(boolean isPlaying) {
        Intent intent = new Intent(ACTION_STATUS_CHANGED);
        intent.putExtra("isPlaying", isPlaying);
        sendBroadcast(intent);
    }
    // 获取音乐播放状态
    public boolean isPlaying() {
        return player != null && player.isPlaying();
    }

    // 获取音乐总时长
    public int getDuration() {
        if (player != null && player.isPlaying()) {
            return player.getDuration();
        }
        return 0;
    }

    // 获取当前播放位置
    public int getCurrentPosition() {
        if (player != null && player.isPlaying()) {
            return player.getCurrentPosition();
        }
        return 0;
    }

    // 跳转到指定播放位置
    public void seekTo(int progress) {
        if (player != null) {
            player.seekTo(progress);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player != null) {
            if (player.isPlaying()) {
                player.stop();
            }
            player.release();
            player = null;
        }
    }

    // 定义 Binder 内部类，用于与客户端进行通信
    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }
}

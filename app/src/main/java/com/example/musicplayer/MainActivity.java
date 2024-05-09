package com.example.musicplayer;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private Button playButton, pauseButton, stopButton;
    private SeekBar seekBar;
    private Spinner songSpinner;
    private Handler handler = new Handler();
    private String[] songs = {"jay", "spacewalk", "takethejourney", "wildfire", "interstellarjourney"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupSpinner();
        setupSeekBar();
        setupButtons();
    }

    private void initializeViews() {
        playButton = findViewById(R.id.btn_play);
        pauseButton = findViewById(R.id.btn_pause);
        stopButton = findViewById(R.id.btn_stop);
        seekBar = findViewById(R.id.seekBar);
        songSpinner = findViewById(R.id.song_spinner);
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, songs);
        songSpinner.setAdapter(adapter);
        songSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                resetMediaPlayer();
                initializeMediaPlayer(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void resetMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        seekBar.setProgress(0);
    }

    private void initializeMediaPlayer(int songPosition) {
        int resourceId = getResources().getIdentifier(songs[songPosition], "raw", getPackageName());
        if (resourceId != 0) {
            mediaPlayer = MediaPlayer.create(MainActivity.this, resourceId);
            mediaPlayer.setOnPreparedListener(mp -> {
                seekBar.setMax(mediaPlayer.getDuration());
                playAudio();  // Auto-start the media upon preparation
            });
            mediaPlayer.setOnCompletionListener(mp -> stopAudio());
        }
    }

    private void setupSeekBar() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupButtons() {
        playButton.setOnClickListener(v -> playAudio());
        pauseButton.setOnClickListener(v -> pauseAudio());
        stopButton.setOnClickListener(v -> stopAudio());
    }

    private void playAudio() {
        if (mediaPlayer == null) {
            int songPosition = songSpinner.getSelectedItemPosition();
            initializeMediaPlayer(songPosition);
        }
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            startUpdatingSeekBar();
        }
    }


    private void pauseAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    private void stopAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        seekBar.setProgress(0);
        handler.removeCallbacksAndMessages(null);
    }

    private void startUpdatingSeekBar() {
        handler.postDelayed(this::updateSeekBar, 1000);
    }

    private void updateSeekBar() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            handler.postDelayed(this::updateSeekBar, 1000);
        } else {
            handler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}

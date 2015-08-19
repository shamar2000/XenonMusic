package com.shamar.themes.xenonmusic;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;

import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    // media player
    private MediaPlayer mPlayer;
    // song list
    private ArrayList<Song> mSongs;
    // song position
    private int mSongPos;

    private final IBinder mMusicBind = new MusicBinder();


    public MusicService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();
        initMediaPlayer();
    }

    private void init() {
        mSongPos = 0;
        mPlayer = new MediaPlayer();
    }

    public void setList(ArrayList<Song> Songs) {
        mSongs = Songs;
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
         return MusicService.this;
        }
    }

    private void initMediaPlayer() {
        // allows playback when the device becomes idle
        mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        // set the stream type to music
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);
        mPlayer.setOnPreparedListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMusicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mPlayer.stop();
        mPlayer.release();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

    }
}

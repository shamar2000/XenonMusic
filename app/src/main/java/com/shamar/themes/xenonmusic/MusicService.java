package com.shamar.themes.xenonmusic;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;

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

    /**
     * Called when the user selects a song from the list
     *
     * @param songIndex the current song position
     */
    public void setSong(int songIndex) {
        mSongPos = songIndex;
    }

    public void playSong() {
        mPlayer.reset();
        // get song
        Song playSong = mSongs.get(mSongPos);
        // get ID
        long currSong = playSong.getmId();
        // set URI
        Uri trackURI = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);

        /**
         * Set URI as the data source, however an error may pop up
         *
         * catch it as an exception
         */
        try {
            mPlayer.setDataSource(getApplicationContext(), trackURI);
        } catch (Exception e) {
            Log.d("MUSIC SERVICE", "Error setting data source", e);

        }
        mPlayer.prepareAsync();
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

    /**
     *  Release the resources when the @Service method is unbound
     *
     *  This will execute when the user exits the app, at which point
     *  we will stop the service
     */
    @Override
    public boolean onUnbind(Intent intent) {
        mPlayer.stop();
        mPlayer.release();
        return false;
    }

    //kddmmdfm,fkf

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        // start song playback
        mp.start();
    }
}

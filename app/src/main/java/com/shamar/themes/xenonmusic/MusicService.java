package com.shamar.themes.xenonmusic;

import android.app.Notification;
import android.app.PendingIntent;
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
import java.util.Random;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    // media player
    private MediaPlayer     mPlayer;
    // song list
    private ArrayList<Song> mSongs;
    // song position
    private int             mSongPos;

    private String           songTitle = "";
    private static final int NOTIFY_ID = 1;
    private boolean          mShuffle = false;
    private Random           mRand;

    private final IBinder    mMusicBind = new MusicBinder();


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
        mRand = new Random();
        mPlayer = new MediaPlayer();
    }

    public void setShuffle() {
        if (mShuffle)
            mShuffle = false;
        mShuffle = true;
    }

    /**
     * Remember that the media playback is happening in the Service class, but that the user
     * interface comes from the Activity class. We bound the Activity instance to the Service
     *
     * instance, so that we could control playback from the user interface.
     * The methods in our Activity class that we added to implement the MediaPlayerControl interface
     *
     * will be called when the user attempts to control playback. We will need the Service class to
     * act on this control
     *
     */

    public int getPosition(){
        return mPlayer.getCurrentPosition();
    }

    public int getDuration(){
        return mPlayer.getDuration();
    }

    public boolean isPlaying(){
        return mPlayer.isPlaying();
    }

    public void pausePlayer(){
        mPlayer.pause();
    }

    public void seekTo(int posn){
        mPlayer.seekTo(posn);
    }

    public void go(){
        mPlayer.start();
    }

    /**
     * @playNext :
     * We decrement the song index variable, check that we haven't gone outside the range of the
     * list, and call the playSong method we added. Now add the method to skip to the next track
     *
     * @playPrev :
     * This is analogous to the method for playing the previous track at the moment
     */

    public void playPrev() {
        mSongPos--;
        if (mSongPos < 0)
            mSongPos = mSongs.size() - 1;
        playSong();
    }

    public void playNext() {

        /**
         * If the shuffle flag is on, we choose a new song from the list at random, making sure we
         * don't repeat the last song played
         */

        if (mShuffle) {
            int newSong = mSongPos;
            while (newSong == mSongPos) {
                newSong = mRand.nextInt(mSongs.size());
            }
            mSongPos = newSong;
        }
        mSongPos++;
        if (mSongPos >= mSongs.size())
            mSongPos = 0;
        playSong();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMusicBind;
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

    public void setList(ArrayList<Song> Songs) {
        mSongs = Songs;
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
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

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mPlayer.getCurrentPosition() > 0) {
            mp.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        // start song playback
        mp.start();

        Song currSong = mSongs.get(mSongPos);
        String currSongArtist = currSong.getmArtist();
        String currSongTitle = currSong.getmTitle();

        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        /**
         * The PendingIntent class will take the user back to the main Activity class when they
         * select the notification
         */
        PendingIntent pendIntent = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentIntent(pendIntent)
                .setSmallIcon(R.drawable.ic_play_notification)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle(currSongArtist)
                .setContentText(currSongTitle);
        Notification not = builder.build();

        startForeground(NOTIFY_ID, not);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }


}

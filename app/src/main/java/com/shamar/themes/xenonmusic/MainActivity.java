package com.shamar.themes.xenonmusic;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController.MediaPlayerControl;

import com.shamar.themes.xenonmusic.MusicService.MusicBinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity implements MediaPlayerControl {

    private ArrayList<Song> mSongList;
    public ListView         mSongView;
    public LinearLayout     mSongLinearLay;
    public SongAdapter      mSongAdapter;

    private MusicService    mMusicSrv;
    private MusicController mMusicController;
    private Intent          mPlayIntent;
    private boolean         mMusicBound = false,
                            mPaused = false,
                            mPlaybackPaused;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setup our custom fonts
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("pnr.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        setContentView(R.layout.activity_main);

        init();
        getSongList();
        setMusicController();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    /**
     * We want to start the @Service instance when the @Activity instance starts
     */

    @Override
    protected void onStart() {
        super.onStart();
        if (mPlayIntent == null) {
            mPlayIntent = new Intent(this, MusicService.class);
            bindService(mPlayIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(mPlayIntent);
        }
    }

    private void init() {
        mSongView = (ListView) findViewById(R.id.song_list);
        mSongList = new ArrayList<>();

        mSongAdapter = new SongAdapter(this, mSongList);
        mSongView.setAdapter(mSongAdapter);
    }

    /**
     * We set the song position as the tag for each item in the list view when we defined the
     * @SongAdapter class. We retrieve it here and pass it to the Service instance before calling the
     * method to start the playback.
     *
     * @param v view that was clicked
     */
    public void onSongPicked(View v) {
        mMusicSrv.setSong(Integer.parseInt(v.getTag().toString()));
        mMusicSrv.playSong();
        if (mPlaybackPaused) {
            setMusicController();
            mPlaybackPaused = false;
        }
        mMusicController.show(0);
    }

    private void setMusicController() {
        // set up the music controller widget (play, pause, skip, etc..)
        mMusicController = new MusicController(this);
        // set up Prev/Next controllers
        mMusicController.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });
        mMusicController.setMediaPlayer(this);
        mMusicController.setAnchorView(findViewById(R.id.song_list));
        mMusicController.setEnabled(true);
    }

    // play next track
    private void playNext() {
        mMusicSrv.playNext();
        if (mPlaybackPaused) {
            setMusicController();
            mPlaybackPaused = false;
        }
        mMusicController.show(0);
    }

    // play previous track
    private void playPrev() {
        mMusicSrv.playPrev();
        if (mPlaybackPaused) {
            setMusicController();
            mPlaybackPaused = false;
        }
        mMusicController.show(0);
    }

    /**
     * We are going to play the music in the @MusicService class, but control it from the @MainActivity
     * class, where the application's user interface operates. To accomplish this, we will have to
     * bind to the @MusicService class
     */

    // connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder) service;
            // get service
            mMusicSrv = binder.getService();
            // pass list
            mMusicSrv.setList(mSongList);
            mMusicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMusicBound = false;
        }
    };

    public void getSongList() {
        // retrieve song information
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if (musicCursor != null & musicCursor.moveToFirst()) {
            // get columns
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            // add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                mSongList.add(new Song(thisId, thisTitle, thisArtist));
            } while (musicCursor.moveToNext());
        }
    }

    public void onSortSongs() {
        Collections.sort(mSongList, new Comparator<Song>() {
            @Override
            public int compare(Song a, Song b) {
                return a.getmTitle().compareTo(b.getmTitle());
            }
        });
    }

    @Override
    protected void onDestroy() {
        stopService(mPlayIntent);
        mMusicSrv=null;
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_close:
                stopService(mPlayIntent);
                mMusicSrv = null;
                System.exit(0);
                break;
            case R.id.action_shuffle:
                mMusicSrv.setShuffle();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void start() {
        mMusicSrv.go();
    }

    @Override
    public void pause() {
        mPlaybackPaused = true;
        mMusicSrv.pausePlayer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPaused = true;
    }

    /**
     * @onResume
     * This will ensure that the music controller displays when the user returns to the app.
     *
     * @onStop
     * This hides the music controller when the user exits the app
     */

    @Override
    protected void onResume() {
        super.onResume();
        if (mPaused) {
            setMusicController();
            mPaused = false;
        }
    }

    @Override
    protected void onStop() {
        mMusicController.hide();
        super.onStop();
    }

    /**
     * @getCurrentPosition, @getDuration :
     * The conditional tests are to avoid various exceptions that may occur when using the
     * MediaPlayer and MediaController classes. If you attempt to enhance the app in any way,
     *
     * you will likely find that you need to take such steps since the media playback classes
     * throw lots of exceptions. Notice that we call the @getPosition method of the Service class.
     *
     */
    @Override
    public int getDuration() {
        if (mMusicSrv != null && mMusicBound && mMusicSrv.isPlaying())
            return mMusicSrv.getDuration();
        else return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (mMusicSrv != null && mMusicBound && mMusicSrv.isPlaying())
            return mMusicSrv.getPosition();
        else return 0;
    }

    @Override
    public void seekTo(int pos) {
        mMusicSrv.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        if (mMusicSrv != null && mMusicBound)
            return mMusicSrv.isPlaying();
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}

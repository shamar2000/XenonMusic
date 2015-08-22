package com.shamar.themes.xenonmusic;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Song> mSongList;
    private ListView mSongView;
    private SongAdapter mSongAdapter;

    private MusicService mMusicSrv;
    private Intent mPlayIntent;
    private boolean mMusicBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        getSongList();
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
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
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
        }

        return super.onOptionsItemSelected(item);
    }
}

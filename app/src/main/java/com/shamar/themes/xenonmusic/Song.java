package com.shamar.themes.xenonmusic;

public class Song {

    private long mId;
    private String mTitle;
    private String mArtist;

    public Song(long id, String title, String artist) {
        mId = id;
        mTitle = title;
        mArtist = artist;
    }

    public long getmId() {
        return mId;
    }

    public String getmTitle() {
        return mTitle;
    }

    public String getmArtist() {
        return mArtist;
    }
}

package ru.centhis.songbook.data;

import java.io.Serializable;
import java.util.List;

public class SearchSongResult implements Serializable {
    private String artistName;
    private String songName;
    private String href;

    public SearchSongResult(String artistName, String songName, String href) {
        this.artistName = artistName;
        this.songName = songName;
        this.href = href;

    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }
}

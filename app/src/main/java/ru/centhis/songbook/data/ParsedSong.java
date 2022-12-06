package ru.centhis.songbook.data;

import android.media.Image;

import java.util.List;

public class ParsedSong {
    private String text;
    private List<String> chordsSrc;

    public ParsedSong(String text, List<String> chordsSrc) {
        this.text = text;
        this.chordsSrc = chordsSrc;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<String> getChordsSrc() {
        return chordsSrc;
    }

    public void setChordsSrc(List<String> chordsSrc) {
        this.chordsSrc = chordsSrc;
    }

}
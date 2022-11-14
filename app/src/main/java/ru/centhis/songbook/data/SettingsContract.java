package ru.centhis.songbook.data;

import android.graphics.Color;

public class SettingsContract {
    public static final String FS_SONG = "fsSong";
    public static final int FS_SONG_MIN = 6;
    public static final int FS_SONG_MAX = 30;
    public static final String TRANSPOND_SONG = "transpond";
    public static final int TRANSPOND_MAX = 10;
    public static final int TRANSPOND_MIN = -10;
    public static final String APP_NAME = "ru.centhis.songbook";
    public static final int SCROLL_SONG_MAX = 99;
    public static final int SCROLL_SONG_MIN = -99;
    public static final String SCROLL_SONG = "scroll_song";
    public static final int DEFAULT_SCROLL_COUNTDOWN = 10;
    public static final int SCROLL_COUNTDOWN_MIN = 1;
    public static final int SCROLL_COUNTDOWN_MAX = 99;
    public static final String SCROLL_COUNTDOWN = "scrollCountDown";
    public static final String GUITAR_TEXT_FILE = "guitar.txt";
    public static final String UKULELE_TEXT_FILE = "ukulele.txt";
    public static final int CHORDS_COLOR = Color.RED;


    //chords
    public static final String TONES = "[ABCDEFGH]{1}";
    public static final String HALF_TONES = "[#b]?";
    public static final String MINOR = "[mmaj]*+";
    public static final String ADDED = "[-+5679]*+";
    public static final String SUS = "[sus2sus4dim]*+";
    public static final String ADD = "[add9]*+";
}

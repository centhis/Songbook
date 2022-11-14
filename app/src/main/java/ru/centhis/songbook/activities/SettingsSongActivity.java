package ru.centhis.songbook.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import ru.centhis.songbook.R;
import ru.centhis.songbook.data.Item;
import ru.centhis.songbook.data.SettingsContract;
import ru.centhis.songbook.util.MBJsonSharedPrefs;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class SettingsSongActivity extends AppCompatActivity {

    Item itemRoot;
    TextView fsSongTV;
    TextView scrollSongTV;
    TextView scrollCountDownTV;
    TextView toneTV;
    SharedPreferences prefs;
    MBJsonSharedPrefs songPrefs;
    int fsSong;
    int scrollSong;
    int scrollCountDown;
    int tone;
    ImageButton fsSongUpBtn;
    ImageButton fsSongDownBtn;
    ImageButton scrollSongUpBtn;
    ImageButton scrollSongDownBtn;
    ImageButton scrollCountDownUpBtn;
    ImageButton scrollCountDownDownBtn;
    ImageButton toneUpBtn;
    ImageButton toneDownBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_song);

        if (getIntent().getExtras() != null){
            itemRoot = (Item) getIntent().getSerializableExtra("item");
            setTitle(itemRoot.getName() + " - " + getString(R.string.settings));
        }

        fsSongTV = findViewById(R.id.fsSongTV);
        songPrefs = new MBJsonSharedPrefs(itemRoot.getSource() + "/songPrefs.json");
        prefs = getSharedPreferences(SettingsContract.APP_NAME, MODE_PRIVATE);
        fsSong = songPrefs.getInt(SettingsContract.FS_SONG, 0);
        if (fsSong == 0) {
            fsSong = prefs.getInt(SettingsContract.FS_SONG, 0);
        }
        String result;
        if (fsSong > 9)
            result = String.valueOf(fsSong);
        else
            result = "0"+fsSong;
        fsSongTV.setText(result);

        scrollSong = songPrefs.getInt(SettingsContract.SCROLL_SONG, 0);
        scrollSongTV = findViewById(R.id.scrollSongTV);
        scrollSongTV.setText(String.valueOf(scrollSong));

        scrollCountDown = songPrefs.getInt(SettingsContract.SCROLL_COUNTDOWN, 0);
        if (scrollCountDown == 0)
            scrollCountDown = prefs.getInt(SettingsContract.SCROLL_COUNTDOWN, SettingsContract.DEFAULT_SCROLL_COUNTDOWN);
        scrollCountDownTV = findViewById(R.id.scrollCountDownTV);
        scrollCountDownTV.setText(String.valueOf(scrollCountDown));

        tone = songPrefs.getInt(SettingsContract.TRANSPOND_SONG, 0);
        toneTV = findViewById(R.id.toneTV);
        toneTV.setText(String.valueOf(tone));

        fsSongDownBtn = findViewById(R.id.fsSongDownBtn);
        fsSongDownBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fsSong--;
                if (fsSong < SettingsContract.FS_SONG_MIN)
                    fsSong = SettingsContract.FS_SONG_MIN;
                String result;
                if (fsSong > 9)
                    result = String.valueOf(fsSong);
                else
                    result = "0"+fsSong;
                fsSongTV.setText(result);
                songPrefs.putInt(SettingsContract.FS_SONG, fsSong);
                songPrefs.apply();
            }
        });
        fsSongUpBtn = findViewById(R.id.fsSongUpBtn);
        fsSongUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fsSong++;
                if (fsSong > SettingsContract.FS_SONG_MAX)
                    fsSong = SettingsContract.FS_SONG_MAX;
                String result;
                if (fsSong > 9)
                    result = String.valueOf(fsSong);
                else
                    result = "0"+fsSong;
                fsSongTV.setText(result);
                songPrefs.putInt(SettingsContract.FS_SONG, fsSong);
                songPrefs.apply();
            }
        });

        scrollSongDownBtn = findViewById(R.id.scrollSongDownBtn);
        scrollSongDownBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scrollSong--;
                if (scrollSong < SettingsContract.SCROLL_SONG_MIN)
                    scrollSong = SettingsContract.SCROLL_SONG_MIN;
                scrollSongTV.setText(String.valueOf(scrollSong));
                songPrefs.putInt(SettingsContract.SCROLL_SONG, scrollSong);
                songPrefs.apply();
            }
        });
        scrollSongUpBtn = findViewById(R.id.scrollSongUpBtn);
        scrollSongUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scrollSong++;
                if (scrollSong > SettingsContract.SCROLL_SONG_MAX)
                    scrollSong = SettingsContract.SCROLL_SONG_MAX;
                scrollSongTV.setText(String.valueOf(scrollSong));
                songPrefs.putInt(SettingsContract.SCROLL_SONG, scrollSong);
                songPrefs.apply();
            }
        });

        scrollCountDownDownBtn = findViewById(R.id.scrollCountDownDownBtn);
        scrollCountDownDownBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scrollCountDown--;
                if (scrollCountDown < SettingsContract.SCROLL_COUNTDOWN_MIN)
                    scrollCountDown = SettingsContract.SCROLL_COUNTDOWN_MIN;
                scrollCountDownTV.setText(String.valueOf(scrollCountDown));
                songPrefs.putInt(SettingsContract.SCROLL_COUNTDOWN, scrollCountDown);
                songPrefs.apply();
            }
        });
        scrollCountDownUpBtn = findViewById(R.id.scrollCountDownUpBtn);
        scrollCountDownUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scrollCountDown++;
                if (scrollCountDown > SettingsContract.SCROLL_COUNTDOWN_MAX)
                    scrollCountDown = SettingsContract.SCROLL_COUNTDOWN_MAX;
                scrollCountDownTV.setText(String.valueOf(scrollCountDown));
                songPrefs.putInt(SettingsContract.SCROLL_COUNTDOWN, scrollCountDown);
                songPrefs.apply();
            }
        });

        toneDownBtn = findViewById(R.id.toneDownBtn);
        toneDownBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tone--;
                if (tone < SettingsContract.TRANSPOND_MIN)
                    tone = SettingsContract.TRANSPOND_MIN;
                toneTV.setText(String.valueOf(tone));
                songPrefs.putInt(SettingsContract.TRANSPOND_SONG, tone);
                songPrefs.apply();
            }
        });
        toneUpBtn = findViewById(R.id.toneUpBtn);
        toneUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tone++;
                if (tone > SettingsContract.TRANSPOND_MAX)
                    tone = SettingsContract.TRANSPOND_MAX;
                toneTV.setText(String.valueOf(tone));
                songPrefs.putInt(SettingsContract.TRANSPOND_SONG, tone);
                songPrefs.apply();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                Intent backIntent = new Intent(this, TextSongActivity.class);
                backIntent.putExtra("item", itemRoot);
                startActivity(backIntent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
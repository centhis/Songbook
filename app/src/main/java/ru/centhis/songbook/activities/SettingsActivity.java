package ru.centhis.songbook.activities;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SwitchCompat;

import com.dropbox.core.v2.users.FullAccount;

import java.io.File;
import java.io.IOException;

import ru.centhis.songbook.R;
import ru.centhis.songbook.data.SettingsContract;
import ru.centhis.songbook.dropbox.DbxUtil;
import ru.centhis.songbook.dropbox.DropboxActivity;
import ru.centhis.songbook.dropbox.DropboxClientFactory;
import ru.centhis.songbook.dropbox.GetCurrentAccountTask;
import ru.centhis.songbook.util.DeleteSongUtil;


public class SettingsActivity extends DropboxActivity {

    private static final String TAG = MainActivity.class.getName();

    ImageButton fsSongUpBtn;
    ImageButton fsSongDownBtn;
    TextView fsSongTV;
    SharedPreferences prefs;
    int defaultScrollCountDown;
    TextView defaultScrollCountDownTV;
    ImageButton defaultScrollCountDownDownBtn;
    ImageButton defaultScrollCountDownUpBtn;
    SwitchCompat showChordSwitch;
    Button settingsClearCacheBtn;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);




        Button dbxButton = findViewById(R.id.dbxButton);
        fsSongUpBtn = findViewById(R.id.fsSongUpBtn);
        fsSongDownBtn = findViewById(R.id.fsSongDownBtn);
        fsSongTV = findViewById(R.id.fsSongTV);
        prefs = getSharedPreferences(SettingsContract.APP_NAME, MODE_PRIVATE);

        dbxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dbxButton.getText().equals(getString(R.string.dbx_button_login)))
                    DropboxActivity.startOAuth2Authentication(SettingsActivity.this, getString(R.string.APP_KEY), null);
                else if (dbxButton.getText().equals(getString(R.string.dbx_button_sync))){
                    DbxUtil.dbxSyncFiles(SettingsActivity.this, getFilesDir().toString(), prefs);
                }
            }
        });

        int fsSong = prefs.getInt(SettingsContract.FS_SONG, 0);
        if (fsSong == 0){
            fsSong = Integer.parseInt(fsSongTV.getText().toString());
            prefs.edit().putInt(SettingsContract.FS_SONG, fsSong).apply();
        } else {
            String result;
            if (fsSong > 9)
                result = String.valueOf(fsSong);
            else
                result = "0"+fsSong;
            fsSongTV.setText(result);
        }
        fsSongUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int i = Integer.parseInt(fsSongTV.getText().toString());
                i++;
                if (i > SettingsContract.FS_SONG_MAX)
                    i = SettingsContract.FS_SONG_MAX;
                String result;
                if (i > 9)
                    result = String.valueOf(i);
                else
                    result = "0"+i;
                fsSongTV.setText(result);
                prefs.edit().putInt(SettingsContract.FS_SONG, i).apply();
            }
        });
        fsSongDownBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int i = Integer.parseInt(fsSongTV.getText().toString());
                i--;
                if (i < SettingsContract.FS_SONG_MIN)
                    i = SettingsContract.FS_SONG_MIN;
                String result;
                if (i > 9)
                    result = String.valueOf(i);
                else
                    result = "0"+i;
                fsSongTV.setText(result);
                prefs.edit().putInt(SettingsContract.FS_SONG, i).apply();
            }
        });

        defaultScrollCountDown = prefs.getInt(SettingsContract.SCROLL_COUNTDOWN, SettingsContract.DEFAULT_SCROLL_COUNTDOWN);
        defaultScrollCountDownTV = findViewById(R.id.defaultScrollCountDownTV);
        defaultScrollCountDownTV.setText(String.valueOf(defaultScrollCountDown));
        defaultScrollCountDownDownBtn = findViewById(R.id.defaultScrollCountDownDownBtn);
        defaultScrollCountDownDownBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                defaultScrollCountDown--;
                if (defaultScrollCountDown < SettingsContract.SCROLL_COUNTDOWN_MIN)
                    defaultScrollCountDown = SettingsContract.SCROLL_COUNTDOWN_MIN;
                defaultScrollCountDownTV.setText(String.valueOf(defaultScrollCountDown));
                prefs.edit().putInt(SettingsContract.SCROLL_COUNTDOWN, defaultScrollCountDown).apply();
            }
        });
        defaultScrollCountDownUpBtn = findViewById(R.id.defaultScrollCountDownUpBtn);
        defaultScrollCountDownUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                defaultScrollCountDown++;
                if (defaultScrollCountDown > SettingsContract.SCROLL_COUNTDOWN_MAX)
                    defaultScrollCountDown = SettingsContract.SCROLL_COUNTDOWN_MAX;
                defaultScrollCountDownTV.setText(String.valueOf(defaultScrollCountDown));
                prefs.edit().putInt(SettingsContract.SCROLL_COUNTDOWN, defaultScrollCountDown).apply();
            }
        });

        showChordSwitch = findViewById(R.id.defaultShowChordSwitch);
        showChordSwitch.setChecked(prefs.getBoolean(SettingsContract.SHOW_CHORDS, Boolean.FALSE));
        showChordSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                prefs.edit().putBoolean(SettingsContract.SHOW_CHORDS, b).apply();
            }
        });

        TextView settingsPathTV = findViewById(R.id.settingsPathTV);
        settingsPathTV.setText(getFilesDir().toString());

        settingsClearCacheBtn = findViewById(R.id.settingsClearCacheBtn);
        settingsClearCacheBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    DeleteSongUtil.deleteCache(getFilesDir().toString());
                    Toast.makeText(SettingsActivity.this, "Cache cleared", Toast.LENGTH_LONG).show();
                } catch (IOException e){
                    Log.e(TAG, "clearCache: ", e);
                    Toast.makeText(SettingsActivity.this, "Something wrong", Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasToken()) {
            ((Button) findViewById(R.id.dbxButton)).setText(getString(R.string.dbx_button_sync));
            TextView dbxTextView = findViewById(R.id.dbxTextView);
            dbxTextView.setText(getString(R.string.loading));
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.alpha);
            dbxTextView.startAnimation(animation);

        } else {
            ((Button) findViewById(R.id.dbxButton)).setText(getString(R.string.dbx_button_login));
        }

    }

    @Override
    protected void loadData() {
        new GetCurrentAccountTask(DropboxClientFactory.getClient(), new GetCurrentAccountTask.Callback() {
            @Override
            public void onComplete(FullAccount result) {
                TextView dbxTextView = findViewById(R.id.dbxTextView);
                dbxTextView.clearAnimation();
                dbxTextView.setText(result.getName().getDisplayName());

            }

            @Override
            public void onError(Exception e) {
                Log.e(getClass().getName(), "Failed to get account details. ", e);
            }
        }).execute();
    }





}
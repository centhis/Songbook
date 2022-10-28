package ru.centhis.songbook.activities;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;

import com.dropbox.core.v2.users.FullAccount;

import java.io.File;
import java.util.Arrays;

import ru.centhis.songbook.R;
import ru.centhis.songbook.data.SettingsContract;
import ru.centhis.songbook.dropbox.DbxUtil;
import ru.centhis.songbook.dropbox.DropboxActivity;
import ru.centhis.songbook.dropbox.DropboxClientFactory;
import ru.centhis.songbook.dropbox.GetCurrentAccountTask;


public class SettingsActivity extends DropboxActivity {

    private static File filesDir;

    ImageButton fsSongUpBtn;
    ImageButton fsSongDownBtn;
    TextView fsSongTV;
    SharedPreferences prefs;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        filesDir = new File(getFilesDir() + "/songs");

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
                    DbxUtil.dbxSyncFiles(SettingsActivity.this, getFilesDir().toString());
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
                if (i > 30)
                    i = 30;
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
                if (i < 6)
                    i = 6;
                String result;
                if (i > 9)
                    result = String.valueOf(i);
                else
                    result = "0"+i;
                fsSongTV.setText(result);
                prefs.edit().putInt(SettingsContract.FS_SONG, i).apply();
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


    public static File getFilesDirMethod(){
        return filesDir;
    }


}
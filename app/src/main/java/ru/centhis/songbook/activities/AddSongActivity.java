package ru.centhis.songbook.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.PrintWriter;

import ru.centhis.songbook.R;
import ru.centhis.songbook.data.Item;
import ru.centhis.songbook.data.ParsedSong;
import ru.centhis.songbook.data.SearchSongResult;
import ru.centhis.songbook.data.SettingsContract;
import ru.centhis.songbook.parsing.ChordDownloadTask;
import ru.centhis.songbook.parsing.ParseAmDmTask;

public class AddSongActivity extends AppCompatActivity {

    private static final String TAG = AddSongActivity.class.getName();

    TextView testFind;
    LinearLayout searchChordsLL;
    String text;
    SearchSongResult searchSongResult;
    String source;
    Context context;
    File fileSongPath;
    boolean isTurnToText = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_song);

        testFind = findViewById(R.id.test_find);
        searchChordsLL = findViewById(R.id.searchChordsLL);
        context = this;

        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        if (getIntent().getExtras() != null) {
            searchSongResult = (SearchSongResult) getIntent().getSerializableExtra("item");
            setTitle(searchSongResult.getSongName());
            final ProgressDialog dialog = new ProgressDialog(AddSongActivity.this);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCancelable(false);
            dialog.setMessage(getString(R.string.loading));
            dialog.show();
            new ParseAmDmTask(searchSongResult.getHref(), new ParseAmDmTask.Callback() {
                @Override
                public void onDataLoaded(ParsedSong result) {
                    dialog.dismiss();
                    text = result.getText();

                    testFind.setText(result.getText());
                    for (String url:result.getChordsSrc()){
                        File file = new File(MainActivity.getChordsDir() + "/" + FilenameUtils.getName(url));
                        if (!file.exists())
                            downloadChord(url);
                        else {
                            createImage(file);
                        }
                    }
                }

                @Override
                public void onError(Exception e) {
                    dialog.dismiss();
                    Log.e(TAG, "onError: ", e);
                }
            }).execute(searchSongResult.getHref());

        }

    }

    private void downloadChord(String url){
        new ChordDownloadTask(url, new ChordDownloadTask.Callback() {
            @Override
            public void onDataLoaded(File result) {
                createImage(result);
            }

            @Override
            public void onError(Exception e) {

            }
        }).execute(url);
    }

    private void createImage(File file){
        if (file.exists()){

            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            ImageView imageView = new ImageView(this);
            searchChordsLL.addView(imageView);
            imageView.getLayoutParams().height = dpToPx(60);
            imageView.getLayoutParams().width = dpToPx(60);
            imageView.setImageBitmap(bitmap);


        }
    }

    public int dpToPx(int dp) {
        float density = this.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.add_song_AmDm:
                boolean isSuccess = saveSong(searchSongResult.getArtistName(), searchSongResult.getSongName(), text);

                if (isSuccess) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setPositiveButton(R.string.add_song_ok_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Item itemSong = new Item(searchSongResult.getSongName(), source, "text");
                            Intent intent = new Intent(context, TextSongActivity.class);
                            intent.putExtra("item", itemSong);
                            SharedPreferences prefs = getSharedPreferences(SettingsContract.APP_NAME, MODE_PRIVATE);
                            prefs.edit().putString(SettingsContract.CURRENT_AUTHOR, searchSongResult.getArtistName()).apply();
                            prefs.edit().putString(SettingsContract.CURRENT_SOURCE, fileSongPath.toString());
                            startActivity(intent);
                        }
                    });
                    builder.setNegativeButton(R.string.add_song_cancel_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            AddSongActivity.this.finish();
                        }
                    });
                    builder.setMessage(R.string.add_song_ad_message).setTitle(R.string.add_song_ad_title);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else
                    Toast.makeText(this, getString(R.string.song_saved_error), Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.songbook_add_song_menu, menu);
        return true;
    }

    public boolean saveSong(String artist, String songName, String text){
        File filePath = MainActivity.getSongDir();
        File fileArtistPath = new File(filePath + "/" + artist);
        if (!fileArtistPath.exists())
            fileArtistPath.mkdir();
        fileSongPath = new File(fileArtistPath + "/" + songName);
        if (!fileSongPath.exists())
            fileSongPath.mkdir();
        try (PrintWriter out = new PrintWriter(fileSongPath + "/text.txt")){
            out.println(text);
            source = fileSongPath.getAbsolutePath();
            return true;
        } catch (Exception e){
            Log.e(TAG, "saveSong: ", e);
            return false;
        }
    }
}
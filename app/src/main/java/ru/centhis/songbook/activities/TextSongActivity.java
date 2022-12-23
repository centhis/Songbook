package ru.centhis.songbook.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.centhis.songbook.R;
import ru.centhis.songbook.data.Item;
import ru.centhis.songbook.data.SettingsContract;
import ru.centhis.songbook.data.Song;
import ru.centhis.songbook.parsing.UkuleleChordDownloadTask;
import ru.centhis.songbook.util.DeleteSongUtil;
import ru.centhis.songbook.util.MBJsonSharedPrefs;
import ru.centhis.songbook.util.TranspondUtil;

public class TextSongActivity extends AppCompatActivity {

    private static final String TAG = TextSongActivity.class.getName();

    Item itemRoot;
    String file = "text.txt";
    String mp3 = "song.mp3";
    File mp3File;
    LinearLayout textSongLayout;
    static int count;
    Song song;
    String songVersion;
    ImageButton playBtn;
    ImageButton scrollBtn;
    ImageButton fsUpBtn;
    ImageButton fsDownBtn;
    ImageButton increaseToneBtn;
    ImageButton decreaseToneBtn;
    MediaPlayer mediaPlayer;
    ScrollView sv;
    CountDownTimer scrollCountDownTimer;
    CountDownTimer startScrollCountDownTimer;
    SharedPreferences prefs;
    MBJsonSharedPrefs songPrefs;
    int fsSong;
    int transpondSong;
    Map<String, String> chordsMap = new HashMap<>();
    LinearLayout textChordsLL;


    ViewTreeObserver.OnGlobalLayoutListener listener;


    @Override
    protected void onStart() {
        super.onStart();
        setTextSongTextView();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_song);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        textChordsLL = findViewById(R.id.textChordsLL);
        textSongLayout = findViewById(R.id.textSongLayout);
        playBtn = findViewById(R.id.playBtn);
        scrollBtn = findViewById(R.id.scrollBtn);
        sv = findViewById(R.id.textScrollView);
        fsUpBtn = findViewById(R.id.fsUpBtn);
        fsDownBtn = findViewById(R.id.fsDownBtn);
        increaseToneBtn = findViewById(R.id.increaseToneBtn);
        decreaseToneBtn = findViewById(R.id.decreaseToneBtn);


        if (getIntent().getExtras() != null){
            itemRoot = (Item) getIntent().getSerializableExtra("item");
            setTitle(itemRoot.getName());
            songVersion = getIntent().getStringExtra(SettingsContract.SONG_VERSION);
            if (songVersion == null)
                songVersion = SettingsContract.SONG_EDIT_VERSION_GUITAR;
        }

        songPrefs = new MBJsonSharedPrefs(itemRoot.getSource() + "/songPrefs.json");
        prefs = getSharedPreferences(SettingsContract.APP_NAME, MODE_PRIVATE);

        fsSong = prefs.getInt(SettingsContract.FS_SONG, SettingsContract.FS_SONG_MIN);
        int fsSongLocal = songPrefs.getInt(SettingsContract.FS_SONG, 0);
        if (fsSongLocal != 0)
            fsSong = fsSongLocal;
        transpondSong = songPrefs.getInt(SettingsContract.TRANSPOND_SONG, 0);

        mp3File = new File(itemRoot.getSource() + "/" + mp3);
        if (!mp3File.exists()){
            playBtn.setClickable(false);
            playBtn.setEnabled(false);
            playBtn.setImageAlpha(25);

            scrollBtn.setClickable(false);
            scrollBtn.setEnabled(false);
            scrollBtn.setImageAlpha(25);
        } else {
            mediaPlayer = MediaPlayer.create(this, Uri.parse(itemRoot.getSource() + "/" + mp3));
        }
        if (songVersion.equals(SettingsContract.SONG_VERSION_GUITAR))
            song = new Song(itemRoot.getSource() + "/" + SettingsContract.GUITAR_TEXT_FILE);
        else if (songVersion.equals(SettingsContract.SONG_VERSION_UKULELE))
            song = new Song(itemRoot.getSource() + "/" + SettingsContract.UKULELE_TEXT_FILE);



        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    playBtn.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                } else {
                    mediaPlayer.seekTo(0);
                    mediaPlayer.start();
                    playBtn.setImageResource(R.drawable.ic_baseline_stop_24);
                }
            }
        });

        scrollBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (scrollCountDownTimer != null || startScrollCountDownTimer != null) {
                    if (scrollCountDownTimer != null)
                        scrollCountDownTimer.cancel();
                    scrollCountDownTimer = null;
                    if (startScrollCountDownTimer != null)
                        startScrollCountDownTimer.cancel();
                    startScrollCountDownTimer = null;
                }
                else {

                    int svHeight = sv.getChildAt(0).getHeight();
                    int duration = mediaPlayer.getDuration();
                    int svScreeHeight = sv.getHeight();
                    int scrollSpeed = songPrefs.getInt(SettingsContract.SCROLL_SONG, 0);


                    scrollCountDownTimer = new CountDownTimer(duration, (duration + (scrollSpeed * 1000)) / 10000) {
                        double svPosition = 0;

                        @Override
                        public void onTick(long l) {

                            svPosition = svPosition + (svHeight / 10000.0);
                            sv.scrollTo(0, (int) svPosition);
                            if (svPosition > (svHeight - svScreeHeight))
                                scrollCountDownTimer.cancel();
                        }

                        @Override
                        public void onFinish() {

                        }
                    };

                    sv.scrollTo(0, 0);


                    int scrollCountDown = songPrefs.getInt(SettingsContract.SCROLL_COUNTDOWN, 0);
                    if (scrollCountDown == 0)
                        scrollCountDown = prefs.getInt(SettingsContract.SCROLL_COUNTDOWN, SettingsContract.DEFAULT_SCROLL_COUNTDOWN);
                    int finalScrollCountDown = scrollCountDown;
                    startScrollCountDownTimer = new CountDownTimer(finalScrollCountDown * 1000, 1000) {
                        int i = finalScrollCountDown;
                        @Override
                        public void onTick(long l) {
                            Toast toast = Toast.makeText(TextSongActivity.this, String.valueOf(i), Toast.LENGTH_SHORT);
                            toast.show();
                            try {
                                Thread.sleep(400);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            toast.cancel();
                            i--;
                        }

                        @Override
                        public void onFinish() {
                            scrollCountDownTimer.start();
                        }
                    };
                    startScrollCountDownTimer.start();
                }
            }
        });

        fsUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fsSong++;
                if (fsSong > SettingsContract.FS_SONG_MAX)
                    fsSong = SettingsContract.FS_SONG_MAX;
                songPrefs.putInt(SettingsContract.FS_SONG, fsSong);
                songPrefs.apply();
                textSongLayout.removeAllViews();
                setTextSongTextView();
            }
        });

        fsDownBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fsSong--;
                if (fsSong < SettingsContract.FS_SONG_MIN)
                    fsSong = SettingsContract.FS_SONG_MIN;
                songPrefs.putInt(SettingsContract.FS_SONG, fsSong);
                songPrefs.apply();
                textSongLayout.removeAllViews();
                setTextSongTextView();
            }
        });

        if (transpondSong > 0)
            increaseToneBtn.setColorFilter(getResources().getColor(R.color.red));
        increaseToneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                transpondSong++;
                if (transpondSong > SettingsContract.TRANSPOND_MAX)
                    transpondSong = SettingsContract.TRANSPOND_MAX;
                if (transpondSong > 0) {
                    increaseToneBtn.setColorFilter(getResources().getColor(R.color.red));
                    decreaseToneBtn.setColorFilter(null);
                } else if (transpondSong < 0) {
                    increaseToneBtn.setColorFilter(null);
                    decreaseToneBtn.setColorFilter(getResources().getColor(R.color.red));
                } else if (transpondSong == 0){
                    increaseToneBtn.setColorFilter(null);
                    decreaseToneBtn.setColorFilter(null);
                }
                songPrefs.putInt(SettingsContract.TRANSPOND_SONG, transpondSong);
                songPrefs.apply();
                textSongLayout.removeAllViews();
                textChordsLL.removeAllViews();
                chordsMap.clear();
                setTextSongTextView();
            }
        });

        if (transpondSong < 0)
            decreaseToneBtn.setColorFilter(getResources().getColor(R.color.red));
        decreaseToneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                transpondSong--;
                if (transpondSong < SettingsContract.TRANSPOND_MIN)
                    transpondSong = SettingsContract.TRANSPOND_MIN;
                if (transpondSong > SettingsContract.TRANSPOND_MAX)
                    transpondSong = SettingsContract.TRANSPOND_MAX;
                if (transpondSong > 0) {
                    increaseToneBtn.setColorFilter(getResources().getColor(R.color.red));
                    decreaseToneBtn.setColorFilter(null);
                } else if (transpondSong < 0) {
                    increaseToneBtn.setColorFilter(null);
                    decreaseToneBtn.setColorFilter(getResources().getColor(R.color.red));
                } else if (transpondSong == 0){
                    increaseToneBtn.setColorFilter(null);
                    decreaseToneBtn.setColorFilter(null);
                }
                songPrefs.putInt(SettingsContract.TRANSPOND_SONG, transpondSong);
                songPrefs.apply();
                textSongLayout.removeAllViews();
                textChordsLL.removeAllViews();
                chordsMap.clear();
                setTextSongTextView();
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem actionAddUkuleleVersion = menu.findItem(R.id.action_add_ukulele_version);
        MenuItem actionSwitchUkuleleVersion = menu.findItem(R.id.actionSwitchInstrument);
        if (itemRoot.isUkulele()) {
            actionAddUkuleleVersion.setTitle(getString(R.string.edit_ukulele_version));
            actionSwitchUkuleleVersion.setVisible(true);
        }
        if (songVersion.equals(SettingsContract.SONG_VERSION_UKULELE))
            actionSwitchUkuleleVersion.setTitle(getString(R.string.guitar));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.songbook_song_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                stopActivity();
                Intent backIntent = new Intent(this, ListSongActivity.class);
                Item backItem = new Item();
                backItem.setType("folder");
                backItem.setName(prefs.getString(SettingsContract.CURRENT_AUTHOR, null));
                backItem.setSource(new File(itemRoot.getSource()).getParent().toString());
                backIntent.putExtra("item", backItem);
                startActivity(backIntent);
                return true;
            case R.id.action_song_settings:
                stopActivity();
                Intent openSettings = new Intent(this, SettingsSongActivity.class);
                openSettings.putExtra("item", itemRoot);
                startActivity(openSettings);
                return true;
            case R.id.actionSwitchInstrument:
                stopActivity();
                Intent openSong = new Intent(this, TextSongActivity.class);
                if (songVersion.equals(SettingsContract.SONG_VERSION_GUITAR))
                    openSong.putExtra(SettingsContract.SONG_VERSION, SettingsContract.SONG_EDIT_VERSION_UKULELE);
                else
                    openSong.putExtra(SettingsContract.SONG_VERSION, SettingsContract.SONG_EDIT_VERSION_GUITAR);
                openSong.putExtra("item", itemRoot);
                startActivity(openSong);
                return true;
            case R.id.action_edit:
                stopActivity();
                Intent openEdit = new Intent(this, EditTextActivity.class);
                openEdit.putExtra("item", itemRoot);
                openEdit.putExtra(SettingsContract.SONG_EDIT_VERSION, SettingsContract.SONG_EDIT_VERSION_GUITAR);
                startActivity(openEdit);
                return true;
            case R.id.action_add_ukulele_version:
                stopActivity();
                Intent openUkuleleEdit = new Intent(this, EditTextActivity.class);
                openUkuleleEdit.putExtra("item", itemRoot);
                openUkuleleEdit.putExtra(SettingsContract.SONG_EDIT_VERSION, SettingsContract.SONG_EDIT_VERSION_UKULELE);
                startActivity(openUkuleleEdit);
                return true;
            case R.id.action_delete:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.alert_dialog_delete_song_message_start) + itemRoot.getName() + getString(R.string.alert_dialog_delete_song_message_end)).
                        setTitle(getString(R.string.alert_dialog_delete_song_title));
                builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            if (DeleteSongUtil.delete(itemRoot.getSource(), MainActivity.getSongDir().toString(), prefs)){
                                stopActivity();
                                Intent backIntent = new Intent(TextSongActivity.this, MainActivity.class);
                                startActivity(backIntent);
                            } else {
                                stopActivity();
                                Intent backIntent = new Intent(TextSongActivity.this, ListSongActivity.class);
                                startActivity(backIntent);
                            }
                        } catch (IOException e){
                            Log.e(TAG, "onOptionsItemSelected: ", e);
                        }
                    }
                });
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();

        }

        return super.onOptionsItemSelected(item);
    }

    private void stopActivity(){
        if (mediaPlayer != null)
            mediaPlayer.stop();
        if (scrollCountDownTimer != null) {
            scrollCountDownTimer.cancel();
            scrollCountDownTimer = null;
        }
        if (startScrollCountDownTimer != null) {
            startScrollCountDownTimer.cancel();
            startScrollCountDownTimer = null;
        }
    }


    private void setTextSongTextView(){
        String testLine = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        TextView tv =createTextView();
        tv.setText(testLine);
        textViewVisibleCharCount(tv);
    }


    public static String getVisibleText(TextView textView) {
        // test that we have a textview and it has text
        if (textView==null || TextUtils.isEmpty(textView.getText())) return null;
        Layout l = textView.getLayout();
        if (l!=null) {
            // find the last visible position
            int end = l.getLineEnd(textView.getMaxLines()-1);
            // get only the text after that position
            return textView.getText().toString().substring(0,end);
        }

        return null;
    }

    private TextView createTextView(){
        TableRow.LayoutParams lparams = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        lparams.setMarginStart(10);
        lparams.setMarginEnd(10);
        TextView tv=new TextView(this);
        tv.setLayoutParams(lparams);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, fsSong);
        tv.setTypeface(Typeface.MONOSPACE);
        this.textSongLayout.addView(tv);
        tv.setMaxLines(1);
        return tv;
    }

    private void textViewVisibleCharCount(TextView tv){

        tv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                tv.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                count = getVisibleText(tv).length();
                textSongLayout.removeView(tv);
                showText(song.getSongText(), count);
            }

        });
    }


    private void showText(String[] lines, int count){
        String[][] songText = song.getSongTextWithBreaks(count, transpondSong);
        for (int i = 0; i < songText.length; i++){
            TextView tv = createTextView();
            if (songText[i][1].equals("chord"))
                tv.setTextColor(SettingsContract.CHORDS_COLOR);
            tv.setText(songText[i][0]);
        }
        if (prefs.getBoolean(SettingsContract.SHOW_CHORDS, Boolean.FALSE)){
            List<String> chords = song.getChords(transpondSong);
            if (songVersion.equals(SettingsContract.SONG_VERSION_GUITAR)) {
                for (String chord : chords) {
                    File file = new File(MainActivity.getChordsDir() + "/" + chord.replaceAll("#", "w") + "_0.gif");
                    createImage(file);
                }
            }
            else if (songVersion.equals(SettingsContract.SONG_VERSION_UKULELE)){
                for (String chord:chords){
                    File file = new File(MainActivity.getUkuleleChordsDir() + "/" + chord.replaceAll("#", "w") + ".png");
                    if (!file.exists()){
                        String url = "https://ukula.ru/newchords/" + chord.replaceAll("#", "w") + ".png";
                        new UkuleleChordDownloadTask(url, new UkuleleChordDownloadTask.Callback() {
                            @Override
                            public void onDataLoaded(File result) {
                                createImage(result);
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e(TAG, "onError: ", e);
                            }
                        }).execute(url);
                    } else {
                        createImage(file);
                    }
                }
            }
        }



    }

    private void createImage(File file){
        if (file.exists()){

            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            ImageView imageView = new ImageView(this);
            textChordsLL.addView(imageView);
            imageView.getLayoutParams().height = dpToPx(60);
            imageView.getLayoutParams().width = dpToPx(60);
            imageView.setImageBitmap(bitmap);


        }
    }

    public int dpToPx(int dp) {
        float density = this.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }







}
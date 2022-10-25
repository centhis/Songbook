package ru.centhis.songbook.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.TextViewCompat;

import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Html;
import android.text.Layout;
import android.text.PrecomputedText;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ru.centhis.songbook.R;
import ru.centhis.songbook.data.Chords;
import ru.centhis.songbook.data.Item;
import ru.centhis.songbook.data.Song;

public class TextSongActivity extends AppCompatActivity {

    private static final String TAG = TextSongActivity.class.getName();

    Item itemRoot;
    String file = "text.txt";
    String mp3File = "song.mp3";
    LinearLayout textSongLayout;
    static int count;
    Song song;
    ImageButton playBtn;
    ImageButton scrollBtn;
    MediaPlayer mediaPlayer;
    ScrollView sv;


    @Override
    protected void onStart() {
        super.onStart();
        setTextSongTextView();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_song);

        textSongLayout = findViewById(R.id.textSongLayout);
        playBtn = findViewById(R.id.playBtn);
        scrollBtn = findViewById(R.id.scrollBtn);
        sv = findViewById(R.id.textScrollView);



        if (getIntent().getExtras() != null){
            itemRoot = (Item) getIntent().getSerializableExtra("item");
            setTitle(itemRoot.getName());
        }
        song = new Song(itemRoot.getSource() + "/" + file);

        mediaPlayer = MediaPlayer.create(this, Uri.parse(itemRoot.getSource() + "/" + mp3File));

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
                int svHeight = sv.getHeight();
                int duration = mediaPlayer.getDuration();


                CountDownTimer countDownTimer = new CountDownTimer(duration, duration / 10000) {
                    double svPosition = 0;
                    @Override
                    public void onTick(long l) {
                        svPosition = svPosition + (svHeight / 1800.0);
                        sv.scrollTo(0, (int)svPosition);
                    }

                    @Override
                    public void onFinish() {

                    }
                };
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        countDownTimer.start();
                    }
                }, 10000);

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.songbook_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings){
            Intent openSettings = new Intent(this, SettingsActivity.class);
            startActivity(openSettings);
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
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

    private boolean isChordLine (String line){
        boolean result = true;
        line = line.replaceAll("\\p{C}","");
        line = line.trim();
        String[] words = line.split(" +");
        int count = 0;
        for (String word:words){
            for (Chords chord:Chords.values()){
                if (word.equals(chord.toString())){
//                    Log.d(TAG, "isChordLine: " + word);
                    count++;
                    //TODO добавить список используемых аккордов
                    break;
                }
            }
        }
        if (count == words.length)
            return true;
        return false;
    }

    private String[] lineWithBreaks(String line, int count){
        String lineToBreaks = line;
        int lineLength = line.length();
        List<String> linesBreaks = new ArrayList<>();
        while (count < lineLength){
            for (int i = count; i > 1; i--){
                if (lineToBreaks.charAt(i) == ' '){
                    linesBreaks.add(lineToBreaks.substring(0, i));
                    lineToBreaks = lineToBreaks.substring(i);
                    lineLength = lineToBreaks.length();
                    break;
                }
            }
        }
        if (lineToBreaks.length() > 0)
            linesBreaks.add(lineToBreaks);
        return linesBreaks.toArray(new String[linesBreaks.size()]);
    }

    private String[] twoLinesWithBreaks(String lineChords, String lineText, int count){
        String lineToBreaks = lineText;
        String lineChordsToBreaks = lineChords;
        int lineLength = lineToBreaks.length();
        List<String> linesBreaks = new ArrayList<>();
        while (count < lineLength){
            for (int i = count; i > 1; i--){
                if (lineToBreaks.charAt(i) == ' '){
                    if (lineChordsToBreaks.length() > i)
                        linesBreaks.add(lineChordsToBreaks.substring(0, i));
                    else
                        linesBreaks.add(" ");
                    linesBreaks.add(lineToBreaks.substring(0, i));
                    if (lineChordsToBreaks.length() > i)
                        lineChordsToBreaks = lineChordsToBreaks.substring(i);
                    else
                        lineChordsToBreaks = " ";
                    lineToBreaks = lineToBreaks.substring(i);
                    lineLength = lineToBreaks.length();
                    break;
                }
            }
        }
        if (lineChordsToBreaks.length() > 0)
            linesBreaks.add(lineChordsToBreaks);
        if (lineToBreaks.length() > 0)
            linesBreaks.add(lineToBreaks);
        return linesBreaks.toArray(new String[linesBreaks.size()]);
    }

    private void showText(String[] lines, int count){
        for (int i = 0; i < lines.length; i++){
            if (isChordLine(lines[i])){
                if (count + 1 < lines[i].length()){
                    String[] linesBreaks = twoLinesWithBreaks(lines[i], lines[i+1], count);
                    for (String line:linesBreaks){
                        TextView tv = createTextView();
                        tv.setText(line);
                    }
                    i++;
                } else {
                    TextView tv = createTextView();
                    tv.setText(lines[i]);
                }
            } else {
                if (count + 1 < lines[i].length()){
                    String[] linesBreaks = lineWithBreaks(lines[i], count);


                    for (String line:linesBreaks){
                        TextView tv = createTextView();
                        tv.setText(line);
                    }


                } else {
                    TextView tv = createTextView();
                    tv.setText(lines[i]);
                }
            }
        }
    }


}
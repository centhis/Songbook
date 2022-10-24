package ru.centhis.songbook.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.TextViewCompat;

import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
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
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
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
import java.util.List;

import ru.centhis.songbook.R;
import ru.centhis.songbook.data.Chords;
import ru.centhis.songbook.data.Item;
import ru.centhis.songbook.data.Song;

public class TextSongActivity extends AppCompatActivity {

    private static final String TAG = TextSongActivity.class.getName();

    Item itemRoot;
    String file = "text.txt";
    LinearLayout textSongLayout;
    static int count;
    Song song;


    @RequiresApi(api = Build.VERSION_CODES.P)
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

        if (getIntent().getExtras() != null){
            itemRoot = (Item) getIntent().getSerializableExtra("item");
            setTitle(itemRoot.getName());
        }
        song = new Song(itemRoot.getSource() + "/" + file);

        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

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
                    Log.d(TAG, "isChordLine: " + word);
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

    private void showText(String[] lines, int count){
        for (int i = 0; i < lines.length; i++){
            if (isChordLine(lines[i])){

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
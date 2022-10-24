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
import java.util.List;

import ru.centhis.songbook.R;
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

    private int textViewVisibleCharCount(TextView tv){

        tv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                tv.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                count = getVisibleText(tv).length();
                textSongLayout.removeView(tv);
                showText(song.getSongText(), count);
            }

        });
        Log.d(TAG, "textViewVisibleCharCount: " + count);
        return count;
    }

    private boolean isChordLine (String line){
        int spaceCount = 0;
        for (char c:line.toCharArray()){
            if (c == ' ')
                spaceCount++;
        }
        if (spaceCount > (line.length() / 2))
            return true;
        return false;
    }

    private void showText(String[] lines, int count){
        for (int i = 0; i < lines.length; i++){
            if (isChordLine(lines[i])){

            } else {
                TextView textView = createTextView();
                textView.setText(lines[i]);
            }
        }
    }


}
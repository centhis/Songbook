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

        chordsMap.put("Am", MainActivity.getChordsDir() + "/Am_0.gif");

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

        song = new Song(itemRoot.getSource() + "/" + file);



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


                    scrollCountDownTimer = new CountDownTimer(duration, duration / 10000) {
                        double svPosition = 0;

                        @Override
                        public void onTick(long l) {
                            int scrollSpeed = songPrefs.getInt(SettingsContract.SCROLL_SONG, 0);
                            svPosition = svPosition + (svHeight / (10000.0 + (scrollSpeed * 1000)));
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.songbook_song_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
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
//                NavUtils.navigateUpFromSameTask(this);
                Intent backIntent = new Intent(this, ListSongActivity.class);
                startActivity(backIntent);
                return true;
            case R.id.action_song_settings:
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
                Intent openSettings = new Intent(this, SettingsSongActivity.class);
                openSettings.putExtra("item", itemRoot);
                startActivity(openSettings);
                return true;
            case R.id.action_edit:
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
                Intent openEdit = new Intent(this, EditTextActivity.class);
                openEdit.putExtra("item", itemRoot);
                startActivity(openEdit);
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
                                Intent backIntent = new Intent(TextSongActivity.this, MainActivity.class);
                                startActivity(backIntent);
                            } else {

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


//        int id = item.getItemId();
//        if (id == R.id.action_settings){
//            Intent openSettings = new Intent(this, SettingsActivity.class);
//            startActivity(openSettings);
//            return true;
//        }
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

    private boolean isChordLine (String line){
//        boolean result = true;
//        line = line.replaceAll("\\p{C}","");
//        line = line.trim();
//        String[] words = line.split(" +");
//        int count = 0;
//        for (String word:words){
//            for (Chords chord:Chords.values()){
//                if (word.equals(chord.toString())){
//                    count++;
//                    break;
//                }
//            }
//        }
//        if (count == words.length)
//            return true;
//        return false;
        line = line.replaceAll("\\p{C}", "");
        line = line.trim();
        String[] words = line.split(" +");
        int count = 0;
        Pattern pattern = Pattern.compile("(\\s*?" + SettingsContract.TONES +
                SettingsContract.HALF_TONES +
                SettingsContract.MINOR +
                SettingsContract.ADDED +
                SettingsContract.SUS +
                SettingsContract.ADD +
                SettingsContract.ENDNOTE +
                ")");
        Matcher matcher = pattern.matcher(line);

            while (matcher.find()) {
                count++;
                if (songPrefs.getInt(SettingsContract.TRANSPOND_SONG, 0) == 0) {
                    File chordImg = new File(MainActivity.getChordsDir() + "/" + matcher.group().replaceAll("#", "w").trim() + "_0.gif");
                    if (chordImg.exists())
                        chordsMap.put(matcher.group().trim(), chordImg.toString());
                }
            }

        if (count == words.length)
            return true;
        return false;

    }

    private boolean isTabLine (String line){
        Pattern pattern = Pattern.compile("([\\-\\d\\|]{10,})");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find())
            return true;
        return false;
    }

    private String[] lineWithBreaks(String line, int count){
        if (isTabLine(line))
            return new String[]{line};
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

//        transponce(lineChords, 1);
        if (isTabLine(lineText))
            return new String[]{lineChords, lineText};
        String lineToBreaks = lineText;
        String lineChordsToBreaks = lineChords;
        int lineLength = lineToBreaks.length();
        List<String> linesBreaks = new ArrayList<>();
        while (count < lineLength){
            for (int i = count; i > 1; i--){
                if (lineToBreaks.charAt(i) == ' '){
                    if (lineChordsToBreaks.length() <= i)
                        linesBreaks.add(lineChordsToBreaks);
                    if (lineChordsToBreaks.length() > i)
                        linesBreaks.add(lineChordsToBreaks.substring(0, i));
//                    else if (lineChordsToBreaks.length() < i)
//                        linesBreaks.add(lineChordsToBreaks);
//                    else if (lineChordsToBreaks.length() != i)
//                        linesBreaks.add(" ");
                    linesBreaks.add(lineToBreaks.substring(0, i));
                    if (lineChordsToBreaks.length() > i)
                        lineChordsToBreaks = lineChordsToBreaks.substring(i);
                    else
                        lineChordsToBreaks = "";
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
            if (!isTabLine(lines[i]) && isChordLine(lines[i])){
                if (count + 1 < lines[i + 1].length()){
                    String[] linesBreaks = twoLinesWithBreaks(lines[i], lines[i+1], count);
                    for (String line:linesBreaks){
                        TextView tv = createTextView();
                        String resultLine = line;
                        if (!isTabLine(line) && isChordLine(line)) {
                            tv.setTextColor(SettingsContract.CHORDS_COLOR);
                            if (transpondSong != 0)
                                resultLine = transponce(resultLine, transpondSong);
                        }
                        tv.setText(resultLine);

                    }
                    i++;
                } else {
                    TextView tv = createTextView();
                    String resultLine = lines[i];
                    if (!isTabLine(lines[i]) && isChordLine(lines[i])) {
                        tv.setTextColor(SettingsContract.CHORDS_COLOR);
                        if (transpondSong != 0)
                            resultLine = transponce(resultLine, transpondSong);
                    }
                    tv.setText(resultLine);

                }
            } else {
                if (count + 1 < lines[i].length()){
                    String[] linesBreaks = lineWithBreaks(lines[i], count);


                    for (String line:linesBreaks){
                        TextView tv = createTextView();
                        String resultLine = line;
                        if (!isTabLine(line) && isChordLine(line)) {
                            tv.setTextColor(SettingsContract.CHORDS_COLOR);
                            if (transpondSong != 0)
                                resultLine = transponce(resultLine, transpondSong);
                        }
                        tv.setText(resultLine);

                    }


                } else {
                    TextView tv = createTextView();
                    String resultLine = lines[i];
                    if (!isTabLine(lines[i]) && isChordLine(lines[i])) {
                        tv.setTextColor(SettingsContract.CHORDS_COLOR);
                        if (transpondSong != 0)
                            resultLine = transponce(resultLine, transpondSong);
                    }
                    tv.setText(resultLine);

                }
            }
        }
        if (prefs.getBoolean(SettingsContract.SHOW_CHORDS, Boolean.FALSE)){
            for(Map.Entry entry:chordsMap.entrySet()){
                File file = new File(entry.getValue().toString());
                createImage(file);
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

//    private String transponce(String line, int steps){
//        line = line.replaceAll("\\p{C}","");
//        Pattern pattern = Pattern.compile("(\\s*?\\S+)");
//        Matcher matcher = pattern.matcher(line);
//        StringBuilder sb = new StringBuilder();
//        while (matcher.find()){
//            String chordWidthSpaces = line.substring(matcher.start(), matcher.end());
//            String chordString = chordWidthSpaces.trim();
//            TestChord chord = TestChord.valueOf(chordString);
//            Pattern chordPattern = Pattern.compile(SettingsContract.TONES +
//                    SettingsContract.HALF_TONES +
//                    SettingsContract.MINOR +
//                    SettingsContract.ADDED +
//                    SettingsContract.SUS +
//                    SettingsContract.ADD);
//            Matcher chordMatcher = chordPattern.matcher(chordString);
//            while (chordMatcher.find()){
//                Log.d(TAG, "transponce: " + chordString.substring(chordMatcher.start(), chordMatcher.end()));
//            }
//
//            for (int i = 0; i < Math.abs(steps); i++){
//                if (steps > 0)
//                    chord = chord.getIncreaseChord();
//                else if (steps < 0){
//                    chord = chord.getDecreaseChord();
//                }
//            }
//            chordWidthSpaces = chordWidthSpaces.replaceAll(chordString, chord.toString());
//            sb.append(chordWidthSpaces);
////            Log.d(TAG, "transponce: " + sb.toString());
//        }
//        return null;
//    }

    private String transponce(String line, int steps){
        line = line.replaceAll("\\p{C}", "");
        Pattern pattern = Pattern.compile("(\\s*?" + SettingsContract.TONES +
                SettingsContract.HALF_TONES +
                SettingsContract.MINOR +
                SettingsContract.ADDED +
                SettingsContract.SUS +
                SettingsContract.ADD +
                SettingsContract.ENDNOTE +
                ")");
        Matcher matcher = pattern.matcher(line);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()){
            String chordWidthSpaces = line.substring(matcher.start(), matcher.end());
            String originalChord = chordWidthSpaces.trim();
            String chord = originalChord;
            for (int i = 0; i < Math.abs(steps); i++){
                if (steps > 0)
                    chord = increaseChordTone(chord);
                else if (steps < 0)
                    chord = decreaseChordTone(chord);
            }
            chordWidthSpaces = chordWidthSpaces.replaceAll(originalChord, chord);
            sb.append(chordWidthSpaces);
        }
        Log.d(TAG, "transponce: " + sb.toString());
        return sb.toString();
    }

    private String increaseChordTone (String chord){
        Pattern pattern = Pattern.compile(SettingsContract.TONES + SettingsContract.HALF_TONES);
        Matcher matcher = pattern.matcher(chord);
        while (matcher.find()){
            String tone = chord.substring(matcher.start(), matcher.end());
            String resultTone = TranspondUtil.increaseTone(tone);
            chord = chord.replaceAll(tone, resultTone);
        }
        File chordImg = new File(MainActivity.getChordsDir() + "/" + chord.replaceAll("#", "w") + "_0.gif");
        if (chordImg.exists())
            chordsMap.put(chord, chordImg.toString());
        return chord;
    }

    private String decreaseChordTone (String chord){
        Pattern pattern = Pattern.compile(SettingsContract.TONES + SettingsContract.HALF_TONES);
        Matcher matcher = pattern.matcher(chord);
        while (matcher.find()){
            String tone = chord.substring(matcher.start(), matcher.end());
            String resultTone = TranspondUtil.decreaseTone(tone);
            chord = chord.replaceAll(tone, resultTone);
        }
        File chordImg = new File(MainActivity.getChordsDir() + "/" + chord.replaceAll("#", "w") + "_0.gif");
        if (chordImg.exists())
            chordsMap.put(chord, chordImg.toString());
        return chord;
    }


}
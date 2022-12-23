package ru.centhis.songbook.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;


import java.io.File;
import java.io.IOException;

import ru.centhis.songbook.R;
import ru.centhis.songbook.data.Item;
import ru.centhis.songbook.data.SettingsContract;
import ru.centhis.songbook.data.Song;

public class EditTextActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    Item itemRoot;
    EditText editText;
    Song song;
    String file;
    String version;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_text);

        editText = findViewById(R.id.songTextET);


        if (getIntent().getExtras() != null){
            itemRoot = (Item) getIntent().getSerializableExtra("item");
            version = getIntent().getStringExtra(SettingsContract.SONG_EDIT_VERSION);
            if (version.equals(SettingsContract.SONG_EDIT_VERSION_UKULELE)){
                setTitle(getString(R.string.ukulele));
                file = SettingsContract.UKULELE_TEXT_FILE;
                File ukuleleFile = new File(itemRoot.getSource() + "/" + file);
                if (ukuleleFile.exists()){
                    song = new Song(itemRoot.getSource() + "/" + file);
                    editText.setText(getTextFromFile(song));
                } else {
                    song = new Song(itemRoot.getSource() + "/" + SettingsContract.GUITAR_TEXT_FILE);
                    editText.setText(getTextFromFile(song));
                    try {
                        ukuleleFile.createNewFile();
                    } catch (IOException e){
                        Log.e(TAG, "onCreate: ", e);
                    }

                    song = new Song(itemRoot.getSource() + "/" + file);
                }
            } else if (version.equals(SettingsContract.SONG_EDIT_VERSION_GUITAR)){
                setTitle(getString(R.string.guitar));
                file = SettingsContract.GUITAR_TEXT_FILE;
                song = new Song(itemRoot.getSource() + "/" + file);
                editText.setText(getTextFromFile(song));
            }

        }

        ActionBar actionBar = this.getSupportActionBar();
        if(actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                if (isTextChanged()){
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(getString(R.string.save_question));
                    builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //save file
                            String text = editText.getText().toString();
                            String[] lines = text.split("\n");
                            song.saveToFile(lines, itemRoot.getSource() + "/" + file);
                            Intent backIntent = new Intent(EditTextActivity.this, TextSongActivity.class);
                            backIntent.putExtra("item", itemRoot);
                            startActivity(backIntent);
                        }
                    });
                    builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //cancel
                            Intent backIntent = new Intent(EditTextActivity.this, TextSongActivity.class);
                            backIntent.putExtra("item", itemRoot);
                            startActivity(backIntent);
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else {
                    Intent backIntent = new Intent(EditTextActivity.this, TextSongActivity.class);
                    backIntent.putExtra("item", itemRoot);
                    startActivity(backIntent);
                }

        }
        return super.onOptionsItemSelected(item);
    }


    private boolean isTextChanged(){
        String editedText = editText.getText().toString();
        String originalText = getTextFromFile(song);
        if (editedText.equals(originalText))
            return false;
        return true;
    }

    private String getTextFromFile(Song song){
        StringBuilder sb = new StringBuilder();
        for (String line:song.getSongText()){
            sb.append(line + "\n");
        }
        return sb.toString();
    }

}
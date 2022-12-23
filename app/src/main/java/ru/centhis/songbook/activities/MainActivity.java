package ru.centhis.songbook.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ScrollCaptureCallback;
import android.view.ScrollCaptureSession;
import android.webkit.MimeTypeMap;
import android.widget.EditText;


import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import ru.centhis.songbook.R;
import ru.centhis.songbook.data.Item;
import ru.centhis.songbook.data.ItemAdapter;
import ru.centhis.songbook.data.SettingsContract;
import ru.centhis.songbook.util.DeleteSongUtil;

public class MainActivity extends AppCompatActivity implements ItemAdapter.ViewHolder.OnItemListener {

    private static final String TAG = MainActivity.class.getName();

    private static File songDir;
    private static File chordsDir;
    private static File filesDir;
    private static File ukuleleChordsDir;
    RecyclerView recyclerView;
    ItemAdapter adapter;
    List<Item> items = new ArrayList<>();
    List<Item> itemsFiltered;
    EditText mainSearchET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainSearchET = findViewById(R.id.mainSearchET);
        mainSearchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (items.size() > 0)
                    recyclerViewFilter(editable.toString());
            }
        });

        filesDir = new File(getFilesDir().toString());
        songDir = new File(getFilesDir() + "/songs");
        chordsDir = new File(getFilesDir() + "/chords");
        ukuleleChordsDir = new File(getFilesDir() + "/ukuleleChords");

        setInitialData(songDir);
        recyclerView = findViewById(R.id.list);

        if (itemsFiltered != null) {
            adapter = new ItemAdapter(this, itemsFiltered, this);
            recyclerView.setAdapter(adapter);
        }



    }

    @Override
    protected void onResume() {
        super.onResume();
//        items.clear();
//        itemsFiltered.clear();
//        setInitialData(songDir);
        if (itemsFiltered != null)
            adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.songbook_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:
                Intent openSettings = new Intent(this, SettingsActivity.class);
                startActivity(openSettings);
                return true;
            case R.id.find_AmDm:
                Intent openSearchAmDm = new Intent(this, SearchActivity.class);
                openSearchAmDm.putExtra(SettingsContract.SONG_SEARCH_SITE, SettingsContract.FIND_AMDM);
                startActivity(openSearchAmDm);
                return true;
            case R.id.find_5lad:
                Intent openSearch5lad = new Intent(this, SearchActivity.class);
                openSearch5lad.putExtra(SettingsContract.SONG_SEARCH_SITE, SettingsContract.FIND_5LAD);
                startActivity(openSearch5lad);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setInitialData(File dir){
//        File dir = filesDir;
        if (dir.exists()) {
            try {
                for (File file : dir.listFiles()) {
                    boolean isMp3 = true;
                    boolean isGuitar = false;
                    boolean isUkulele = false;


                    Item item = new Item();
                    item.setName(file.getName());
                    item.setSource(file.getAbsolutePath());
                    if (file.isDirectory()) {
                        item.setType("folder");
                        for (File song:file.listFiles()) {
                            if (song.isDirectory()){
                                boolean isMp3Exist = false;
                                for (File songFile:song.listFiles()){
                                    if (songFile.isFile() && songFile.getName().equals(SettingsContract.GUITAR_TEXT_FILE))
                                        isGuitar = true;
                                    else if (songFile.isFile() && songFile.getName().equals(SettingsContract.UKULELE_TEXT_FILE))
                                        isUkulele = true;
                                    else if (songFile.isFile() && FilenameUtils.getExtension(songFile.getAbsolutePath()).equals("mp3"))
                                        isMp3Exist = true;
                                }
                                if (!isMp3Exist)
                                    isMp3 = false;
                            }
                        }
                        item.setGuitar(isGuitar);
                        item.setUkulele(isUkulele);
                        item.setMp3(isMp3);
                    }
                    else if (file.isFile() && MimeTypeMap.getFileExtensionFromUrl(String.valueOf(Uri.fromFile(file))).equals("txt"))
                        item.setType("text");
//                    Item item = new Item(file.getName(), file.getAbsolutePath(), "folder");
                    if (item.getType() != null)
                        items.add(item);
                }
                Collections.sort(items);
                itemsFiltered = new ArrayList<>(items);
            } catch (NullPointerException e){
                Log.e(TAG, "Empty folder", e);
            }

        }
    }


    @Override
    public void onItemClick(int position) {
        Log.d(TAG, "onItemClick: clicked " + itemsFiltered.get(position).getSource());
        Intent intent = new Intent(this, ListSongActivity.class);
        intent.putExtra("item", itemsFiltered.get(position));
        startActivity(intent);

    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case 121:
                Item itemToDelete = itemsFiltered.get(item.getGroupId());
                SharedPreferences prefs = getSharedPreferences(SettingsContract.APP_NAME, MODE_PRIVATE);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.alert_dialog_delete_artist_message_start) + itemToDelete.getName() + getString(R.string.alert_dialog_delete_song_message_end)).
                        setTitle(getString(R.string.alert_dialog_delete_artist_title));
                builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            DeleteSongUtil.delete(itemToDelete.getSource(), getSongDir().toString(), prefs);
                            Intent intent = new Intent(MainActivity.this, MainActivity.class);
                            startActivity(intent);
                        } catch (IOException e){
                            Log.e(TAG, "onClick: ", e);
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
                return true;
            default:
                return super.onContextItemSelected(item);
        }


    }

    private void recyclerViewFilter(String filter){
        if (itemsFiltered != null)
            itemsFiltered.clear();
        for (Item item:items){
            if(item.getName().toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT)))
                itemsFiltered.add(item);
        }
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    public static File getFileDir(){
        return filesDir;
    }

    public static File getSongDir(){
        return songDir;
    }

    public static File getChordsDir(){
        return chordsDir;
    }
    public static File getUkuleleChordsDir(){
        return ukuleleChordsDir;
    }
}
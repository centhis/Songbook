package ru.centhis.songbook.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    RecyclerView recyclerView;
    ItemAdapter adapter;
    List<Item> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        filesDir = new File(getFilesDir().toString());
        songDir = new File(getFilesDir() + "/songs");
        chordsDir = new File(getFilesDir() + "/chords");

        setInitialData(songDir);
        recyclerView = findViewById(R.id.list);
        adapter = new ItemAdapter(this, items, this);
        recyclerView.setAdapter(adapter);




    }

    @Override
    protected void onResume() {
        super.onResume();
        items.clear();
        setInitialData(songDir);
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
                Intent openSearch = new Intent(this, SearchActivity.class);
                startActivity(openSearch);
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
                    Item item = new Item();
                    item.setName(file.getName());
                    item.setSource(file.getAbsolutePath());
                    if (file.isDirectory())
                        item.setType("folder");
                    else if (file.isFile() && MimeTypeMap.getFileExtensionFromUrl(String.valueOf(Uri.fromFile(file))).equals("txt"))
                        item.setType("text");
//                    Item item = new Item(file.getName(), file.getAbsolutePath(), "folder");
                    if (item.getType() != null)
                        items.add(item);
                }
                Collections.sort(items);
            } catch (NullPointerException e){
                Log.e(TAG, "Empty folder", e);
            }

        }
    }


    @Override
    public void onItemClick(int position) {
        Log.d(TAG, "onItemClick: clicked " + items.get(position).getSource());
        Intent intent = new Intent(this, ListSongActivity.class);
        intent.putExtra("item", items.get(position));
        startActivity(intent);

    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case 121:
                Item itemToDelete = items.get(item.getGroupId());
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

    public static File getFileDir(){
        return filesDir;
    }

    public static File getSongDir(){
        return songDir;
    }

    public static File getChordsDir(){
        return chordsDir;
    }
}
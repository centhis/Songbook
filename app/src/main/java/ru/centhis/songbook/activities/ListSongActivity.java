package ru.centhis.songbook.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import ru.centhis.songbook.R;
import ru.centhis.songbook.data.Item;
import ru.centhis.songbook.data.ItemAdapter;
import ru.centhis.songbook.data.SettingsContract;
import ru.centhis.songbook.util.DeleteSongUtil;

public class ListSongActivity extends AppCompatActivity implements ItemAdapter.ViewHolder.OnItemListener{

    private static final String TAG = MainActivity.class.getName();

    Item itemRoot;
    List<Item> items = new ArrayList<>();
    List<Item> itemsFiltered;
    RecyclerView recyclerView;
    ItemAdapter adapter;
    SharedPreferences prefs;
    EditText songSearchET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_song);

        songSearchET = findViewById(R.id.songSearchET);
        songSearchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                recyclerViewFilter(editable.toString());
            }
        });

        prefs = getSharedPreferences(SettingsContract.APP_NAME, MODE_PRIVATE);

        if (getIntent().getExtras() != null){
            itemRoot = (Item) getIntent().getSerializableExtra("item");
            prefs.edit().putString(SettingsContract.CURRENT_AUTHOR, itemRoot.getName()).apply();
            prefs.edit().putString(SettingsContract.CURRENT_SOURCE, itemRoot.getSource()).apply();
        } else {
            itemRoot = new Item();
            itemRoot.setName(prefs.getString(SettingsContract.CURRENT_AUTHOR, null));
            itemRoot.setSource(prefs.getString(SettingsContract.CURRENT_SOURCE, null));
            itemRoot.setType("folder");
        }
        if (itemRoot != null) {
            setTitle(itemRoot.getName());
            setInitialData(new File(itemRoot.getSource()));
            recyclerView = findViewById(R.id.listSongs);
            adapter = new ItemAdapter(this, itemsFiltered, this);
            recyclerView.setAdapter(adapter);
        }

        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

    }

    @Override
    protected void onResume() {
        super.onResume();
        itemRoot.setName(prefs.getString(SettingsContract.CURRENT_AUTHOR, null));
        itemRoot.setSource(prefs.getString(SettingsContract.CURRENT_SOURCE, null));
//        setInitialData(new File(itemRoot.getSource()));
        recyclerView.getAdapter().notifyDataSetChanged();
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
        switch (item.getItemId()) {
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
        if (items != null)
            items.clear();
        if (itemsFiltered != null)
            itemsFiltered.clear();
        if (dir.exists()){
            try {
                for (File file:dir.listFiles()){
                    boolean isMp3 = false;
                    boolean isGuitar = false;
                    boolean isUkulele = false;

                    Item item = new Item();
                    item.setName(file.getName());
                    item.setSource(file.getAbsolutePath());
                    if (file.isDirectory()) {
                        item.setType("folder");
                        for (File songFile:file.listFiles()){
                            if (songFile.isFile() && songFile.getName().equals(SettingsContract.GUITAR_TEXT_FILE))
                                isGuitar = true;
                            else if (songFile.isFile() && songFile.getName().equals(SettingsContract.UKULELE_TEXT_FILE))
                                isUkulele = true;
                            else if (songFile.isFile() && FilenameUtils.getExtension(songFile.getAbsolutePath()).equals("mp3"))
                                isMp3 = true;
                        }
                        item.setMp3(isMp3);
                        item.setUkulele(isUkulele);
                        item.setGuitar(isGuitar);
                    }
                    else if (file.isFile() && MimeTypeMap.getFileExtensionFromUrl(String.valueOf(Uri.fromFile(file))).equals("txt"))
                        item.setType("text");
                    if (item.getType() != null)
                        items.add(item);

                }
            } catch (NullPointerException e){
                Log.e(TAG, "setInitialData: empty folder", e);
            }
            Collections.sort(items);
            itemsFiltered = new ArrayList<>(items);
        }
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(this, TextSongActivity.class);
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
                builder.setMessage(getString(R.string.alert_dialog_delete_song_message_start) + itemToDelete.getName() + getString(R.string.alert_dialog_delete_song_message_end)).
                        setTitle(getString(R.string.alert_dialog_delete_song_title));
                builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            if (DeleteSongUtil.delete(itemToDelete.getSource(), MainActivity.getSongDir().toString(), prefs)){
                                Intent intent = new Intent(ListSongActivity.this, MainActivity.class);
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(ListSongActivity.this, ListSongActivity.class);
                                intent.putExtra("item", itemRoot);
                                startActivity(intent);
                            }
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
            case 111:
                Item itemToEdit = itemsFiltered.get(item.getGroupId());
                Intent openEdit = new Intent(ListSongActivity.this, EditTextActivity.class);
                openEdit.putExtra("item", itemToEdit);
                openEdit.putExtra(SettingsContract.SONG_EDIT_VERSION, SettingsContract.SONG_EDIT_VERSION_GUITAR);
                startActivity(openEdit);
                return true;
            case 110:
                Item itemToEditUkulele = itemsFiltered.get(item.getGroupId());
                Intent openEditUkulele = new Intent(ListSongActivity.this, EditTextActivity.class);
                openEditUkulele.putExtra("item", itemToEditUkulele);
                openEditUkulele.putExtra(SettingsContract.SONG_EDIT_VERSION, SettingsContract.SONG_EDIT_VERSION_UKULELE);
                startActivity(openEditUkulele);
                return true;
            default:
                return super.onContextItemSelected(item);
        }

    }

    private void recyclerViewFilter(String filter){
        itemsFiltered.clear();
        for (Item item:items){
            if (item.getName().toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT)))
                itemsFiltered.add(item);
        }
        recyclerView.getAdapter().notifyDataSetChanged();
    }
}
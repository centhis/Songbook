package ru.centhis.songbook.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.centhis.songbook.R;
import ru.centhis.songbook.data.Item;
import ru.centhis.songbook.data.ItemAdapter;
import ru.centhis.songbook.data.SettingsContract;

public class ListSongActivity extends AppCompatActivity implements ItemAdapter.ViewHolder.OnItemListener{

    private static final String TAG = MainActivity.class.getName();

    Item itemRoot;
    List<Item> items = new ArrayList<>();
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_song);

        SharedPreferences prefs = getSharedPreferences(SettingsContract.APP_NAME, MODE_PRIVATE);

        if (getIntent().getExtras() != null){
            itemRoot = (Item) getIntent().getSerializableExtra("item");
            prefs.edit().putString("currentAuthor", itemRoot.getName()).apply();
            prefs.edit().putString("currentSource", itemRoot.getSource()).apply();
        } else {
            itemRoot = new Item();
            itemRoot.setName(prefs.getString("currentAuthor", null));
            itemRoot.setSource(prefs.getString("currentSource", null));
            itemRoot.setType("folder");
        }
        if (itemRoot != null) {
            setTitle(itemRoot.getName());
            setInitialData(new File(itemRoot.getSource()));
            recyclerView = findViewById(R.id.listSongs);
            ItemAdapter adapter = new ItemAdapter(this, items, this);
            recyclerView.setAdapter(adapter);
        }

        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

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

    private void setInitialData(File dir){
        if (dir.exists()){
            try {
                for (File file:dir.listFiles()){
                    Item item = new Item();
                    item.setName(file.getName());
                    item.setSource(file.getAbsolutePath());
                    if (file.isDirectory())
                        item.setType("folder");
                    else if (file.isFile() && MimeTypeMap.getFileExtensionFromUrl(String.valueOf(Uri.fromFile(file))).equals("txt"))
                        item.setType("text");
                    if (item.getType() != null)
                        items.add(item);
                    Collections.sort(items);
                }
            } catch (NullPointerException e){
                Log.e(TAG, "setInitialData: empty folder", e);
            }
        }
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(this, TextSongActivity.class);
        intent.putExtra("item", items.get(position));
        startActivity(intent);
    }
}
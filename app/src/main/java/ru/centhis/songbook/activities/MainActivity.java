package ru.centhis.songbook.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
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

public class MainActivity extends AppCompatActivity implements ItemAdapter.ViewHolder.OnItemListener {

    private static final String TAG = MainActivity.class.getName();
    private static File filesDir;
    RecyclerView recyclerView;
    List<Item> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        filesDir = new File(getFilesDir() + "/songs");

        setInitialData(filesDir);
        recyclerView = findViewById(R.id.list);
        ItemAdapter adapter = new ItemAdapter(this, items, this);
        recyclerView.setAdapter(adapter);




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
}
package ru.centhis.songbook.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import ru.centhis.songbook.R;
import ru.centhis.songbook.data.ItemAdapter;
import ru.centhis.songbook.data.SearchSongResult;
import ru.centhis.songbook.data.SearchSongResultAdapter;
import ru.centhis.songbook.parsing.SearchAmDmTask;

public class SearchActivity extends AppCompatActivity implements SearchSongResultAdapter.ViewHolder.OnItemListener {

    private static final String TAG = SearchActivity.class.getName();

    EditText searchET;
    Button searchBTN;
    RecyclerView recyclerView;
    List<SearchSongResult> searchSongResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchET = findViewById(R.id.searchET);
        searchBTN = findViewById(R.id.searchBTN);
        recyclerView = findViewById(R.id.search_list);

        setTitle(getString(R.string.search_title));

        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        searchBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    final ProgressDialog dialog = new ProgressDialog(SearchActivity.this);
                    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    dialog.setCancelable(false);
                    dialog.setMessage(getString(R.string.loading));
                    dialog.show();
                    String url = "https://amdm.ru/search/?q=" + URLEncoder.encode(searchET.getText().toString(), StandardCharsets.UTF_8.toString());
                    new SearchAmDmTask(url, new SearchAmDmTask.Callback() {
                        @Override
                        public void onDataLoaded(List<SearchSongResult> result) {
                            dialog.dismiss();
                            searchSongResults = result;
                            SearchSongResultAdapter adapter = new SearchSongResultAdapter(SearchActivity.this, result, SearchActivity.this);
                            recyclerView.setAdapter(adapter);
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e(TAG, "onError: ", e);
                            dialog.dismiss();
                        }
                    }).execute(url);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(this, AddSongActivity.class);
        intent.putExtra("item", searchSongResults.get(position));
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
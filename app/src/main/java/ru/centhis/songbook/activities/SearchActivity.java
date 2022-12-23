package ru.centhis.songbook.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ru.centhis.songbook.R;
import ru.centhis.songbook.data.ItemAdapter;
import ru.centhis.songbook.data.SearchSongResult;
import ru.centhis.songbook.data.SearchSongResultAdapter;
import ru.centhis.songbook.data.SettingsContract;
import ru.centhis.songbook.parsing.Search5ladTask;
import ru.centhis.songbook.parsing.SearchAmDmTask;

public class SearchActivity extends AppCompatActivity implements SearchSongResultAdapter.ViewHolder.OnItemListener {

    private static final String TAG = SearchActivity.class.getName();

    EditText searchET;
    Button searchBTN;
    RecyclerView recyclerView;
    List<SearchSongResult> searchSongResults;
    List<SearchSongResult> searchSongResultsFiltered;
    String searchSite;
    WebView searchWV;
    String HTMLSource;
    ProgressDialog dialog;
    int level;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchET = findViewById(R.id.searchET);
        searchBTN = findViewById(R.id.searchBTN);
        recyclerView = findViewById(R.id.search_list);
        searchWV = findViewById(R.id.searchWV);


        if (getIntent().getExtras() != null){
            searchSite = getIntent().getStringExtra(SettingsContract.SONG_SEARCH_SITE);
        }
        if (searchSite.equals(SettingsContract.FIND_AMDM))
            setTitle(getString(R.string.find_AmDm));
        else if (searchSite.equals(SettingsContract.FIND_5LAD)) {
            searchET.addTextChangedListener(new TextWatcher() {
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
            setTitle(getString(R.string.find_5lad));
            dialog = new ProgressDialog(SearchActivity.this);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCancelable(false);
            dialog.setMessage(getString(R.string.loading));
            dialog.show();
            searchBTN.setVisibility(View.GONE);
            level = 0;
            prepareWebView(searchWV, "https://www.5lad.ru", level);
        }


        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        searchBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    dialog = new ProgressDialog(SearchActivity.this);
                    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    dialog.setCancelable(false);
                    dialog.setMessage(getString(R.string.loading));
                    dialog.show();
                    if (searchSite.equals(SettingsContract.FIND_AMDM)) {
                        String url = "https://amdm.ru/search/?q=" + URLEncoder.encode(searchET.getText().toString(), StandardCharsets.UTF_8.toString());
                        new SearchAmDmTask(url, new SearchAmDmTask.Callback() {
                            @Override
                            public void onDataLoaded(List<SearchSongResult> result) {
                                dialog.dismiss();
                                searchSongResults = result;
                                searchSongResultsFiltered = new ArrayList<>(searchSongResults);
                                SearchSongResultAdapter adapter = new SearchSongResultAdapter(SearchActivity.this, searchSongResultsFiltered, SearchActivity.this);
                                recyclerView.setAdapter(adapter);
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e(TAG, "onError: ", e);
                                dialog.dismiss();
                            }
                        }).execute(url);
                    }

                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "onClick: ", e);
                }
            }
        });
    }


    @Override
    public void onItemClick(int position) {
        if (searchSite.equals(SettingsContract.FIND_5LAD) && level == 0){
            dialog.show();
            level = 1;
            prepareWebView(searchWV, searchSongResultsFiltered.get(position).getHref(), level);
            setTitle(searchSongResultsFiltered.get(position).getSongName());
            searchET.setText("");
        } else if (searchSite.equals(SettingsContract.FIND_5LAD) && level == 1 && searchSongResultsFiltered.get(position).getSongName().equals("...")){
            dialog.show();
            level = 0;
            prepareWebView(searchWV, searchSongResultsFiltered.get(position).getHref(), level);
            setTitle(getString(R.string.find_AmDm));
            searchET.setText("");
        } else if (searchSite.equals(SettingsContract.FIND_5LAD) && level == 1){
            Intent intent = new Intent(this, AddSongActivity.class);
            SearchSongResult item = searchSongResultsFiltered.get(position);
            item.setArtistName(getTitle().toString());
            intent.putExtra("item", item);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, AddSongActivity.class);
            intent.putExtra("item", searchSongResultsFiltered.get(position));
            startActivity(intent);
        }
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

    private void prepareWebView(WebView webView, String url, int level){
//        dialog.setMessage(url);
        webView.getSettings().setJavaScriptEnabled(true);
//        dialog.setMessage("javascript");
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
//                String script = "javascript:window.JS.getHTML" +
//                        "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');";
                String script = "";
                if (level == 0) {
                    script = "javascript:window.JS.getHTML" +
                            "('<html>'+document.getElementsByClassName('level0')[7].innerHTML+'</html>');";
                } else if (level == 1){
                    script = "javascript:window.JS.getHTML" +
                            "('<html>'+document.getElementsByClassName('level0')[8].innerHTML+'</html>');";
                }
//                dialog.setMessage("javascript4");
                webView.evaluateJavascript(script, null);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Log.e(TAG, "onPageFinished: ", e);
                }
                if (HTMLSource != null){
                    if (level == 0){
//                        dialog.setMessage("preparing json");
                        searchSongResults = prepareArtistData(HTMLSource, url);
                        searchSongResultsFiltered = new ArrayList<>(searchSongResults);
//                        dialog.setMessage("loaded");
                        dialog.dismiss();
                        SearchSongResultAdapter adapter = new SearchSongResultAdapter(SearchActivity.this, searchSongResultsFiltered, SearchActivity.this);
                        recyclerView.setAdapter(adapter);
                    } else if (level == 1){
                        searchSongResults = prepareArtistData(HTMLSource, url);
                        searchSongResultsFiltered = new ArrayList<>(searchSongResults);
                        dialog.dismiss();
                        SearchSongResultAdapter adapter = new SearchSongResultAdapter(SearchActivity.this, searchSongResultsFiltered, SearchActivity.this);
                        recyclerView.setAdapter(adapter);
                    }
                } else {
                    Toast.makeText(SearchActivity.this, "Something wrong with http answer", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        });
//        dialog.setMessage("javascript2");
        webView.addJavascriptInterface(new JS(this), "JS");
//        dialog.setMessage("javascript3");
        webView.loadUrl(url);

    }

    private List<SearchSongResult> prepareArtistData(String HTMLSource, String url){
        Document document = Jsoup.parse(HTMLSource);
        List<SearchSongResult> result5lad = new ArrayList<>();
        Elements elements = null;
        if (level == 0)
            elements = document.select("li.level1");
        else if (level == 1) {
            result5lad.add(new SearchSongResult("", "...", "https://www.5lad.ru"));
            elements = document.select("li.level2");
        }
        for(Element element:elements){
            String artistName = element.child(0).child(0).text();
            String href = "https://www.5lad.ru" + element.child(0).attr("href");
            result5lad.add(new SearchSongResult("", artistName, href));
            dialog.setMessage(artistName);
        }
        return result5lad;
    }

    private void recyclerViewFilter(String filter){
        searchSongResultsFiltered.clear();
        for (SearchSongResult searchSongResult:searchSongResults){
            if (searchSongResult.getSongName().toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT)))
                searchSongResultsFiltered.add(searchSongResult);
        }
        recyclerView.getAdapter().notifyDataSetChanged();
    }


    class JS {
        private Context context;

        public JS(Context context){
            this.context = context;
        }

        @JavascriptInterface
        public void getHTML(String html){
            HTMLSource = html;
        }
    }
}
package ru.centhis.songbook.parsing;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import ru.centhis.songbook.data.SearchSongResult;

public class SearchAmDmTask extends AsyncTask<String, Void, List<SearchSongResult>> {

    private final String mSearchString;
    private final Callback mCallback;
    private Exception mException;

    public interface Callback {
        void onDataLoaded(List<SearchSongResult> result);
        void onError(Exception e);
    }

    public SearchAmDmTask(String searchString, Callback callback){
        mSearchString = searchString;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(List<SearchSongResult> result){
        super.onPostExecute(result);

        if (mException != null)
            mCallback.onError(mException);
        else
            mCallback.onDataLoaded(result);
    }

    @Override
    protected List<SearchSongResult> doInBackground(String... strings) {
        try {
            List<SearchSongResult> result = new ArrayList<>();
            Document doc = Jsoup.connect(strings[0])
                    .userAgent("Chrome/4.0.249.0 Safari/532.5")
                    .referrer("http://www.google.com")
                    .get();
            Elements elements = doc.select("td.artist_name");
            for (Element element : elements){
                String artistName = element.child(0).text();
                String songName = element.child(1).text();
                String href = element.child(1).attr("href");
                result.add(new SearchSongResult(artistName, songName, href));
            }
            return result;
        } catch (Exception e){
            mException = e;
        }

        return null;
    }
}

package ru.centhis.songbook.parsing;

import android.media.Image;
import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ru.centhis.songbook.data.ParsedSong;


public class ParseAmDmTask extends AsyncTask<String, Void, ParsedSong> {

    private final String mUrl;
    private final Callback mCallback;
    private Exception mException;

    public interface Callback {
        void onDataLoaded(ParsedSong result);

        void onError(Exception e);
    }

    public ParseAmDmTask(String url, Callback callback){
        mUrl = url;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(ParsedSong result) {
        super.onPostExecute(result);

        if (mException != null){
            mCallback.onError(mException);
        } else {
            mCallback.onDataLoaded(result);
        }
    }

    @Override
    protected ParsedSong doInBackground(String... strings) {
        try {
            Document doc = Jsoup.connect(strings[0])
                    .userAgent("Chrome/4.0.249.0 Safari/532.5")
                    .referrer("http://www.google.com")
                    .get();
            Element element = doc.select("div.b-podbor__text").first();
            String text = null;
            if (element != null)
                text = element.wholeText();
            List<String> chordsSrc = new ArrayList<>();
            Elements elements = doc.select("div#song_chords").select("img");
            for (Element elementImg:elements){
                chordsSrc.add(elementImg.absUrl("src"));
            }

            return new ParsedSong(text, chordsSrc);
        } catch (Exception e){
            mException = e;
        }


        return null;
    }
}

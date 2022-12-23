package ru.centhis.songbook.parsing;

import android.net.InetAddresses;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.WebView;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.chrome.ChromeDriver;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import ru.centhis.songbook.data.SearchSongResult;

public class Search5ladTask extends AsyncTask<String, Void, List<SearchSongResult>> {

    private final String mSearchString;
    private final Callback mCallback;
    private Exception mException;

    public interface Callback {
        void onDataLoaded(List<SearchSongResult> result);
        void onError(Exception e);
    }

    public Search5ladTask(String searchString, Callback callback){
        mSearchString = searchString;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(List<SearchSongResult> searchSongResults) {
        super.onPostExecute(searchSongResults);

        if (mException != null)
            mCallback.onError(mException);
        else
            mCallback.onDataLoaded(searchSongResults);
    }

    @Override
    protected List<SearchSongResult> doInBackground(String... strings) {
        try {
//            URL url = new URL("https://www.5lad.ru/");
//            final HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
////            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36 OPR/93.0.0.0");
//            connection.setRequestProperty("Host", "www.5lad.ru");
//            connection.setRequestMethod("GET");
//            connection.setReadTimeout(10000);
//            connection.connect();
//            final InputStream stream = connection.getInputStream();
//            String s1 = getString(stream);
//            Log.d("TAG", "doInBackground: " + s1);
        } catch (Exception e){
            mException = e;
        }
//        System.setProperty("webdriver.chrome.driver", "lib");



//        try{
//            List<SearchSongResult> results = new ArrayList<>();
//            Document doc = Jsoup.connect("https://www.5lad.ru/")
////                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
//                    .method(Connection.Method.GET)
//                    .followRedirects(true)
//                    .header("Host", "www.5lad.ru")
////                    .referrer("https://google.com")
//                    .timeout(20000)
//                    .get();
//            Elements elements = doc.select("div.VanillaReact.OrganicTitle.OrganicTitle_multiline.Typo.Typo_text_l.Typo_line_m.organic__title-wrapper");
//            for (Element element:elements){
//
//            }
//        } catch (Exception e){
//            mException = e;
//        }
        try {

        } catch (Exception e){
            mException = e;
        }

        return null;
    }

    public static String getString(InputStream is) {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            /** finally block to close the {@link BufferedReader} */
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }
}

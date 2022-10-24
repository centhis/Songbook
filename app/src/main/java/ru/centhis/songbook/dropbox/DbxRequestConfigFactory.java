package ru.centhis.songbook.dropbox;

import android.util.Log;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.http.OkHttp3Requestor;

public class DbxRequestConfigFactory {
    private static DbxRequestConfig sDbxRequestConfig;

    public static DbxRequestConfig getRequestConfig(){
        Log.d("SB-file", "DbxRequestConfigFactory DbxRequestConfig start");
        if (sDbxRequestConfig == null){
            sDbxRequestConfig = DbxRequestConfig.newBuilder("CenthisSongBook").
                    withHttpRequestor(new OkHttp3Requestor(OkHttp3Requestor.defaultOkHttpClient())).build();
        }
        Log.d("SB-file", "DbxRequestConfigFactory DbxRequestConfig end");
        return sDbxRequestConfig;
    }
}

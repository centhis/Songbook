package ru.centhis.songbook.dropbox;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.dropbox.core.android.Auth;
import com.dropbox.core.json.JsonReadException;
import com.dropbox.core.oauth.DbxCredential;

import java.util.List;

public abstract class DropboxActivity extends AppCompatActivity {

    private final static boolean USE_SLT = true;

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getSharedPreferences("ru.centhis.songbook", MODE_PRIVATE);

        if (USE_SLT){
            String serializedCredential = prefs.getString("credential", null);

            if (serializedCredential == null){
                DbxCredential credential = Auth.getDbxCredential();
                if (credential != null){
                    prefs.edit().putString("credential", credential.toString()).apply();
                    initAndLoadData(credential);
                }
            } else {
                try {
                    DbxCredential credential = DbxCredential.Reader.readFully(serializedCredential);
                    initAndLoadData(credential);
                } catch (JsonReadException e){
                    throw new IllegalStateException("Credential data corrupted: " + e.getMessage());
                }
            }
        } else {
            String accessToken = prefs.getString("access-token", null);
            if (accessToken == null){
                accessToken = Auth.getOAuth2Token();
                if (accessToken != null){
                    prefs.edit().putString("access-token", accessToken).apply();
                    initAndLoadData(accessToken);
                }
            } else {
                initAndLoadData(accessToken);
            }
        }

        String uid = Auth.getUid();
        String storeUid = prefs.getString("user-id", null);
        if (uid != null && !uid.equals(storeUid)){
            prefs.edit().putString("user-id", uid).apply();
        }
    }

    private void initAndLoadData(String accessToken){
        DropboxClientFactory.init(accessToken);
        loadData();
    }

    private void initAndLoadData(DbxCredential dbxCredential){
        DropboxClientFactory.init(dbxCredential);
        loadData();
    }

    protected abstract void loadData();

    protected boolean hasToken(){
        SharedPreferences prefs = getSharedPreferences("ru.centhis.songbook", MODE_PRIVATE);
        if (USE_SLT){
            return prefs.getString("credential", null) != null;
        } else {
            String accessToken = prefs.getString("access-token", null);
            return accessToken != null;
        }
    }
    public static void startOAuth2Authentication(Context context, String app_key, List<String> scope) {
        Log.d("SB-file", "dropboxactivity startOAuth2Authentication");
        if (USE_SLT)
            Auth.startOAuth2PKCE(context, app_key, DbxRequestConfigFactory.getRequestConfig(), scope);
        else
            Auth.startOAuth2Authentication(context, app_key);
    }
}

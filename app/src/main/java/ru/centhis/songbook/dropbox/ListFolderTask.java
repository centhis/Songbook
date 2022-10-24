package ru.centhis.songbook.dropbox;

import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderResult;

public class ListFolderTask extends AsyncTask<String, Void, ListFolderResult> {

    private final DbxClientV2 mDbxClient;
    private final Callback mCalback;
    private Exception mException;



    public interface Callback {
        void onDataLoaded(ListFolderResult result);

        void onError(Exception e);
    }

    public ListFolderTask(DbxClientV2 dbxClient, Callback callback){
        mDbxClient = dbxClient;
        mCalback = callback;
    }

    @Override
    protected void onPostExecute(ListFolderResult result) {
        super.onPostExecute(result);

        if (mException != null){
            mCalback.onError(mException);
        } else {
            mCalback.onDataLoaded(result);
        }
    }

    @Override
    protected ListFolderResult doInBackground(String... strings) {
        try {
//            return mDbxClient.files().listFolder(strings[0]);
            return mDbxClient.files().listFolderBuilder(strings[0]).withRecursive(true).start();
        } catch (DbxException e){
            mException = e;
        }

        return null;
    }
}

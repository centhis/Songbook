package ru.centhis.songbook.dropbox;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DeleteResult;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.SearchV2Builder;
import com.dropbox.core.v2.files.SearchV2Result;

import java.util.List;

import ru.centhis.songbook.activities.MainActivity;

public class DeleteFileTask extends AsyncTask<String, Void, DeleteResult> {
    private static final String TAG = MainActivity.class.getName();

    private final Context mContext;
    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;

    public interface Callback{
        void onDeleteComplete(DeleteResult result);
        void onError(Exception e);
    }

    public DeleteFileTask(Context context, DbxClientV2 dbxClient, Callback callback){
        mContext = context;
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(DeleteResult result) {
        super.onPostExecute(result);
        if (mException != null)
            mCallback.onError(mException);
//        else if (result == null)
//            mCallback.onError(null);
        else
            mCallback.onDeleteComplete(result);
    }

    @Override
    protected DeleteResult doInBackground(String... strings) {
        try {

            String search = "/" + strings[0];
            Metadata metadata = null;
                    try {
                        metadata = mDbxClient.files().getMetadata(search);
                    } catch (Exception e){
                        Log.d(TAG, "doInBackground: File" + search + " not exist");
                    }
            if (metadata != null) {
                mDbxClient.files().deleteV2(metadata.getPathLower());
                String[] folders = strings[0].split("/");
                if (folders.length > 2){
                    ListFolderResult listFolderResult = mDbxClient.files().listFolderBuilder("/" + folders[0] + "/" + folders[1]).start();
                    if (!listFolderResult.getHasMore())
                        mDbxClient.files().deleteV2("/" + folders[0] + "/" + folders[1]);
                }
            }

        } catch (DbxException e){
            mException = e;
        }
        return null;
    }
}

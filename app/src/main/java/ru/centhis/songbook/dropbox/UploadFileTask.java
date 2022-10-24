package ru.centhis.songbook.dropbox;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class UploadFileTask extends AsyncTask<String, Void, FileMetadata> {
    private final Context mContext;
    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;

    public interface Callback {
        void onUploadComplete(FileMetadata result);
        void onError(Exception e);
    }

    public UploadFileTask(Context context, DbxClientV2 dbxClient, Callback callback){
        mContext = context;
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(FileMetadata fileMetadata) {
        super.onPostExecute(fileMetadata);
        if (mException != null) {
            mCallback.onError(mException);
        } else if (fileMetadata == null){
            mCallback.onError(null);
        } else {
            mCallback.onUploadComplete(fileMetadata);
        }
    }

    @Override
    protected FileMetadata doInBackground(String... strings) {
        String localUri = strings[0];
        File localFile = UriHelpers.getFileForUri(mContext, Uri.parse(localUri));

        if (localFile != null){
            String remoteFolderPath = strings[1];

            String remoteFileName = localFile.getName();

            try (InputStream inputStream = new FileInputStream(localFile)){
                return mDbxClient.files().uploadBuilder(remoteFolderPath + "/" + remoteFileName).
                        withMode(WriteMode.OVERWRITE).uploadAndFinish(inputStream);
            } catch (DbxException | IOException e){
                mException = e;
            }
        }
        return null;
    }
}

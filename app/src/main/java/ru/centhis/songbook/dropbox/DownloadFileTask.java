package ru.centhis.songbook.dropbox;


import android.content.Context;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import ru.centhis.songbook.activities.MainActivity;

public class DownloadFileTask extends AsyncTask<FileMetadata, File, File> {
    private final Context mContext;
    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;

    public interface Callback {
        void onDownloadComplete(File result);
        void onError(Exception e);
    }

    public DownloadFileTask(Context context, DbxClientV2 dbxClient, Callback callback){
        mContext = context;
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(File file) {
        super.onPostExecute(file);
        if (mException != null){
            mCallback.onError(mException);
        } else
            mCallback.onDownloadComplete(file);
    }

    @Override
    protected File doInBackground(FileMetadata... fileMetadata) {
        FileMetadata metadata = fileMetadata[0];
        try {
//            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File path = new File(MainActivity.getFileDir() + metadata.getPathDisplay()).getParentFile();
            File file = new File(path, metadata.getName());

            if (!path.exists()){
                if (!path.mkdirs()){
                    mException = new RuntimeException("Unable to create directory: " + path);
                }
            } else if (!path.isDirectory()){
                mException = new IllegalStateException("Download path is not a directory: " + path);
                return null;
            }

            if (file.exists())
                file.delete();

            try (OutputStream outputStream = new FileOutputStream(file)){
//                mDbxClient.files().download(metadata.getPathLower(), metadata.getRev()).download(outputStream);

                mDbxClient.files().downloadBuilder(metadata.getPathLower()).start().download(outputStream);
            }

//            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//            intent.setData(Uri.fromFile(file));
//            mContext.sendBroadcast(intent);

            return file;
        } catch (DbxException | IOException e){
            mException = e;
        }

        return null;
    }
}

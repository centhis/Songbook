package ru.centhis.songbook.parsing;

import android.os.AsyncTask;

import org.apache.commons.io.FilenameUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.file.Files;

import ru.centhis.songbook.activities.MainActivity;
import ru.centhis.songbook.activities.SettingsActivity;

public class ChordDownloadTask extends AsyncTask<String, Void, File> {

    private final String mUrl;
    private final Callback mCallback;
    private Exception mException;

    public interface Callback {
        void onDataLoaded(File result);
        void onError(Exception e);
    }

    public ChordDownloadTask(String url, Callback callback){
        mUrl = url;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(File result){
        super.onPostExecute(result);

        if (mException != null)
            mCallback.onError(mException);
        else
            mCallback.onDataLoaded(result);
    }

    @Override
    protected File doInBackground(String... strings) {
        if (!MainActivity.getChordsDir().exists())
            MainActivity.getChordsDir().mkdir();
        String fileName = MainActivity.getChordsDir() + "/" + FilenameUtils.getName(strings[0]);
        File file = new File(fileName);
        if (!file.exists()) {
            try (BufferedInputStream in = new BufferedInputStream(new URL(strings[0]).openStream());
                 FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
                byte[] dataBuffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1){
                    fileOutputStream.write(dataBuffer, 0 , bytesRead);
                }
                return file;
            } catch (Exception e) {
                mException = e;
            }
        }
        return null;
    }
}

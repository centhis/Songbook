package ru.centhis.songbook.dropbox;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.core.v2.files.DeleteResult;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.fasterxml.jackson.core.JsonParser;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import ru.centhis.songbook.R;
import ru.centhis.songbook.activities.SettingsActivity;
import ru.centhis.songbook.data.SettingsContract;

public final class DbxUtil {

    private static final String TAG = SettingsActivity.class.getName();


    public static int threadCounter;



    private static List<String> folderPaths = new ArrayList<>();
    private static List<String> filesPath = new ArrayList<>();
    private static List<File> localFiles = new ArrayList<>();

    public static void dbxSyncFiles(Context context, String filesDir, SharedPreferences prefs){
        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage(context.getString(R.string.loading));
        dialog.show();
        threadCounter = 1;
        Timer timer1 = new Timer();
        deleteDBXFiles(context, prefs);
        timer1.schedule(new TimerTask() {
            @Override
            public void run() {
                if (threadCounter == 1) {
//                    listFolders("", context, filesDir + "/songs");
                    listFolders("", context, filesDir);
                    timer1.cancel();
                }
            }
        }, 0, 500);
        Timer timer2 = new Timer();
        timer2.schedule(new TimerTask() {
            @Override
            public void run() {
                if (threadCounter == 0){
                    dialog.dismiss();
                    timer2.cancel();
                }
            }
        },0 , 500);

    }

    private static void listFolders(String path, Context context, String filesDir){
        listLocalFiles(filesDir, localFiles);
        new ListFolderTask(DropboxClientFactory.getClient(), new ListFolderTask.Callback() {
            @Override
            public void onDataLoaded(ListFolderResult result) {
                for (Metadata entry : result.getEntries()){
                    try {
                        JSONObject jsonObject = new JSONObject(entry.toString());
                        if (jsonObject.getString(".tag").equals("folder")){
                            folderPaths.add(entry.getPathDisplay());
                        } else if (jsonObject.getString(".tag").equals("file")){
                            filesPath.add(entry.getPathDisplay());
                            FileMetadata fileMetadata = (FileMetadata) entry;
                            syncFile(filesDir, context, fileMetadata);
                        }

                    } catch (JSONException e){
                        Log.e(TAG, "Something wrong with JSON answer.", e);
                    }
                }
                for (File localFile:localFiles){
                    String remoteFilePath = localFile.getAbsolutePath().substring(filesDir.length());
                    boolean isFileExistOnDBX = false;
                    for (Metadata entry : result.getEntries()){
                        if (entry.getPathDisplay().equals(remoteFilePath)) {
                            isFileExistOnDBX = true;
                            break;
                        }
                    }
                    if (!isFileExistOnDBX)
                        uploadFile(context, localFile.getAbsolutePath(), remoteFilePath);
                }
                threadCounter--;
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to list folder", e);
                threadCounter--;
                Toast.makeText(context, "An error has occured", Toast.LENGTH_SHORT).show();
            }
        }).execute(path);


    }

    private static void syncFile(String storagePath, Context context, FileMetadata fileMetadata){
        String filePath = storagePath + fileMetadata.getPathDisplay();
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                downloadFile(context, fileMetadata);
                return;
            }
            if (file.length() != fileMetadata.getSize()){
                int compareValue = Long.compare(file.lastModified(), fileMetadata.getServerModified().getTime());
                if (compareValue < 0){
                    downloadFile(context, fileMetadata);
                } else if (compareValue > 0){
                    uploadFile(context, filePath, fileMetadata.getPathLower());
                }
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void downloadFile(Context context, FileMetadata fileMetadata){
        threadCounter++;
        new DownloadFileTask(context, DropboxClientFactory.getClient(), new DownloadFileTask.Callback() {
            @Override
            public void onDownloadComplete(File result) {
                threadCounter--;
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to download file", e);
                threadCounter--;
                Toast.makeText(context, "An error has occured", Toast.LENGTH_LONG).show();
            }
        }).execute(fileMetadata);
    }

    private static void uploadFile(Context context, String fileUri, String remotePath){
        threadCounter++;
        new UploadFileTask(context, DropboxClientFactory.getClient(), new UploadFileTask.Callback() {
            @Override
            public void onUploadComplete(FileMetadata result) {
                threadCounter--;
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to upload file", e);
                threadCounter--;
                Toast.makeText(context, "An error has occured", Toast.LENGTH_LONG).show();
            }
        }).execute(fileUri, remotePath);
    }

    private static void listLocalFiles(String localPath, List<File> files){
        File directory = new File(localPath);
        File[] fList = directory.listFiles();
        if (fList != null){
            for (File file:fList){
//                if (file.isFile() && FilenameUtils.getExtension(file.getName()).equals("json"))
                  if (file.isFile())
                    files.add(file);
                else if (file.isDirectory()){
                    listLocalFiles(file.getAbsolutePath(), files);
                }
            }
        }
    }

    private static void deleteDBXFiles(Context context, SharedPreferences prefs){
        String songsToDelete = prefs.getString(SettingsContract.SONGS_TO_DELETE, "");
        if (!songsToDelete.equals("")){
            List<String> songs = Arrays.asList(songsToDelete.split(";"));
            List<String> songsToPrefs = new ArrayList<>();
            for (String song:songs)
                songsToPrefs.add(song);
            for (String song:songs){
                threadCounter++;
                String songToDelDBX = "songs/" + song;
                new DeleteFileTask(context, DropboxClientFactory.getClient(), new DeleteFileTask.Callback(){

                    @Override
                    public void onDeleteComplete(DeleteResult result) {
                        threadCounter--;
                        for (int i = 0; i < songsToPrefs.size(); i++){
                            if (songsToPrefs.get(i).equals(song)) {
                                songsToPrefs.remove(i);
                                break;
                            }
                        }
                        String prefsString = String.join(";", songsToPrefs);
                        prefs.edit().putString(SettingsContract.SONGS_TO_DELETE, prefsString).apply();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "onError: Failed to delete file", e);
                        threadCounter--;
                    }
                }).execute(songToDelDBX);
            }
        }
    }
}

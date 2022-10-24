package ru.centhis.songbook.dropbox;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.fasterxml.jackson.core.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import ru.centhis.songbook.activities.SettingsActivity;

public final class DbxUtil {

    private static final String TAG = SettingsActivity.class.getName();


    public static int threadCounter;



    private static List<String> folderPaths = new ArrayList<>();
    private static List<String> filesPath = new ArrayList<>();

    public static void dbxSyncFiles(Context context, String filesDir){
        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Loading");
        dialog.show();
        threadCounter = 1;
        listFolders("", context, filesDir + "/songs");

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (threadCounter == 0){
                    dialog.dismiss();
                    timer.cancel();
                }
            }
        },0 , 500);

    }

    private static void listFolders(String path, Context context, String filesDir){
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
                if (compareValue > 0){
                    downloadFile(context, fileMetadata);
                } else if (compareValue < 0){
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
}

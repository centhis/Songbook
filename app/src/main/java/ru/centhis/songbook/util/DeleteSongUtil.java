package ru.centhis.songbook.util;

import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import ru.centhis.songbook.data.SettingsContract;


public class DeleteSongUtil {

    public static boolean delete(String path, String songsDataFolder, SharedPreferences prefs) throws IOException {
        String songsToDelete = prefs.getString(SettingsContract.SONGS_TO_DELETE, "");
        if (!songsToDelete.equals(""))
            songsToDelete = songsToDelete + ";";
        String DBPath = path.substring(songsDataFolder.length() + 1);
        songsToDelete = songsToDelete + DBPath;
        prefs.edit().putString(SettingsContract.SONGS_TO_DELETE, songsToDelete).apply();
        File songFolder = new File(path);
        FileUtils.deleteDirectory(songFolder);
        File artistFolder = songFolder.getParentFile();
        if (artistFolder.listFiles().length == 0){
            FileUtils.deleteDirectory(artistFolder);
            return true;
        } else
            return false;
    }

    public static void deleteCache(String path) throws IOException{
        File cacheFolder = new File(path);
        if (cacheFolder.exists()){
            FileUtils.cleanDirectory(cacheFolder);
        }
    }
}

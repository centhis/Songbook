package ru.centhis.songbook.data;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import ru.centhis.songbook.activities.TextSongActivity;

public class Song {

    private static final String TAG = TextSongActivity.class.getName();

    String[] songText;

    public Song(String path) {
        try (FileReader fileReader = new FileReader(path);
        BufferedReader bufferedReader = new BufferedReader(fileReader)){
            List<String> lines = new ArrayList<>();
            String line = null;
            while ((line = bufferedReader.readLine()) != null){
                lines.add(line);
            }
            songText = lines.toArray(new String[lines.size()]);
        }catch ( Exception e){
            Log.e(TAG, "Song: Can't read file", e);
        }
    }

    public String[] getSongText() {
        return songText;
    }

    public void saveToFile(String[] lines, String path){
        try (FileWriter fileWriter = new FileWriter(path);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)){
            for (String line:lines){
                bufferedWriter.write(line);
                bufferedWriter.newLine();
            }
            bufferedWriter.flush();
        } catch (Exception e){
            Log.e(TAG, "saveToFile: ", e);
        }
    }
}


package ru.centhis.songbook.data;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.centhis.songbook.activities.TextSongActivity;
import ru.centhis.songbook.util.TranspondUtil;

public class Song {

    private static final String TAG = TextSongActivity.class.getName();

    private String[] songText;
    private final List<String> chords;



    public Song(String path) {
        try (FileReader fileReader = new FileReader(path);
        BufferedReader bufferedReader = new BufferedReader(fileReader)){
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = bufferedReader.readLine()) != null){
                lines.add(line);
            }
            songText = lines.toArray(new String[lines.size()]);
        }catch ( Exception e){
            Log.e(TAG, "Song: Can't read file", e);
        }
        chords = new ArrayList<>();
        for (String line:songText){
            if (isChordLine(line)){
                line = line.replaceAll("\\p{C}", "");
                line = line.trim();
                String[] words = line.split(" +");
                for (String word:words){
                    word = word.trim();
                    if (!chords.contains(word))
                        chords.add(word);
                }
            }
        }
    }

    public String[] getSongText() {
        return songText;
    }

    public List<String> getChords(int transpositionCount){
        List<String> result;
        if (transpositionCount != 0){
            result = new ArrayList<>();
            for (String chord:chords){
                for (int i = 0; i < Math.abs(transpositionCount); i++){
                    if (transpositionCount > 0)
                        chord = increaseChordTone(chord);
                    else if (transpositionCount < 0)
                        chord = decreaseChordTone(chord);
                }
                result.add(chord);
            }
        }
        else {
            result = new ArrayList<>(chords);
        }
        return result;
    }

    public String[][] getSongTextWithBreaks(int length, int transpositionCount){
        List<String> result = new ArrayList<>();
        List<String> description = new ArrayList<>();
        for (int i = 0; i < songText.length; i++){
            if (!isTabLine(songText[i]) && isChordLine(songText[i])){
                if (length + 1 < songText[i + 1].length()) {
                    String[] lineWithChordsBreaks = lineWithChordsBreaks(songText[i], songText[i + 1], length);
                    for (String line : lineWithChordsBreaks) {
                        if (!isTabLine(line) && isChordLine(line)) {
                            if (transpositionCount != 0)
                                line = transposition(line, transpositionCount);
                            result.add(line);
                            description.add("chord");
                        } else {
                            result.add(line);
                            description.add("text");
                        }
                    }
                    i++;
                } else {
                    String resultLine = songText[i];
                    if (!isTabLine(songText[i]) && isChordLine(songText[i])) {
                        if (transpositionCount != 0)
                            resultLine = transposition(resultLine, transpositionCount);
                        result.add(resultLine);
                        description.add("chord");
                    } else {
                        result.add(songText[i]);
                        description.add("text");
                    }
                }

            }
            else {
                if (length + 1 < songText[i].length()){
                    String[] lineBreaks = lineBreaks(songText[i], length);
                    for (String line:lineBreaks){
                        String resultLine = line;
                        if (!isTabLine(line) && isChordLine(line)){
                            if (transpositionCount != 0)
                                resultLine = transposition(resultLine, transpositionCount);
                            result.add(resultLine);
                            description.add("chord");
                        }
                        else {
                            result.add(line);
                            description.add("text");
                        }
                    }
                }
                else {
                    String resultLine = songText[i];
                    if (!isTabLine(resultLine) && isChordLine(resultLine)){
                        if (transpositionCount != 0)
                            resultLine = transposition(resultLine, transpositionCount);
                        result.add(resultLine);
                        description.add("chord");
                    }
                    else {
                        result.add(resultLine);
                        description.add("text");
                    }
                }
            }
        }
        String[][] returnResult = new String[result.size()][2];
        for (int i = 0; i < result.size(); i++){
            returnResult[i][0] = result.get(i);
            returnResult[i][1] = description.get(i);
        }
        return returnResult;
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

    private boolean isChordLine (String line){
        line = line.replaceAll("\\p{C}", "");
        line = line.trim();
        String[] words = line.split(" +");
        int count = 0;
        Pattern pattern = Pattern.compile("(\\s*?" +
                SettingsContract.TONES +
                SettingsContract.HALF_TONES +
                SettingsContract.MINOR +
                SettingsContract.ADDED +
                SettingsContract.SUS +
                SettingsContract.ADD +
                SettingsContract.ENDNOTE +
                ")");
        Matcher matcher = pattern.matcher(line);
        while (matcher.find())
            count++;
        return count == words.length;
    }

    private boolean isTabLine(String line){
        Pattern pattern = Pattern.compile("([\\-\\d|]{10,})");
        Matcher matcher = pattern.matcher(line);
        return matcher.find();
    }

    private String[] lineBreaks(String line, int length){
        if (isTabLine(line))
            return new String[]{line};
        String lineToBreacks = line;
        int lineLength = line.length();
        List<String> result = new ArrayList<>();
        while (length < lineLength){
            for (int i = length; i > 1; i--){
                if (lineToBreacks.charAt(i) == ' '){
                    result.add(lineToBreacks.substring(0, i));
                    lineToBreacks = lineToBreacks.substring(i);
                    lineLength = lineToBreacks.length();
                    break;
                }
            }
        }
        if (lineToBreacks.length() > 0)
            result.add(lineToBreacks);
        return result.toArray(new String[result.size()]);
    }

    private String[] lineWithChordsBreaks(String lineChords, String lineText, int length){
        String textLineToBreaks = lineText;
        String chordLineToBreaks = lineChords;
        int lineLength = textLineToBreaks.length();
        List<String> result = new ArrayList<>();
        while (length < lineLength){
            for (int i = length; i > 1; i--){
                if (textLineToBreaks.charAt(i) == ' '){
                    //todo: добавить сдвиг влево, если последний символ не равен ' '
                    if (chordLineToBreaks.length() <= i) {
                        result.add(chordLineToBreaks);
                        chordLineToBreaks = "";
                    }
                    else {
                        result.add(chordLineToBreaks.substring(0, i));
                        chordLineToBreaks = chordLineToBreaks.substring(i);
                    }
                    result.add(textLineToBreaks.substring(0, i));
                    textLineToBreaks = textLineToBreaks.substring(i);
                    lineLength = textLineToBreaks.length();
                    break;
                }
            }
        }
        if (chordLineToBreaks.length() > 0)
            result.add(chordLineToBreaks);
        if (textLineToBreaks.length() > 0)
            result.add(textLineToBreaks);
        return result.toArray(new String[result.size()]);
    }

    private String transposition(String line, int transpositionCount){
        line = line.replaceAll("\\p{C}", "");
        Pattern pattern = Pattern.compile("(\\s*?" + SettingsContract.TONES +
                SettingsContract.HALF_TONES +
                SettingsContract.MINOR +
                SettingsContract.ADDED +
                SettingsContract.SUS +
                SettingsContract.ADD +
                SettingsContract.ENDNOTE +
                ")");
        Matcher matcher = pattern.matcher(line);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()){
            String chordWidthSpaces = line.substring(matcher.start(), matcher.end());
            String originalChord = chordWidthSpaces.trim();
            String chord = originalChord;
            for (int i = 0; i < Math.abs(transpositionCount); i++){
                if (transpositionCount > 0)
                    chord = increaseChordTone(chord);
                else if (transpositionCount < 0)
                    chord = decreaseChordTone(chord);
            }
            chordWidthSpaces = chordWidthSpaces.replaceAll(originalChord, chord);
            sb.append(chordWidthSpaces);
        }
        Log.d(TAG, "transposition: " + sb);
        return sb.toString();
    }

    private String increaseChordTone (String chord){
        Pattern pattern = Pattern.compile(SettingsContract.TONES + SettingsContract.HALF_TONES);
        Matcher matcher = pattern.matcher(chord);
        while (matcher.find()){
            String tone = chord.substring(matcher.start(), matcher.end());
            String resultTone = TranspondUtil.increaseTone(tone);
            chord = chord.replaceAll(tone, resultTone);
        }
        return chord;
    }

    private String decreaseChordTone (String chord){
        Pattern pattern = Pattern.compile(SettingsContract.TONES + SettingsContract.HALF_TONES);
        Matcher matcher = pattern.matcher(chord);
        while (matcher.find()){
            String tone = chord.substring(matcher.start(), matcher.end());
            String resultTone = TranspondUtil.decreaseTone(tone);
            chord = chord.replaceAll(tone, resultTone);
        }
        return chord;
    }
}


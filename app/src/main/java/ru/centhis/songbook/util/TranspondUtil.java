package ru.centhis.songbook.util;

public class TranspondUtil {

    public static String increaseTone(String tone){
        String resultTone = "";
        switch (tone){
            case "A#":
                resultTone = "B";
                break;
            case "B#":
                resultTone = "C#";
                break;
            case "C#":
                resultTone = "D";
                break;
            case "D#":
                resultTone = "E";
                break;
            case "E#":
                resultTone = "F#";
                break;
            case "F#":
                resultTone = "G";
                break;
            case "G#":
                resultTone = "A";
                break;
            case "H#":
                resultTone = "C#";
                break;
            case "Ab":
                resultTone = "A";
                break;
            case "Bb":
                resultTone = "B";
                break;
            case "Cb":
                resultTone = "C";
                break;
            case "Db":
                resultTone = "D";
                break;
            case "Eb":
                resultTone = "E";
                break;
            case "Fb":
                resultTone = "F";
                break;
            case "Gb":
                resultTone = "G";
                break;
            case "Hb":
                resultTone = "H";
                break;
            case "A":
                resultTone = "A#";
                break;
            case "B":
                resultTone = "C";
                break;
            case "C":
                resultTone = "C#";
                break;
            case "D":
                resultTone = "D#";
                break;
            case "E":
                resultTone = "F";
                break;
            case "F":
                resultTone = "G#";
                break;
            case "G":
                resultTone = "G#";
                break;
            case "H":
                resultTone = "C";
                break;
        }
        return resultTone;
    }

    public static String decreaseTone(String tone){
        String resultTone = "";
        switch (tone){
            case "A#":
                resultTone = "A";
                break;
            case "B#":
                resultTone = "B";
                break;
            case "C#":
                resultTone = "C";
                break;
            case "D#":
                resultTone = "D";
                break;
            case "E#":
                resultTone = "E";
                break;
            case "F#":
                resultTone = "F";
                break;
            case "G#":
                resultTone = "G";
                break;
            case "H#":
                resultTone = "H";
                break;
            case "Ab":
                resultTone = "G";
                break;
            case "Bb":
                resultTone = "A";
                break;
            case "Cb":
                resultTone = "A#";
                break;
            case "Db":
                resultTone = "C";
                break;
            case "Eb":
                resultTone = "D";
                break;
            case "Fb":
                resultTone = "D#";
                break;
            case "Gb":
                resultTone = "F";
                break;
            case "Hb":
                resultTone = "A";
                break;
            case "A":
                resultTone = "G#";
                break;
            case "B":
                resultTone = "A#";
                break;
            case "C":
                resultTone = "B";
                break;
            case "D":
                resultTone = "C#";
                break;
            case "E":
                resultTone = "D#";
                break;
            case "F":
                resultTone = "E";
                break;
            case "G":
                resultTone = "F#";
                break;
            case "H":
                resultTone = "A#";
                break;
        }
        return resultTone;
    }
}

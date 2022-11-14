package ru.centhis.songbook.data;

public class Chord {
    String name;

    public Chord(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String increaseTone(int steps){
        for (int i = 0; i < steps; i++){
            if (name.matches("C#"))
                name = name.replaceAll("C#", "D");
            else if (name.matches("D#"))
                name = name.replaceAll("D#", "E");
            else if (name.matches("E#"))
                name = name.replaceAll("E#", "F#");
            else if (name.matches("F#"))
                name = name.replaceAll("F#", "G");
            else if (name.matches("G#"))
                name = name.replaceAll("G#", "A");
        }
        return name;
    }
}

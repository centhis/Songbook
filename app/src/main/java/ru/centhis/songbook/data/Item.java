package ru.centhis.songbook.data;

import java.io.Serializable;

public class Item implements Comparable<Item>, Serializable {
    private String name;
    private String source;
    private String type;
    private boolean isMp3;
    private boolean isGuitar;
    private boolean isUkulele;

    public Item(String name, String source, String type) {
        this.name = name;
        this.source = source;
        this.type = type;
    }

    public Item() {
    }

    public String getName() {
        return name;
    }

    public String getSource() {
        return source;
    }

    public String getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isMp3() {
        return isMp3;
    }

    public void setMp3(boolean mp3) {
        isMp3 = mp3;
    }

    public boolean isGuitar() {
        return isGuitar;
    }

    public void setGuitar(boolean guitar) {
        isGuitar = guitar;
    }

    public boolean isUkulele() {
        return isUkulele;
    }

    public void setUkulele(boolean ukulele) {
        isUkulele = ukulele;
    }

    @Override
    public int compareTo(Item item) {
        return name.compareToIgnoreCase(item.getName());
    }
}

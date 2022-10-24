package ru.centhis.songbook.data;

import java.io.Serializable;

public class Item implements Comparable<Item>, Serializable {
    private String name;
    private String source;
    private String type;

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

    @Override
    public int compareTo(Item item) {
        return name.compareToIgnoreCase(item.getName());
    }
}

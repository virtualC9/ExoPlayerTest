package com.z.exoplayertest.database;

public class Channel {
    private int id;
    private String name;
    private String url;
    private int isLike;

    public Channel(int id, String name, String url, int isLike) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.isLike = isLike;
    }

    public Channel(String name, String url, int isLike) {
        this.name = name;
        this.url = url;
        this.isLike = isLike;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getIsLike() {
        return isLike;
    }

    public void setIsLike(int isLike) {
        this.isLike = isLike;
    }
}

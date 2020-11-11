package com.example.player3;

public class Music {
    private String name;
    private String path;

    public Music(String name, String path){
        this.name = name;
        //this.singer = singer;
        this.path = path;
    }
    public void setNameM(String nameM) {
        this.name = nameM;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getNameM() {
        return name;
    }

    public String getPath() {
        return path;
    }

}

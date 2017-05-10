package com.example.lg.work9;

/**
 * Created by LG on 2017-05-04.
 */

public class Url {
    String name;
    String url;
    public Url(String name, String url){
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "<"+this.name+"> "+this.url;
    }
}

package com.udacity.popular_movies;

public class Config {

    private Config(){}

    private static final String resolution = "w185/";
    private static final String highResolution = "w500/";
    private static final String language = "en";

    public static String getLanguage() {
        return language;
    }

    public static String getResolution() {
        return resolution;
    }

    public static String getHighResolution() {
        return highResolution;
    }

}

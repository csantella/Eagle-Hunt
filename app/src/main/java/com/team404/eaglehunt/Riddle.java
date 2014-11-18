package com.team404.eaglehunt;

/**
 * Created by Chris on 11/17/2014.
 *
 * Handles the INSTANCE of a riddle
 */
public class Riddle {

    protected String content;
    protected int difficulty;
    protected final int id = RiddleFactory.getNewID();
    protected double latitude = 0;
    protected double longitude = 0;


    public Riddle(String c, int d, double la, double lo)
    {
        this.content = c;
        this.difficulty = d;
        this.latitude = la;
        this.longitude = lo;
    }

    public String getContent()
    {
        return this.content;
    }

    public int getDifficulty()
    {
        return this.difficulty;
    }

}

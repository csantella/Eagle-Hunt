package com.team404.eaglehunt;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chris on 11/17/2014.
 *
 * Riddle Factory stores/manages all riddles
 */
public class RiddleFactory {

    public static ArrayList<Riddle> riddlesList = new ArrayList<Riddle>();
    protected static int maxID = 0;

    public void riddleFactory()
    {

    }

    public static List<Riddle> getRiddles()
    {
        ArrayList<Riddle> riddles = new ArrayList<Riddle>();
        return riddles;
    }

    public static int getNewID()
    {
        maxID = maxID + 1;
        return maxID;
    }

    public static void addRiddle(String context, int difficulty, double la, double lo)
    {
        Riddle newRiddle = new Riddle(context, difficulty, la, lo);
        riddlesList.add(newRiddle);
    }

    public static void addRiddle(Riddle riddle)
    {
        riddlesList.add(riddle);
    }

}

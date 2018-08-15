package com.tonsaker.syph;

import java.util.Calendar;

/**
 * Created by Markus Tonsaker on 2018-01-16.
 */
public abstract class Console {


    public static void println(String string){
        String formattedString = format(string);
        System.out.println(formattedString);
        //TODO add file log
    }

    public static void printError(String string){
        String formattedString = format(string);
        System.err.println(formattedString);
        //TODO add file log
    }

    private static String format(String string){
        Calendar calendar = Calendar.getInstance();
        return String.format("[%02d-%02d-%02d %02d:%02d:%02d] ", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND)).concat(string);
    }

}

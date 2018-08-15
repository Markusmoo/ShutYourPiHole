package com.tonsaker.syph;

import spark.Request;

import java.io.UnsupportedEncodingException;

/**
 * Created by Markus Tonsaker on 2018-01-05.
 */
public class SMSBody {

    private String rawDataList;

    public String TO_COUNTRY;
    public String TO_STATE;
    public String SMS_MESSAGE_SID;
    public String NUM_MEDIA;
    public String TO_CITY;
    public String FROM_ZIP;
    public String SMS_SID;
    public String FROM_STATE;
    public String SMS_STATUS;
    public String FROM_CITY;
    public String BODY;
    public String FROM_COUNTRY;
    public String TO;
    public String TO_ZIP;
    public String NUM_SEGMENTS;
    public String MESSAGE_SID;
    public String ACCOUNT_SID;
    public String FROM;
    public String API_VERSION;

    public SMSBody(Request body) throws IndexOutOfBoundsException{

        rawDataList = body.body();
        String[] b = rawDataList.split("&");

        TO_COUNTRY      = parse(b[0]);
        TO_STATE        = parse(b[1]);
        SMS_MESSAGE_SID = parse(b[2]);
        NUM_MEDIA       = parse(b[3]);
        TO_CITY         = parse(b[4]);
        FROM_ZIP        = parse(b[5]);
        SMS_SID         = parse(b[6]);
        FROM_STATE      = parse(b[7]);
        SMS_STATUS      = parse(b[8]);
        FROM_CITY       = parse(b[9]);
        BODY            = parse(b[10]);
        FROM_COUNTRY    = parse(b[11]);
        TO              = parse(b[12]);
        TO_ZIP          = parse(b[13]);
        NUM_SEGMENTS    = parse(b[14]);
        MESSAGE_SID     = parse(b[15]);
        ACCOUNT_SID     = parse(b[16]);
        FROM            = parse(b[17]);
        API_VERSION     = parse(b[18]);
    }

    /**
     *
     * @param text string to be processed
     * @return processed String
     */
    protected String parse(String text){
        return parse(text, true);
    }

    protected String parse(String text, boolean format){
        try {
            if(format){
                return java.net.URLDecoder.decode(text.substring(text.indexOf('=') + 1), "UTF-8");
            }else{
                return text.substring(text.indexOf('=') + 1);
            }
        }catch(UnsupportedEncodingException e){
            e.printStackTrace();
            return "ERROR";
        }
    }

    public String[] toArray(){
        String[] arr = {TO_COUNTRY, TO_STATE, SMS_MESSAGE_SID, NUM_MEDIA, TO_CITY, FROM_ZIP, SMS_SID, FROM_STATE,
                SMS_STATUS, FROM_CITY, BODY, FROM_COUNTRY, TO, TO_ZIP, NUM_SEGMENTS, MESSAGE_SID, ACCOUNT_SID,
                FROM, API_VERSION};
        return arr;
    }

    public String rawListData(){
        return rawDataList;
    }
}

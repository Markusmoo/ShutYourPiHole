package com.tonsaker.syph.test;

import com.tonsaker.syph.SMSBody;
import com.twilio.Twilio;
import com.twilio.twiml.Body;
import com.twilio.twiml.Message;
import com.twilio.twiml.MessagingResponse;
import com.twilio.type.PhoneNumber;
import spark.Request;

import static spark.Spark.post;


/**
 * Created by Marku on 2018-01-15.
 */
public class TwilioTest {

    private static String accountSID;
    private static String apiKey;
    private static String apiSecret;
    private static String outboundNumber;
    private static String masterNumber;

    public static void main(String[] args){
        accountSID      = args[0];
        apiKey          = args[1];
        apiSecret       = args[2];
        outboundNumber  = args[3];
        masterNumber    = args[4];
        //debug = args[5].equals("debug");

        System.out.println("*** Initializing.. *******************************");
        System.out.println("VERSION 1.0");
        System.out.println("SuperAdmin number = "+masterNumber);
        System.out.println("Outbound number = "+outboundNumber);
        System.out.println("Account SID = [HIDDEN]");
        System.out.println("API Key     = [HIDDEN]");
        System.out.println("API Secret  = [HIDDEN]");
        System.out.println("**************************************************\n\n");

        new TwilioTest();
    }

    public TwilioTest(){
        Twilio.init(apiKey, apiSecret, accountSID);
        post("/receive-sms", (req, res) -> parseSMSBody(req).toXml());
    }

    public MessagingResponse parseSMSBody(Request r){
        SMSBody smsBody = new SMSBody(r);
        Message m = new com.twilio.twiml.Message.Builder().body(new Body(smsBody.BODY)).build();
        MessagingResponse res = new MessagingResponse.Builder().message(m).build();

        System.out.println("123");

        try {
            com.twilio.rest.api.v2010.account.Message.creator(new PhoneNumber("ENTER NUMBER HERE"), new PhoneNumber(outboundNumber), "This is a test").create();
        }catch (Exception e){
            e.printStackTrace();
        }

        System.out.println("test");

        return res;
    }
}

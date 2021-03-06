package com.tonsaker.syph;

import static spark.Spark.post;

import com.twilio.Twilio;
import com.twilio.exception.TwilioException;
import com.twilio.twiml.Body;
import com.twilio.twiml.Message;
import com.twilio.twiml.MessagingResponse;
import com.twilio.type.PhoneNumber;
import spark.Request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Markus Tonsaker on 2018-01-05.
 *
 */
public class Main{

    //Change every roll-out of new version! Informs all users.
    private static final String VERSION = "1.1";
    private static final String CHANGELOG = "ShutYourPiHole has updated to Version 1.1\n\n" +
                                            "Changelog:\n" +
                                            "Added new command GD mute.\n" +
                                            "Added new command GD warnings off/on.\n" +
                                            "Added time and who info to \"GD status\" command.\n" +
                                            "Added this changelog for new versions";

    static boolean debug;

    private static String accountSID;
    private static String apiKey;
    private static String apiSecret;
    private static String outboundNumber;
    private static String masterNumber;

    static CommandHandler commandHandler;

    static ArrayList<User> users = new ArrayList<>();

    /**
     *
     * @param args runtime parameters in such order: [accountSID, apiKey, apiSecret, outboundNumber, masterNumber]
     */
    public static void main(String[] args){

        accountSID      = args[0];
        apiKey          = args[1];
        apiSecret       = args[2];
        outboundNumber  = args[3];
        masterNumber    = args[4];
        //if(args[5] != null) debug = args[5].equals("debug"); else debug = false;

        System.out.println("*** Initializing.. *******************************");
        System.out.println("VERSION "+VERSION);
        System.out.println("SuperAdmin number = "+masterNumber);
        System.out.println("Outbound number = "+outboundNumber);
        System.out.println("Account SID = [HIDDEN]");
        System.out.println("API Key     = [HIDDEN]");
        System.out.println("API Secret  = [HIDDEN]");
        System.out.println("**************************************************\n\n");

        new Main();
        commandHandler = new CommandHandler();
        sendSMS(masterNumber, "ShutYourPiHole has started successfully!");

        //Update user's version
        for(User u : users){
            if(u.getLastUsedVersion() == null || !u.getLastUsedVersion().equals(VERSION)){
                sendSMS(u.getNumber(), CHANGELOG);
                u.setLastUsedVersion(VERSION);
                try{
                    u.save();
                }catch (Exception e){
                    e.printStackTrace();
                    Console.printError("ERROR: Could not update user version!!!");
                }
            }
        }
    }

    public Main(){
        Twilio.init(Main.apiKey, Main.apiSecret, Main.accountSID);
        users = User.loadUsers();
        boolean containsMaster = false;
        for(User u : users) if(u.equalsNumber(masterNumber)) containsMaster = true;
        if(!containsMaster){
            try {
                User masterUser = new User(masterNumber, true);
                User.addUser(masterUser);
                users.add(masterUser);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        post("/receive-sms", (req, res) -> parseSMSBody(req).toXml());
    }

    public MessagingResponse parseSMSBody(Request body){
        SMSBody smsBody = new SMSBody(body);
        String res = smsBody.BODY.toLowerCase();
        String say = "ERROR: Something went wrong";
        Console.println("Received message from "+smsBody.FROM+":\n"+smsBody.BODY);


        boolean foundUser = false;
        for(User u : users){
            if(u.equalsNumber(smsBody.FROM)){
                say = u.process(res);
                foundUser = true;
                System.out.println("** Our Response: **\n"+ say +"\n***************");
                break;
            }
        }
        if(!foundUser){
            Console.printError("!!!WARNING!!! Message received from NON-USER:");
            Console.printError(smsBody.rawListData() + "\n");
            Console.printError("Known users:");
            Iterator<User> iterator = users.iterator();
            while(iterator.hasNext()){
                Console.printError("User: " + iterator.next().getNumber());
            }
            Console.printError("**********************************************");
        }
        Message sms = new Message.Builder().body(new Body(say)).build();
        return new MessagingResponse.Builder().message(sms).build();
    }

    public static void sendSMS(String number, String body){
        try {
            Console.println("Sending message to "+number+":\n"+body+"\n***END***");
            com.twilio.rest.api.v2010.account.Message.creator(new PhoneNumber(number), new PhoneNumber(Main.outboundNumber), body).create();
        }catch (TwilioException e){
            e.printStackTrace();
        }
    }

    public static void sendSMSAll(String body){
        Main.sendSMSAll(body, true);
    }

    public static void sendSMSAll(String body, boolean ignoreMute){
        users.forEach((temp) -> {
            if(ignoreMute || !temp.isMute()) sendSMS(temp.getNumber(), body);
        });
    }
}

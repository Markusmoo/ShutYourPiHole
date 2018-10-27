package com.tonsaker.syph;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Markus Tonsaker on 2018-01-05.
 */
public class User {

    public static final String usersSavePath = "/home/pi/ShutYourPiHole/users/";

    @Expose private String lastUsedVersion = "";

    @Expose private int numberOpens;
    @Expose private int numberCloses;
    @Expose private int numberToggles;
    @Expose private int numberStatus;
    @Expose private int numberMutes;
    @Expose private int numberWarningsChanges;
    @Expose private int numberAdminsUsages;

    @Expose private boolean isMute;

    @Expose private boolean isAdmin;
    @Expose private String number = "";

    public User(String number, boolean isAdmin){
        this.number = number;
        this.isAdmin = isAdmin;
    }

    public boolean equalsNumber(String number){
        long n1 = Long.parseLong(number.substring(1).trim());
        long n2 = Long.parseLong(getNumber().substring(1).trim());
        return n1==n2;
    }

    public String process(String msg){
        String msgBack = "Sorry I don't understand.\n\n" +
                "Type \"commands\" for a list of commands";

        if(msg.equals("commands")){
            msgBack = "List of commands are:\n" +
                    "\"gd open\" - Opens garage\n" +
                    "\"gd close\" - Closes garage\n" +
                    "\"gd toggle\" - Toggles garage position\n" +
                    "\"gd status\" - Returns position of garage\n" +
                    "\"gd mute\" - Mutes GD Open Warnings Until garage is closed\n" +
                    "\"gd warnings on/off\" - Turns on/off gd warnings for yourself";

            if(isAdmin()){
                    msgBack += "\n\"admin add +1##########\"\n" +
                            "\"admin remove +1##########\"";
            }
        }else if(msg.equals("gd open") || msg.equals("open garage door")){
            msgBack = Main.commandHandler.openGarage(this) ? "Opening garage door.." : "Garage door ALREADY is open..";
            numberOpens++;

        }else if(msg.equals("gd close") || msg.equals("close garage door")){
            msgBack = Main.commandHandler.closeGarage() ? "Closing garage door.." : "Garage door ALREADY is closed..";
            numberCloses++;

        }else if(msg.equals("gd toggle") || msg.equals("toggle garage door")){
            msgBack = Main.commandHandler.isGarageDoorClosed() ? "Opening garage door.." : "Closing garage door..";
            Main.commandHandler.toggleGarage(this);
            numberToggles++;

        }else if(msg.equals("gd status") || msg.equals("status garage door") || msg.equals("status of garage door")) {
            CommandHandler.GarageStatus gs = Main.commandHandler.garageStatus();
            if (gs.isGarageClosed()) {
                msgBack = "Garage door is currently: CLOSED.";
            } else {
                msgBack = "Garage door is currently: OPEN.\n\n" +
                        "Garage was opened at " + gs.getOpenTime();
                User u = gs.getBlame();
                if (u != null) {
                    msgBack += " by " + u.getNumber();
                } else {
                    msgBack += " manually";
                }
            }
            numberStatus++;
        }else if(msg.equals("gd mute")) {
            msgBack = Main.commandHandler.mute() ? "Garage door warning notifications muted until garage door closed" :
                    "Cannot mute notifications while garage door is closed";
            numberMutes++;
        }else if(msg.equals("gd warnings on")){ //TODO Fix warning mute
            this.isMute = true;
            msgBack = "Garage door warnings = ON";
            numberWarningsChanges++;
        }else if(msg.equals("gd warnings off")){
            this.isMute = false;
            msgBack = "Garage door warnings = OFF";
            numberWarningsChanges++;
        }else if(isAdmin()) {

            if(msg.contains("admin add ")){
                try{
                    msgBack = addUserAndNotify(msg.replaceAll("admin add ", "").trim());
                }catch (IOException i){
                    i.printStackTrace();
                    //TODO add more
                }
                numberAdminsUsages++;
            }else if(msg.contains("admin remove ")){
                msgBack = removeUser(msg.replaceAll("admin remove ", "").trim());
                numberAdminsUsages++;
            }

        }
        try{
            save();
        }catch (IOException e){
            e.printStackTrace();
        }
        return msgBack;
    }

    //TODO load them
    public static ArrayList<User> loadUsers(){
        Reader reader;
        File[] userDirs = new File(User.usersSavePath).listFiles();
        ArrayList<User> usersArray = new ArrayList();
        if(userDirs != null) {
            for (File f : userDirs) {
                if (!f.getName().contains("user") && !f.getName().contains(".json")) continue;
                try {
                    reader = new InputStreamReader(new FileInputStream(f));
                } catch (FileNotFoundException e) {
                    System.err.println("File not found: " + f.getPath());
                    e.printStackTrace();
                    continue;
                }
                Gson gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
                usersArray.add(gson.fromJson(reader, User.class));
                Console.println("User loaded: " + f.getName());
            }
        }else{
            new File(User.usersSavePath).mkdirs();
        }
        return usersArray;
    }

    protected void notifyOtherUsers(boolean onlyAdmins, String body){
        for(User user : Main.users){
            if(equalsNumber(user.getNumber())) break;
            if(user.isAdmin() || !onlyAdmins){
                Main.sendSMS(user.getNumber(), body);
            }
        }
    }

    public void save() throws IOException{
        String filePath = User.usersSavePath + "user" + this.getNumber().replace("+","");

        Writer writer = new OutputStreamWriter(new FileOutputStream(filePath));
        Gson gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();

        writer.write(gson.toJson(this));
        writer.flush();
        writer.close();
    }

    public String addUserAndNotify(String number) throws IOException{
        User u = new User(number, false);
        Main.users.add(u);
        String filePath = User.usersSavePath + "user" + u.getNumber().replace("+","");
        File f = new File(filePath);
        if(!f.exists()){
            f.getParentFile().mkdirs();

            Writer writer = new OutputStreamWriter(new FileOutputStream(filePath));
            Gson gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();

            writer.write(gson.toJson(u));
            writer.flush();
            writer.close();

            notifyOtherUsers(true,getNumber()+" added " + number + " to user list!");

            Main.sendSMS(number, "Hi there! You have been given authorization to operate a garage door remotely!\n\n" +
                    "Please send \"commands\" back to see a list of commands.");
            Console.println("!ALERT! " + this.number + " added " + number);
            return number + " was added to the user list!";
        }else{
            return "Woops, " + number + " is already a user!";
        }
    }

    public static void addUser(User u) throws IOException{
        String filePath = User.usersSavePath + "user" + u.getNumber().replace("+","");
        File f = new File(filePath);
        if(!f.exists()) {
            f.getParentFile().mkdirs();

            Writer writer = new OutputStreamWriter(new FileOutputStream(filePath));
            Gson gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();

            writer.write(gson.toJson(u));
            writer.flush();
            writer.close();
        }else{
            System.err.println("Something went wrong when adding "+u.getNumber());
        }
    }

    public String removeUser(String number){
        Iterator<User> iterator = Main.users.iterator();
        while(iterator.hasNext()){
            User u = iterator.next();
            if(u.equalsNumber(number)){
                if(!new File(usersSavePath+"user"+u.getNumber().replace("+","")).delete()){
                    System.err.println("!Error! while deleting "+number);
                    return "Failed to remove "+number+". Please check the console!";
                }
                Main.users.remove(u);
                Console.println("!ALERT! " + this.number + " removed " + number);
                return number + " was removed from the user list!";
            }
        }
        Console.println("!ALERT! " + this.number + " tried to removed " + number +" but FAILED!");
        return "An error has occurred!";
    }

    public String getLastUsedVersion(){
        return lastUsedVersion;
    }

    public void setLastUsedVersion(String version){
        this.lastUsedVersion = version;
    }

    public String getNumber(){
        return number;
    }

    public boolean isAdmin(){
        return isAdmin;
    }

    public boolean isMute(){
        return isMute;
    }
}

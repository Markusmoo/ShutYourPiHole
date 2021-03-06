package com.tonsaker.syph;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Markus Tonsaker on 2018-01-05.
 */
public final class CommandHandler implements GpioPinListenerDigital, ActionListener{

    public class GarageStatus{

        private boolean isGarageClosed;

        private String openTime;
        private User blame;

        public GarageStatus(boolean isGarageClosed, String openTime, User blame){
            this.isGarageClosed = isGarageClosed;
            this.openTime = openTime;
            this.blame = blame;
        }

        public boolean isGarageClosed() {
            return isGarageClosed;
        }

        public void setGarageClosed(boolean garageClosed) {
            isGarageClosed = garageClosed;
        }

        public String getOpenTime() {
            return openTime;
        }

        public void setOpenTime(String openTime) {
            this.openTime = openTime;
        }

        public User getBlame() {
            return blame;
        }

        public void setBlame(User blame) {
            this.blame = blame;
        }
    }

    private class DoorTimer extends Timer{

        private final int MAX_REPEATS_BEFORE_NOTIFY_ALL = 1;

        private String lastOpenedString = "NULL";
        private long lastOpenedLong = 0;
        private User lastUser = null;
        private int repeats = 0;

        public DoorTimer(int delay, ActionListener listener){
            super(delay, listener);
        }

        @Override
        public void start(){
            setDoorInfo();
            super.start();
        }

        @Override
        public void restart() {
            setDoorInfo();
            super.restart();
        }

        public void setLastUser(User user){
            this.lastUser = user;
        }

        public User getLastUser(){
            return lastUser;
        }

        public String getLastOpenedTime(){
            return lastOpenedString;
        }

        public String getElapsedTimeMins(){
            return String.format("%d minutes", TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis()-lastOpenedLong));
        }

        public boolean isRepeatsExceedingMax(){
            return (repeats > MAX_REPEATS_BEFORE_NOTIFY_ALL);
        }

        @Override
        protected void fireActionPerformed(ActionEvent e) {
            repeats++;
            super.fireActionPerformed(e);
        }

        private void setDoorInfo(){
            DateFormat dFmt = new SimpleDateFormat("HH:mm:ss");
            lastOpenedString = dFmt.format(new Date());
            lastOpenedLong = System.currentTimeMillis();
            repeats = 0;
            lastUser = null;
        }
    }

    private static final int MINUTES_TIL_NOTIFY = 30;

    private static final Pin GARAGE_PIN = RaspiPin.GPIO_26;
    private static final Pin GARAGE_POSITION_SENSOR_PIN = RaspiPin.GPIO_27;

    private GpioController gpio;

    private GpioPinDigitalOutput garageMotorPin;
    private GpioPinDigitalInput garagePositionSensorPin;

    private DoorTimer garageDoorTimeout;

    private boolean isMute;

    public CommandHandler(){
        setupGPIO();

        garageDoorTimeout = new DoorTimer(MINUTES_TIL_NOTIFY*1000*60, this);

        garagePositionSensorPin.addListener(this);
        garagePositionSensorPin.setDebounce(1000);
    }

    private void setupGPIO(){
        if(Main.debug){
            Console.println("!!!!!!!!WARNING!!!!!!!!! GPIO IS DISABLED");
            return;
        }
        gpio = GpioFactory.getInstance();
        garageMotorPin = gpio.provisionDigitalOutputPin(CommandHandler.GARAGE_PIN, PinState.LOW);
        garagePositionSensorPin = gpio.provisionDigitalInputPin(CommandHandler.GARAGE_POSITION_SENSOR_PIN, PinPullResistance.PULL_UP);
    }

    /**
     * Toggles garage door state.
     */
    public void toggleGarage(User user){
        if(isGarageDoorClosed()) handleBlame(user);
        fireRelay();
    }

    /** TODO Check to see if garage closed after 10 seconds
     * @return false if the garage was already closed
     */
    public boolean closeGarage(){
        if(!isGarageDoorClosed()){
            fireRelay();
            return true;
        }else{
            return false;
        }
    }

    /** TODO Check to see if garage opened after 10 seconds
     * @return false if the garage was already open
     */
    public boolean openGarage(User user){
        if(isGarageDoorClosed()){
            fireRelay();
            handleBlame(user);
            return true;
        }else{
            return false;
        }
    }

    /**
     * @return GarageStatus information on door
     */
    public GarageStatus garageStatus(){
        if(!isGarageDoorClosed()) {
            return new GarageStatus(false, garageDoorTimeout.getLastOpenedTime(), garageDoorTimeout.getLastUser());
        }else{
            return new GarageStatus(true, "", null);
        }
    }

    /**
     * @return true if the garage is closed
     */
    public boolean isGarageDoorClosed(){
        return garagePositionSensorPin.isLow();
    }

    /**
     * Pulses the garage door relay
     */
    private void fireRelay(){
        if(Main.debug){
            Console.println("DEBUG: ...RELAY FIRED...");
            return;
        }
        garageMotorPin.pulse(1000, PinState.HIGH);
    }

    private void handleBlame(User user){
        Timer t = new Timer(5000, e -> garageDoorTimeout.setLastUser(user));
        t.setRepeats(false);
        t.start();
        Console.println("Setting blame to "+user.getNumber());
    }

    public boolean mute(){
        if(isGarageDoorClosed()) return false;
        setMute(true);
        return true;
    }

    private void setMute(boolean mute){
        isMute = mute;
    }

    public boolean isMute(){
        return isMute;
    }

    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent e) {
        if(isGarageDoorClosed()){
            garageDoorTimeout.stop();

            Console.println("Garage closed..");
        }else{
            garageDoorTimeout.restart();

            Console.println("Garage opened..");
        }
        setMute(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(!isMute() && e.getSource().equals(garageDoorTimeout)){
            User lastUser = garageDoorTimeout.getLastUser();

            String consoleText = "Garage door left open for "+garageDoorTimeout.getElapsedTimeMins();
            if(lastUser != null) consoleText += " by "+lastUser.getNumber();
            Console.println(consoleText);

            if(lastUser == null || garageDoorTimeout.isRepeatsExceedingMax()){
                String byUserString = "manually ";

                if(lastUser != null) byUserString = "by " + lastUser.getNumber() + " ";

                Main.sendSMSAll("Hey!! The garage door was opened " + byUserString +
                        garageDoorTimeout.getElapsedTimeMins() + " ago.\n\n" +
                        "The door was initially opened at " + garageDoorTimeout.getLastOpenedTime(), false);
            }else if(!lastUser.isMute()){
                Main.sendSMS(lastUser.getNumber(), "Hey!! The garage door was opened by you " +
                        garageDoorTimeout.getElapsedTimeMins() + " ago.\n\n" +
                        "The door was initially opened at " + garageDoorTimeout.getLastOpenedTime());
            }
        }
    }
}

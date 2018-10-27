# ShutYourPiHole
A garage door controller/monitor to be used with a Raspberry Pi.

## What does it do?

ShutYourPiHole monitors the garage door position. If the garage door has been left open for over an hour, it sends a text message to the last person to use the garage door. If garage door is still not closed after 2 hours, it sends a text message to all authorized users. 

ShutYourPiHole also allows users to remote control the garage door with their mobile phone via SMS.

### Commands

User Commands:
* GD Open - Opens garage door
* GD Close - Closes garage door
* GD Toggle - Toggles garage door position
* GD Status - Returns what position garage door is in
* GD Mute - Mutes garage door left open warnings until garage door is reopened
* GD Warnings Off - Turns off garage door warnings for yourself
* GD Warnings On - Turns on garage door warnings for yourself

Admin Commands:
* Admin add +1########## - Authorizes phone number to use garage door
* Admin remove +1########## - Deauthorizes phone number from using garage door

### Materials Needed

* Raspberry Pi Zero W
* 5v Power Supply
* Relay Board

### Installing

1. Create a file on the raspberry pi named run.sh with the text:

  #!/bin/sh
  
  sudo java -Dpi4j.linking=dynamic -jar ShutYourPiHole.jar [INSERT ACCOUNT SID] [INSERT API SID] [INSERT API SECRET] [INSERT TWILIO PHONE NUMBER] [INSERT MASTER PHONE NUMBER] 

2. Wire relay to garage door with the input coil across WiringPi pin 26.
3. Wire garage door position sensor to pin 27.
4. Port forward TCP & UDP port 4567 to raspberry pi's IP Address.
5. Run "run.sh"

## Built With
* [Spark](http://sparkjava.com/) - Used to run a web server which recieves HTTP requests.
* [Twilio](https://www.twilio.com) - Used to handle text messages
* [Pi4J](http://pi4j.com/) - Used to communicate with Raspberry Pi's GPIO
* [Maven](https://maven.apache.org/) - Dependency Management

## Authors

* **Markus Tonsaker** - [Markusmoo](https://github.com/Markusmoo)

## License

Free to use! Open source.

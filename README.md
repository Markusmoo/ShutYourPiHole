# ShutYourPiHole
A garage door controller/monitor to be used with a Raspberry Pi.

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

# Encrypted Video Stream

This project allows a host computer to stream their video to both a single other computer, or to a multicast address, 
allowing any number of recipients to also receive the stream. This program uses Webcam capture API to easily access
the webcam. 

## Getting Started


### Prerequisites and Installation

This project was built using Java 8. Some networks may prevent multicast addresses from being used, in which case
the server must select the "forward to client" option on the main page. The host must have a built in webcam
(external webcam has never been tested). Maven must be installed.

Once this project has been cloned, run the following commands to package to program

```
mvn clean
mvn package
```


### Running the program

To run the program, first build/package the program, and run the following command to launch the application:

```
java -cp target/csc445hw03-1.0-SNAPSHOT.jar Main

```

### Some notes on running the program

In order to successfully run this application, the following things must occur.

#### Running the host/server

You must set a password (no longer than 16 characters) on the main page if you are the server. This will encrypt the video stream

##### Situation a)
If the host computer can directly access the multicast address indicated on the initial page (230.12.123.4), 
then select the multicast option from the initial page, set the password, and hit "launch server".


##### Situation b)
If the host computer cannot directly access the multicast address indicated on the initial page (230.12.123.4),
the select the "forward to client option" on the main page. In the textfield provided, enter the IP address of another client
which CAN directly access the multicast socket, and is running this application, set a password, and hit "launch server".

#### Running the client
The clients must be able to connect to the multicast address provided (230.12.123.4).
In all cases, the clients select "Launch client" from the main screen, and then do the following: 

###### Situation a)
If the host computer can send directly to the multicast socket OR another client is doing Situation b), then select regular client.

###### Situation b)
If the host computer is streaming directly to your computer, then you must select the "Forwarder" option.


In both cases, the correct password must be provided, followed by pressing the "connect" button on the client window.
If the password is incorrect, the stream video decryption will fail, and you will be informed that the password was incorrect.



## Built With

* [WebcamCapture](https://github.com/sarxos/webcam-capture) - The webcam capture library used
* [Maven](https://maven.apache.org/) - Dependency Management


## Authors

* **Chris Townsley** - *Group member* - [github](https://github.com/ctownsle)
* **Cedric Hansen** - *Group member* - [github](https://github.com/cedrichansen) - [website](cs.oswego.edu/~chansen)
* **John Santos** - *Group member* - [github](https://github.com/jsantos4)
* **Christian Sumano** - *Group member* - [github](https://github.com/csumano)
* **Tonia Sanzo** - *Group member* - [github](https://github.com/ToniaSanzo)

See also the list of [contributors](https://github.com/csc445finalproject/csc445hw03/graphs/contributors) who participated in this project.


## Acknowledgments

* Doug Lea - csc445 (computer networks at SUNY Oswego) instructor

# Door Unlock Lock System

<img src="https://github.com/danvolchek/door-unlocker/blob/master/images/system_image.jpg" alt="drawing" height="500">

- [Door Unlock Lock System](#door-unlock-lock-system)
  * [Hardware Requirements](#hardware-requirements)
  * [3D Model](#3d-model)
    + [Install instructions](#install-instructions)
    + [Overview](#overview)
  * [Android Code](#android-code)
    + [Install instructions](#install-instructions-1)
    + [Overview](#overview-1)
  * [Arduino Code](#arduino-code)
    + [Install instructions](#install-instructions-2)
    + [Overview](#overview-2)

This repo holds the hardware and software needed to create the door unlock/lock system that I've created. The main components are as follows. See below for a detailed description of each.

| Component    | Files           |  Description                                    |
|--------------|-----------------|-------------------------------------------------|
| 3D Model     | `Door.fcstd`    | The 3d model needed to print the door assembly. |
| Android Code |  `DoorManager/` | The Android studio project for the Android app. |
| Arduino Code | `arduino/`      | The sketch for the Arduino.                     |

## Hardware Requirements
 - Some [magnets](https://www.amazon.com/DIYMAG-Powerful-Neodymium-Permanent-Scientific/dp/B06XD2X45M) (any work, but they need to be 3mm thickness, 32mm diameter).
  - An [Arduino Yun](https://store.arduino.cc/usa/arduino-yun). All the Arduino code is specific to it so you're best off getting this if you don't want to rewrite it.
  - A [servo](https://www.adafruit.com/product/1404). If you want to use another, you'll need to edit the 3D model.
  - Print out the 3D model.
  - Some wires to wire up the Arduino to the servo.
  - Some hot glue to attach the circular servo horn to the servo gear.
  - A [portable battery](https://www.amazon.com/Anker-PowerCore-Ultra-Compact-High-Speed-Technology/dp/B0194WDVHI). Note that this specific one ended up being slightly too large so I had to print the battery holder at a slight angle so the cable would fit.

## 3D Model
### Install instructions
  - Install [FreeCAD](https://www.freecadweb.org/). See the note at the end of the overview about FreeCAD, though.
  - Once you've printed things out (unless you improve the model) you'll need to:
    - File down the motor holder slightly to get it to fit the servo.
    - File the gear teeth down slightly so they mesh nicer.
  - I ended up not using the magnet where the Arduino is because it was already firmly attached to the door and I didn't want to break the electronics.
### Overview
The main components of the model are:
 - The **door holes** fit around the door knob and lock knob, aligning the assembly in the proper spot.
 - The **magnet pockets** hold magnets that keep the assembly firmly attached to the door.
 - The **motor holder** holds the servo in the right spot.
 - The **Arduino pegs** hold the Arduino in the right spot.
 - The **battery holder** holds the battery. Note that this may need to be shifted/angled in order to fit based on the size of your USB cable.
 - The **gears** that attach to the motor and door lock.
 
The model was created using FreeCAD. I would honestly not recommend this software and encourage you to export it to another CAD tool if you want to make changes.

## Android Code
### Install instructions
  - Install [Android Studio](https://developer.android.com/studio).
  - Install the Android libraries for SDK version 28.
  - **You'll need to update the key, iv, and ip of your Yun in MainActivity.java. See the TODOs.**
  - Connect your phone (or an emulator) to your PC and install the app.
### Overview
This holds the code for the Android app that communicates with the Arduino. It
  - Encrypts and sends lock/unlock commands to the Arduino.
  - Periodically updates the current door state by querying the Arduino.

The app was made using Android Studio and is fairly standard.

## Arduino Code
### Install instructions
  - Follow [this guide](https://www.arduino.cc/en/Guide/ArduinoYunLin#toc18) (or possibly [this one](https://www.arduino.cc/en/Guide/ArduinoYun#toc16) depending on which Yun you bought) to get the Yun connected to your Wifi.
  - Follow [this guide](https://www.arduino.cc/en/Tutorial/YunSysupgrade) to upgrade your Yun to the latest version.
  - SSH into your Yun and install the openssl utils using `opk update` and then `opkg install openssl-util`.
  - **You'll need to update the key and iv in arduino.ino. See the TODOs.**
  - Connect your Yun to your PC and upload the sketch.
  - Wiring diagram:
  ![wiring diagram](https://github.com/danvolchek/door-unlocker/blob/master/images/wiring_diagram.png)
    - In the diagram:
      - the servo ground should go to the ground of the Yun.
      - the servo Vin should go to the 5v out of the Yun.
      - the servo signal should go to digital pin 9 out of the Yun.
      - the servo feedback should go to analog pin 0 out of the Yun.
### Overview
This is the the code for the Arduino which:
  - Listens for lock/unlock commands and decrypts them.
  - Moves the servo when valid commands are received.
  - Hosts the current door state.

This was made using the [Arduino IDE](https://www.arduino.cc/en/Main/Software).

<img align="left" width="100" height="100" src="logo.png">

# AstroAllInOne
AstroAllInOne is an INDI driver that forwards commands from an 
Arduino to two INDI devices to control with just _one_ Arduino a 
MoonLite focuser and a customizable list of digital and PWM pins.

### Behind the scenes
To work, AstroAllInOne uses `socat` and creates two virtual devices (sockets).
Let's say, for example, that /dev/port1 and /dev/port2 are created:
the first virtual port is used to read whatever is sent to the second one
and the end user will be asked to connect the MoonLite driver to /dev/port2.
AstroAllInOne will forward **every** byte sent to port2 to the real Arduino,
plus command `:AVxxyyy#`, where `xx` is the pin nd `yyy` its new analog value,
to change the state of a pin, and command `:RS#` to reset the board.

## Starting AstroAllInOne using its GUI
After starting AstroAllInOne a control panel will appear asking you to check
the stored pin definitions. You'll be able to change the INDI server port
(default 7625 to allow other INDI clients like KStars to use 7624),
the serial port (the list is automatically updated) and
the list of digital and PWM pins. You can click "Add" to add a pin definition:
adding a digital pin means adding an INDI _switch element_
(a checkbox in the INDI control panel in the client)
that allows the user to switch the state of the pin ON and OFF;
instead, after creating a PWM pin, AstroAllInOne will add an INDI _number element_
to its driver that allows the end user to write the pin value (1→100%)
directly from the client INDI control panel.
You'll be asked about the pin port (for example, pin 13). Then you can
click "Edit" to modify the pin's properties: a custom name ("Dew heater")
and a default value, applied when the driver starts.

### What's next?
After having accepted all the settings (click "Continue") a small status window
will appear: there you'll be able to copy the focuser port to the clipboard,
restart the Arduino board (pay attention! **you may modify the focuser state
unexpectedly**) or close the INDI driver & server. Click on "Copy focuser port"
and paste the copied port in the MoonLite driver's port field (you'll
need a INDI server and client for that, see below). You're done! Click "Connect"
in the MoonLite driver and enjoy the focuser driver. To open the pin manager's
INDI control panel connect an INDI client to the port you chose in the previous
step. A "Control" tab will appear in the INDI client, allowing you to modify the pins
values. To close the entire driver (MoonLite + pin manager) disconnect all the
clients and click "Exit".

### More boring and exhaustive instructions
If you are new to the INDI protocol please read more in the
<a href="http://indilib.org/about/discover-indi.html">INDI website</a> and in
<a href="https://en.wikipedia.org/wiki/Instrument_Neutral_Distributed_Interface">Wikipedia</a>.
In order to use the pin manager driver you'll need an INDI server and a client.
The server will host the MoonLite device, in whose control panel you'll have
to paste the port, while the client must be connected to the chosen port
to access the pin manager control panel. Here there's an example:
open KStars, open Ekos from the toolbar and create a new profile containing
your telescope mount, CCD camera or reflex and a MoonLite focuser. Uncheck
the auto-connect box and give the profile a name. Start the INDI server
and open the INDI control panel from Ekos. Connect each device using a different port
and, in the MoonLite tab, paste the port in the "Port" field. Select baud speed to
115200 and connect the MoonLite device. If everything is OK you'll get
the full MoonLite control panel. Otherwise check if AstroAllInOne is running,
if the virtual port exists and if the speed is 115200. Now close Ekos
and go to Tools→Devices→Devices Manager→Client→Add. Write a name ("AstroAllInOne"),
the AstroAllInOne server IP address (or localhost if on the same computer) and port,
then click OK→Connect. The pin manager control panel will be added to the
INDI control panel in Ekos, just like any other device started before from the
Ekos profile. Enjoy!

## Starting from the command line (`bash`)

| Short option |  Long option  |               Param              |                          Description                          |
|:------------:|:-------------:|:--------------------------------:|:-------------------------------------------------------------:|
|      -g      |    --no-gtk   |                                  |          Forces the app to use the Java default L&F.          |
|      -n      |    --no-gui   |                                  |             Do not show the control panel, no GUI.            |
|      -p      |  --indi-port  |             e.g. 7624            |      Specifies a port for the INDI server (default 7625).     |
|      -a      | --serial-port |        e.g. "/dev/ttACM0"        |                    Specifies a serial port.                   |

For instance, run `astroallinone --no-gui --indi-port="7624" --serial-port="/dev/ttACM0"`
to start AstroAllInOne without GUI, INDI server port 7624, Arduino serial port /dev/ttACM0

## The Arduino MoonLite firmware
Take a look at
<a href="https://github.com/SquareBoot/Arduino-MoonLite-focuser">Arduino-MoonLite-focuser</a>
to download the firmware, 3D mounting brackets, Eagle CAD circuit project and more!
<br>**Note: standard MoonLite focusers do NOT support pin management! They won't work in AstroAllInOne properly, making this app useless!**

## Contributions
- SquareBoot

Be the first to fork AstroAllInOne!

### Libraries
- <a href="https://github.com/google/gson">Gson</a> by Google, <a href="https://github.com/google/gson/blob/master/LICENSE">license</a>
- <a href="https://commons.apache.org/proper/commons-cli/">Commons CLI</a> by Apache Commons, <a href="http://www.apache.org/licenses/">license</a>
- <a href="http://indiforjava.sourceforge.net/stage/">INDI for Java</a> by Zerjillo
    - <a href="https://ostermiller.org/utils/">Ostermiller Java Utilities</a> by Stephen Ostermiller, <a href="https://ostermiller.org/utils/license.html">license</a>
- <a href="https://github.com/scream3r/java-simple-serial-connector">jSSC</a> by scream3r, <a href="http://www.gnu.org/licenses/lgpl.html">license</a>
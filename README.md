<img align="left" width="100" height="100" src="logo.png">

# AstroAllInOne
AstroAllInOne is an INDI driver that forwards commands from an 
Arduino to two INDI devices to control with just _one_ Arduino a 
MoonLite focuser and a customizable list of digital and PWM pins.

## Using the control panel
In the AstroAllInOne Control Panel you'll be able to select the port for the INDI server
(default 7625 to allow other INDI clients like KStars to use 7624) and define
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
After defining all the pins, you can save and close the control panel to use the driver or the server
from the command line (see below), or you can start the server directly from the control panel.

## Starting the INDI driver
The INDI driver can be run inside another INDI server executing `astroallinone -d`.

## Stand-alone CLI server
Use `astroallinone -p=xxxx`, replacing `xxxx` with whatever port you want (or `0` to use the saved port),
to start the server without GUI in the terminal.

## Connecting and using the driver
If you are new to the INDI protocol please read more in the
<a href="http://indilib.org/about/discover-indi.html">INDI website</a> and in
<a href="https://en.wikipedia.org/wiki/Instrument_Neutral_Distributed_Interface">Wikipedia</a>.<br>
In order to use the pin manager driver you'll need an INDI client.
In KStars, open Ekos from the toolbar and create a new profile containing
your telescope mount, CCD camera or reflex and a MoonLite focuser, and in the "Remote:" driver field
write `INDI Arduino pin driver@localhost:7625`. Be careful to replace `localhost:7625` with the
right host and port. Uncheck the auto-connect box and give the profile a name.
Now start the AstroAllInOne server (from the control panel or from the command line).
Start the Ekos INDI server and open the INDI control panel. Connect your devices,
go to the "INDI Arduino pin driver" tab and connect the driver. In the "Serial connection"
tab select a serial port and hit connect. A new tab called "Manage pins" will show up,
in which you can mange the PWM and digital pins you selected in the control panel.
Copy the MoonLite port to the clipboard and paste it in the "Port" field in the MoonLite
driver tab. Select baud speed to 115200 and connect the MoonLite device. If everything is OK you'll get
the full MoonLite control panel. Otherwise check if AstroAllInOne server is running,
if the virtual port exists and if the speed is 115200. Enjoy!

### Sending configuration to another computer
You can send the pin configuration and all the settings to another computer. Ensure AstroAllInOne is installed
on both computer alongside with socat. The other computer must have a SSH server installed, while the sender
a SSH client. From the sender computer, open the control panel and click on "Send configuration". You'll
asked about the remote host, username and password. If the process fails due a missing remote folder,
open and close one time the control panel in the remote computer and retry (this will create the required
config folder in the remote user directory, which must be present in order to send the settings file).

### Starting from the command line (`bash`)

| Short option | Long option     | Param             | Description                                                                                           |
|:------------:|-----------------|-------------------|-------------------------------------------------------------------------------------------------------|
| -a           | --serial-port   | e.g. /dev/ttyUSB0 | Specifies a serial port and connects to it if possible. Otherwise it will be stored to settings only. |
| -c           | --control-panel |                   | Shows the control panel.                                                                              |
| -d           | --driver        |                   | Driver-only mode (no server, stdin/stdout)                                                            |
| -p           | --indi-port     | e.g. 7625         | Stand-alone server mode, CLI. If port=0, fetch the last used port from the settings.                  |
| -v           | --verbose       |                   | Verbose mode.                                                                                         |

### The Arduino MoonLite firmware
Take a look at
<a href="https://github.com/SquareBoot/Arduino-MoonLite-focuser">Arduino-MoonLite-focuser</a>
to download the firmware, 3D mounting brackets, Eagle CAD circuit project and more!
<br>**Note: standard MoonLite focusers do NOT support pin management! They won't work in AstroAllInOne properly, making this app useless!**

### Behind the scenes
To work, AstroAllInOne uses `socat` and creates two virtual devices (sockets).
Let's say, for example, that `/dev/port1` and `/dev/port2` are created:
the first virtual port is used to read whatever is sent to the second one
and the end user will be asked to connect the MoonLite driver to /dev/port2.
AstroAllInOne will forward **every** byte sent to port2 to the real Arduino,
plus command `:AVxxyyy#`, where `xx` is the pin nd `yyy` its new analog value,
to change the state of a pin, and command `:RS#` to reset the board.

### Contributions
- SquareBoot

Be the first to fork AstroAllInOne!

### Libraries
- <a href="https://github.com/google/gson">Gson</a> by Google, <a href="https://github.com/google/gson/blob/master/LICENSE">license</a>
- <a href="https://commons.apache.org/proper/commons-cli/">Commons CLI</a> by Apache Commons, <a href="http://www.apache.org/licenses/">license</a>
- <a href="http://indiforjava.sourceforge.net/stage/">INDI for Java</a> by Zerjillo
    - <a href="https://ostermiller.org/utils/">Ostermiller Java Utilities</a> by Stephen Ostermiller, <a href="https://ostermiller.org/utils/license.html">license</a>
- <a href="https://github.com/scream3r/java-simple-serial-connector">jSSC</a> by scream3r, <a href="http://www.gnu.org/licenses/lgpl.html">license</a>
- <a href="http://www.jcraft.com/jsch/">JSch</a> by JCraft, <a href="http://www.jcraft.com/jsch/LICENSE.txt">license</a>
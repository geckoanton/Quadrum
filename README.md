# Quadrum

<p align="left">
  <a href="https://geckoanton.github.io/quadrum/">
    <img src="logo.png" width="128" alt="Quadrum logo">
  </a>
</p>

[![Version](https://img.shields.io/badge/release-v2.0-blue)](https://img.shields.io/badge/release-v2.0-blue)
[![Version](https://img.shields.io/badge/license-GPL--3.0-blue)](https://www.gnu.org/licenses/)
[![Version](https://img.shields.io/badge/build-passing-brightgreen)](https://img.shields.io/badge/build-passing-brightgreen)

Quadrum is a LED-Cube framework and editor built to make it easier to upload and stream animations. Visit <https://geckoanton.github.io/quadrum/> for help regarding implementation details of Quadrum, as well as downloadable release versions of the Quadrum editor (available for Windows, Linux and MacOS) and Arduino library.

## IntelliJ Development

The project is set up for the IntelliJ IDE, to edit the desktop software source code - clone this repository and open the directory <i>desktop/Quadrum</i> in IntelliJ. To be able to compile and run the project you must download and link the libraries described below. Compile the project with <b>java version 11.0.9</b> or similar (the main class is located under <i>src/main/Main.java</i>).

### Third Party Libraries

Download the following libraries and add them to the cloned project in IntelliJ (to add a library navigate to <i>File</i> > <i>Project Structure</i>, click on <i>Libraries</i>, click on <i>+</i> and add the path to the library).

<ul>
  <li>JavaFX - https://gluonhq.com/products/javafx/ Download <b>version 11.0.2 SDK</b> or similar, link to library path <i>javafx-sdk-11.0.2/lib</i></li>
  <li>jSerialComm - https://fazecast.github.io/jSerialComm/ Download <b>the jar file version 2.6.2</b> or similar and link to it</li>
</ul>

## Arduino Library

The Quadrum Arduino library is located under <i>arduino/Quadrum</i>. To compile and upload the source code - add the directory to the Arduino IDE as a library and include the header files as usual.

## Compile Custom JRE

To compile a custom JRE that is distributed with Quadrum run the following with jlink

```Bash
jlink --output QuadrumJRE --module-path <PATH_TO_JAVAFX>/javafx-jmods-11.0.2:<PATH_TO_JSERIALCOMM>/jSerialComm-2.6.2.jar --add-modules java.datatransfer,java.desktop,jdk.xml.dom,javafx.graphics,javafx.fxml,javafx.controls,javafx.web,com.fazecast.jSerialComm
```
replace <i>&lt;PATH_TO_JAVAFX&gt;</i> and <i>&lt;PATH_TO_JSERIALCOMM&gt;</i> with their respective paths. <i>&lt;PATH_TO_JAVAFX&gt;</i> must link to JavaFX jmods and <i>not</i> JavaFX SDK (the type we linked to from IntelliJ).

## License

Quadrum is released under the GNU General Public License which can be found in the LICENSE file. However, the third-party libraries utilized in the project such as <i>jSerialComm</i> are not necessarily distributed under the same license as Quadrum - to review license information regarding jSerialComm, navigate to <i>thirdPartyLicenses/jSerialComm</i> or visit https://github.com/Fazecast/jSerialComm.

All icons except for the Quadrum logo have been downloaded from https://icons8.com/.

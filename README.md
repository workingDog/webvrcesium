# Example using Scala.js WebVR API and CesiumScala 

The [WebVR](https://w3c.github.io/webvr/) API provides purpose-built interfaces to VR hardware 
to allow developers to build compelling, comfortable VR experiences. A Scala.js facade to the WebVR API is at [WebvrScala](https://github.com/workingDog/WebvrScala).
 
[Cesium](http://cesiumjs.org/) is a JavaScript library for creating 3D globes and 2D maps in a web browser without a plugin.
A Scala.js facade to Cesium is at [CesiumScala](https://github.com/workingDog/CesiumScala).

This example is a Scala version of [Cesium Cardboard](https://github.com/AnalyticalGraphicsInc/cesium/blob/master/Apps/Sandcastle/gallery/Cardboard.html)

## Dependencies

See the build.sbt file for the library dependencies. 

Using SBT 0.13.13 and scala.js 0.6.13


## Installation 

To compile and generate a javascript file from the source code:

    sbt fastOptJS 

The javascript file (webvrtest-fastopt.js) will be in the "./target/scala-2.11" directory.

## Usage

Install [Cesium](http://cesiumjs.org/).

I don't have a VR headset, so I use Chrome with the [WebVR API Emulation](https://chrome.google.com/webstore/detail/webvr-api-emulation/gbdnpaebafagioggnhkacnaaahpiefil?hl=en) extention.

Put "webvrtest.html" and "webvrtest-fastopt.js" files in the "Cesium/Apps" directory and launch Cesium (node server.js).

Point your browser to http://localhost:8080/Apps/webvrtest.html

Select from the Chrome menu bar, 

    View->Developer->Developer Tools

then in the "developer tools" top bar, click on the ">>" and select "WebVR" 

The webvr emulation will then be displayed. Use the mouse to turn the rotation widget.


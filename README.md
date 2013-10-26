World Metro
===========

World Metro is a universal metro/subway/underground Android app, with
the ambition to cover major cities with metros. It is currently under
development and is expected to have following features:

* Supported cities: Beijing, Cairo, Guangzhou, Hong Kong, Kyoto,
  London, Munich, Osaka, Paris, Rome, São Paulo, Seoul, Shanghai,
  Singapore, Shenzhen, Tokyo, Washington
* Designed for offline usage
* Vector map
* Display both English (if available) and local languages for station/line names
* Time schedule
* Real time feedback of stations
* Easy input to search station/destination (ignoring
  accents/diacritical marks)

For more information about World Metro, please go to
  <https://github.com/whily/worldmetro>

Wiki pages can be found at
  <https://wiki.github.com/whily/worldmetro>

Development
-----------

The following tools are needed to build World Metro from source:

* JDK version 6/7 from <http://www.java.com> if Java is not available. 
  Note that JDK is preinstalled on Mac OS X and available via package manager
  on many Linux systems. 
* Android SDK r22.
* Scala (2.10.0)
* sbt (0.12.4)
* [Inkscape](http://inkscape.org) and [ImageMagick](http://www.imagemagick.org)
  to generate icons.
  
To compile/run the code, follow the steps below:

1. Please ignore this step as it is a mental note to the
   author. Generate the code from the template as below:

        $ g8 fxthomas/android-app

2. This is step is a work around. It seems that the plugin sbt-android
   assumes that tools like `aapt` and `dx` are located in
   `$ANDROID_HOME/platform-tools`. However at least in Android SDK
   r22, the location is `$ANDROID_HOME/build-tools/18.0.1/`. The
   simplest solution is to copy those binaries (including directory
   **lib** which is related to `dx`) to folder
   `$ANDROID_HOME/platform-tools`.
   
3. In the project directory, run the following command to build the
   app and start it in a connected device or emulator:

        $ sbt start
  
TODO
----

* Regression test (for all station pairs in all cities)
* Draw line



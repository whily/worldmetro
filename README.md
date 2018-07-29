World Metro
===========

World Metro is a universal metro/subway/underground Android app, with
the ambition to cover major cities with metros. It is currently under
development and is expected to have following features:

* Supported cities: Beijing, Munich, Prague, Wuhan.
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

* JDK version 8 from <http://www.java.com> if Java is not available.
  Note that JDK is preinstalled on Mac OS X and available via package manager
  on many Linux systems.
* Android SDK r25.2.5.
* Scala (2.11.11)
* sbt (0.13.16)
* [Inkscape](http://inkscape.org) and [ImageMagick](http://www.imagemagick.org)
  to generate icons.

### Generate the icons

In project directory, run following command:

        $ ./genart

### Build the code

The library dependencies include
[scasci](https://github.com/whily/scasci) and
[scaland](https://github.com/whily/scaland).  Please
follow the steps discussed in those libraries on how to use them.

To compile/run the code, run the following command to build the
   app and start it in a connected device:

        $ sbt android:run

To build a release version and start it in a connected device:

        $ sbt android:set-release android:run

TODO
----

* Regression test (for all station pairs in all cities)
* Draw line

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

Installation
------------

Development
-----------

The following tools are needed to build World Metro from source:

* JDK version 6/7 from <http://www.java.com> if Java is not available. 
  Note that JDK is preinstalled on Mac OS X and available via package manager
  on many Linux systems. 
* Android SDK r22.
* Eclipse (4.2 Juno)
* Scala (2.10.0)
* Scalar IDE for Eclipse plugin (v3.0.1) from <http://scala-ide.org/>.
* AndroidProguardScala from
  <https://github.com/banshee/AndroidProguardScala>
* [Inkscape](http://inkscape.org) and [ImageMagick](http://www.imagemagick.org)
  to generate icons.
  
TODO
----

* If the source/target station are transits, consider the shortest
  route.
* Regression test (for all station pairs in all cities)
* Route/Segment, Station, Line
* Draw line
* Display results
* Handle time/transit search (i.e. add transit search)
* Draw map.
* Add start with one station and show the stations afterwards.



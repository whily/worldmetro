/**
 * Class City.
 *
 * @author  Yujian Zhang <yujian{dot}zhang[at]gmail(dot)com>
 *
 * License: 
 *   GNU General Public License v2
 *   http://www.gnu.org/licenses/gpl-2.0.html
 * Copyright (C) 2013 Yujian Zhang
 */

package net.whily.android.worldmetro

import java.io.InputStream
import scala.collection.mutable.Set
import android.app.Activity

// Using mutable.HashMap might improve performance. However, there is 
// runtime error "NoSuchMethod" when calling HashMap.keys
import scala.collection.immutable.HashMap

import scala.xml

/** 
 *  City class holds all data for a metro network of a city. 
 *  
 *  cityName should be in English; and if there is spaces (e.g. San Francisco), 
 *  use underscore for space. cityName is the base name for the corresponding xml file
 *  (e.g. san_francisco.xml).
 *  */
class City(activity: Activity, cityName: String) {
  private val city = xml.XML.load(activity.getResources.openRawResource(
    Util.getRawId(activity, cityName)))
  private val stations = city \ "stations" \ "station"
  private val lines = city \ "lines" \ "line"
  
  // Map (stationId -> stationName)
  val stationIdMap = getStationIdMap
  // Map (stationName -> stationId)
  private val stationNameMap = stationIdMap.map(_ swap)
  
  private val timeGraph = Graph.Graph(getTravelTimeMap)
  
  val stationNames = stationNameMap.keys.toArray sortWith (_ < _)
  
  def findRoute(sourceName: String, targetName: String): List[String] = 
    timeGraph.find(stationNameMap(sourceName), stationNameMap(targetName))
   
  private def getStationIdMap: HashMap[String, String] = {
    var map = new HashMap[String, String]()
    val languagePref = Util.getLanguagePref(activity)
    for (station <- stations) {
      val id             = (station \ "@id").text
      val englishName    = (station \ "@english").text
      val localName      = (station \ "@local").text
      assert(!id.isEmpty && !(englishName.isEmpty && localName.isEmpty))
      val name = 
        if (languagePref == "both" && !localName.isEmpty && !englishName.isEmpty)
          localName + " (" + englishName + ")"
        else if ((!localName.isEmpty && englishName.isEmpty) ||
                 (!localName.isEmpty && !englishName.isEmpty && languagePref == "local"))
          localName
        else
          englishName
        
      map += (id -> name)  
      
      val transits = station \ "transit"
      var ids: Set[String] = Set()
      for (transit <- transits) {
        val ids = (transit \ "@ids").text.split(" ")
        for (altId <- ids if altId != id)
          map += (altId -> name)
      }
    }

    map
  }
  
  private def getTravelTimeMap: HashMap[(String, String), Int] = {
    var map = new HashMap[(String, String), Int]()

    for (line <- lines) {
      val id = (line \ "@id").text
      val lineType = (line \ "@type").text

    	val stations = line \ "stations" \ "station"
    	var prevStation = ""
    	var index = 0
    	var firstStation = ""
    	var firstTime = ""
    	for (station <- stations) {
    	  val stationId = (station \ "@id").text
    	  if (index == 0) {
    	    index = 1
    	    prevStation = stationId
    	    if (lineType == "ring") {
    	      firstStation = stationId
    	      firstTime = (station \ "@time").text
    	      assert(!firstTime.isEmpty)
    	    }
    	  } else {
    	    val time = (station \ "@time").text
    	    assert(!time.isEmpty())
    	    map += ((prevStation, stationId) -> time.toInt)
    	    map += ((stationId, prevStation) -> time.toInt)
    	    prevStation = stationId
    	  }
    	}
      if (lineType == "ring") {
        map += ((firstStation, prevStation) -> firstTime.toInt)
        map += ((prevStation, firstStation) -> firstTime.toInt)            
      }
      
      // Add transit weights.
      val transits = city \ "stations" \ "station" \ "transit"
      for (transit <- transits) {
        val ids = (transit \ "@ids").text.split(" ")
        val time = (transit \ "@time").text.toInt
        val oneway = (transit \ "@oneway").text
        if (oneway != "") {
          assert(ids.length >= 2 && ((oneway == "source") || (oneway == "target")))
          val first = ids(0)
          for (id <- ids.drop(1))
            if (oneway == "source")
              map += ((first, id) -> time)
            else
              map += ((id, first) -> time)
        } else {
        for (i <- 0 until ids.length)
          for (j <- (i + 1) until ids.length) {
            map += ((ids(i), ids(j)) -> time)
            map += ((ids(j), ids(i)) -> time)
          }
        }        
      }
    }
    
    map
  }
  
  /** Return true if `thisId` and `thatId` refer to the same station 
   *  (i.e. they can be on the different lines).
   */
  def sameStation(thisId: String, thatId: String) = {
    thisId == thatId
  }
  
  /** Trim the unnecessary transits at the begiing and end of the `path`. */
  def trimPath(path: List[String]): List[String] = {
    val len = path.length
    if (len <= 1) path
    else if (sameStation(path.head, path.tail.head)) trimPath(path.tail)
    else {
    	val (u, v) = path.splitAt(len - 1)
    	if (sameStation(u.last, v.head)) trimPath(u)
    	else path
    }
  }  
}
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
import android.app.Activity

// Using mutable.HashMap might improve performance. However, there is 
// runtime error "NoSuchMethod" when calling HashMap.keys
import scala.collection.immutable.HashMap

import scala.xml

/** 
 *  City class holds all data for a metro network of a city. 
 *  
 *  cityName should be in English; and if there is spaces (e.g. San Francisco), 
 *  use hyphen for space. cityName is the base name for the corresponding xml file
 *  (e.g. san-francisco.xml).
 *  */
class City(activity: Activity, cityName: String) {
  private val city = xml.XML.load(activity.getResources.openRawResource(
    Util.getRawId(activity, cityName)))
  private val stations = city \ "stations" \ "station"
  private val lines = city \ "lines" \ "line"
  
  // Map (stationName -> stationId)
  private val stationNameMap = getStationNameMap
  // Map (stationId -> stationName)
  val getStationIdMap = stationNameMap.map(_ swap)
  
  private val timeGraph = Graph.Graph(getTravelTimeMap)
  
  def stationNames: Array[String] = stationNameMap.keys.toArray
  
  def findRoute(sourceName: String, targetName: String): List[String] = 
    timeGraph.find(stationNameMap(sourceName), stationNameMap(targetName))
   
  private def getStationNameMap: HashMap[String, String] = {
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
        
      map += (name -> id)  
    }

    map
  }
  
  private def getTravelTimeMap: HashMap[(String, String), Int] = {
    var map = new HashMap[(String, String), Int]()

    for (line <- lines) {
      val id = (line \ "@id").text

    	val stations = line \ "stations" \ "station"
    	var prevStation = ""
    	var index = 0
    	// TODO: handle ring-type metro line.
    	for (station <- stations) {
    	  val stationId = (station \ "@id").text
    	  if (index == 0) {
    	    index = 1
    	    prevStation = stationId
    	  } else {
    	    val time = (station \ "@time").text
    	    assert(!time.isEmpty())
    	    map += ((prevStation, stationId) -> time.toInt)
    	    map += ((stationId, prevStation) -> time.toInt)
    	    prevStation = stationId
    	  }
    	}
    }
    
    map
  }
}
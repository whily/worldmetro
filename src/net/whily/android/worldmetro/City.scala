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
import scala.collection.mutable
import scala.xml
import android.app.Activity

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
  
  // stationIdMap:   (stationId   -> stationName)[
  // stationNameMap: (stationName -> (set of stationId))
  val (stationIdMap, stationNameMap) = getStationIdMap
  
  private val timeGraph    = Graph.Graph(getTimeTransitMap(true))
  private val transitGraph = Graph.Graph(getTimeTransitMap(false))
  
  val stationNames = stationNameMap.keys.toArray sortWith (_ < _)
  
  def findRoutes(sourceName: String, targetName: String): List[List[String]] = {
    val sourceTagList = stationNameMap(sourceName).toList
    val targetTagList = stationNameMap(targetName).toList
    val leastTimeRoutes = sourceTagList.flatMap(timeGraph.find(_, targetTagList).map(trimPath _))
    val leastTransitRoutes = sourceTagList.flatMap(transitGraph.find(_, targetTagList).map(trimPath _))
    
    // Return only unique routes.
    (leastTimeRoutes ::: leastTransitRoutes).toSet.toList
  }
   
  // Return two maps: (stationId -> stationName) and (stationName -> (set of stationId))
  private def getStationIdMap: (mutable.HashMap[String, String],
                                mutable.HashMap[String, Set[String]]) = {
    var idMap = new mutable.HashMap[String, String]()
    var nameMap = new mutable.HashMap[String, Set[String]]()
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
        
      idMap += (id -> name)
      nameMap += (name -> Set(id))
      
      val transits = station \ "transit"
      var ids: mutable.Set[String] = mutable.Set()
      for (transit <- transits) {
        val ids = (transit \ "@ids").text.split(" ")
        for (altId <- ids if altId != id) {
          idMap += (altId -> name)
          nameMap(name) += altId
        }
      }
    }

    (idMap, nameMap)
  }
  
  /** Return time map if `isTimeMap` is true; otherwise transit map where each transit has a very high
   *  penalty.
   */
  private def getTimeTransitMap(isTimeMap: Boolean): mutable.HashMap[(String, String), Int] = {
    val TransitPenalty = 9999
    var map = new mutable.HashMap[(String, String), Int]()

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
        val weight = if (isTimeMap) time else TransitPenalty
        val oneway = (transit \ "@oneway").text
        if (oneway != "") {
          assert(ids.length >= 2 && ((oneway == "source") || (oneway == "target")))
          val first = ids(0)
          for (id <- ids.drop(1))
            if (oneway == "source")
              map += ((first, id) -> weight)
            else
              map += ((id, first) -> weight)
        } else {
        for (i <- 0 until ids.length)
          for (j <- (i + 1) until ids.length) {
            map += ((ids(i), ids(j)) -> weight)
            map += ((ids(j), ids(i)) -> weight)
          }
        }        
      }
    }
    
    map
  }
  
  /** Return true if `thisId` and `thatId` refer to the same station 
   *  (i.e. they can be on the different lines).
   */
  def sameStation(thisId: String, thatId: String): Boolean = {
    // Since sameStation is only called by trimPath, it is an error case
    // thisId == thatId.
    require(thisId != thatId)
    stationIdMap(thisId) == stationIdMap(thatId)
  }
  
  /** Trim the unnecessary transits at the begining and end of the `path`. */
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
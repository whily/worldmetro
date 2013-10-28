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

import org.xmlpull.v1.XmlPullParser
import scala.collection.mutable
import android.app.Activity
import android.util.Log

/**
  *  City class holds all data for a metro network of a city. 
  *  
  *  cityName should be in English; and if there is spaces (e.g. San Francisco), 
  *  use underscore for space. cityName is the base name for the corresponding xml file
  *  (e.g. san_francisco.xml).
  *  */
class City(activity: Activity, cityName: String) {
  private val logTag = "City.scala"
  private val languagePref = Util.getLanguagePref(activity)
  private var transits: List[Transit] = Nil
  
  // (stationId   -> stationName)
  var stationIdMap = new mutable.HashMap[String, String]()
  // (stationName -> (set of stationId)
  var stationNameMap = new mutable.HashMap[String, Set[String]]()
  // (localStationName -> stationName)
  var localStationNameMap = new mutable.HashMap[String, String]()
  
  private var timeMap, transitMap = new mutable.HashMap[(String, String), Int]()
  private var stationLineMap = new mutable.HashMap[String, MetroLine]()
  
  // (placeName -> localStationName)
  var placeLocalStationMap = new mutable.HashMap[String, String]()
  
  init()
  
  private val timeGraph    = Graph.Graph(timeMap)
  private val transitGraph = Graph.Graph(transitMap)

  // Todo: direclty use array concatenation, which seems raising error.
  private val stationNameArray: Array[String] = (stationNameMap.keys.toList ::: placeLocalStationMap.keys.toList).toArray
  val stationNames = stationNameArray sortWith (_ < _)
  
  /** Returns `true` if the display `name` is a station or place name. */
  def stationPlaceExists(name: String) = {
    stationNameMap.contains(name) || placeLocalStationMap.contains(name)
  }
  
  def findRoutes(sourceName: String, targetName: String): List[Route] = {
    /** Return a list of station Ids corresponding the station/place. */
    def tagList(name: String) = {
      val stationName =
        if (placeLocalStationMap.contains(name))
          localStationNameMap(placeLocalStationMap(name))
        else
          name
      stationNameMap(stationName).toList
    }
    val sourceTagList = tagList(sourceName)
    val targetTagList = tagList(targetName)
    val leastTimeRoutes = sourceTagList.flatMap(timeGraph.find(_, targetTagList).map(trimPath _))
    val leastTransitRoutes = sourceTagList.flatMap(transitGraph.find(_, targetTagList).map(trimPath _))
    
    // Return only unique routes.
    val routes = (leastTimeRoutes ::: leastTransitRoutes).distinct.map(new Route(_))
    var leastTime, timeForLeastTransit = routes.head.travelTime
    var transitForLeastTime, leastTransit = routes.head.transitNum
    for (route <- routes.tail) {
      val travelTime = route.travelTime
      val transitNum = route.transitNum
      if (travelTime < leastTime) {
        leastTime = travelTime
        transitForLeastTime = transitNum
      }
      if (transitNum < leastTransit) {
        leastTransit = transitNum
        timeForLeastTransit = travelTime
      }
    }
    
    val filteredRoutes =
      routes.filter(x => ((x.travelTime - leastTime <= 10) && (x.transitNum <= transitForLeastTime)) ||
        ((leastTransit < transitForLeastTime) && (x.transitNum == leastTransit) &&
          (x.travelTime - timeForLeastTransit <= 10)))
    
    filteredRoutes sortWith
    ((x, y) => x.travelTime < y.travelTime ||
      (x.travelTime == y.travelTime && x.transitNum < y.transitNum))
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
  
  /** Trim the unnecessary transits at the beginning and end of the `path`. */
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
  
  /**
    * Class 
    */
  class Route(route: List[String]) {
    val segments = routeSegments(route).map(new Segment(_))
    val transitNum = segments.length - 1
    def travelTime = {
      // Initialize with the wait time of the first segment.
      var time = segments(0).line.waitTime
      var prevStation = route.head
      for (station <- route.tail) {
        time += timeMap((prevStation, station))
        prevStation = station
      }
      time
    }
    
    def linesInfo: String = {
      segments.map(_.lineId).mkString(" > ")
    }
    
    override def toString = travelTime + " min, " + linesInfo
  }

  class Segment (val segment: List[String]) {
    private val stopNum = segment.length - 1
    def line : MetroLine = stationLineMap(segment(0))
    override def toString = {
      "Line " + line.id + " " +
      "towards " + line.headsign(this) + ", " +
      Util.getPluralString(activity, R.plurals.stops, stopNum) + "\n" +
      segment.map(stationIdMap(_)).mkString("→")
    }
    
    def lineId: String = {
      line.id
    }
  }

  // Metro line. Note that we cannot have field "wait" since AnyRef already has such field.
  class MetroLine(val id: String, val color: String, val waitTime: Int, val stations: List[String]) {
    // Return headsign.
    def headsign(segment: Segment): String = ""
  }
  
  // Typical metro line which is linear.
  class MetroLinear(id: String, color: String, waitTime: Int, stations: List[String])
      extends MetroLine(id, color, waitTime, stations) {
    override def headsign(segment: Segment): String = {
      val i = stations.indexOf(segment.segment(0))
      val j = stations.indexOf(segment.segment(1))
      assert (i == j + 1 || j == i + 1)
      val stationId = if (j == i + 1) stations.last else stations.head
      stationIdMap(stationId)
    }
  }
  
  // Metro line which is a ring (loop).
  class MetroRing(id: String, color: String, waitTime: Int, stations: List[String])
      extends MetroLine(id, color, waitTime, stations) {
    override def headsign(segment: Segment): String =
      stationIdMap(segment.segment(1))
  }
  
  private def routeSegments(route: List[String]): List[List[String]] = {
    route match {
      case Nil    => Nil
      case x :: y =>
        routeSegments(y) match {
          case Nil    => List(List(x))
          case u :: v => if (stationLineMap(x).id == stationLineMap(u.head).id)
            (x :: u) :: v
          else
            List(x) :: u :: v
        }
    }
  }

  /** Read XML file and initialize accordingly. */
  private def init() {
    val xpp = activity.getResources.getXml(Util.getXmlId(activity, cityName))
    while (xpp.next() != XmlPullParser.START_TAG) {}
    xpp.require(XmlPullParser.START_TAG, null, "city")
    xpp.nextTag()
    readStations()
    readLines()
    readPlaces()
    initTransits()
    
    /** Read information of metro stations from city XML. */
    def readStations() {
      readElements("stations") {
        val id          = attr("id")
	val englishName = attr("english")
	val localName   = attr("local")
	// Local name should be present even for English-speaking cities (in this case,
	// there is "local" name only.
	assert(!id.isEmpty)
	val name = displayName(localName, englishName)
	
	stationIdMap += (id -> name)
	stationNameMap += (name -> Set(id))
	localStationNameMap += (localName -> name)
	
	if (xpp.next == XmlPullParser.START_TAG) {
	  do {
	    xpp.require(XmlPullParser.START_TAG, null, "transit")
            val ids = attr("ids").split(" ")
            transits = Transit(ids, attr("time").toInt, attr("oneway")) :: transits
	    for (altId <- ids if altId != id) {
	      stationIdMap += (altId -> name)
	      stationNameMap(name) += altId
	    }
	    xpp.nextTag()
	  } while (xpp.next() != XmlPullParser.END_TAG)
	    }
      }
    }
    
    /** Read information of metro lines from city XML. */
    def readLines() {
      readElements("lines") {
	val id = attr("id")
	// Three types
	// - line: default type (no attribute is needed).
	// - ring: bi-directional ring/loop
	// - uniring: uni-directional ring/loop
	val lineType = attr("type")
	val color = attr("color")
	val wait = attr("wait").toInt
	assert(color != "")
	var prevStation = ""
	var index = 0
	var firstStation = ""
	var firstTime = ""
	var stationIds: List[String] = Nil
	
	xpp.nextTag()
	readElements("stations") {
	  val stationId = attr("id")
	  stationIds = stationId :: stationIds
	  if (index == 0) {
	    index = 1
	    prevStation = stationId
	    if (lineType == "ring" || lineType == "uniring") {
	      firstStation = stationId
	      firstTime = attr("time")
	      assert(!firstTime.isEmpty)
	    }
	  } else {
	    val time = attr("time")
	    assert(!time.isEmpty())
	    timeMap += ((prevStation, stationId) -> time.toInt)
	    transitMap += ((prevStation, stationId) -> time.toInt)
	    if (lineType != "uniring") {
	      timeMap += ((stationId, prevStation) -> time.toInt)
	      transitMap += ((stationId, prevStation) -> time.toInt)
	    }
	    prevStation = stationId
	  }
	  xpp.nextTag() // Now we are at </station>
	}
 	
	if (lineType == "ring") {
	  timeMap += ((firstStation, prevStation) -> firstTime.toInt)
	  timeMap += ((prevStation, firstStation) -> firstTime.toInt)
	  transitMap += ((firstStation, prevStation) -> firstTime.toInt)
	  transitMap += ((prevStation, firstStation) -> firstTime.toInt)
	} else if (lineType == "uniring") {
	  timeMap += ((prevStation, firstStation) -> firstTime.toInt)
	  transitMap += ((prevStation, firstStation) -> firstTime.toInt)
	}

        // Build map (stationId -> line)
        stationIds = stationIds.reverse
	val metroLine = lineType match {
	  case "ring" | "uniring" => new MetroRing(id, color, wait, stationIds)
	  case _                   => new MetroLinear(id, color, wait, stationIds)
	}
	for (stationId <- stationIds) stationLineMap += (stationId -> metroLine)
	
	// xpp.nextTag() is not needed since after reading <stations> we're already at </line>.
      }
    }
    
    /** Read information of places from city XML. */
    def readPlaces() {
      readElements("places") {
	val localName = attr("local")
	val englishName = attr("english")
	val stationLocalName = attr("stationLocal")
	val name = displayName(localName, englishName)
	assert(stationLocalName != "")
	
	placeLocalStationMap += (name -> stationLocalName)
	
	xpp.nextTag() // We are now at </place.
      }
    }
    
    /**
      * Read element `names` and the sub elements `name` with `action` performed on
      * each sub element. 
      * 
      * At the beginning, `xpp` is at the <names>. 
      * After the function finishes, `xpp` is at the tag after </names>.
      * 
      * For `action`, `xpp` is at the start tag, and at the end of `action`,
      * 'xpp` should be at the corresponding end tag. 					
      */
    def readElements(names: String)(action: => Unit) {
      assert(names.endsWith("s"))
      val name = names.init
      
      // Log.i(logTag, cityName + ".xml:" + xpp.getLineNumber + " <" + xpp.getName + ">")
      xpp.require(XmlPullParser.START_TAG, null, names)
      while (xpp.next() != XmlPullParser.END_TAG) {
	// Log.i(logTag, cityName + ".xml:" + xpp.getLineNumber + " <" + xpp.getName + ">")
	xpp.require(XmlPullParser.START_TAG, null, name)
	action
      }
      xpp.nextTag()
    }
    
    /** Return attribute value; return "" if attribute does not exist. */
    def attr(name: String) = {
      val result = xpp.getAttributeValue(null, name)
      if (result == null) "" else result
    }
    
    /** Initialize transit weights information. */
    def initTransits() {
      val TransitPenalty = 9999
      
      for (transit <- transits) {
        val ids = transit.ids
        val time = transit.time
        val oneway = transit.oneway
        if (oneway != "") {
          assert(ids.length >= 2 && ((oneway == "source") || (oneway == "target")))
          val first = ids(0)
          for (id <- ids.drop(1))
            if (oneway == "source") {
              timeMap += ((first, id) -> time)
              transitMap += ((first, id) -> TransitPenalty)
            } else {
              timeMap += ((id, first) -> time)
              transitMap += ((id, first) -> TransitPenalty)
            }
        } else {
          for (i <- 0 until ids.length)
            for (j <- (i + 1) until ids.length) {
              timeMap += ((ids(i), ids(j)) -> time)
              timeMap += ((ids(j), ids(i)) -> time)
              transitMap += ((ids(i), ids(j)) -> TransitPenalty)
              transitMap += ((ids(j), ids(i)) -> TransitPenalty)
            }
        }
      }
    }
  }
  
  private def displayName(localName: String, englishName: String): String = {
    // Local name should be present even for English-speaking cities (in this case,
    // there is "local" name only.
    assert(!localName.isEmpty)
    if (languagePref == "both" && !englishName.isEmpty)
      localName + " (" + englishName + ")"
    else if (englishName.isEmpty || (!englishName.isEmpty && languagePref == "local"))
      localName
    else
      englishName
  }
  
  case class Transit(ids: Array[String], time: Int, oneway: String)
}

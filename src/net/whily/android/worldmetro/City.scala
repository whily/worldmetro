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


/** 
 *  City class holds all data for a metro network of a city. 
 *  
 *  cityName should be in English; and if there is spaces (e.g. San Francisco), 
 *  use underscore for space. cityName is the base name for the corresponding xml file
 *  (e.g. san_francisco.xml).
 *  */
class City(activity: Activity, cityName: String) {
	private val languagePref = Util.getLanguagePref(activity) 
	private var transits: List[Transit] = Nil
  
  // (stationId   -> stationName)
  var stationIdMap = new mutable.HashMap[String, String]() 
  // (stationName -> (set of stationId)
  var stationNameMap = new mutable.HashMap[String, Set[String]]()
  
  private var timeMap, transitMap = new mutable.HashMap[(String, String), Int]()
  private var stationLineMap = new mutable.HashMap[String, MetroLine]()
  
  init()
  
  private val timeGraph    = Graph.Graph(timeMap)
  private val transitGraph = Graph.Graph(transitMap)

  val stationNames = stationNameMap.keys.toArray sortWith (_ < _)
  
  def findRoutes(sourceName: String, targetName: String): List[Route] = {
    val sourceTagList = stationNameMap(sourceName).toList
    val targetTagList = stationNameMap(targetName).toList
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
    
    routes.filter(x => ((x.travelTime - leastTime <= 10) && (x.transitNum <= transitForLeastTime)) ||
                       ((leastTransit < transitForLeastTime) && (x.transitNum == leastTransit) && 
                        (x.travelTime - timeForLeastTransit <= 10)))
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
    
    override def toString = travelTime + " min, " + transitNum + " transits"   
  }

  class Segment (val segment: List[String]) {
    private val stopNum = segment.length - 1
    def line : MetroLine = stationLineMap(segment(0))
    override def toString = {
      "Line " + line.id + " " +
        "towards " + line.direction(this) + ", " +
        Util.getPluralString(activity, R.plurals.stops, stopNum) + "\n" +
        segment.map(stationIdMap(_)).mkString("→")
    }
  }

  // Metro line. Note that we cannot have field "wait" since AnyRef already has such field.
  class MetroLine(val id: String, val color: String, val waitTime: Int, val stations: List[String]) {
    // Return direction (a station) name.
    def direction(segment: Segment): String = ""
  }  
  
  // Typical metro line which is linear.
  class MetroLinear(id: String, color: String, waitTime: Int, stations: List[String]) 
    extends MetroLine(id, color, waitTime, stations) {
    override def direction(segment: Segment): String = {
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
    override def direction(segment: Segment): String =
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
	      assert(!id.isEmpty && !(englishName.isEmpty && localName.isEmpty))
	      val name = 
	        if (languagePref == "both" && !localName.isEmpty && !englishName.isEmpty)
	          localName + " (" + englishName + ")"
	        else if ((!localName.isEmpty && englishName.isEmpty) ||
	                 (!localName.isEmpty && !englishName.isEmpty && languagePref == "local"))
	          localName
	        else
	          englishName
	        
	      stationIdMap += (id -> name)
	      stationNameMap += (name -> Set(id))
	      
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
	    	    if (lineType == "ring") {
	    	      firstStation = stationId
	    	      firstTime = attr("time")
	    	      assert(!firstTime.isEmpty)
	    	    }
	    	  } else {
	    	    val time = attr("time")
	    	    assert(!time.isEmpty())
	    	    timeMap += ((prevStation, stationId) -> time.toInt)
	    	    timeMap += ((stationId, prevStation) -> time.toInt)
	    	    transitMap += ((prevStation, stationId) -> time.toInt)
	    	    transitMap += ((stationId, prevStation) -> time.toInt)    	    
	    	    prevStation = stationId
	    	  }
	    	  xpp.nextTag() // Consume </station>
	      }
 	  
	      if (lineType == "ring") {
	        timeMap += ((firstStation, prevStation) -> firstTime.toInt)
	        timeMap += ((prevStation, firstStation) -> firstTime.toInt)  
	        transitMap += ((firstStation, prevStation) -> firstTime.toInt)
	        transitMap += ((prevStation, firstStation) -> firstTime.toInt)          
	      }

        // Build map (stationId -> line)
        stationIds = stationIds.reverse   
	    	val metroLine = lineType match {
	        case "ring" => new MetroRing(id, color, wait, stationIds)
	        case _      => new MetroLinear(id, color, wait, stationIds)
	      }
	    	for (stationId <- stationIds) stationLineMap += (stationId -> metroLine)      
	      
	    	xpp.nextTag() // Consume </line>
	    }    
	  }
	  
	  /** Read information of places from city XML. */
    def readPlaces() { 
	    readElements("places") {
	      
	    }    
	  }
	  
	  /** 
	   * Read element `names` and the sub elements `name` with `action` performed on
	   * each sub element. 
	   * 
	   * At the beginning, `xpp` is at the start tag <names>. 
	   * After the function finishes, `xpp` is at the start tag of the next element.
	   * 
	   * For `action`, `xpp` is at the start tag, and at the end of `action`,
	   * 'xpp` should be at the corresponding end tag. 					*/
	  def readElements(names: String)(action: => Unit) {
	    assert(names.endsWith("s"))
	    val name = names.init
	    
	    println("Line: " + xpp.getLineNumber() + "<" + xpp.getName + ">")
	    xpp.require(XmlPullParser.START_TAG, null, names)
	    while (xpp.next() != XmlPullParser.END_TAG) {
  	    println("Line: " + xpp.getLineNumber() + "<" + xpp.getName + ">")
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
  
  case class Transit(ids: Array[String], time: Int, oneway: String)
}
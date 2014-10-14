#!/bin/sh
exec scala "$0" "$@"
!#

/**
 * Utility to get metro line information.
 *
 * @author  Yujian Zhang <yujian{dot}zhang[at]gmail(dot)com>
 *
 * License: 
 *   GNU General Public License v2
 *   http://www.gnu.org/licenses/gpl-2.0.html
 * Copyright (C) 2013-2014 Yujian Zhang
 */

import java.net._
import java.io._

object Line {
  // Configurable parameters.
  val url = "http://en.wikipedia.org/w/index.php?title=Line_1,_Wuhan_Metro&action=edit&section=2"
  val wikiPrefix = "http://en.wikipedia.org/wiki/"
  // Whether English name of the station should be upper case.
  val upperCaseStationName = false
  // Whether travel time information is available or not.
  val timeInfo = false
  val hasLocalName = true
  val stationInfoIndent = " " * 4
  val lineInfoIndent    = " " * 8
  val reverse = false // Reverse all stations. This is mainly used to align the station ids.

  def main(args: Array[String]) = {
    val str = webPage(url).replace("{BJS line links|}", "") // Remove the confusing |}
    
    // The actual data is betwen |[[ and |}
    val s = str.substring(str.indexOf("|[["), str.indexOf("|}")).trim
    
    // Character | should be escaped. Note that there might be trailing rows which do not contain data.
    val stations = s.split("\\|-")
    
    var stationResults: List[String] = Nil
    for (station <- stations) {
      if (station.indexOf("[[") >= 0) {
        val sa = between(station, "[[", "]]").split("\\|")
        val stationUrl = wikiPrefix + sa(0).split(" ").mkString("_")
        val englishStationName1 = if (sa.length == 1) sa(0) else sa(1)
        val englishStationName = if (upperCaseStationName) englishStationName1.toUpperCase else englishStationName1
        val localName = between(station, ">", "\n")
        
        try {
          val (latitude, longitude) = coordinates(stationUrl) 
      
          val d = stationInfoIndent + "<station id=\"\" local=\"" + localName + 
                  "\" english=\"" + englishStationName + 
                  "\" latitude=\"" + latitude + 
                  "\" longitude=\"" + longitude + "\" />"
          stationResults = d :: stationResults
        } catch {
          case ex: FileNotFoundException => None // The station URL does not exist in Wikipedia.
        }
      }
    }
    val stationShow = if (reverse) stationResults else stationResults.reverse
    println(stationShow.mkString("\n"))
    
    if (timeInfo) {
      println("*" * 66)
    }
  }
  
  // Get coordinates (latitude, longitude) in double given `stationUrl`.
  def coordinates(stationUrl: String): (String, String) = {
    val page = webPage(stationUrl)
    val startIndex = page.indexOf("tools.wmflabs.org")
    if (startIndex >= 0) {
      val endIndex = page.indexOf("\">", startIndex)
      val geohackUrl = "http://" + page.substring(startIndex, endIndex).replace("&amp;", "&")
      val geohackPage = webPage(geohackUrl)
      val latitude = between(geohackPage, "<span class=\"latitude\" title=\"Latitude\">", "</span>")
      val longitude = between(geohackPage, "<span class=\"longitude\" title=\"Longitude\">", "</span>")
      (latitude, longitude)
    } else 
      ("", "")
  }
  
  // Return the substring of `str` between `start` and `end`.
  def between(str: String, start: String, end: String): String = {
    val startIndex = str.indexOf(start) + start.length
    val endIndex = str.indexOf(end, startIndex)
    str.substring(startIndex, endIndex)
  }
  
  // Get the web page in plain text. Each line is separated by "\n".
  def webPage(url: String): String = {
    val connection = (new URL(url)).openConnection
    connection.setRequestProperty("User-Agent", "Mozilla/5.0");
    val is = connection.getInputStream
    scala.io.Source.fromInputStream(is).getLines().mkString("\n")    
  }
}

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

/** City class holds all data for a metro network of a city. */
class City(activity: Activity, id: Int) {
  private val city = xml.XML.load(activity.getResources.openRawResource(id))
  private val stations = city \ "stations" \ "station"
  private val stationNameMap = getStationNameMap
  
  def stationNames: Array[String] = stationNameMap.keys.toArray
  
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
}
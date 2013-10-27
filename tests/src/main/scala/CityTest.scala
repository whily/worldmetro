/**
 * Test for class City.
 *
 * @author  Yujian Zhang <yujian{dot}zhang[at]gmail(dot)com>
 *
 * License: 
 *   GNU General Public License v2
 *   http://www.gnu.org/licenses/gpl-2.0.html
 * Copyright (C) 2013 Yujian Zhang
 */

package net.whily.android.worldmetro.test

import junit.framework.TestCase
import junit.framework.Assert.assertEquals
import junit.framework.Assert.fail
import android.app.Activity
import android.test.ActivityInstrumentationTestCase2
import net.whily.android.worldmetro.SearchActivity
import net.whily.android.worldmetro.City

class CityTestCase extends ActivityInstrumentationTestCase2[SearchActivity](
  "net.whily.android.worldmetro", classOf[SearchActivity]) {
  private var activity: Activity = null
  private var city: City = null
  
  override protected def setUp() {
    super.setUp()
    setActivityInitialTouchMode(false)
    // activity = getActivity
    // city = new City(activity, "munich")  
  }
  
  def testRoute() {
    assertEquals(1, 1)
    //assertEquals(city.stationNames(0), "Hello")
  }
}

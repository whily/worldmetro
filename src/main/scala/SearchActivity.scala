/**
 * Search activity for World Metro.
 *
 * @author  Yujian Zhang <yujian{dot}zhang[at]gmail(dot)com>
 *
 * License:
 *   GNU General Public License v2
 *   http://www.gnu.org/licenses/gpl-2.0.html
 * Copyright (C) 2013-2016 Yujian Zhang
 */

package net.whily.android.worldmetro

import scala.collection.mutable
import android.app.{ActionBar, Activity}
import android.content.Intent
import android.os.Bundle
import android.view.{Menu, MenuItem, MotionEvent, View}
import android.util.{Log, TypedValue}
import android.widget.{AdapterView, ArrayAdapter, AutoCompleteTextView, Button, ExpandableListView, TextView}
import net.whily.scaland.{ExceptionHandler, Util}

class SearchActivity extends Activity with ActionBar.OnNavigationListener {
  private var bar: ActionBar = null
  private var fromEntry: AutoCompleteTextView = null
  private var toEntry: AutoCompleteTextView = null
  private var reverseButton: Button = null
  private var cityInfo: TextView = null
  private var routeList: ExpandableListView = null
  private var city: City = null
  private var cityId: String = ""
  // Please also modify res/values/string.xml for the city messages.
  private val cityIds = Array("athens", "beijing", "munich", "nanjing", "prague", "wuhan")//, "shanghai", "tokyo")
  private var stationIdMap: mutable.HashMap[String, String] = null
  private var stationNameMap: mutable.HashMap[String, Set[String]] = null
  private val ResultSettings = 1
  private val LastDisplayedCity = "last_displayed_city"

  // The key is locale city name (which is displayed, note that this is not the LOCAL name),
  // and the value is the city name (which is used as a unique identifier) as in class City.
  private var cities = new mutable.HashMap[String, String]()
  private var localeCityNames: Array[String] = null

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)

    // Set handler for uncaught exception raised from current activity.
    Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this))

    Misc.setMaterialTheme(this)
    setContentView(R.layout.search)
    setTitle("")

    bar = getActionBar
    bar.setHomeButtonEnabled(true)

    cities = new mutable.HashMap[String, String]()
    localeCityNames = cityIds.map(Util.getString(this, _))
    for (city <- cityIds)
      cities += (Util.getString(this, city) -> city)

    // Show navigation list, which is at the left side of action bar.
    val cityAdapter = new ArrayAdapter[String](this, android.R.layout.simple_spinner_item, localeCityNames)
    cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST)
    bar.setListNavigationCallbacks(cityAdapter, this)

    initWidgets()
    initContents()
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    getMenuInflater().inflate(R.menu.search, menu)

    return super.onCreateOptionsMenu(menu)
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    item.getItemId match {
      case android.R.id.home | R.id.about =>
        startActivity(new Intent(this, classOf[AboutActivity]))
        true

      case R.id.settings =>
        startActivityForResult(new Intent(this, classOf[SettingsActivity]), ResultSettings)
        true
    }
  }

  override def onNavigationItemSelected(itemPosition: Int, itemId: Long): Boolean = {
    if (itemPosition != getLastDisplayedCity) {
      // If we don't set below explicitly, the previous input text still remains
      // when city is changed. This is rather strange as the whole activity is
      // recreated after city switching. Also the two lines below are not effective
      // if put in initContents().
      fromEntry.setText("")
      toEntry.setText("")

      Util.setSharedPref(SearchActivity.this, LastDisplayedCity, itemPosition.toString)
      initContents
    }

    true
  }

  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    super.onActivityResult(requestCode, resultCode, data)
    requestCode match {
      case ResultSettings => recreate() // Trigger to apply new theme.
    }
  }

  private def reverseFromTo () {
    val temp = fromEntry.getText
    fromEntry.setText(toEntry.getText)
    toEntry.setText(temp)
    showRoutes()
    // TODO: focus on the incomplete entry.
  }

  // Initialize the widgets. The contents are initialized in `initContent`.
  private def initWidgets() {
    val editTextSize = Util.getEditTextSize(this)
    val cityInfoTextSize = editTextSize - 2
    fromEntry = findViewById(R.id.from_entry).asInstanceOf[AutoCompleteTextView]
    toEntry = findViewById(R.id.to_entry).asInstanceOf[AutoCompleteTextView]
    fromEntry.setThreshold(1)
    toEntry.setThreshold(1)
    fromEntry.setTextSize(TypedValue.COMPLEX_UNIT_SP, editTextSize)
    toEntry.setTextSize(TypedValue.COMPLEX_UNIT_SP, editTextSize)
    fromEntry.setOnTouchListener(new View.OnTouchListener() {
      override def onTouch(v: View, e: MotionEvent): Boolean = {
      	fromEntry.showDropDown()
      	false
      }
    })
    toEntry.setOnTouchListener(new View.OnTouchListener() {
      override def onTouch(v: View, e: MotionEvent): Boolean = {
      	toEntry.showDropDown()
      	false
      }
    })

    reverseButton = findViewById(R.id.reverse).asInstanceOf[Button]
    reverseButton.setOnClickListener(new View.OnClickListener() {
      override def onClick(v: View) {
        reverseFromTo()
      }
    })

    cityInfo = findViewById(R.id.city_info).asInstanceOf[TextView]
    cityInfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, cityInfoTextSize)

    routeList = findViewById(android.R.id.list).asInstanceOf[ExpandableListView]
  }

  // Initialize the contents of the widgets.
  private def initContents() {
    bar.setSelectedNavigationItem(getLastDisplayedCity)
    cityId = cityIds(getLastDisplayedCity)
    city = new City(this, cityId)
    val stations = city.stationNames
    stationIdMap = city.stationIdMap
    stationNameMap = city.stationNameMap

    fromEntry.setAdapter(new AccentFoldingArrayAdapter(this, R.layout.simple_dropdown_item_1line, stations))
    toEntry.setAdapter(new AccentFoldingArrayAdapter(this, R.layout.simple_dropdown_item_1line, stations))
    fromEntry.setOnItemClickListener(new AdapterView.OnItemClickListener () {
      override def onItemClick(parentView: AdapterView[_], selectedItemView: View, position: Int, id: Long) {
        showRoutes()
      }
    })
    toEntry.setOnItemClickListener(new AdapterView.OnItemClickListener () {
      override def onItemClick(parentView: AdapterView[_], selectedItemView: View, position: Int, id: Long) {
        showRoutes()
      }
    })

    cityInfo.setVisibility(View.VISIBLE)
    cityInfo.setText(Util.getString(this, "general_message") + "\n" +
                     Util.getString(this, cityId + "_message"))
    routeList.setVisibility(View.GONE)
  }

  private def showRoutes() {
    // TODO: check the entry text is actually can be filtered by the corresponding
    // adapters.
    val fromStationPlace = fromEntry.getText.toString
    val toStationPlace   = toEntry.getText.toString
    if (city.stationPlaceExists(fromStationPlace) &&
        city.stationPlaceExists(toStationPlace) &&
        fromStationPlace != toStationPlace) {
      routeList.setVisibility(View.VISIBLE)
      cityInfo.setVisibility(View.GONE)
      val routes = city.findRoutes(fromStationPlace, toStationPlace)
      val groupArray = routes.map(_.toString)
      val childArray = routes.map(_.segments.map(_.toString))
      routeList.setAdapter(new ExpandableListAdapter(this, groupArray, childArray))

      // Hide input method window.
      Util.hideSoftInput(this, fromEntry)
      Util.hideSoftInput(this, toEntry)

      // If there is only one route shown, expand it directly.
      if (routes.length == 1)
        routeList.expandGroup(0)
    }
  }

  private def getLastDisplayedCity =
    Util.getSharedPref(this, LastDisplayedCity, "0").toInt
}

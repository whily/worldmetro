/**
 * Search activity for World Metro.
 *
 * @author  Yujian Zhang <yujian{dot}zhang[at]gmail(dot)com>
 *
 * License: 
 *   GNU General Public License v2
 *   http://www.gnu.org/licenses/gpl-2.0.html
 * Copyright (C) 2013 Yujian Zhang
 */

package net.whily.android.worldmetro

// Using mutable.HashMap might improve performance. However, there is 
// runtime error "NoSuchMethod" when calling HashMap.keys
import scala.collection.immutable.HashMap
import android.app.{ActionBar, Activity}
import android.content.Intent
import android.os.Bundle
import android.view.{Menu, MenuItem, MotionEvent, View}
import android.util.{Log, TypedValue}
import android.widget.{AdapterView, ArrayAdapter, AutoCompleteTextView, Spinner}

class SearchActivity extends Activity with ActionBar.OnNavigationListener {
  private var fromEntry: AutoCompleteTextView = null
  private var toEntry: AutoCompleteTextView = null
  private var fromSelectedPosition = -1
  private var toSelectedPosition = -1
  private var city: City = null
  private var citySpinner: Spinner = null
  private val ResultSettings = 1
  private val LastDisplayedCity = "last_displayed_city"
  
  // The key is locale city name (which is displayed, note that this is not the LOCAL name),
  // and the value is the city name (which is used as a unique identifier) as in class City.
  private var cities = new HashMap[String, String]()
  private var localeCityNames: Array[String] = null
  
  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    Util.setHoloTheme(this)
    setContentView(R.layout.search)
    setTitle("")
    
    val bar = getActionBar
    bar.setHomeButtonEnabled(true)
      
    val cityIds = Array("beijing", "munich", "shanghai", "tokyo")
    cities = new HashMap[String, String]()
    localeCityNames = cityIds.map(Util.getString(this, _))
    for (city <- cityIds) 
      cities += (Util.getString(this, city) -> city)
      
    // Show navigation list, which is at the left side of action bar.
    val cityAdapter = new ArrayAdapter[String](this, android.R.layout.simple_spinner_item, localeCityNames) 
    cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST)
    bar.setListNavigationCallbacks(cityAdapter, this)
    bar.setSelectedNavigationItem(getLastDisplayedCity)      
                     
    city = new City(this, cityIds(getLastDisplayedCity))  
    val stations = city.stationNames
    val stationIdMap = city.getStationIdMap
    
    val editTextSize = Util.getEditTextSize(this)
    fromEntry = findViewById(R.id.from_entry).asInstanceOf[AutoCompleteTextView]
    toEntry = findViewById(R.id.to_entry).asInstanceOf[AutoCompleteTextView]
    fromEntry.setThreshold(1)
    toEntry.setThreshold(1)
    fromEntry.setTextSize(TypedValue.COMPLEX_UNIT_SP, editTextSize)
    toEntry.setTextSize(TypedValue.COMPLEX_UNIT_SP, editTextSize)
    fromEntry.setAdapter(new AccentFoldingArrayAdapter(this, R.layout.simple_dropdown_item_1line, stations))
    toEntry.setAdapter(new AccentFoldingArrayAdapter(this, R.layout.simple_dropdown_item_1line, stations))
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
    fromEntry.setOnItemClickListener(new AdapterView.OnItemClickListener () {
      override def onItemClick(parentView: AdapterView[_], selectedItemView: View, position: Int, id: Long) {
        fromSelectedPosition = position
        if (toSelectedPosition >= 0) showRoute()
      }    
    })  
    toEntry.setOnItemClickListener(new AdapterView.OnItemClickListener () {
      override def onItemClick(parentView: AdapterView[_], selectedItemView: View, position: Int, id: Long) {
        toSelectedPosition = position
        if (fromSelectedPosition >= 0) showRoute()
      }    
    })           
    
    def showRoute() {
      Util.toast(SearchActivity.this, city.findRoute(fromEntry.getText.toString, 
        toEntry.getText.toString).map(stationIdMap(_)).mkString("->"))
      
    }
  }
   
  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    getMenuInflater().inflate(R.menu.search, menu)
    
    return super.onCreateOptionsMenu(menu)
  }  
  
  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    item.getItemId match {
      case android.R.id.home | R.id.about => {
        startActivity(new Intent(this, classOf[AboutActivity]))
        true
      }
      case R.id.settings => { 
        startActivityForResult(new Intent(this, classOf[SettingsActivity]), ResultSettings)
        true
      }
    }
  }
  
  override def onNavigationItemSelected(itemPosition: Int, itemId: Long): Boolean = {
    if (itemPosition != getLastDisplayedCity) {
      Util.setSharedPref(SearchActivity.this, LastDisplayedCity, itemPosition.toString)
      recreate
    }    
    
    true
  }
  
  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    super.onActivityResult(requestCode, resultCode, data)
    requestCode match {
      case ResultSettings => recreate() // Trigger to apply new theme.
    }
  } 
  
  private def getLastDisplayedCity = 
    Util.getSharedPref(this, LastDisplayedCity, "0").toInt
}

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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.{Menu, MenuItem, MotionEvent, View}
import android.widget.{AdapterView, ArrayAdapter, AutoCompleteTextView, Spinner}

import android.util.Log

class SearchActivity extends Activity {
  private var fromEntry: AutoCompleteTextView = null
  private var toEntry: AutoCompleteTextView = null
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
    getActionBar.setHomeButtonEnabled(true)
      
    val cityIds = Array("beijing", "munich", "shanghai", "tokyo")
    Log.d("City", "Before")
    cities = new HashMap[String, String]()
    localeCityNames = for (city <- cityIds) yield Util.getString(this, city)
    Log.d("City", "One")
    for (city <- cityIds) 
      cities += (Util.getString(this, city) -> city)
    Log.d("City", "Two")
                     
    city = new City(this, cityIds(getLastDisplayedCity))  
    val stations = city.stationNames
    
    fromEntry = findViewById(R.id.from_entry).asInstanceOf[AutoCompleteTextView]
    toEntry = findViewById(R.id.to_entry).asInstanceOf[AutoCompleteTextView]
    fromEntry.setThreshold(1)
    toEntry.setThreshold(1)
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
  }
  
  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    getMenuInflater().inflate(R.menu.search, menu)
    citySpinner = menu.findItem(R.id.city_spinner).getActionView.asInstanceOf[Spinner]
    val cityAdapter = new ArrayAdapter[String](this, android.R.layout.simple_spinner_item, localeCityNames) 
    cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    citySpinner.setAdapter(cityAdapter)
    citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener () {
      override def onItemSelected(parentView: AdapterView[_], selectedItemView: View, position: Int, id: Long) {
        if (position != getLastDisplayedCity) {
          Util.setSharedPref(SearchActivity.this, LastDisplayedCity, position.toString)
          recreate
        }
      }
 
      override def onNothingSelected(parentView: AdapterView[_]) {
        // Do nothing.
      }      
    })
    citySpinner.setSelection(getLastDisplayedCity)
    
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
  
  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    super.onActivityResult(requestCode, resultCode, data)
    requestCode match {
      case ResultSettings => recreate() // Trigger to apply new theme.
    }
  } 
  
  private def getLastDisplayedCity = 
    Util.getSharedPref(this, LastDisplayedCity, "0").toInt
}

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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.{Menu, MenuItem, MotionEvent, View}
import android.widget.{ArrayAdapter, AutoCompleteTextView}

class SearchActivity extends Activity {
  private var fromEntry: AutoCompleteTextView = null
  private var toEntry: AutoCompleteTextView = null
  private var city: City = null
  private val ResultSettings = 1  
  
  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    Util.setHoloTheme(this)
    setContentView(R.layout.search)
    getActionBar.setHomeButtonEnabled(true)
      
    city = new City(getResources.openRawResource(R.raw.beijing))  
    val stations = city.stationNames
    
    fromEntry = findViewById(R.id.from_entry).asInstanceOf[AutoCompleteTextView]
    toEntry = findViewById(R.id.to_entry).asInstanceOf[AutoCompleteTextView]
    fromEntry.setThreshold(1)
    toEntry.setThreshold(1)
    fromEntry.setAdapter(new AccentFoldingArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, stations))
    toEntry.setAdapter(new AccentFoldingArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, stations))
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
    return true
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
}

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

class SearchActivity extends Activity {
  private var fromEntry: AutoCompleteTextView = null
  private var toEntry: AutoCompleteTextView = null
  private var city: City = null
  private var citySpinner: Spinner = null
  private val ResultSettings = 1
  
  // The key is locale city name (which is displayed, note that this is not the LOCAL name),
  // and the value is the city name (which is used as a unique identifier) as in class City.
  private var cities = new HashMap[String, String]()
  
  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    Util.setHoloTheme(this)
    setContentView(R.layout.search)
    setTitle("")
    getActionBar.setHomeButtonEnabled(true)
      
    val cityIds = Array("beijing", "munich")
    cities = new HashMap[String, String]()
    for (city <- cityIds) 
      cities += (Util.getString(this, city) -> city)
              
    city = new City(this, "munich")  
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
    val localeCityNames = cities.keys.toArray
    val cityAdapter = new ArrayAdapter[String](this, android.R.layout.simple_spinner_item, localeCityNames) 
    cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    citySpinner.setAdapter(cityAdapter)
    citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener () {
      override def onItemSelected(parentView: AdapterView[_], selectedItemView: View, position: Int, id: Long) {
        // TODO
      }
 
      override def onNothingSelected(parentView: AdapterView[_]) {
        // Do nothing.
      }      
    })
    
    return super.onCreateOptionsMenu(menu)
  }  
 /* 

    cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener () {
        public void onCheckedChanged(CompoundButton buttonView, 
                                     boolean isChecked) {
          if (isChecked) {
            rl.setVisibility(View.VISIBLE);
            entry.requestFocus();
          }
          else {
            rl.setVisibility(View.GONE);
          }
        }
      });
          
        //添加事件Spinner事件监听    
        spinner.setOnItemSelectedListener(new SpinnerSelectedListener());  
          
        //设置默认值  
        spinner.setVisibility(View.VISIBLE);  
          
    }  
      
    //使用数组形式操作  
    class SpinnerSelectedListener implements OnItemSelectedListener{  
  
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,  
                long arg3) {  
            view.setText("你的血型是："+m[arg2]);  
        }  
  
        public void onNothingSelected(AdapterView<?> arg0) {  
        }  
    }     */
  
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
}

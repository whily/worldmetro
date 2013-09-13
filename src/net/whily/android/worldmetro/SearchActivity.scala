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
import android.view.{MenuItem, MotionEvent, View}
import android.widget.{ArrayAdapter, AutoCompleteTextView}

class SearchActivity extends Activity {
  private[this] var fromEntry: AutoCompleteTextView = null
  private[this] var toEntry: AutoCompleteTextView = null
  
  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    Util.setHoloTheme(this)
    setContentView(R.layout.search)
    getActionBar.setHomeButtonEnabled(true)
    
    fromEntry = findViewById(R.id.from_entry).asInstanceOf[AutoCompleteTextView]
    toEntry = findViewById(R.id.to_entry).asInstanceOf[AutoCompleteTextView]
    fromEntry.setThreshold(1)
    toEntry.setThreshold(1)
    val stations = Array("Huilongguan", "Zhichunlu", "Xizhimeng", "Life Science Museum", "Musée d'Orsay")
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
  
  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    item.getItemId match {
      // App icon in action bar clicked; go home.
      case android.R.id.home => {
        val intent = new Intent(this, classOf[MainActivity])
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        true
      }
    }
  }
}

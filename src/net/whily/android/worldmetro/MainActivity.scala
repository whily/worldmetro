package net.whily.android.worldmetro
 
import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.app.Activity
import android.view.MenuItem
import android.view.Menu
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.GridView
 
class MainActivity extends Activity with AdapterView.OnItemClickListener {
  private val items = 
    Array("Beijing", "Shanghai", "Guangzhou", "Hong Kong",
          "London", "Paris", "Berlin", "Munich", "Seoul", "Tokyo");
  private[this] var grid: GridView = null;
  
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    grid = findViewById(R.id.city_grid).asInstanceOf[GridView]
    grid.setAdapter(new ArrayAdapter[String](this, R.layout.city, items))
    grid.setOnItemClickListener(this)
  }
 
  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    getMenuInflater().inflate(R.menu.main, menu)
    return true
  }
  
  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    item.getItemId match {
      case R.id.about    => startActivity(new Intent(this, classOf[AboutActivity])); true
      case R.id.settings => startActivity(new Intent(this, classOf[SettingsActivity])); true
      case _             => super.onOptionsItemSelected(item)
    }
  }
  
  def onItemClick(parent: AdapterView[_], v: View, position: Int, id: Long) {    
  }
}

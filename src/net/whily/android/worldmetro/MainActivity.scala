package net.whily.android.worldmetro
 
import android.app.{ActionBar, Activity}
import android.content.{Context, Intent}
import android.os.Bundle
import android.view.{Menu, MenuItem, View}
import android.widget.{AdapterView, ArrayAdapter, GridView}
 
class MainActivity extends Activity with AdapterView.OnItemClickListener {
  private val items = 
    Array("Beijing", "Shanghai", "Guangzhou", "Hong Kong",
          "London", "Paris", "Berlin", "Munich", "Seoul", "Tokyo")
  private[this] var grid: GridView = null
  private val ResultSettings = 1
  
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    Util.setHoloTheme(this)      
    setContentView(R.layout.activity_main)
    getActionBar.setHomeButtonEnabled(true)
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
      case android.R.id.home | R.id.about => {
        startActivity(new Intent(this, classOf[AboutActivity]))
        true
      }
      case R.id.settings => { 
        startActivityForResult(new Intent(this, classOf[SettingsActivity]), ResultSettings)
        true
      }
      case _ => super.onOptionsItemSelected(item)
    }
  }
  
  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    super.onActivityResult(requestCode, resultCode, data)
    requestCode match {
      case ResultSettings => recreate() // Trigger to apply new theme.
    }
  }
  
  def onItemClick(parent: AdapterView[_], v: View, position: Int, id: Long) {     
    startActivity(new Intent(this, classOf[SearchActivity]))
  }
}

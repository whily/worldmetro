package net.whily.android.worldmetro
 
import android.os.Bundle
import android.app.Activity
import android.view.Menu
 
class MainActivity extends Activity {
  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
  }
 
  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    getMenuInflater().inflate(R.menu.main, menu)
    return true
  }
}

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
import android.os.Bundle

class SearchActivity extends Activity {
  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    Util.setHoloTheme(this)
    setContentView(R.layout.search)
  }
}


/**
 * About activity for World Metro.
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
import android.webkit.WebView

class AboutActivity extends Activity {
  private[this] var browser: WebView = null

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle);
    setContentView(R.layout.about)

    browser = findViewById(R.id.webkit).asInstanceOf[WebView]
    browser.loadData(getString(R.string.about_html), "text/html", "UTF-8")
  }
}


/**
 * Settings activity for World Metro.
 *
 * @author  Yujian Zhang <yujian{dot}zhang[at]gmail(dot)com>
 *
 * License: 
 *   GNU General Public License v2
 *   http://www.gnu.org/licenses/gpl-2.0.html
 * Copyright (C) 2013 Yujian Zhang
 */

package net.whily.android.worldmetro;

import android.os.Bundle
import android.preference.PreferenceActivity

class SettingsActivity extends PreferenceActivity {
  override def onCreate(savedInstanceState: Bundle) {
    // For PreferenceActivity, setTheme should proceed super.oCreate. 
    // See http://stackoverflow.com/questions/11751498/how-to-change-preferenceactivity-theme
    Util.setHoloTheme(this) 
    super.onCreate(savedInstanceState)
    
    // Load the preferences from an XML resource
    addPreferencesFromResource(R.xml.settings)
  }
}

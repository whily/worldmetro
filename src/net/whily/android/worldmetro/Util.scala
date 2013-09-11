/**
 * Utilities.
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
import android.content.SharedPreferences
import android.preference.PreferenceManager

object Util {
  /** Set Holo Light/Dark theme according to preference value. */
  def setHoloTheme(activity: Activity) {
    val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
    val theme: Int = if (prefs.getString("theme_preference", "0").toInt == 0)
    			             android.R.style.Theme_Holo_Light_DarkActionBar
    			           else
    			             android.R.style.Theme_Holo
    activity.setTheme(theme)    
  }
}
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
  /** 
   *  Return string value of the corresponding key; return default if not found. 
   *  
   *  @param activity is used to get the shared preference object.
   */
  def getSharedPref(activity: Activity, key: String, default: String) = 
    PreferenceManager.getDefaultSharedPreferences(activity).getString(key, default)
    
  /** Return the theme preference in {0, 1}. */
  def getTheme(activty: Activity) = getSharedPref(activty, "theme_preference", "0").toInt
    
  /** Set Holo Light/Dark theme according to preference value. */
  def setHoloTheme(activity: Activity) {
    val theme: Int = 
      if (getTheme(activity) == 0)
      	android.R.style.Theme_Holo
      else
    	  android.R.style.Theme_Holo_Light_DarkActionBar
    activity.setTheme(theme)    
  }
}
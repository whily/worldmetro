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
  def getThemePref(activty: Activity) = getSharedPref(activty, "theme_preference", "0").toInt
  
  /** Return the language preference. */
  def getLanguagePref(activity: Activity) = 
    getSharedPref(activity, "language_preference", "local")
    
  /** Set Holo Light/Dark theme according to preference value. */
  def setHoloTheme(activity: Activity) {
    val theme: Int = 
      if (getThemePref(activity) == 0)
      	android.R.style.Theme_Holo
      else
    	  android.R.style.Theme_Holo_Light_DarkActionBar
    activity.setTheme(theme)    
  }
  
  /** Return id (like R.raw.name). 
   *  
   *  @param activity: it is needed to access global variable Resources.
   */
  def getRawId(activity: Activity, name: String) =
    getId(activity, name, "raw")

  /** Return string value correponding to R.string.name. 
   *  
   *  @param activity: it is needed to access global variable Resources.
   */    
  def getString(activity: Activity, name: String) =
    activity.getResources.getString(getStringId(activity, name))

  /** Return id (like R.string.name). 
   *  
   *  @param activity: it is needed to access global variable Resources.
   */    
  def getStringId(activity: Activity, name: String) =
    getId(activity, name, "string")    

  /** Core function used by getRawId() or getStringId(). 
   *  
   *  @param defType indicates e.g. "raw" or "string". For details, see
   *  android.content.res.Resources.getIdentifier().
   */   
  def getId(activity: Activity, name: String, defType: String) = {
    activity.getResources.getIdentifier(name, defType, 
      activity.getApplicationContext.getPackageName)
  }
}
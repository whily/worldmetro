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
import android.content.{Context, SharedPreferences}
import android.preference.PreferenceManager
import android.widget.Toast

object Util {
  def toast (context: Context, text: String) {
    Toast.makeText(context, text, Toast.LENGTH_LONG).show()
  }
  
  /** 
   *  Return string value of the corresponding key; return default if not found. 
   *  
   *  @param activity is used to get the shared preference object.
   */
  def getSharedPref(activity: Activity, key: String, default: String) = 
    PreferenceManager.getDefaultSharedPreferences(activity).getString(key, default)
    
  /** 
   *  Set (key, value) to the shared preference object. 
   *  
   *  @value String value
   *  @param activity is used to get the shared preference object.
   */
  def setSharedPref(activity: Activity, key: String, value: String) {
    val editor = PreferenceManager.getDefaultSharedPreferences(activity).edit()
    editor.putString(key, value)
    editor.commit()
  }
    
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
    	  android.R.style.Theme_Holo_Light
    activity.setTheme(theme)    
  }
  
  /**
   * Return plural string.
   * 
   * @param activity: it is needed to access global variable Resources.
   */
  def getPluralString(activity: Activity, id: Int, quantity: Int) = {
    val quantityInteger: Integer = quantity
    activity.getResources.getQuantityString(id, quantity, quantityInteger)  
  }
  
  /** 
   *  Return id (like R.raw.name). 
   *  
   *  @param activity: it is needed to access global variable Resources.
   */
  def getRawId(activity: Activity, name: String) =
    getId(activity, name, "raw")

  /** 
   *  Return string value corresponding to R.string.name. 
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
  
  /** Return text size for Edit component in unit of sp. */
  def getEditTextSize(activity: Activity) = {
    getSharedPref(activity, "text_size_preference", "1") match {
      case "0" => 12
      case "1" => 15
      case "2" => 18
    }    
  }
}
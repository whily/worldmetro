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
import net.whily.scaland.Util

object Misc {
  /** Set Material Light/Dark theme according to preference value. */
  def setMaterialTheme(activity: Activity) {
    val theme: Int = 
      if (Util.getThemePref(activity) == 0)
      	R.style.AppTheme_Dark
      else
    	R.style.AppTheme_Light
    activity.setTheme(theme)    
  }
}

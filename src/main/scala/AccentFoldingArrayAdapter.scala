/**
 * Class AccentFoldingArrayAdapter.
 *
 * @author  Yujian Zhang <yujian{dot}zhang[at]gmail(dot)com>
 *
 * License:
 *   GNU General Public License v2
 *   http://www.gnu.org/licenses/gpl-2.0.html
 * Copyright (C) 2013-2015 Yujian Zhang
 */

package net.whily.android.worldmetro

import java.text.Normalizer
import android.content.Context
import net.whily.scaland.FilterArrayAdapter

/**
 * Filter the string by ignoring the accents.
 */
class AccentFoldingArrayAdapter(context: Context, textViewResourceId: Int, objects: Array[String])
  extends FilterArrayAdapter(context, textViewResourceId, objects) {

  /** Return an accent-folded string, i.e. in lower case and no accents. */
  override def normalize(s: String): String = {
    val t = Normalizer.normalize(s.toLowerCase, Normalizer.Form.NFD)
    // Remove according to http://en.wikipedia.org/wiki/Diacritical_marks.
    // We simply remove any Unicode character between \u0300 to \u036F.
    t.filter((x: Char) => (x < '\u0300') || (x > '\u036f'))
  }
}

/**
 * Class AccentFoldingArrayAdapter.
 *
 * @author  Yujian Zhang <yujian{dot}zhang[at]gmail(dot)com>
 *
 * License:
 *   GNU General Public License v2
 *   http://www.gnu.org/licenses/gpl-2.0.html
 * Copyright (C) 2013 Yujian Zhang
 */

package net.whily.android.worldmetro

import java.text.Normalizer
import android.content.Context
import android.widget.{ ArrayAdapter, Filterable, Filter }

/**
 * Filter the string by ignoring the accents.
 */
class AccentFoldingArrayAdapter(context: Context, textViewResourceId: Int, objects: Array[String])
  extends ArrayAdapter[String](context, textViewResourceId, objects) with Filterable {

  /** Return an accent-folded string, i.e. in lower case and no accents. */
  private def accentFolding(s: String): String = {
    val t = Normalizer.normalize(s.toLowerCase, Normalizer.Form.NFD)
    // Remove according to http://en.wikipedia.org/wiki/Diacritical_marks.
    // We simply remove any Unicode character between \u0300 to \u036F.
    t.filter((x: Char) => (x < '\u0300') || (x > '\u036f'))
  }

  /* Keep the original data and corresponding accent-folded version. */
  private val foldedPair =
    for (s <- objects) yield (s, accentFolding(s))

  /* The actual data displayed. */
  private var data: Array[String] = objects

  private var filter: Filter = null

  override def getCount(): Int = data.length
  override def getItem(index: Int): String = data(index)

  override def getFilter(): Filter = {
    if (filter == null) filter = new CustomFilter()
    filter
  }

  private class CustomFilter extends Filter {
    override def performFiltering(constraint: CharSequence): Filter.FilterResults = {
      var filterResults = new Filter.FilterResults
      if (constraint == null || constraint.length == 0) {
        filterResults.values = objects
        filterResults.count = objects.length
      } else {
        val query = accentFolding(constraint.toString)
        val filteredData = for (item <- foldedPair if item._2.contains(query))
          yield item._1
        filterResults.values = filteredData
        filterResults.count = filteredData.length
      }
      filterResults
    }

    override def publishResults(constraint: CharSequence, results: Filter.FilterResults) {
      data = results.values.asInstanceOf[Array[String]]
      if (results != null && results.count > 0) {
        notifyDataSetChanged()
      } else {
        notifyDataSetInvalidated()
      }
    }
  }
}

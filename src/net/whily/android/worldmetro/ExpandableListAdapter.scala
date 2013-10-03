/**
 * Class ExpandableListAdapter.
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
import android.view.{Gravity, View, ViewGroup}
import android.widget.{AbsListView, BaseExpandableListAdapter, TextView}

/**
 * Expandable ListView adapter to show routes.
 */
class ExpandableListAdapter(activity: Activity, groupArray: List[String], childArray: List[List[String]]) extends BaseExpandableListAdapter {
  override def getChild(groupPosition: Int, childPosition: Int): AnyRef = 
    childArray(groupPosition)(childPosition)

  override def getChildId(groupPosition: Int, childPosition: Int): Long = childPosition

  override def getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean,
                            convertView: View, parent: ViewGroup) =
    getGenericView(childArray(groupPosition)(childPosition))

  override def getChildrenCount(groupPosition: Int) = childArray(groupPosition).length
   
  override def getGroup(groupPosition: Int): AnyRef = getGroup(groupPosition)

  override def getGroupCount(): Int = groupArray.length

  override def getGroupId(groupPosition: Int): Long = groupPosition

  override def getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View,
                            parent: ViewGroup): View =
    getGenericView(groupArray(groupPosition))

  override def hasStableIds(): Boolean = false

  override def isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true

  private def getGenericView(string: String): TextView = {
    val layoutParams: AbsListView.LayoutParams = 
      new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 
                                   ViewGroup.LayoutParams.WRAP_CONTENT)
    val textView = new TextView(activity)
    textView.setLayoutParams(layoutParams)
    textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT)
    textView.setPadding(40, 0, 0, 0)
    textView.setText(string)
    textView
  }
}

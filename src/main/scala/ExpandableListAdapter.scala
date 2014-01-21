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
import android.content.Context
import android.graphics.{Canvas, Color, Paint}
import android.view.{Gravity, View, ViewGroup}
import android.widget.{AbsListView, BaseExpandableListAdapter, LinearLayout, TextView}

class MetroLineView(context: Context) extends View(context) {
  override protected def onDraw(canvas: Canvas) {
    val paint = new Paint()
    paint.setAntiAlias(true)
    paint.setStyle(Paint.Style.FILL)
    canvas.drawText("test", 1, 1, paint)
  }
}

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
                            parent: ViewGroup): View = {
    val layout = new LinearLayout(activity)
    val layoutParams: AbsListView.LayoutParams = 
      new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 
                                   ViewGroup.LayoutParams.WRAP_CONTENT)
    layout.setLayoutParams(layoutParams)
    layout.setOrientation(LinearLayout.HORIZONTAL)

    layout.addView(getGenericView(groupArray(groupPosition)))
    layout.addView(new MetroLineView(activity))
    layout
  }

  override def hasStableIds(): Boolean = false

  override def isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true

  private def getGenericView(string: String): TextView = {
    val layoutParams: AbsListView.LayoutParams = 
      new AbsListView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 
                                   ViewGroup.LayoutParams.WRAP_CONTENT)
    val textView = new TextView(activity)
    textView.setLayoutParams(layoutParams)
    textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT)
    textView.setPadding(70, 0, 0, 0)
    textView.setText(string)
    textView.setTextSize(Util.getEditTextSize(activity))
    textView
  }
}

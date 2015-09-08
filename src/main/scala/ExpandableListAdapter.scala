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
import android.graphics.{Canvas, Color, Paint, Rect}
import android.view.{Gravity, View, ViewGroup}
import android.view.View.MeasureSpec
import android.widget.{AbsListView, BaseExpandableListAdapter, LinearLayout, TextView}
import net.whily.scaland.Util

class MetroLineView(context: Context, textSizeSp: Int, info: String) extends View(context) {
  val layoutParams =
    new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
      ViewGroup.LayoutParams.WRAP_CONTENT)
  setLayoutParams(layoutParams)

  // Measure text width/height.
  private val textBounds = new Rect()  

  val paint = new Paint()
  paint.setAntiAlias(true)
  paint.setStyle(Paint.Style.STROKE)
  paint.setColor(Color.BLUE)
  val textSizePx = Util.dp2px(textSizeSp, context)
  paint.setTextSize(textSizePx)

  override protected def onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    val text = info
    paint.getTextBounds(text, 0, text.length(), textBounds)    
    canvas.drawText(text, 1, canvas.getHeight() / 2.0f - textBounds.exactCenterY(), paint)
  }

  // From http://stackoverflow.com/questions/12266899/onmeasure-custom-view-explanation
  // This function is necessary otherwise nothing will be shown.
  override protected def onMeasure (widthMeasureSpec: Int, heightMeasureSpec: Int) {
    val desiredWidth = 100
    val desiredHeight = 100

    val widthMode = MeasureSpec.getMode(widthMeasureSpec)
    val widthSize = MeasureSpec.getSize(widthMeasureSpec)
    val heightMode = MeasureSpec.getMode(heightMeasureSpec)
    val heightSize = MeasureSpec.getSize(heightMeasureSpec)

    val width = widthMode match {
      case MeasureSpec.EXACTLY => widthSize  // Must be this size
      case MeasureSpec.AT_MOST => Math.min(desiredWidth, widthSize)
      case _ => desiredWidth
    }

    val height = heightMode match {
      case MeasureSpec.EXACTLY => heightSize  // Must be this size
      case MeasureSpec.AT_MOST => Math.min(desiredHeight, heightSize)
      case _ => desiredHeight
    }

    // MUST CALL THIS.
    setMeasuredDimension(width, height)
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
   
  override def getGroup(groupPosition: Int): AnyRef = groupArray(groupPosition)

  override def getGroupCount(): Int = groupArray.length

  override def getGroupId(groupPosition: Int): Long = groupPosition

  override def getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View,
                            parent: ViewGroup): View = {
    val layout = new LinearLayout(activity)
    val layoutParams =
      new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 
        ViewGroup.LayoutParams.WRAP_CONTENT)
    layout.setLayoutParams(layoutParams)
    layout.setOrientation(LinearLayout.HORIZONTAL)

    layout.addView(new MetroLineView(activity, Util.getEditTextSize(activity),
      groupArray(groupPosition)))
    layout
  }

  override def hasStableIds(): Boolean = false

  override def isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true

  private def getGenericView(string: String): TextView = {
    val layoutParams = 
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

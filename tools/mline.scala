#!/bin/sh
exec scala "$0" "$@"
!#

/**
 * Utility to get metro line information.
 *
 * @author  Yujian Zhang <yujian{dot}zhang[at]gmail(dot)com>
 *
 * License: 
 *   GNU General Public License v2
 *   http://www.gnu.org/licenses/gpl-2.0.html
 * Copyright (C) 2013 Yujian Zhang
 */

import java.net._
import java.io._

object Line {
  // Configurable parameters.
  val url = "http://en.wikipedia.org/w/index.php?title=Line_9,_Beijing_Subway&action=edit&section=2"
  // End of configurable parameters.

  def main(args: Array[String]) = {
    val is = (new URL(url)).openConnection.getInputStream
    println(scala.io.Source.fromInputStream(is).getLines().mkString("\n"))
  }
}

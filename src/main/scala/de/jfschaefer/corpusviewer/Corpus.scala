package de.jfschaefer.corpusviewer

import java.io.Reader
import java.util

import de.jfschaefer.corpusviewer.preview.{PreviewGroup, Slider}


import scalafx.scene.Group

import scala.collection.JavaConversions._


/*
   Both, the Corpus itself, and the GUI component for scrolling through it
 */

class Corpus(iterator: java.util.Iterator[de.up.ling.irtg.corpus.Instance]) extends Group {
  /*
     STEP 1: LOAD CORPUS
   */

  //val iws: Array[InstanceWrapper] = new Array(iterator.size)
  val iws : util.ArrayList[InstanceWrapper]= new util.ArrayList[InstanceWrapper]()

  var index = 0

  for (instance <- iterator) {
    val iw = new InstanceWrapper(instance)
    iw.index = index + 1   //Starting from 1, not 0
    //iws(index) = iw
    iws.add(iw)
    iw.preview = Configuration.visualizationFactory.getPreview(iw)
    if (index == 0) {
      iw.corpusOffsetStart = 0.5 * Configuration.previewMargin
    } else {
      iw.corpusOffsetStart = iws(index - 1).corpusOffsetEnd + Configuration.previewMargin
    }
    iw.corpusOffsetEnd = iw.corpusOffsetStart + iw.preview.getHeight // iw.preview.boundsInLocal.value.getHeight
    index += 1
  }

  val lastIndex = index - 1

  println("Done generating visualizations")


  /*
     STEP 2: GENERATE GUI
   */

  val slider = new Slider {
    layoutX = Configuration.windowMargin
    layoutY = Configuration.windowMargin
    minHeight <== Main.stage.height - 2*Configuration.windowMargin
    maxHeight <== Main.stage.height - 2*Configuration.windowMargin
    rangeStart.set(0d)
    rangeEnd.set(iws(lastIndex).corpusOffsetEnd + 0.5 * Configuration.previewMargin)
  }

  children.add(slider)
  val offset = slider.value

  val previewGroup = new PreviewGroup(this)
  children.add(previewGroup)

  val trash = new Trash
  children.add(trash)


  def getNextIndex(pos: Double): Option[Int] = {
    // some kind of a binary search, returns the index of the instance corresponding to pos, or the first instance
    // after pos, if it falls into a gap
    // It's easy to find a more efficient search (as the distribution is very predictable), but that's not urgent
    var left = 0
    var right = lastIndex
    while (left != right) {
      val mid : Int = (left + right) / 2
      if (iws(mid).corpusOffsetStart <= pos) {
        if (iws(mid).corpusOffsetEnd >= pos) {
          return Some(mid)  //Found it
        } else {
          left = mid + 1
        }
      } else {
        right = mid
      }
    }
    if (iws(left).corpusOffsetEnd < pos) {
      assert(left == lastIndex)
      None
    }
    else if (iws(left).corpusOffsetStart > pos) {
      Some(left)
    } else {
      Some(left)
    }
  }

  //returns the index corresponding to the corpus offset pos
  def getIndex(pos: Double): Option[Int] = {
    getNextIndex(pos) match {
      case Some(i) =>
        if (iws(i).corpusOffsetEnd > pos && iws(i).corpusOffsetStart < pos) {
          Some(i)
        } else None
      case None => None
    }
  }
}

package de.jfschaefer.corpusviewer

import java.io.Reader
import java.util

import de.jfschaefer.corpusviewer.preview.{PreviewGroup, Slider}
import de.jfschaefer.corpusviewer.visualization.Displayable

import de.up.ling.irtg.algebra.graph.GraphAlgebra
import de.up.ling.irtg.InterpretedTreeAutomaton
import de.up.ling.irtg.algebra.{StringAlgebra, Algebra, SetAlgebra}
import de.up.ling.irtg.corpus.Instance

import scalafx.scene.Group
import scalafx.Includes._

import scala.collection.JavaConversions._


/*
   Both, the Corpus itself, and the GUI component for scrolling through it
 */

class Corpus(reader: Reader) extends Group {
  /*
     STEP 1: LOAD CORPUS
   */
  val algebraMap : java.util.Map[String, Algebra[_]] = new util.HashMap()
  algebraMap.put("string", new StringAlgebra)
  algebraMap.put("graph", new GraphAlgebra)
  algebraMap.put("set", new SetAlgebra)
  val irtg = InterpretedTreeAutomaton.forAlgebras(algebraMap)
  val corpus = de.up.ling.irtg.corpus.Corpus.readCorpus(reader, irtg)

  println("Loaded " + corpus.getNumberOfInstances + " instances")

  println("Generating preview visualizations")

  val instances: Array[Instance] = new Array(corpus.getNumberOfInstances)
  val instanceStartPositions: Array[Double] = new Array(corpus.getNumberOfInstances)
  val instanceEndPositions: Array[Double] = new Array(corpus.getNumberOfInstances)
  val instancePreviews: Array[Displayable] = new Array(corpus.getNumberOfInstances)

  var index = 0;

  for (instance <- corpus.iterator) {
    instances(index) = instance
    instancePreviews(index) = Configuration.visualizationFactory.getRootVisualization(instance)
    if (index == 0) {
      instanceStartPositions(index) = 0.5 * Configuration.previewMargin
    } else {
      instanceStartPositions(index) = instanceEndPositions(index - 1) + Configuration.previewMargin
    }
    instanceEndPositions(index) = instanceStartPositions(index) + instancePreviews(index).boundsInLocal.value.getHeight
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
    rangeEnd.set(instanceEndPositions(lastIndex) + 0.5 * Configuration.previewMargin)
  }

  children.add(slider)
  val offset = slider.value

  val previewGroup = new PreviewGroup(this)
  children.add(previewGroup)


  def getNextIndex(pos: Double): Option[Int] = {
    // some kind of a binary search, returns the index of the instance corresponding to pos, or the first instance
    // after pos, if it falls into a gap
    // It's easy to find a more efficient search (as the distribution is very predictable), but that's not urgent
    var left = 0
    var right = lastIndex
    while (left != right) {
      val mid : Int = (left + right) / 2
      if (instanceStartPositions(mid) <= pos) {
        if (instanceEndPositions(mid) >= pos) {
          return Some(mid)  //Found it
        } else {
          left = mid + 1
        }
      } else {
        right = mid
      }
    }
    if (instanceEndPositions(left) < pos) {
      assert(left == lastIndex)
      None
    }
    else if (instanceStartPositions(left) > pos) {
      Some(left)
    } else {
      Some(left)
    }
  }

  //returns the index corresponding to the corpus offset pos
  def getIndex(pos: Double): Option[Int] = {
    getNextIndex(pos) match {
      case Some(i) =>
        if (instanceEndPositions(i) > pos && instanceStartPositions(i) < pos) {
          Some(i)
        } else None
      case None => None
    }
  }
}

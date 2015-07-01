package de.jfschaefer.corpusviewer.preview

import de.jfschaefer.corpusviewer.visualization.Displayable
import de.jfschaefer.corpusviewer.{Corpus, Configuration, Main}

import scalafx.beans.property.{ReadOnlyDoubleProperty, ReadOnlyBooleanProperty, DoubleProperty}
import scalafx.scene.Group
import scalafx.Includes._

class PreviewGroup(corpus: Corpus) extends Group {
  val xOffset = Configuration.sliderWidth + 2 * Configuration.windowMargin

  var draggedGraph : Option[Displayable] = None

  val height: ReadOnlyDoubleProperty = Main.stage.height

  corpus.offset onChange {
    update()
  }

  height onChange {
    update()
  }

  // Update the preview images on the screen
  def update(): Unit = {
    // Due to the desired generality (arbitrary scaling function, varying heights of previews within one corpus),
    // this code is rather tricky, especially, because certain things have multiple representations
    // Therefore the following prefix conventions will hopefully help a bit
    //   c_*  -> pixel offset in corpus listing
    //   f_*  -> function object
    //   i_*  -> index in corpus
    //   n_*  -> normalized c_*, i.e. c_* - c_top
    //   p_*  -> pixel offset on screen
    //   s_*  -> scaled position (y axis scaled to [-1, 1], which is the f_* input format

    children.clear()

    val f_scaling = Configuration.previewScaling

    val p_totalHeight = height.value
    val c_totalHeight = p_totalHeight / Configuration.previewScale //* f_scaling.reciprocalIntegralFromMinusOne(1)
    val c_center = corpus.offset.value
    val p_center = 0.5 * p_totalHeight
    //val c_bottom = c_center + 2 * (p_totalHeight - p_center) * (f_scaling.reciprocalIntegral(1) - f_scaling.reciprocalIntegral(0))
    val c_bottom = c_center + p_center / Configuration.previewScale
    //val c_top = c_center - 2 * p_center * f_scaling.reciprocalIntegralFromMinusOne(0)
    val c_top = c_center - p_center / Configuration.previewScale
    var i_top = 0
    corpus.getNextIndex(c_top) match {
      case None =>
        if (corpus.lastIndex == -1) {
          System.err.println("de.jfschaefer.corpusviewer.preview.PreviewGroup: No instances loaded");
          return
        }
        else if (c_top > corpus.instanceEndPositions(corpus.lastIndex)) i_top = corpus.lastIndex //let's keep it simple
        else {
          System.err.println("de.jfschaefer.corpusviewer.preview.PreviewGroup: ...Corpus.getNextIndex returned None");
          return
        }
      case Some(i) => i_top = i
    }
    if (i_top > 0) i_top -= 1

    // iterate over all the nodes to be displayed
    var i_it = i_top
    while (i_it <= corpus.lastIndex && corpus.instanceStartPositions(i_it) < c_bottom) {
      val node = corpus.instancePreviews(i_it)
      val c_nodeHeight = corpus.instanceEndPositions(i_it) - corpus.instanceStartPositions(i_it)
      val c_nodeCenter = corpus.instanceStartPositions(i_it) + 0.5 * c_nodeHeight
      val s_nodeCenter = 2 * (c_nodeCenter - c_top)/c_totalHeight - 1
      val scaling = f_scaling.function(s_nodeCenter) * Configuration.previewScale
      node.scaleX = scaling
      node.scaleY = scaling
      val p_yCenter = p_totalHeight * f_scaling.normalizedIntegral(s_nodeCenter) //* Configuration.previewScale
      //val p_yCenter = p_totalHeight * f_scaling.reciprocalIntegralFromMinusOne(s_nodeCenter) / f_scaling.reciprocalIntegralFromMinusOne(1)
      val p_yTop = p_yCenter - 0.5 * c_nodeHeight * scaling
      children.add(node)
      node.translateX = node.translateX.value + xOffset - node.boundsInParent.value.getMinX + (    // need some correction in case object hadn't been visible - no clue why
            if (node.boundsInParent.value.getMinX == 0.0) 0.5 * (node.boundsInParent.value.getWidth - node.boundsInLocal.value.getWidth())  else 0)
      node.translateY = node.translateY.value + p_yTop - node.boundsInParent.value.getMinY
      i_it += 1
    }
  }
}

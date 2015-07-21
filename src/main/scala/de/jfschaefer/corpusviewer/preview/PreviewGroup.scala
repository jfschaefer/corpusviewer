package de.jfschaefer.corpusviewer.preview

import de.jfschaefer.corpusviewer.visualization.Displayable
import de.jfschaefer.corpusviewer.{Corpus, Configuration, Main}

import scalafx.beans.property.ReadOnlyDoubleProperty
import scalafx.scene.Group
import scalafx.scene.input.{MouseEvent, ScrollEvent}
import scalafx.scene.shape.Rectangle
import scalafx.Includes._

class PreviewGroup(corpus: Corpus) extends Group {
  // Due to the desired generality (arbitrary scaling function, varying heights of previews within one corpus),
  // this code is rather tricky, especially, because certain things have multiple representations
  // Therefore, the following prefix conventions will hopefully help a bit
  //   c_*  -> pixel offset in corpus listing
  //   f_*  -> function object
  //   i_*  -> index in corpus
  //   n_*  -> normalized c_*, i.e. c_* - c_top
  //   p_*  -> pixel offset on screen
  //   s_*  -> scaled position (y axis scaled to [-1, 1], which is the f_* input format

  val xOffset = Configuration.sliderWidth + 2 * Configuration.windowMargin
  val height: ReadOnlyDoubleProperty = Main.stage.height

  val f_scaling = Configuration.previewScaling

  // Just in order to capture all the scroll events
  val bgRect = new Rectangle {
    width = Configuration.previewSectionWidth - xOffset
    height <== PreviewGroup.this.height
    styleClass.add("previewGroup_bgRect")
  }

  bgRect.layoutX = xOffset
  children.add(bgRect)

  var p_totalHeight = 0d
  var c_totalHeight = 0d
  var c_center = 0d
  var p_center = 0d
  var c_bottom = 0d
  var c_top = 0d

  def updateVars():Unit = {
    p_totalHeight = height.value
    c_totalHeight = p_totalHeight / Configuration.previewScale //* f_scaling.reciprocalIntegralFromMinusOne(1)
    c_center = corpus.offset.value
    p_center = 0.5 * p_totalHeight
    c_bottom = c_center + p_center / Configuration.previewScale
    c_top = c_center - p_center / Configuration.previewScale
  }


  corpus.offset onChange {
    update()
  }

  height onChange {
    update()
  }

  // Update the preview images on the screen
  def update(): Unit = {
    updateVars()

    children.clear()
    children.add(bgRect)

    var i_top = 0
    corpus.getNextIndex(c_top) match {
      case None =>
        if (corpus.lastIndex == -1) {
          System.err.println("de.jfschaefer.corpusviewer.preview.PreviewGroup: No instances loaded")
          return
        }
        else if (c_top > corpus.iws(corpus.lastIndex).corpusOffsetEnd) i_top = corpus.lastIndex //let's keep it simple
        else {
          System.err.println("de.jfschaefer.corpusviewer.preview.PreviewGroup: ...Corpus.getNextIndex returned None")
          return
        }
      case Some(i) => i_top = i
    }
    //if (i_top > 0) i_top -= 1

    // iterate over all the nodes to be displayed
    var i_it = i_top
    while (i_it <= corpus.lastIndex && corpus.iws(i_it).corpusOffsetStart < c_bottom) {
      val node = corpus.iws(i_it).preview
      val c_nodeHeight = corpus.iws(i_it).corpusOffsetEnd - corpus.iws(i_it).corpusOffsetStart
      val c_nodeCenter = corpus.iws(i_it).corpusOffsetStart + 0.5 * c_nodeHeight
      val s_nodeCenter = 2 * (c_nodeCenter - c_top)/c_totalHeight - 1
      val scaling = f_scaling.function(s_nodeCenter) * Configuration.previewScale
      node.scale.set(scaling)
      val p_yCenter = p_totalHeight * f_scaling.normalizedIntegral(s_nodeCenter) //* Configuration.previewScale
      //val p_yCenter = p_totalHeight * f_scaling.reciprocalIntegralFromMinusOne(s_nodeCenter) / f_scaling.reciprocalIntegralFromMinusOne(1)
      val p_yTop = p_yCenter - 0.5 * c_nodeHeight * scaling
      children.add(node)

      //THE ETERNAL FIGHT WITH THE X TRANSLATION ... -.-
      //node.translateX = node.translateX.value + xOffset - node.boundsInParent.value.getMinX + (    // need some correction in case object hadn't been visible - no clue why
      //      //if (node.boundsInParent.value.getMinX == 0.0) 1 * (node.boundsInParent.value.getWidth - node.boundsInLocal.value.getWidth)  else 0)
      //  if (node.boundsInParent.value.getMinX == 0.0) 0.5 * node.boundsInParent.value.getWidth - 0.5*node.boundsInLocal.value.getWidth/node.scale.value  else 0)

      // node.translateX = node.translateX.value + xOffset - node.boundsInParent.value.getMinX + (
      //  if (node.boundsInParent.value.getMinX == 0d)  0.5 * (node.boundsInParent.value.getWidth * (node.scale.value - 1)) else 0 )
      //node.translateX = node.translateX.value+ xOffset - node.boundsInParent.value.getMinX
      node.layoutX = xOffset + 0.5 * (node.boundsInParent.value.getWidth - node.boundsInLocal.value.getWidth)


      //node.translateY = node.translateY.value + p_yTop - node.boundsInParent.value.getMinY
      node.layoutY = p_yTop + 0.5 * (node.boundsInParent.value.getHeight - node.boundsInLocal.value.getHeight)
      i_it += 1
    }
  }


  // EVENT HANDLING - scrolling
  var c_prevScrollY = 0d

  // Using the same notation as in update()
  onScrollStarted = { ev: ScrollEvent =>
    updateVars()
    val p_yPos = ev.y
    val s_pos = f_scaling.normalizedIntegralInverse(p_yPos / p_totalHeight)
    c_prevScrollY = (1 + s_pos) * c_totalHeight * 0.5 + c_top
    ev.consume()
  }

  onScroll = { ev: ScrollEvent =>
    updateVars()
    val p_yPos = ev.y
    val s_pos: Double = f_scaling.normalizedIntegralInverse(p_yPos / p_totalHeight)
    val c_newY: Double = (1d + s_pos) * c_totalHeight * 0.5 + c_top
    corpus.offset.set(corpus.offset.value - (c_newY - c_prevScrollY)) // minus as direction reversed
    //c_prevScrollY = c_newY    //don't just enable this line - it's stupid
    ev.consume()
  }

  // EVENT HANDLING - dragging
  var p_dragStart = (0d, 0d)
  var dragInitialScale = 0d
  var p_dragLast = (0d, 0d)
  var draggedNode : Option[Displayable] = None
  onMousePressed = { ev: MouseEvent =>
    p_dragStart = (ev.x, ev.y)
    p_dragLast = (ev.x, ev.y)
    val s_pos: Double = f_scaling.normalizedIntegralInverse(ev.y / p_totalHeight)
    val c_pos: Double = (1d + s_pos) * c_totalHeight * 0.5 + c_top
    corpus.getIndex(c_pos) match {
      case Some(i) =>
        if (!corpus.iws(i).hasIdAssigned) {
          val node = Configuration.visualizationFactory.getVisualization(corpus.iws(i), "overview", None)//getPreview(corpus.iws(i))
          node.scale.set(corpus.iws(i).preview.scale.value)
          dragInitialScale = node.scale.value
          node.layoutX = corpus.iws(i).preview.boundsInParent.value.getMinX
          node.layoutY = corpus.iws(i).preview.boundsInParent.value.getMinY
          draggedNode = Some(node)
          children.add(node)
        }
      case None => draggedNode = None
    }
    ev.consume()
  }

  onMouseDragged = { ev: MouseEvent =>
    draggedNode match {
      case Some(node) =>
        val dragGoalX = Configuration.previewSectionWidth + p_dragLast._1 - node.translateX.value - xOffset
        node.translateX = node.translateX.value + ev.x - p_dragLast._1
        node.translateY = node.translateY.value + ev.y - p_dragLast._2
        if (ev.x < p_dragStart._1) {
          node.scale.set(dragInitialScale)
          node.getIw.releaseId()
        } else if (ev.x > dragGoalX) {
          node.scale.set(Configuration.initialScale)
          node.getIw.assignId()
        } else {
          val scale = dragInitialScale + (Configuration.initialScale - dragInitialScale) *
                                 (ev.x - p_dragStart._1) / (dragGoalX - p_dragStart._1)
          node.scale.set(scale)
          node.getIw.releaseId()
        }
      case None =>
    }
    p_dragLast = (ev.x, ev.y)
    ev.consume()
  }

  onMouseReleased = { ev: MouseEvent =>
    draggedNode match {
      case Some(node) =>
        children.remove(node)
        val dragGoalX = Configuration.previewSectionWidth + p_dragLast._1 - node.translateX.value - xOffset
        if (ev.x > dragGoalX) {
          Main.corpusScene.getChildren.add(node)
          node.enableInteraction()
          node.getIw.assignId()
        } else {
          node.getIw.releaseId()
        }
        draggedNode = None
      case None =>
    }
    ev.consume()
  }
}

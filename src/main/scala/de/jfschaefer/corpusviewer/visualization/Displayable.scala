package de.jfschaefer.corpusviewer.visualization

import de.jfschaefer.corpusviewer.{Configuration, InstanceWrapper, Main}

import scalafx.beans.property.DoubleProperty
import scalafx.scene.Node
import scalafx.scene.shape.{Polygon, Line}
import scala.collection._

// Something that can be "put directly onto the screen", a first-class citizen so to say
trait Displayable extends Node {
  val parentDisplayable: Option[Displayable]
  val scale: DoubleProperty
  def enableInteraction():Unit
  def getIw: InstanceWrapper
  def trash(): Unit

  val locationLines: mutable.Set[Node] = new mutable.HashSet
  def drawLocationLines(): Unit = {
    parentDisplayable match {
      case Some(parent) =>
        removeLocationLines()
        // Step one: Figure out position relative to parent
        var top: Boolean = false
        var left: Boolean = false
        var right: Boolean = false
        var bottom: Boolean = false
        val me_b = parent.localToScene(parent.boundsInLocal.value)
        val parent_b = this.localToScene(boundsInLocal.value)
        if (me_b.getMaxX < parent_b.getMinX) right = true
        if (me_b.getMinX > parent_b.getMaxX) left = true
        if (me_b.getMaxY < parent_b.getMinY) bottom = true
        if (me_b.getMinY > parent_b.getMaxY) top = true

        assert(!(left && right))
        assert(!(top && bottom))
        if (left || right || top || bottom) {
          val line1 = new Line
          val line2 = new Line
          if ((top && left) || (bottom && right)) {
            line1.startX = me_b.getMinX
            line1.startY = me_b.getMaxY
            line1.endX = parent_b.getMinX
            line1.endY = parent_b.getMaxY

            line2.startX = me_b.getMaxX
            line2.startY = me_b.getMinY
            line2.endX = parent_b.getMaxX
            line2.endY = parent_b.getMinY
          } else if ((top && right) || (bottom && left)) {
            line1.startX = me_b.getMaxX
            line1.startY = me_b.getMaxY
            line1.endX = parent_b.getMaxX
            line1.endY = parent_b.getMaxY

            line2.startX = me_b.getMinX
            line2.startY = me_b.getMinY
            line2.endX = parent_b.getMinX
            line2.endY = parent_b.getMinY
          } else if (top) {
            line1.startX = me_b.getMinX
            line1.startY = me_b.getMinY
            line1.endX = parent_b.getMinX
            line1.endY = parent_b.getMaxY

            line2.startX = me_b.getMaxX
            line2.startY = me_b.getMinY
            line2.endX = parent_b.getMaxX
            line2.endY = parent_b.getMaxY
          } else if (bottom) {
            line1.startX = me_b.getMinX
            line1.startY = me_b.getMaxY
            line1.endX = parent_b.getMinX
            line1.endY = parent_b.getMinY

            line2.startX = me_b.getMaxX
            line2.startY = me_b.getMaxY
            line2.endX = parent_b.getMaxX
            line2.endY = parent_b.getMinY
          } else if (right) {
            line1.startX = me_b.getMaxX
            line1.startY = me_b.getMinY
            line1.endX = parent_b.getMinX
            line1.endY = parent_b.getMinY

            line2.startX = me_b.getMaxX
            line2.startY = me_b.getMaxY
            line2.endX = parent_b.getMinX
            line2.endY = parent_b.getMaxY
          } else if (left) {
            line1.startX = me_b.getMinX
            line1.startY = me_b.getMinY
            line1.endX = parent_b.getMaxX
            line1.endY = parent_b.getMinY

            line2.startX = me_b.getMinX
            line2.startY = me_b.getMaxY
            line2.endX = parent_b.getMaxX
            line2.endY = parent_b.getMaxY
          } else {
            System.err.println("de.jfschaefer.corpusviewer.visualization.Displayable.drawLocationLines(): Invalid relative position")
          }
          val polygon = new Polygon {
            points.add(line1.startX.value)
            points.add(line1.startY.value)
            points.add(line1.endX.value)
            points.add(line1.endY.value)
            points.add(line2.endX.value)
            points.add(line2.endY.value)
            points.add(line2.startX.value)
            points.add(line2.startY.value)
            styleClass.clear()
            // style = "-fx-fill: linear-gradient(from " + (100*(parent_b.getMinX + parent_b.getWidth * 0.5)/Main.stage.width.value) + "% " +
            //   (100*(parent_b.getMinY + parent_b.getHeight * 0.5)/Main.stage.height.value) + "% to " + (100*(me_b.getMinX + 0.5 * me_b.getWidth)/Main.stage.width.value) +"% " +
            //   (100*(me_b.getMinY + 0.5 * me_b.getHeight * 0.5)/Main.stage.height.value) + "%, rgba(187, 187, 187, 1.0) 0%, rgba(187, 187, 187, 0.0) 100%);"
            style = "-fx-fill: linear-gradient(from " + (parent_b.getMinX + parent_b.getWidth * 0.5) + "px " +
              (parent_b.getMinY + parent_b.getHeight * 0.5) + "px to " + (me_b.getMinX + 0.5 * me_b.getWidth) +"px " +
              (me_b.getMinY + 0.5 * me_b.getHeight * 0.5) + "px, rgba(187, 187, 187, 0.0) 0%, rgba(187, 187, 187, 1.0) 100%);"
          }
          locationLines.add(line1)
          locationLines.add(line2)
          locationLines.add(polygon)
          //Main.corpusScene.getChildren.add(line1)
          //Main.corpusScene.getChildren.add(line2)
          Main.corpusScene.getChildren.add(polygon)
          parent.drawLocationLines()
          parent.toFront()
          toFront()
        }
      case None =>
    }
  }

  def removeLocationLines(): Unit = {
    for (line <- locationLines) Main.corpusScene.getChildren.removeAll(line)
    locationLines.clear()
    parentDisplayable match {
      case Some(parent) => parent.removeLocationLines()
      case None =>
    }
  }
}

trait RootDisplayable extends Displayable {
  val index: Int

  override def drawLocationLines(): Unit = {
    removeLocationLines()
    val bounds = this.localToScene(boundsInLocal.value)
    val slider = Main.getCorpus.slider
    val topLine = new Line {
      startX = Configuration.windowMargin + Configuration.sliderWidth
      startY = slider.layoutY.value + (getIw.corpusOffsetStart - slider.rangeStart.value) * slider.track.height.value / (slider.rangeEnd.value - slider.rangeStart.value)
      endX = bounds.getMinX
      endY = bounds.getMinY
    }

    val bottomLine = new Line {
      startX = Configuration.windowMargin + Configuration.sliderWidth
      startY = slider.layoutY.value + (getIw.corpusOffsetEnd - slider.rangeStart.value) * slider.track.height.value / (slider.rangeEnd.value - slider.rangeStart.value)
      endX = bounds.getMinX
      endY = bounds.getMaxY
    }

    val polygon = new Polygon {
      points.add(topLine.startX.value)
      points.add(topLine.startY.value)
      points.add(topLine.endX.value)
      points.add(topLine.endY.value)
      points.add(bottomLine.endX.value)
      points.add(bottomLine.endY.value)
      points.add(bottomLine.startX.value)
      points.add(bottomLine.startY.value)
      styleClass.clear()
      styleClass.add("rootLocationPolygon")
    }

    locationLines.add(topLine)
    locationLines.add(bottomLine)
    locationLines.add(polygon)
    //Main.corpusScene.getChildren.add(topLine)
    //Main.corpusScene.getChildren.add(bottomLine)
    Main.corpusScene.getChildren.add(polygon)
  }
}
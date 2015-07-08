package de.jfschaefer.corpusviewer.visualization

import de.jfschaefer.corpusviewer.{Configuration, InstanceWrapper, Main}

import scalafx.beans.property.{BooleanProperty, DoubleProperty}
import scalafx.scene.Node
import scalafx.scene.shape.{Polygon, Line}
import scala.collection._

// Something that can be "put directly onto the screen", a first-class citizen so to say
trait Displayable extends Node {
  val parentDisplayable: Option[Displayable]
  val scale: DoubleProperty
  val isInInitialExpansion: BooleanProperty = new BooleanProperty
  def enableInteraction():Unit
  def getIw: InstanceWrapper
  def trash(): Unit

  var idstyleclass: String = "no_id_assigned"
  def setupStyleStuff(): Unit = {
    styleClass.clear()
    styleClass.add("displayable")
    styleClass.add("no_trash_alert")
    styleClass.add("no_id_assigned")
    onStyleClassIdUpdate()
    getIw.id onChange {
      onStyleClassIdUpdate()
    }

    isInInitialExpansion onChange {
      onStyleClassIdUpdate()
    }

    def onStyleClassIdUpdate(): Unit = {
      while (styleClass.contains(idstyleclass)) styleClass.remove(styleClass.indexOf(idstyleclass))
      idstyleclass = if (isInInitialExpansion.value) "no_id_assigned" else getIw.getStyleClass
      styleClass.add(idstyleclass)
    }
  }

  val locationLines: mutable.Set[Node] = new mutable.HashSet
  def drawLocationLines(): Unit = {
    parentDisplayable match {
      case Some(parent) =>
        removeLocationLines()
        // Step one: Figure out position relative to parent
        /*
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
          val me_padding = 5 * scale.value
          val parent_padding = 5 * parent.scale.value
          if ((top && left) || (bottom && right)) {
            line1.startX = me_b.getMinX + me_padding
            line1.startY = me_b.getMaxY - me_padding
            line1.endX = parent_b.getMinX + parent_padding
            line1.endY = parent_b.getMaxY - parent_padding

            line2.startX = me_b.getMaxX - me_padding
            line2.startY = me_b.getMinY + me_padding
            line2.endX = parent_b.getMaxX - parent_padding
            line2.endY = parent_b.getMinY + parent_padding
          } else if ((top && right) || (bottom && left)) {
            line1.startX = me_b.getMaxX - me_padding
            line1.startY = me_b.getMaxY - me_padding
            line1.endX = parent_b.getMaxX - parent_padding
            line1.endY = parent_b.getMaxY - parent_padding

            line2.startX = me_b.getMinX + me_padding
            line2.startY = me_b.getMinY + me_padding
            line2.endX = parent_b.getMinX + parent_padding
            line2.endY = parent_b.getMinY + parent_padding
          } else if (top) {
            line1.startX = me_b.getMinX + me_padding
            line1.startY = me_b.getMinY + me_padding
            line1.endX = parent_b.getMinX + parent_padding
            line1.endY = parent_b.getMaxY - parent_padding

            line2.startX = me_b.getMaxX - me_padding
            line2.startY = me_b.getMinY + me_padding
            line2.endX = parent_b.getMaxX - parent_padding
            line2.endY = parent_b.getMaxY - parent_padding
          } else if (bottom) {
            line1.startX = me_b.getMinX + me_padding
            line1.startY = me_b.getMaxY - me_padding
            line1.endX = parent_b.getMinX + parent_padding
            line1.endY = parent_b.getMinY + parent_padding

            line2.startX = me_b.getMaxX - me_padding
            line2.startY = me_b.getMaxY - me_padding
            line2.endX = parent_b.getMaxX - parent_padding
            line2.endY = parent_b.getMinY + parent_padding
          } else if (right) {
            line1.startX = me_b.getMaxX - me_padding
            line1.startY = me_b.getMinY + me_padding
            line1.endX = parent_b.getMinX + parent_padding
            line1.endY = parent_b.getMinY + parent_padding

            line2.startX = me_b.getMaxX - me_padding
            line2.startY = me_b.getMaxY - me_padding
            line2.endX = parent_b.getMinX + parent_padding
            line2.endY = parent_b.getMaxY - parent_padding
          } else if (left) {
            line1.startX = me_b.getMinX + me_padding
            line1.startY = me_b.getMinY + me_padding
            line1.endX = parent_b.getMaxX - parent_padding
            line1.endY = parent_b.getMinY + parent_padding

            line2.startX = me_b.getMinX + me_padding
            line2.startY = me_b.getMaxY - me_padding
            line2.endX = parent_b.getMaxX - parent_padding
            line2.endY = parent_b.getMaxY - parent_padding
          } else {
            System.err.println("de.jfschaefer.corpusviewer.visualization.Displayable.drawLocationLines(): Invalid relative position")
          }
         */

        val me_padding = 5 * scale.value
        val parent_padding = 5 * parent.scale.value

        val line1 = new Line
        val line2 = new Line
        val parent_b = parent.boundsInParent.value
        val me_b = boundsInParent.value
        val p_c_x = 0.5 * (parent_b.getMinX + parent_b.getMaxX)
        val p_c_y = 0.5 * (parent_b.getMinY + parent_b.getMaxY)

        val c_c_x = 0.5 * (me_b.getMinX + me_b.getMaxX)
        val c_c_y = 0.5 * (me_b.getMinY + me_b.getMaxY)

        // vector from parent to child
        val deltaX = c_c_x - p_c_x
        val deltaY = c_c_y - p_c_y

        // orthogonal vector (doesn't need to be a unit vector)
        val normX = deltaY
        val normY = -deltaX

        // returns the distance (times the length of the normal vector)
        def getDistance(x: Double, y: Double): Double =
          (x - c_c_x) * normX + (y - c_c_y) * normY

        var parentminx = parent_b.getMinX + parent_padding
        var parentminy = parent_b.getMinY + parent_padding
        var parentmaxx = parent_b.getMinX + parent_padding
        var parentmaxy = parent_b.getMinY + parent_padding
        var parentmindist = getDistance(parentminx + parent_padding, parentminy + parent_padding)
        var parentmaxdist = parentmindist

        def updateParentMinMax(x: Double, y: Double): Unit = {
          val newDist = getDistance(x, y)
          if (newDist < parentmindist) {
            parentmindist = newDist
            parentminx = x
            parentminy = y
          }
          if (newDist > parentmaxdist) {
            parentmaxdist = newDist
            parentmaxx = x
            parentmaxy = y
          }
        }

        updateParentMinMax(parent_b.getMinX + parent_padding, parent_b.getMaxY - parent_padding)
        updateParentMinMax(parent_b.getMaxX - parent_padding, parent_b.getMinY + parent_padding)
        updateParentMinMax(parent_b.getMaxX - parent_padding, parent_b.getMaxY - parent_padding)

        var meminx = me_b.getMinX + me_padding
        var meminy = me_b.getMinY + me_padding
        var memaxx = me_b.getMinX + me_padding
        var memaxy = me_b.getMinY + me_padding
        var memindist = getDistance(meminx + me_padding, meminy + me_padding)
        var memaxdist = memindist

        def updateMeMinMax(x: Double, y: Double): Unit = {
          val newDist = getDistance(x, y)
          if (newDist < memindist) {
            memindist = newDist
            meminx = x
            meminy = y
          }
          if (newDist > memaxdist) {
            memaxdist = newDist
            memaxx = x
            memaxy = y
          }
        }

        updateMeMinMax(me_b.getMinX + me_padding, me_b.getMaxY - me_padding)
        updateMeMinMax(me_b.getMaxX - me_padding, me_b.getMinY + me_padding)
        updateMeMinMax(me_b.getMaxX - me_padding, me_b.getMaxY - me_padding)

        line1.startX = meminx
        line1.startY = meminy
        line1.endX = parentminx
        line1.endY = parentminy

        line2.startX = memaxx
        line2.startY = memaxy
        line2.endX = parentmaxx
        line2.endY = parentmaxy

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
            (me_b.getMinY + 0.5 * me_b.getHeight * 0.5) + "px, rgba(187, 187, 187, 1.0) 0%, rgba(187, 187, 187, 0.2) 100%);"
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
      endX = bounds.getMinX + 5 * scale.value
      endY = bounds.getMinY + 5 * scale.value
    }

    val bottomLine = new Line {
      startX = Configuration.windowMargin + Configuration.sliderWidth
      startY = slider.layoutY.value + (getIw.corpusOffsetEnd - slider.rangeStart.value) * slider.track.height.value / (slider.rangeEnd.value - slider.rangeStart.value)
      endX = bounds.getMinX + 5 * scale.value
      endY = bounds.getMaxY - 5 * scale.value
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
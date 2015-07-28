package de.jfschaefer.corpusviewer.visualization

import scalafx.beans.property.{DoubleProperty, BooleanProperty}
import scalafx.scene.Node
import scalafx.scene.input.{MouseEvent, ScrollEvent, ZoomEvent}
import scalafx.scene.shape.{Line, Polygon}
import scalafx.Includes._
import scala.collection.mutable

import de.jfschaefer.corpusviewer.{Util, InstanceWrapper, Main, Configuration}

/*
    A Displayable is something that can be dragged over the screen.
    It is associated with some corpus instance.
    Displayables have a tree-like hierarchy.

    Most of the code below is for displaying the location lines. Maybe we should put it somewhere else instead.
    Also, the code for drawing the location lines isn't perfect yet - at some point, it should be rewritten.
    I know how it should be implemented - when I have some time, I'll do it.
 */

trait Displayable extends Node {
  val parentDisplayable : Option[Displayable] = None
  val childDisplayables : mutable.Set[Displayable] = new mutable.HashSet
  val isInInitialExpansion = new BooleanProperty()
  val header : Header = null
  def getIw : InstanceWrapper

  def trash(): Unit = {
    removeLocationLines()
    for (child <- childDisplayables) {
      child.trash()
    }
    if (parentDisplayable.isEmpty) getIw.releaseId()
    Main.corpusScene.getChildren.remove(this)
  }

  private var interactionDragStartX = 0d
  private var interactionDragStartY = 0d
  def enableInteraction(): Unit = {
    //onZoom = {ev : ZoomEvent => Util.handleZoom(this, scale)(ev); toFront()}
    onScroll = {ev : ScrollEvent => Util.handleScroll(this)(ev); toFront(); Util.trashStyleUpdate(this, this); drawLocationLines() }

    if (header != null) {
      header.onMousePressed = { ev: MouseEvent =>
        drawLocationLines()
        interactionDragStartX = ev.sceneX
        interactionDragStartY = ev.sceneY
        toFront()
        ev.consume()
      }
      header.onMouseDragged = { ev: MouseEvent =>
        drawLocationLines()
        translateX = translateX.value + ev.sceneX - interactionDragStartX
        translateY = translateY.value + ev.sceneY - interactionDragStartY
        interactionDragStartX = ev.sceneX
        interactionDragStartY = ev.sceneY
        Util.trashStyleUpdate(this, this)
        toFront()
        ev.consume()
      }
      header.onMouseReleased = { ev: MouseEvent =>
        Util.trashIfRequired(this)
        removeLocationLines()
        toFront()
        ev.consume()
      }
      header.onMouseClicked = { ev: MouseEvent =>
        toFront()
        ev.consume()
      }
    }

    onScrollFinished = {ev : ScrollEvent => Util.trashIfRequired(this); toFront(); removeLocationLines() }
  }

  var idstyleclass : String = "no_id_assigned"

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

        val me_padding = 5// * scale.value
        val parent_padding = 5// * parent.scale.value

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
        var parentminy = parent_b.getMinY + 10 + parent_padding
        var parentmaxx = parent_b.getMinX + parent_padding
        var parentmaxy = parent_b.getMinY + 10 + parent_padding
        var parentmindist = getDistance(parentminx + parent_padding, parentminy + 10 + parent_padding)
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
        updateParentMinMax(parent_b.getMaxX - parent_padding, parent_b.getMinY + 10 + parent_padding)
        updateParentMinMax(parent_b.getMaxX - parent_padding, parent_b.getMaxY - parent_padding)

        var meminx = me_b.getMinX + me_padding
        var meminy = me_b.getMinY + 10 + me_padding
        var memaxx = me_b.getMinX + me_padding
        var memaxy = me_b.getMinY + 10 + me_padding
        var memindist = getDistance(meminx + me_padding, meminy + 10 + me_padding)
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
        updateMeMinMax(me_b.getMaxX - me_padding, me_b.getMinY + 10 + me_padding)
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
          style = "-fx-fill: linear-gradient(from " + (parent_b.getMinX + parent_b.getWidth * 0.5) + "px " +
            (parent_b.getMinY + parent_b.getHeight * 0.5) + "px to " + (me_b.getMinX + 0.5 * me_b.getWidth) +"px " +
            (me_b.getMinY + 0.5 * me_b.getHeight * 0.5) + "px, " + Configuration.locationPolygonColor1 + " 0%, " +
            Configuration.locationPolygonColor2 + " 100%);"
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
        removeLocationLines()
        val bounds = this.localToScene(boundsInLocal.value)
        val slider = Main.getCorpus.slider
        val topLine = new Line {
          startX = Configuration.windowMargin + Configuration.sliderWidth
          startY = slider.layoutY.value + (getIw.corpusOffsetStart - slider.rangeStart.value) * slider.track.height.value / (slider.rangeEnd.value - slider.rangeStart.value)
          endX = bounds.getMinX + 5// * scale.value
          endY = bounds.getMinY + 15// * scale.value
        }

        val bottomLine = new Line {
          startX = Configuration.windowMargin + Configuration.sliderWidth
          startY = slider.layoutY.value + (getIw.corpusOffsetEnd - slider.rangeStart.value) * slider.track.height.value / (slider.rangeEnd.value - slider.rangeStart.value)
          endX = bounds.getMinX + 5// * scale.value
          endY = bounds.getMaxY - 5// * scale.value
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
          style = "-fx-fill: linear-gradient(to right, " + Configuration.locationPolygonColor1 + " 0%, " + Configuration.locationPolygonColor2 + " 100%);"
        }

        locationLines.add(topLine)
        locationLines.add(bottomLine)
        locationLines.add(polygon)
        //Main.corpusScene.getChildren.add(topLine)
        //Main.corpusScene.getChildren.add(bottomLine)

        Main.corpusScene.getChildren.add(polygon)
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

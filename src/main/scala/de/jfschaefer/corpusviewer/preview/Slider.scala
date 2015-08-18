package de.jfschaefer.corpusviewer.preview

import de.jfschaefer.corpusviewer.Configuration

import scalafx.beans.property.DoubleProperty
import scalafx.scene.shape.Rectangle
import scalafx.scene.layout.Pane
import scalafx.scene.input.MouseEvent

import scalafx.Includes._

/**A simple slider. The JavaFX slider is really annoying when it comes to resizing, scaling and absolute placement. */

class Slider extends Pane {
  prefWidth = Configuration.sliderWidth
  val rangeStart = DoubleProperty(0d)
  val rangeEnd = DoubleProperty(3d)
  val value = DoubleProperty(0d)

  val length = DoubleProperty(200d)
  length <== height

  val track = new Rectangle {
    width = Configuration.sliderWidth
    height <== length
    styleClass.clear()
    styleClass.add("slider_track")
  }

  val thumbClip = new Rectangle {
    width = Configuration.sliderWidth
  }

  val thumb = new Rectangle {
    width = Configuration.sliderWidth
    height = Configuration.sliderThumbHeight
    styleClass.add("slider_thumb")
    clip = thumbClip
  }

  thumb.layoutY onChange {  //update thumbClip
    thumbClip.layoutY = math.max(0, track.layoutY.value - thumb.layoutY.value)
    thumbClip.height = math.min(thumb.height.value, thumb.height.value + (track.layoutY.value - thumb.layoutY.value)
                                                    + (track.height.value - thumb.height.value))
  }

  children.add(track)
  children.add(thumb)
  updateThumbPos()

  //update whenever something changes
  length onChange updateThumbPos()
  rangeStart onChange updateThumbPos()
  rangeEnd onChange updateThumbPos()
  value onChange updateThumbPos()

  def valueToY(v: Double): Double = {
    val rangeLength = rangeEnd.value - rangeStart.value
    assert(rangeLength > 0d)
    val offset = v - rangeStart.value
    track.height.value / rangeLength * offset + track.layoutY.value
  }

  def yToValue(y: Double): Double = {
    (1 - (track.height.value - track.layoutY.value - y) / track.height.value) * (rangeEnd.value - rangeStart.value)
  }

  def updateThumbPos(): Unit = {
    thumb.layoutY = valueToY(value.value) - 0.5 * thumb.height.value
  }

  onMousePressed = { ev: MouseEvent =>
    value.set(yToValue(ev.y))
  }

  onMouseDragged = { ev: MouseEvent =>
    value.set(yToValue(ev.y))
  }

  onMouseClicked = { ev: MouseEvent =>
    value.set(yToValue(ev.y))
  }
}

package de.jfschaefer.corpusviewer

import scalafx.scene.Node
import scalafx.scene.input.{ZoomEvent, ScrollEvent}

import scalafx.Includes._

object Util {
  def handleZoom(node: Node): ZoomEvent => Unit = {
    ev: ZoomEvent => {
      val bounds = node.boundsInLocal.value

      val localX = ev.x - bounds.getMinX
      val localY = ev.y - bounds.getMinY
      val shiftX = node.scaleX.value * (1 - ev.zoomFactor) * (localX - bounds.getWidth/2)
      val shiftY = node.scaleY.value * (1 - ev.zoomFactor) * (localY - bounds.getHeight/2)

      node.scaleX = node.scaleX.value * ev.zoomFactor
      node.scaleY = node.scaleY.value * ev.zoomFactor

      node.translateX = node.translateX.value + shiftX
      node.translateY = node.translateY.value + shiftY
    }
  }

  def handleScroll(node: Node): ScrollEvent => Unit = {
    ev: ScrollEvent => {
      node.translateX = node.translateX.value + ev.deltaX
      node.translateY = node.translateY.value + ev.deltaY
    }
  }
}

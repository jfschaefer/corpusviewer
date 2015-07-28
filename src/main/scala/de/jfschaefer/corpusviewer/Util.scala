package de.jfschaefer.corpusviewer

import de.jfschaefer.corpusviewer.visualization.Displayable

import scalafx.beans.property.DoubleProperty
import scalafx.scene.Node
import scalafx.scene.input.{ZoomEvent, ScrollEvent}

import java.awt.datatransfer.{StringSelection, Clipboard}
import java.awt.Toolkit

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
      ev.consume()
    }
  }

  def handleZoom(node: Node, scale: DoubleProperty): ZoomEvent => Unit = {
    ev: ZoomEvent => {
      val bounds = node.boundsInLocal.value

      val localX = ev.x - bounds.getMinX
      val localY = ev.y - bounds.getMinY
      val shiftX = scale.value * (1 - ev.zoomFactor) * (localX - bounds.getWidth/2)
      val shiftY = scale.value * (1 - ev.zoomFactor) * (localY - bounds.getHeight/2)

      scale.set(scale.value * ev.zoomFactor)
      scale.set(scale.value * ev.zoomFactor)

      node.translateX = node.translateX.value + shiftX
      node.translateY = node.translateY.value + shiftY
      ev.consume()
    }
  }

  def dispHandleZoom(d : Displayable, content : Node): ZoomEvent => Unit = {
    ev: ZoomEvent => {
      val boundsParent = content.boundsInParent.value
      val boundsLocal = content.boundsInLocal.value

      val xLocal = ev.x - boundsLocal.getMinX - boundsParent.getMinX
      val yLocal = ev.y - boundsLocal.getMinY - boundsParent.getMinY

      val oldWidth = boundsParent.getWidth
      val newWidth = oldWidth * ev.zoomFactor

      val oldHeight = boundsParent.getHeight
      val newHeight = oldHeight * ev.zoomFactor

      val xRatio = xLocal / oldWidth
      val yRatio = yLocal / oldHeight

      val shiftX = (newWidth - oldWidth) * xRatio
      val shiftY = (newHeight - oldHeight) * yRatio

      d.translateX = d.translateX.value - shiftX
      d.translateY = d.translateY.value - shiftY

      content.scaleX = content.scaleX.value * ev.zoomFactor
      content.scaleY = content.scaleY.value * ev.zoomFactor
      content.translateX = content.translateX.value + 0.5 * (newWidth - oldWidth)
      content.translateY = content.translateY.value + 0.5 * (newHeight - oldHeight)

      ev.consume()
    }
  }

  def handleScroll(node: Node): ScrollEvent => Unit = {
    ev: ScrollEvent => {
      node.translateX = node.translateX.value + ev.deltaX
      node.translateY = node.translateY.value + ev.deltaY
      ev.consume()
    }
  }

  def trashStyleUpdate(displayable: Displayable, styleNode: Node):Unit = {
    if (Main.getCorpus.trash.isOverTrash(displayable)) {
      while (styleNode.styleClass.contains("no_trash_alert")) styleNode.styleClass.remove(styleNode.styleClass.indexOf("no_trash_alert"))
      if (!styleNode.styleClass.contains("trash_alert")) styleNode.styleClass.add("trash_alert")
    } else {
      while (styleNode.styleClass.contains("trash_alert")) styleNode.styleClass.remove(styleNode.styleClass.indexOf("trash_alert"))
      if (!styleNode.styleClass.contains("no_trash_alert")) styleNode.styleClass.add("no_trash_alert")
    }
  }

  def trashIfRequired(displayable: Displayable):Unit = {
    if (Main.getCorpus.trash.isOverTrash(displayable)) {
      // displayable.getIw.releaseId()
      // Main.corpusScene.getChildren.remove(displayable)
      displayable.trash()
    }
  }

  def copyIntoClipboard(string : String): Unit = {
    val selection = new StringSelection(string)
    val clipboard : Clipboard = Toolkit.getDefaultToolkit.getSystemClipboard
    clipboard.setContents(selection, null)
  }
}

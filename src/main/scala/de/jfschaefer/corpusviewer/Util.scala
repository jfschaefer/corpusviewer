package de.jfschaefer.corpusviewer

import de.jfschaefer.corpusviewer.visualization.Displayable

import scalafx.beans.property.DoubleProperty
import scalafx.scene.Node
import scalafx.scene.input.{ZoomEvent, ScrollEvent}

import java.awt.datatransfer.{StringSelection, Clipboard}
import java.awt.Toolkit

/** A diverse collection of usefule functions */
object Util {
  /** Creates a function that handles zoom events for some node
    *
    * @param node the node
    * @return the function that handles the events
    */
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

  /** Creates a function that handles zoom events for some node
    *
    * @param node the node
    * @param scale the BooleanProperty to be set instead of the node's scale itself
    * @return the function that handles the events
    */
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

  /** Creates a function handling ZoomEvents for a Displayable
   *
   * @param d the Displayable
   * @param content the node that shall be scaled (we don't scale the entire Displayable)
   * @return the function
   */
  def dispHandleZoom(d : Displayable, content : Node): ZoomEvent => Unit = {
    ev: ZoomEvent => {
      d.toFront()
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

  /** Returns a function handling the ScrollEvents for a Node by translating it accordingly
    *
    * @param node the node
    * @return the function
    */
  def handleScroll(node: Node): ScrollEvent => Unit = {
    ev: ScrollEvent => {
      node.translateX = node.translateX.value + ev.deltaX
      node.translateY = node.translateY.value + ev.deltaY
      ev.consume()
    }
  }

  /** Updates the style of a Displayable depending on whether or not it is over some trash area
   *
   * @param displayable the displayable
   * @param styleNode the node which gets the style
   */
  def trashStyleUpdate(displayable: Displayable, styleNode: Node):Unit = {
    if (Main.getCorpus.trash.isOverTrash(displayable)) {
      while (styleNode.styleClass.contains("no_trash_alert")) styleNode.styleClass.remove(styleNode.styleClass.indexOf("no_trash_alert"))
      if (!styleNode.styleClass.contains("trash_alert")) styleNode.styleClass.add("trash_alert")
    } else {
      while (styleNode.styleClass.contains("trash_alert")) styleNode.styleClass.remove(styleNode.styleClass.indexOf("trash_alert"))
      if (!styleNode.styleClass.contains("no_trash_alert")) styleNode.styleClass.add("no_trash_alert")
    }
  }

  /** Trashes a Displayable, if it is over the trash area
    *
    * @param displayable the displayable
    */
  def trashIfRequired(displayable: Displayable):Unit = {
    if (Main.getCorpus.trash.isOverTrash(displayable)) {
      // displayable.getIw.releaseId()
      // Main.corpusScene.getChildren.remove(displayable)
      displayable.trash()
    }
  }

  /** Copies a String into the clipboard
    *
    * @param string the string
    */
  def copyIntoClipboard(string : String): Unit = {
    val selection = new StringSelection(string)
    val clipboard : Clipboard = Toolkit.getDefaultToolkit.getSystemClipboard
    clipboard.setContents(selection, null)
  }
}

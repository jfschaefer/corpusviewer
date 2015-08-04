package de.jfschaefer.corpusviewer.visualization

import de.jfschaefer.corpusviewer.{Util, Configuration, InstanceWrapper}

import de.up.ling.tree.Tree

import scalafx.scene.layout.Pane
import scalafx.scene.input.ZoomEvent
import scalafx.Includes._

class TreeStringVisualization(iw : InstanceWrapper, parentDisp : Option[Displayable], key : String) extends Pane with Displayable {
  override val parentDisplayable = parentDisp
  override def getIw = iw

  setupStyleStuff()

  val instanceMap = iw.instance.getInputObjects
  assert(instanceMap.containsKey(key))
  val algObj = instanceMap.get(key)
  assert(algObj.isInstanceOf[Tree[String @unchecked]])
  val tree = algObj.asInstanceOf[Tree[String]]

  // HEADER
  val menu = new RadialMenu {
    displayable = Some(TreeStringVisualization.this)
    items = new MenuEntryFunction("Trash", () => trash() )::Nil
  }
  menu.enableInteraction()
  override val header = new Header(iw.index + ". " + key, Some(menu))

  children.add(header)

  // CONTENT
  val treePane = new TreePane(tree)
  children.add(treePane)
  treePane.translateY = header.getHeight

  header.toFront()
  header.headerWidth.set(treePane.getWidth)

  updateSize()

  def updateSize(): Unit = {
    minHeight = treePane.getHeight * treePane.scaleY.value + header.getHeight
    maxHeight = treePane.getHeight * treePane.scaleY.value + header.getHeight
    minWidth = treePane.getWidth * treePane.scaleX.value
    maxWidth = treePane.getWidth * treePane.scaleX.value
    header.headerWidth.set(treePane.getWidth * treePane.scaleX.value)
    treePane.translateY = treePane.translateY.value - treePane.getBoundsInParent.getMinY + header.getHeight
  }


  onZoom = { ev : ZoomEvent => Util.dispHandleZoom(this, treePane)(ev); updateSize()}
}


package de.jfschaefer.corpusviewer.visualization

import de.jfschaefer.corpusviewer.InstanceWrapper

import de.up.ling.irtg.algebra.graph.SGraph

import scalafx.scene.layout.Pane


class GraphVisualization(iw : InstanceWrapper, parentDisp : Option[Displayable], key : String) extends Pane with Displayable {
  override val parentDisplayable = parentDisp
  override def getIw = iw

  scaleX <== scale
  scaleY <== scale

  setupStyleStuff()
  val instanceMap = iw.instance.getInputObjects

  // HEADER
  val menu = new RadialMenu
  menu.enableInteraction()
  val header = new Header(iw.index + ". Graph", Some(menu))
  children.add(header)

  // CONTENT
  assert(instanceMap.containsKey(key))
  val algObj = instanceMap.get(key)
  assert(algObj.isInstanceOf[SGraph])
  val sgraph: SGraph = algObj.asInstanceOf[SGraph]
  val graphpane = new SGraphPane(sgraph)
  graphpane.translateX = 0
  graphpane.translateY = header.getHeight
  children.add(graphpane)

  header.toFront()

  header.headerWidth.set(graphpane.getWidth)
  minWidth = graphpane.getWidth
  maxWidth = graphpane.getWidth
  minHeight = graphpane.getHeight + header.getHeight
}

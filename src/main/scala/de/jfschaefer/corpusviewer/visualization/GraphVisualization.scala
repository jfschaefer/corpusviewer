package de.jfschaefer.corpusviewer.visualization

import de.jfschaefer.corpusviewer.{InstanceWrapper, Util}

import de.up.ling.irtg.algebra.graph.SGraph

import scalafx.scene.layout.Pane


class GraphVisualization(iw : InstanceWrapper, parentDisp : Option[Displayable], key : String) extends Pane with Displayable {
  override val parentDisplayable = parentDisp
  override def getIw = iw

  scaleX <== scale
  scaleY <== scale

  setupStyleStuff()
  val instanceMap = iw.instance.getInputObjects

  assert(instanceMap.containsKey(key))
  val algObj = instanceMap.get(key)
  assert(algObj.isInstanceOf[SGraph])
  val sgraph: SGraph = algObj.asInstanceOf[SGraph]
  val graphpane = new SGraphPane(sgraph)


  // HEADER
  val menu = new RadialMenu {
    items = new MenuEntryFunction("Copy as\nLaTeX", () => {
      Util.copyIntoClipboard(graphpane.getLaTeX())
    })::new MenuEntryFunction("Trash", () => trash() )::Nil
  }
  menu.enableInteraction()
  override val header = new Header(iw.index + ". Graph", Some(menu))
  children.add(header)

  // CONTENT
  graphpane.translateX = 0
  graphpane.translateY = header.getHeight
  children.add(graphpane)

  header.toFront()

  header.headerWidth.set(graphpane.getWidth)
  minWidth = graphpane.getWidth
  maxWidth = graphpane.getWidth
  minHeight = graphpane.getHeight + header.getHeight
}

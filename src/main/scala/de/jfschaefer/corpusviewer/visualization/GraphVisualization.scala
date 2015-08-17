package de.jfschaefer.corpusviewer.visualization

import de.jfschaefer.corpusviewer.{InstanceWrapper, Util}

import de.up.ling.irtg.algebra.graph.SGraph

import scalafx.scene.layout.Pane
import scalafx.scene.input.ZoomEvent
import scalafx.Includes._


class GraphVisualization(iw : InstanceWrapper, parentDisp : Option[Displayable], key : String) extends Pane with Displayable {
  override val parentDisplayable = parentDisp
  override def getIw = iw

  setupStyleStuff()
  val instanceMap = iw.instance.getInputObjects

  assert(instanceMap.containsKey(key))
  val algObj = instanceMap.get(key)
  assert(algObj.isInstanceOf[SGraph])
  val sgraph: SGraph = algObj.asInstanceOf[SGraph]
  var graphpane : SGraphPane = null     //= new SGraphPane(sgraph)


  // HEADER
  val menu = new RadialMenu {
    displayable = Some(GraphVisualization.this)
    items = new NormalMenuEntryFunction("Copy as\nLaTeX", () => {
      Util.copyIntoClipboard(graphpane.getLaTeX())
    })::new NormalMenuEntryFunction("Trash", () => trash() )::
    new MenuEntryToggleFunction("No Bezier", "Bezier", () => {
      graphpane.setBezier(false); graphpane.recreateLayout(); updateSize()
    }, () => {
      graphpane.setBezier(true); graphpane.recreateLayout(); updateSize()
    })::new MenuEntryToggleFunction("Larger", "Smaller", () => {
      graphpane.setLargeLayout(true); graphpane.recreateLGraph(); updateSize()
    }, () => {
      graphpane.setLargeLayout(false); graphpane.recreateLGraph(); updateSize()
    }
    )::new MenuEntryToggleFunction("More\nIterations", "Fewer\nIterations", () => {
      graphpane.setIterations(15000); graphpane.rerunAlgorithm(); updateSize()
    }, () => {
      graphpane.setIterations(4000); graphpane.rerunAlgorithm(); updateSize()
    })::new NormalMenuEntryFunction("Rerun", () => {
      graphpane.rerunAlgorithm(); updateSize()
    })::new NormalMenuEntryFunction("Rerun\n10 times", () => {
        graphpane.rerunAlgorithm10Times(); updateSize()
      })::Nil
    }
  menu.enableInteraction()
  override val header = new Header(iw.getIDForUser + ". Graph", Some(menu))
  children.add(header)

  drawGraphPane()

  // CONTENT

  /** Creates a new [[de.jfschaefer.corpusviewer.visualization.SGraphPane]] with the current properties
    * and replaces the old one with it
    */
  def drawGraphPane(): Unit = {
    if (graphpane != null) children.remove(graphpane)
    graphpane = new SGraphPane(sgraph, bezier = true, iterations = 4000)
    graphpane.translateX = 0
    graphpane.translateY = header.getHeight
    children.add(graphpane)

    header.toFront()

    updateSize()
  }

  /** Updates the size of this GraphVisualization */
  def updateSize(): Unit = {
    header.headerWidth.set(graphpane.getWidth * graphpane.scaleX.value)
    minWidth = graphpane.getWidth * graphpane.scaleX.value
    maxWidth = graphpane.getWidth * graphpane.scaleX.value
    minHeight = graphpane.getHeight * graphpane.scaleY.value + header.getHeight
    maxHeight = graphpane.getHeight * graphpane.scaleY.value + header.getHeight
  }

  onZoom = { ev : ZoomEvent => Util.dispHandleZoom(this, graphpane)(ev); updateSize() }
}

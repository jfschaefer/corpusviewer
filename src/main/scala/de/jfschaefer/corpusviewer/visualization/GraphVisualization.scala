package de.jfschaefer.corpusviewer.visualization

import de.jfschaefer.corpusviewer.{InstanceWrapper, Util}

import de.up.ling.irtg.algebra.graph.SGraph

import scalafx.scene.layout.Pane
import scalafx.scene.input.ZoomEvent
import scalafx.Includes._


class GraphVisualization(iw : InstanceWrapper, parentDisp : Option[Displayable], key : String) extends Pane with Displayable {
  override val parentDisplayable = parentDisp
  override def getIw = iw

  scaleX  // <== scale
  scaleY  // <== scale

  setupStyleStuff()
  val instanceMap = iw.instance.getInputObjects
  var bezier : Boolean = true
  var alternative: Boolean = true

  assert(instanceMap.containsKey(key))
  val algObj = instanceMap.get(key)
  assert(algObj.isInstanceOf[SGraph])
  val sgraph: SGraph = algObj.asInstanceOf[SGraph]
  var graphpane : SGraphPane = null     //= new SGraphPane(sgraph)


  // HEADER
  val menu = new RadialMenu {
    displayable = Some(GraphVisualization.this)
    items = new MenuEntryFunction("Copy as\nLaTeX", () => {
      Util.copyIntoClipboard(graphpane.getLaTeX())
    })::new MenuEntryFunction("Trash", () => trash() )::
      new MenuEntryFunction("Sugiyama\nAlternative", () => {
        alternative = !alternative
        drawGraphPane()
      })::new MenuEntryFunction("Bezier\nNo Bezier", () => {
        bezier = !bezier
        drawGraphPane()
      })::new MenuEntryFunction("Redraw", () => drawGraphPane())::Nil
    }
  menu.enableInteraction()
  override val header = new Header(iw.getIDForUser + ". Graph", Some(menu))
  children.add(header)

  drawGraphPane()

  // CONTENT

  def drawGraphPane(): Unit = {
    if (graphpane != null) children.remove(graphpane)
    graphpane = new SGraphPane(sgraph, bezier=bezier, alternative=alternative)
    graphpane.translateX = 0
    graphpane.translateY = header.getHeight
    children.add(graphpane)

    header.toFront()

    updateSize()
  }

  def updateSize(): Unit = {
    header.headerWidth.set(graphpane.getWidth * graphpane.scaleX.value)
    minWidth = graphpane.getWidth * graphpane.scaleX.value
    maxWidth = graphpane.getWidth * graphpane.scaleX.value
    minHeight = graphpane.getHeight * graphpane.scaleY.value + header.getHeight
    maxHeight = graphpane.getHeight * graphpane.scaleY.value + header.getHeight
  }

  onZoom = { ev : ZoomEvent => Util.dispHandleZoom(this, graphpane)(ev); updateSize() }
}

package de.jfschaefer.corpusviewer.visualization

import de.jfschaefer.corpusviewer.sugiyama_layout.SugiyamaLayout
import de.jfschaefer.corpusviewer.{Main, InstanceWrapper, Util, Configuration}
import de.up.ling.irtg.algebra.StringAlgebra
import edu.uci.ics.jung.algorithms.layout._
import edu.uci.ics.jung.visualization.DefaultVisualizationModel

import scalafx.beans.property.DoubleProperty
import scalafx.geometry.Point2D
import scalafx.scene.layout.Pane
import scalafx.scene.Group
import scalafx.scene.shape.{Polygon, Rectangle, Line}
import scalafx.scene.text.Text
import scalafx.scene.input.{MouseEvent, ZoomEvent, ScrollEvent}
import scalafx.Includes._
import scala.collection.JavaConversions._
import edu.uci.ics.jung.graph._
import de.up.ling.irtg.algebra.graph.{GraphEdge, GraphNode, SGraph}

import scalafx.scene.transform.Rotate

class GraphVisualization(iw: InstanceWrapper, key: String, parentD: Displayable) extends Pane with Displayable {
  override val parentDisplayable = Some(parentD)
  override val scale = new DoubleProperty
  override def getIw = iw
  scale.set(1d)

  setupStyleStuff()

  scaleX <== scale
  scaleY <== scale

  val menu = new RadialMenu
  menu.enableInteraction()
  val header = new Header(getIw.index.toString + ". Graph", menu)
  header.translateY = 10
  children.add(header)


  // generate Graph
  assert(iw.instance.getInputObjects.containsKey(key))
  val algObj = iw.instance.getInputObjects.get(key)
  assert(algObj.isInstanceOf[SGraph])
  val sgraph: SGraph = algObj.asInstanceOf[SGraph]

  val graphPane = new Pane {
    //style = "-fx-background-color: rgba(0, 0, 255, 0.5);"
  }
  val graphGroup = new GraphVisualizationGraphGroup(sgraph, graphPane, 1.0)

  graphPane.children.add(graphGroup)
  children.add(graphPane)
  graphPane.translateY = header.boundsInParent.value.getMaxY

  def updateSize(): Unit = {
    minWidth = math.max(graphPane.boundsInParent.value.width + 2 * Configuration.graphvisualizationPadding, header.boundsInParent.value.getWidth + Configuration.graphvisualizationPadding)
    minHeight = graphPane.boundsInParent.value.height + 2 * Configuration.graphvisualizationPadding + header.boundsInParent.value.height
    maxWidth = minWidth.value
    maxHeight = minHeight.value
    translateX = translateX.value - graphPane.translateX.value + graphPane.boundsInLocal.value.getMinX
    graphPane.translateX = -graphPane.boundsInLocal.value.getMinX
    translateY = translateY.value - graphPane.translateY.value + graphPane.boundsInLocal.value.getMinY
    graphPane.translateY = -graphPane.boundsInLocal.value.getMinY + header.boundsInParent.value.getHeight
    header.headerWidth.set(minWidth.value)
  }

  graphPane.maxHeightProperty onChange {
    updateSize()
  }

  updateSize()

  override def enableInteraction(): Unit = {
    onZoom = {ev : ZoomEvent => Util.handleZoom(this, scale)(ev); Util.trashStyleUpdate(this, this) }
    onScroll = {ev: ScrollEvent => Util.handleScroll(this)(ev); Util.trashStyleUpdate(this, this); drawLocationLines() }

    onZoomFinished = {ev: ZoomEvent => Util.trashIfRequired(this) }
    onScrollFinished = {ev: ScrollEvent => Util.trashIfRequired(this); removeLocationLines() }
    //graphGroup.enableInteraction()
  }

  override def trash(): Unit = {
    removeLocationLines()
    Main.corpusScene.getChildren.remove(this)
  }
}

class GraphVisualizationGraphGroup(sgraph: SGraph, parentPane: Pane, density: Double) extends Group {
  val sugiyamaLayout = new SugiyamaLayout[GraphNode, GraphEdge](density)

  val jgrapht_graph : org.jgrapht.DirectedGraph[GraphNode, GraphEdge] = sgraph.getGraph
  for (node <- jgrapht_graph.vertexSet) {
    sugiyamaLayout.addVertex(node, node.getLabel.length * 10 + 10)
  }
  for (edge <- jgrapht_graph.edgeSet) {
    sugiyamaLayout.addEdge(edge, jgrapht_graph.getEdgeSource(edge), jgrapht_graph.getEdgeTarget(edge))
  }

  sugiyamaLayout.runAlgorithm()

  var draggedNode: Option[GraphVisualizationNode] = None
  var dragLastPost = (0d, 0d)

  parentPane.minWidth = sugiyamaLayout.size._1
  parentPane.maxWidth = sugiyamaLayout.size._1
  parentPane.minHeight = sugiyamaLayout.size._2
  parentPane.maxHeight = sugiyamaLayout.size._2

  val nodemap = new collection.mutable.HashMap[GraphNode, GraphVisualizationNode]
  for (v <- jgrapht_graph.vertexSet) {
    val n = new GraphVisualizationNode(v, this)
    n.layoutX = sugiyamaLayout.vertexPosition(v)._1 - n.boundsInLocal.value.getWidth * 0.5
    n.layoutY = sugiyamaLayout.vertexPosition(v)._2 - n.boundsInLocal.value.getHeight * 0.5
    nodemap(v) = n
    children.add(n)
  }

  for (e <- jgrapht_graph.edgeSet) {
    val n = new GraphVisualizationEdge2(e, nodemap(jgrapht_graph.getEdgeSource(e)), nodemap(jgrapht_graph.getEdgeTarget(e)),
            sugiyamaLayout.edgePosition(e))
    children.add(n)
  }

  for (n <- nodemap.values) {
    n.toFront()
  }

}

class GraphVisualizationNode(node: GraphNode, graph: GraphVisualizationGraphGroup) extends Group {
  val label = new Text("\n" + node.getLabel) {

  }
  val rect = new Rectangle {
    styleClass.clear()
    style = "-fx-fill: rgba(255, 255, 255, 0.9);"
  }
  children.add(rect)
  children.add(label)
  label.translateX = label.translateX.value + Configuration.graphvisualizationNodePadding
  label.translateY = label.translateY.value + Configuration.graphvisualizationNodePadding - 2
  rect.width = label.boundsInParent.value.getWidth + 2 * Configuration.graphvisualizationNodePadding
  rect.height = label.boundsInParent.value.getHeight + 2 * Configuration.graphvisualizationNodePadding - 13

  def findBoundaryPoint(a0: Point2D, b0: Point2D): Point2D = {
    val EPSILON = 0.5d
    var a = a0
    var b = b0
    while (a.distance(b) > EPSILON) {
      val m = a.midpoint(b)
      if (rect.contains(m)) {
        b = m
      } else {
        a = m
      }
    }
    a.midpoint(b)
  }
}


class GraphVisualizationEdge2(edge: GraphEdge, start: GraphVisualizationNode, end: GraphVisualizationNode, coords: Seq[(Double, Double)]) extends Group {
  var headLine = new Line
  var textLine = new Line

  for (i <- 0 to coords.length - 2) {
    headLine = new Line {
      startX = coords(i)._1
      startY = coords(i)._2
      endX = coords(i+1)._1
      endY = coords(i+1)._2
    }
    if (coords.length == 2 || (coords.length == 3 && i == 0) || (coords.length > 3 && i == 1)) {
      textLine = headLine
    }
    children.add(headLine)
  }



  val arrowhead = new Polygon {
    points.add(0d)
    points.add(0d)
    points.add(4d)
    points.add(8d)
    points.add(-4d)
    points.add(8d)
  }

  val label = new Text(edge.getLabel)

  def updateChildren(): Unit = {
    val spoint = new Point2D(headLine.startX.value, headLine.startY.value)
    val epoint = new Point2D(headLine.endX.value, headLine.endY.value)
    val angle = math.toDegrees(math.atan2(epoint.y - spoint.y, epoint.x - spoint.x))
    val root = new Point2D(end.layoutX.value, end.layoutY.value)
    val bp = end.findBoundaryPoint(spoint.subtract(root), epoint.subtract(root))
    arrowhead.layoutX = bp.x + root.x
    arrowhead.layoutY = bp.y + root.y
    arrowhead.transforms.clear()
    arrowhead.transforms.add(new Rotate(angle + 90, 0, 0))
    if (true) {
      val spoint = new Point2D(textLine.startX.value, textLine.startY.value)
      val epoint = new Point2D(textLine.endX.value, textLine.endY.value)
      val angle = math.toDegrees(math.atan2(epoint.y - spoint.y, epoint.x - spoint.x))
      val mp = spoint.midpoint(epoint)
      label.transforms.clear()
      label.layoutX = mp.x - 0.5 * label.boundsInParent.value.getWidth
      label.layoutY = mp.y - 0.5 * label.boundsInParent.value.getHeight
      label.transforms.add(new Rotate(angle, 0.5 * label.boundsInParent.value.getWidth, 0.5 * label.boundsInParent.value.getHeight))
    }

  }

  children.add(arrowhead)
  children.add(label)
  updateChildren()
}


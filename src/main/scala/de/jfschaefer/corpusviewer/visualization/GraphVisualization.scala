package de.jfschaefer.corpusviewer.visualization

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

  // generate Graph
  assert(iw.instance.getInputObjects.containsKey(key))
  val algObj = iw.instance.getInputObjects.get(key)
  assert(algObj.isInstanceOf[SGraph])
  val sgraph: SGraph = algObj.asInstanceOf[SGraph]

  val graphGroup = new GraphVisualizationGraphGroup(sgraph, this)

  children.add(graphGroup)

  minWidth = graphGroup.boundsInParent.value.width + 2 * Configuration.graphvisualizationPadding
  minHeight = graphGroup.boundsInParent.value.height + 2 * Configuration.graphvisualizationPadding
  maxWidth = minWidth.value
  maxHeight = minHeight.value

  override def enableInteraction(): Unit = {
    onZoom = {ev : ZoomEvent => Util.handleZoom(this, scale)(ev); Util.trashStyleUpdate(this, this) }
    onScroll = {ev: ScrollEvent => Util.handleScroll(this)(ev); Util.trashStyleUpdate(this, this); drawLocationLines() }

    onZoomFinished = {ev: ZoomEvent => Util.trashIfRequired(this) }
    onScrollFinished = {ev: ScrollEvent => Util.trashIfRequired(this); removeLocationLines() }
    graphGroup.enableInteraction()
  }

  override def trash(): Unit = {
    removeLocationLines()
    Main.corpusScene.getChildren.remove(this)
  }
}

class GraphVisualizationGraphGroup(sgraph: SGraph, parentPane: Pane) extends Group {
  val jungGraph : DirectedGraph[GraphNode, GraphEdge] = new DirectedSparseGraph
  val jgrapht_graph : org.jgrapht.DirectedGraph[GraphNode, GraphEdge] = sgraph.getGraph
  for (node <- jgrapht_graph.vertexSet) {
    jungGraph.addVertex(node)
  }
  for (edge <- jgrapht_graph.edgeSet) {
    jungGraph.addEdge(edge, jgrapht_graph.getEdgeSource(edge), jgrapht_graph.getEdgeTarget(edge))
  }

  // val layout: Layout[GraphNode, GraphEdge] = new RadialTreeLayout[GraphNode, GraphEdge](jungGraph)
  // val layout: Layout[GraphNode, GraphEdge] = new TreeLayout[GraphNode, GraphEdge](jungGraph.asInstanceOf[Forest[GraphNode, GraphEdge]])
  // val asTree = new DelegateTree[GraphNode, GraphEdge](jungGraph)
  // val asForest = new DelegateForest[GraphNode, GraphEdge](jungGraph)
  // val layout: Layout[GraphNode, GraphEdge] = new TreeLayout[GraphNode, GraphEdge](asForest)

  val layout = new SpringLayout2(jungGraph)
  layout.setSize(new java.awt.Dimension(800, 800))
  layout.setRepulsionRange(600)
  layout.setForceMultiplier(0.09d)

  // for (i <- 0 to 400) {
  var counter = 0
  while (!layout.done() && counter < 600) {
    layout.step()
    counter += 1
  }

  val nodemap = new collection.mutable.HashMap[GraphNode, GraphVisualizationNode]
  var minX = 10000d
  var minY = 10000d
  for (v <- jungGraph.getVertices) {
    val p = layout.transform(v)
    val n = new GraphVisualizationNode(v, this)
    if (p.getX - 0.5 * n.boundsInParent.value.getWidth * 0.5 < minX) minX = p.getX - 0.5 * n.boundsInParent.value.getWidth
    if (p.getY - 0.5 * n.boundsInParent.value.getHeight * 0.5 < minY) minY = p.getY - 0.5 * n.boundsInParent.value.getHeight
    nodemap(v) = n
  }

  for (v <- jungGraph.getVertices) {
    val n = nodemap(v)
    n.layoutX = layout.transform(v).getX - n.boundsInParent.value.getWidth * 0.5 - minX + Configuration.graphvisualizationPadding
    n.layoutY = layout.transform(v).getY - n.boundsInParent.value.getHeight * 0.5 - minY + Configuration.graphvisualizationPadding
    children.add(n)
  }
  for (e <- jungGraph.getEdges) {
    val start = nodemap(jungGraph.getEndpoints(e).getFirst)
    val end = nodemap(jungGraph.getEndpoints(e).getSecond)
    children.add(new GraphVisualizationEdge(e, start, end))
  }
  for (n <- nodemap.values) {
    n.toFront()
  }

  var draggedNode: Option[GraphVisualizationNode] = None
  var dragLastPost = (0d, 0d)

  def enableInteraction(): Unit = {
    for (n <- nodemap.values) n.enableInteraction()
    onMouseDragged = { ev: MouseEvent =>
      draggedNode match {
        case Some(n) =>
          n.layoutX = n.layoutX.value + ev.x - dragLastPost._1
          n.layoutY = n.layoutY.value + ev.y - dragLastPost._2
          dragLastPost = (ev.x, ev.y)
        case None =>
      }
      ev.consume()
    }
    onMouseReleased = { ev: MouseEvent =>
      draggedNode = None

      parentPane.minWidth = boundsInParent.value.width + 2 * Configuration.graphvisualizationPadding
      parentPane.minHeight = boundsInParent.value.height + 2 * Configuration.graphvisualizationPadding
      parentPane.maxWidth = parentPane.minWidth.value
      parentPane.maxHeight = parentPane.minHeight.value

      parentPane.translateX = parentPane.translateX.value + (boundsInParent.value.getMinX - Configuration.graphvisualizationPadding) * parentPane.scaleX.value
      parentPane.translateY = parentPane.translateY.value + (boundsInParent.value.getMinY - Configuration.graphvisualizationPadding) * parentPane.scaleY.value
      translateX = translateX.value - boundsInParent.value.getMinX + Configuration.graphvisualizationPadding
      translateY = translateY.value - boundsInParent.value.getMinY + Configuration.graphvisualizationPadding

      ev.consume()
    }
  }
}

class GraphVisualizationNode(node: GraphNode, graph: GraphVisualizationGraphGroup) extends Group {
  val label = new Text("\n" + node.getLabel) {

  }
  val rect = new Rectangle {
    styleClass.clear()
    style = "-fx-fill: rgba(255, 255, 255, 0.7);"
  }
  children.add(rect)
  children.add(label)
  label.translateX = label.translateX.value + Configuration.graphvisualizationNodePadding
  label.translateY = label.translateY.value + Configuration.graphvisualizationNodePadding - 2
  rect.width = label.boundsInParent.value.getWidth + 2 * Configuration.graphvisualizationNodePadding
  rect.height = label.boundsInParent.value.getHeight + 2 * Configuration.graphvisualizationNodePadding - 13

  def enableInteraction(): Unit = {
    onMousePressed = { ev: MouseEvent =>
      graph.dragLastPost = (ev.x + boundsInParent.value.getMinX, ev.y + boundsInParent.value.getMinY + rect.boundsInParent.value.getMinY - label.boundsInParent.value.getMinY)
      graph.draggedNode = Some(this)
      ev.consume()
    }
  }

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

class GraphVisualizationEdge(edge: GraphEdge, start: GraphVisualizationNode, end: GraphVisualizationNode) extends Group {
  val line = new Line {
    startX <== start.layoutX + start.boundsInParent.value.getWidth * 0.5
    startY <== start.layoutY + start.boundsInParent.value.getHeight * 0.5

    endX <== end.layoutX + end.boundsInParent.value.getWidth * 0.5
    endY <== end.layoutY + end.boundsInParent.value.getHeight * 0.5
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
    val spoint = new Point2D(line.startX.value, line.startY.value)
    val epoint = new Point2D(line.endX.value, line.endY.value)
    val angle = math.toDegrees(math.atan2(epoint.y - spoint.y, epoint.x - spoint.x))
    val root = new Point2D(end.layoutX.value, end.layoutY.value)
    val bp = end.findBoundaryPoint(spoint.subtract(root), epoint.subtract(root))
    arrowhead.layoutX = bp.x + root.x
    arrowhead.layoutY = bp.y + root.y
    arrowhead.transforms.clear()
    arrowhead.transforms.add(new Rotate(angle + 90, 0, 0))
    val mp = spoint.midpoint(epoint)
    label.transforms.clear()
    label.layoutX = mp.x - 0.5 * label.boundsInParent.value.getWidth
    label.layoutY = mp.y - 0.5 * label.boundsInParent.value.getHeight
    label.transforms.add(new Rotate(angle, 0.5 * label.boundsInParent.value.getWidth, 0.5 * label.boundsInParent.value.getHeight))

  }

  line.startXProperty onChange updateChildren
  line.startYProperty onChange updateChildren
  line.endXProperty onChange updateChildren
  line.endYProperty onChange updateChildren

  children.add(line)
  children.add(arrowhead)
  children.add(label)
  updateChildren()
}
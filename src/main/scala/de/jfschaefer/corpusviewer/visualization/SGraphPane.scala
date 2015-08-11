package de.jfschaefer.corpusviewer.visualization

import java.util
import javafx.scene.paint.Color

import de.jfschaefer.layeredgraphlayout.latex.LatexGenerator
import de.jfschaefer.layeredgraphlayout.visualizationfx.{SimpleGraphFXEdgeFactory, SimpleGraphFXNodeFactory, GraphFX}

import scalafx.scene.layout.Pane
import scalafx.scene.text.Text

import de.jfschaefer.layeredgraphlayout._

import scala.collection.JavaConversions._

import de.up.ling.irtg.algebra.graph.{SGraph, GraphNode, GraphEdge}
// import de.jfschaefer.sugiyamalayout.visualizationFX.{GraphFX, GraphFXNodeFactory}
// import de.jfschaefer.sugiyamalayout.{Layout, DiGraph, LatexGenerator}

import de.jfschaefer.corpusviewer.Configuration

import scalafx.scene.shape.Rectangle

/*
    A Pane that visualizes an SGraph, using de.jfschaefer.sugiyamalayout
 */

class SGraphPane(sgraph : SGraph, bezier: Boolean = true, alternative: Boolean = true) extends Pane {
  styleClass.clear()
  //style = "-fx-background-color: lightblue"
  val jgrapht_graph : org.jgrapht.DirectedGraph[GraphNode, GraphEdge] = sgraph.getGraph
  // val digraph : DiGraph[GraphNode, GraphEdge] = new DiGraph()
  val ggraph = new gengraph.GenGraph[GraphNode, GraphEdge]()
  val edgeLabelMap: java.util.Map[GraphEdge, String] = new java.util.HashMap()
  val nodeLabelMap: java.util.Map[GraphNode, String] = new java.util.HashMap()
   for (node : GraphNode <- jgrapht_graph.vertexSet) {
     ggraph.addNode(node, node.getLabel.length * 10 + 30, 30)
     nodeLabelMap.put(node, node.getLabel)
  }
  for (edge : GraphEdge <- jgrapht_graph.edgeSet) {
    ggraph.addEdge(edge, edge.getSource, edge.getTarget)
    edgeLabelMap.put(edge, edge.getLabel)
  }

  //val layout = .generateLayout(config)
  val pgraph = ggraph.generatePGraph()
  val lgraphconfig = new lgraph.LGraphConfig
  val lagraph = pgraph.generateLGraph(lgraphconfig)
  lagraph.treePlacement()
  val layoutconfig = new layout.LayoutConfig
  val thelayout = lagraph.getLayout(layoutconfig)

  //val graphfx : GraphFX[GraphNode, GraphEdge] = new GraphFX(layout, new DefaultGraphFXNodeFactory, labelMap, Color.web(Configuration.graphColor), Color.web(Configuration.graphColor))
  val graphfx = new GraphFX[GraphNode, GraphEdge](thelayout, new SimpleGraphFXNodeFactory[GraphNode](nodeLabelMap, "graph_node", ""),
                      new SimpleGraphFXEdgeFactory[GraphEdge](layoutconfig, edgeLabelMap, Color.BLACK))
  children.add(graphfx)

  def getWidth: Double = thelayout.getWidth

  def getHeight: Double = thelayout.getHeight

  def getLaTeX(): String = {
    /* val map : java.util.Map[GraphNode, String] = new java.util.HashMap()
    for (node : GraphNode <- jgrapht_graph.vertexSet()) {
      map.put(node, node.getLabel)
    }
    val lg = new LatexGenerator(layout, map, labelMap, config)
    lg.getLatex */
    LatexGenerator.generateLatex(thelayout, nodeLabelMap, edgeLabelMap)
  }
}

/* class DefaultGraphFXNodeFactory extends GraphFXNodeFactory[GraphNode] {
  override def getNodeVisualization(node : GraphNode, width_ : Double, height_ : Double): javafx.scene.Node = {
    val pane = new Pane
    val bg = new Rectangle {
      width = width_
      height = height_
      styleClass.clear()
      styleClass.add("graph_node")
    }
    val label = new Text("\n" + node.getLabel)
    label.layoutX = width_ * 0.5 - label.boundsInLocal.value.getWidth * 0.5
    label.layoutY = height_ * 0.5 - label.boundsInLocal.value.getHeight * 0.5 + 5
    pane.children.add(bg)
    pane.children.add(label)
    pane
  }
} */


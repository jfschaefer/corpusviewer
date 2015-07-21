package de.jfschaefer.corpusviewer.visualization

import de.jfschaefer.corpusviewer.InstanceWrapper

import scalafx.scene.layout.Pane
import scalafx.scene.text.Text
import scalafx.scene.Node

import scala.collection.JavaConversions._

import de.up.ling.irtg.algebra.graph.{SGraph, GraphNode, GraphEdge}
import de.jfschaefer.sugiyamalayout.visualizationFX.{GraphFX, GraphFXNodeFactory}
import de.jfschaefer.sugiyamalayout.{Layout, DiGraph, LatexGenerator}

import scalafx.scene.shape.Rectangle

class SGraphPane(sgraph : SGraph) extends Pane {
  styleClass.clear()
  //style = "-fx-background-color: lightblue"
  val jgrapht_graph : org.jgrapht.DirectedGraph[GraphNode, GraphEdge] = sgraph.getGraph
  val digraph : DiGraph[GraphNode, GraphEdge] = new DiGraph()
  val labelMap: java.util.Map[GraphEdge, String] = new java.util.HashMap()
  for (node : GraphNode <- jgrapht_graph.vertexSet) {
    digraph.addNode(node, node.getLabel.length * 10 + 30, 30)
  }
  for (edge : GraphEdge <- jgrapht_graph.edgeSet) {
    digraph.addEdge(edge, edge.getSource, edge.getTarget)
    labelMap.put(edge, edge.getLabel);
  }
  val config = new de.jfschaefer.sugiyamalayout.Configuration
  config.setUseAlternativeAlgorithm(false)
  config.setLayerDistance(81d);
  val layout : Layout[GraphNode, GraphEdge] = digraph.generateLayout(config)

  val graphfx : GraphFX[GraphNode, GraphEdge] = new GraphFX(layout, new DefaultGraphFXNodeFactory, labelMap)
  children.add(graphfx)

  def getWidth: Double = layout.getWidth()
  def getHeight: Double = layout.getHeight()
  def getLaTeX(): String = {
    val map : java.util.Map[GraphNode, String] = new java.util.HashMap()
    for (node : GraphNode <- jgrapht_graph.vertexSet()) {
      map.put(node, node.getLabel)
    }
    val lg = new LatexGenerator(layout, map, labelMap, config)
    return lg.getLatex()
  }
}

class DefaultGraphFXNodeFactory extends GraphFXNodeFactory[GraphNode] {
  override def getNodeVisualization(node : GraphNode, width_ : Double, height_ : Double): javafx.scene.Node = {
    val pane = new Pane
    val bg = new Rectangle {
      width = width_
      height = height_
      styleClass.clear()
      styleClass.add("graph_node")
    }
    val label = new Text("\n" + node.getLabel())
    label.layoutX = width_ * 0.5 - label.boundsInLocal.value.getWidth * 0.5
    label.layoutY = height_ * 0.5 - label.boundsInLocal.value.getHeight * 0.5 + 5
    pane.children.add(bg)
    pane.children.add(label)
    pane
  }
}

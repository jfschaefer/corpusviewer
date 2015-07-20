package de.jfschaefer.corpusviewer.visualization

import de.jfschaefer.corpusviewer.InstanceWrapper

import scalafx.scene.layout.Pane
import scalafx.scene.text.Text
import scalafx.scene.Node

import scala.collection.JavaConversions._

import de.up.ling.irtg.algebra.graph.{SGraph, GraphNode, GraphEdge}
import de.jfschaefer.sugiyamalayout.visualizationFX.{GraphFX, GraphFXNodeFactory}
import de.jfschaefer.sugiyamalayout.{Layout, DiGraph}

import scalafx.scene.shape.Rectangle

class SGraphPane(sgraph : SGraph) extends Pane {
  styleClass.clear()
  //style = "-fx-background-color: lightblue"
  val jgrapht_graph : org.jgrapht.DirectedGraph[GraphNode, GraphEdge] = sgraph.getGraph
  val digraph : DiGraph[GraphNode, GraphEdge] = new DiGraph()
  for (node : GraphNode <- jgrapht_graph.vertexSet) {
    digraph.addNode(node, node.getLabel.length * 10 + 30, 30)
  }
  for (edge : GraphEdge <- jgrapht_graph.edgeSet) {
    digraph.addEdge(edge, edge.getSource, edge.getTarget)
  }
  val config = new de.jfschaefer.sugiyamalayout.Configuration
  config.setUseAlternativeAlgorithm(false)
  val layout : Layout[GraphNode, GraphEdge] = digraph.generateLayout(config)

  val graphfx : GraphFX[GraphNode, GraphEdge] = new GraphFX(layout, new DefaultGraphFXNodeFactory)
  children.add(graphfx)

  def getWidth: Double = layout.getWidth()
  def getHeight: Double = layout.getHeight()
}

class DefaultGraphFXNodeFactory extends GraphFXNodeFactory[GraphNode] {
  override def getNodeVisualization(node : GraphNode, width_ : Double, height_ : Double): javafx.scene.Node = {
    val pane = new Pane
    val bg = new Rectangle {
      width = width_
      height = height_
      styleClass.clear()
      style = "-fx-fill: white"
    }
    val label = new Text("\n" + node.getLabel())
    label.layoutX = width_ * 0.5 - label.boundsInLocal.value.getWidth * 0.5
    label.layoutY = height_ * 0.5 - label.boundsInLocal.value.getHeight * 0.5 + 5
    pane.children.add(bg)
    pane.children.add(label)
    pane
  }
}
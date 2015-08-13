package de.jfschaefer.corpusviewer.visualization

import javafx.scene.paint.Color

import de.jfschaefer.layeredgraphlayout.latex.LatexGenerator
import de.jfschaefer.layeredgraphlayout.visualizationfx.{SimpleGraphFXEdgeFactory, SimpleGraphFXNodeFactory, GraphFX}

import scalafx.scene.layout.Pane

import de.jfschaefer.layeredgraphlayout._

import scala.collection.JavaConversions._

import de.up.ling.irtg.algebra.graph.{SGraph, GraphNode, GraphEdge}

/*
    A Pane that visualizes an SGraph, using de.jfschaefer.layeredgraphlayout
 */

class SGraphPane(sgraph : SGraph, bezier: Boolean = true, alternative: Boolean = true) extends Pane {
  styleClass.clear()
  val jgrapht_graph : org.jgrapht.DirectedGraph[GraphNode, GraphEdge] = sgraph.getGraph
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

  val pgraph = ggraph.generatePGraph()
  SGraphPane.addEnergy(pgraph.runSimulatedAnnealing(5000))
  val lgraphconfig = new lgraph.LGraphConfig
  val lagraph = pgraph.generateLGraph(lgraphconfig)
  lagraph.graphPlacement()
  val layoutconfig = new layout.LayoutConfig
  val thelayout = lagraph.getLayout(layoutconfig)

  val graphfx = new GraphFX[GraphNode, GraphEdge](thelayout, new SimpleGraphFXNodeFactory[GraphNode](nodeLabelMap, "graph_node", ""),
                      new SimpleGraphFXEdgeFactory[GraphEdge](layoutconfig, edgeLabelMap, Color.BLACK))
  children.add(graphfx)

  def getWidth: Double = thelayout.getWidth

  def getHeight: Double = thelayout.getHeight

  def getLaTeX(): String = {
    LatexGenerator.generateLatex(thelayout, nodeLabelMap, edgeLabelMap)
  }
}

object SGraphPane {
  var totalEnergy = 0d;
  def addEnergy(energy : Double): Unit = {
    totalEnergy += energy;
    println("Total energy: " + totalEnergy);
  }
}


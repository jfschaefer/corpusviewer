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

/** A Pane that visualizes an SGraph using [[de.jfschaefer.layeredgraphlayout]]
 *
 * @param sgraph the graph to be visualized
 * @param bezier whether Bezier curves should be used instead of straight line segments
 * @param iterations number of iterations in the visualization algorithm
 */

class SGraphPane(sgraph : SGraph, var bezier: Boolean = true, var iterations: Int = 1000) extends Pane {
  styleClass.clear()
  val jgrapht_graph : org.jgrapht.DirectedGraph[GraphNode, GraphEdge] = sgraph.getGraph
  var largeLayout = false
  var boxed = true

  // The initial run:
  var ggraph = new gengraph.GenGraph[GraphNode, GraphEdge]()
  var edgeLabelMap: java.util.Map[GraphEdge, String] = new java.util.HashMap()
  var nodeLabelMap: java.util.Map[GraphNode, String] = new java.util.HashMap()
   for (node : GraphNode <- jgrapht_graph.vertexSet) {
     ggraph.addNode(node, node.getLabel.length * 10 + 30, 30)
     //ggraph.addNode(node, node.getLabel.length * 7 , 16)
     nodeLabelMap.put(node, node.getLabel)
  }
  for (edge : GraphEdge <- jgrapht_graph.edgeSet) {
    ggraph.addEdge(edge, edge.getSource, edge.getTarget)
    edgeLabelMap.put(edge, edge.getLabel)
  }

  var pgraph = ggraph.generatePGraph()
  SGraphPane.addEnergy(pgraph.runSimulatedAnnealing(iterations))
  var lgraphconfig = new lgraph.LGraphConfig
  var lagraph = pgraph.generateLGraph(lgraphconfig)
  lagraph.graphPlacement()
  var layoutconfig = new layout.LayoutConfig
  layoutconfig.setBezier(bezier)
  var thelayout = lagraph.getLayout(layoutconfig)

  var graphfx = new GraphFX[GraphNode, GraphEdge](thelayout, new SimpleGraphFXNodeFactory[GraphNode](nodeLabelMap, "graph_node", ""),
                      new SimpleGraphFXEdgeFactory[GraphEdge](layoutconfig, edgeLabelMap, Color.BLACK))
  children.add(graphfx)


  /*
        Methods to re-run the layout algorithm from different points
   */
  /** Start from the very beginning */
  def regenerateGGraph(): Unit = {
    ggraph = new gengraph.GenGraph[GraphNode, GraphEdge]()
    edgeLabelMap = new java.util.HashMap()
    nodeLabelMap = new java.util.HashMap()
    for (node : GraphNode <- jgrapht_graph.vertexSet) {
      if (boxed) {
        ggraph.addNode(node, node.getLabel.length * 10 + 30, 30)
      } else {
        ggraph.addNode(node, node.getLabel.length * 7 , 16)
      }
      nodeLabelMap.put(node, node.getLabel)
    }
    for (edge : GraphEdge <- jgrapht_graph.edgeSet) {
      ggraph.addEdge(edge, edge.getSource, edge.getTarget)
      edgeLabelMap.put(edge, edge.getLabel)
    }

    pgraph = ggraph.generatePGraph()
    rerunAlgorithm()
  }

  /** Re-runs the node positioning algorithm */
  def rerunAlgorithm(): Unit = {
    pgraph.runSimulatedAnnealing(iterations)
    recreateLGraph()
  }

  /** Runs the node positioning algorithm 10 times and keeps the best result */
  def rerunAlgorithm10Times(): Unit = {
    var pgraphBest = pgraph
    var score = pgraphBest.runSimulatedAnnealing(iterations)
    for (i <- 1 to 10) {
      val newPGraph = ggraph.generatePGraph()
      val newScore = newPGraph.runSimulatedAnnealing(iterations)
      if (newScore < score) {
        pgraphBest = newPGraph
        score = newScore
      }
    }
    pgraph = pgraphBest
    recreateLGraph()
  }

  /** Recreate the LGraph representation from the current pgraph */
  def recreateLGraph(): Unit = {
    lgraphconfig = new lgraph.LGraphConfig
    if (largeLayout) {
      lgraphconfig.setLayerDistance(121d)
      lgraphconfig.setGapBetweenNodes(25d)
      lgraphconfig.setSpecialPaddingA(1d);
    }
    lagraph = pgraph.generateLGraph(lgraphconfig)
    lagraph.graphPlacement()
    recreateLayout()
  }

  /** Recreate the Layout from the current lgraph */
  def recreateLayout(): Unit = {
    layoutconfig = new layout.LayoutConfig
    layoutconfig.setBezier(bezier)
    thelayout = lagraph.getLayout(layoutconfig)

    graphfx = new GraphFX[GraphNode, GraphEdge](thelayout, new SimpleGraphFXNodeFactory[GraphNode](nodeLabelMap,
      if (boxed) "graph_node" else "invisible_rect" , ""),
      new SimpleGraphFXEdgeFactory[GraphEdge](layoutconfig, edgeLabelMap, Color.BLACK))
    children.clear()
    children.add(graphfx)
  }

  def getWidth: Double = thelayout.getWidth

  def getHeight: Double = thelayout.getHeight

  /** Sets the number of iterations for the positioning algorithm */
  def setIterations(iterations: Int): Unit = { this.iterations = iterations }

  /** Sets whether or not Bezier curves should be used */
  def setBezier(bezier: Boolean): Unit = { this.bezier = bezier }

  /** Sets whether or not a more spacious layout should be used */
  def setLargeLayout(largeLayout: Boolean): Unit = {
    this.largeLayout = largeLayout
  }

  def setBoxed(boxed : Boolean): Unit = {
    this.boxed = boxed
  }

  /** Returns the LaTeX representation of the current layout */
  def getLaTeX(): String = {
    LatexGenerator.generateLatex(thelayout, nodeLabelMap, edgeLabelMap, boxed)
  }
}

object SGraphPane {
  var totalEnergy = 0d;
  def addEnergy(energy : Double): Unit = {
    totalEnergy += energy;
    println("Total energy: " + totalEnergy);
  }
}


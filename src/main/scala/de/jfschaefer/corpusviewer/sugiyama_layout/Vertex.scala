package de.jfschaefer.corpusviewer.sugiyama_layout

import scala.collection.mutable
import scala.collection.immutable

class Vertex {
  private val outgoingEdges : mutable.Set[Edge] = new mutable.HashSet
  private val ingoingEdges : mutable.Set[Edge] = new mutable.HashSet
  private var _layer = -1
  private var layeringIsFixed = false
  private var _layerVertex: Option[LayerVertex] = None


  def layerVertex : LayerVertex = {
    layeringIsFixed = true
    assert(_layer != -1)
    _layerVertex match {
      case Some(l) => l
      case None =>
        val v = new LayerVertex(false)
        _layerVertex = Some(v)
        v
    }
  }

  def addIngoingEdge(edge: Edge) {
    assert(!layeringIsFixed)
    assert(edge.to == this)
    ingoingEdges += edge
  }

  def addOutgoingEdge(edge: Edge) {
    assert(!layeringIsFixed)
    assert(edge.from == this)
    outgoingEdges += edge
  }

  def layer_=(layer: Int) = {
    assert(!layeringIsFixed)
    _layer = layer
  }

  def layer: Int = _layer

  // update sets of ingoing and outgoing edges after an edge reversal
  def reversedOutgoingEdge(edge: Edge) {
    assert(!layeringIsFixed)
    assert(outgoingEdges.contains(edge))
    outgoingEdges.remove(edge)
    addIngoingEdge(edge)
  }

  // update sets of ingoing and outgoing edges after an edge reversal
  def reversedIngoingEdge(edge: Edge) {
    assert(!layeringIsFixed)
    assert(ingoingEdges.contains(edge))
    ingoingEdges.remove(edge)
    addOutgoingEdge(edge)
  }

  def isSink: Boolean = outgoingEdges.isEmpty
  def isSource: Boolean = ingoingEdges.isEmpty

  // In the following, subgraphs are represented as a function which is true for the values contained in it
  def isSinkInSubgraph(subgraph: (Vertex => Boolean)): Boolean = !outgoingEdges.exists((e: Edge) => subgraph(e.to))

  def isSourceInSubgraph(subgraph: (Vertex => Boolean)): Boolean = !ingoingEdges.exists((e: Edge) => subgraph(e.from))

  def subgraphOutdegree(subgraph: (Vertex => Boolean)): Int = outgoingEdges.count((e: Edge) => subgraph(e.to))

  def subgraphIndegree(subgraph: (Vertex => Boolean)): Int = ingoingEdges.count((e: Edge) => subgraph(e.from))

  // returns a new set instead of an iterator, because in the use cases, the actual set gets manipulated
  def getOutgoingSubgraphEdges(subgraph: (Vertex => Boolean)): immutable.Set[Edge] =
    outgoingEdges.filter((e: Edge) => subgraph(e.to)).toSet

  // returns a new set instead of an iterator, because in the use cases, the actual set gets manipulated
  def getIngoingSubgraphEdges(subgraph: (Vertex => Boolean)): immutable.Set[Edge] =
    ingoingEdges.filter((e: Edge) => subgraph(e.from)).toSet
}

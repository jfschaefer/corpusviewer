package de.jfschaefer.corpusviewer.sugiyama_layout

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class Edge(actualFrom: Vertex, actualTo: Vertex) {
  // Some of the edges might get reversed in order to obtain an acyclic graph
  private var reversed : Boolean = false
  private var algorithmHasStarted: Boolean = false

  def reverse(): Unit = {
    assert(!algorithmHasStarted)
    reversed = !reversed
    from.reversedIngoingEdge(this)
    to.reversedOutgoingEdge(this)
  }

  def isReversed: Boolean = reversed

  def from: Vertex = {
    if (reversed)
      actualTo
    else
      actualFrom
  }

  def to: Vertex = {
    if (reversed)
      actualFrom
    else
      actualTo
  }

  private var _layerVertices = new ArrayBuffer[LayerVertex]

  def layerVertices: Seq[LayerVertex] = {
    algorithmHasStarted = true
    if (_layerVertices.isEmpty) {
      _layerVertices.append(from.layerVertex)
      var layer = from.layer + 1
      while (layer < to.layer) {
        val n = new LayerVertex(true)
        _layerVertices.last.addChild(n)
        n.addParent(_layerVertices.last)
        _layerVertices.append(n)
        layer += 1
      }
      val n = to.layerVertex
      _layerVertices.last.addChild(n)
      n.addParent(_layerVertices.last)
      _layerVertices.append(n)
    }
    _layerVertices
  }

  def layerDummyVertices: Seq[LayerVertex] = layerVertices.slice(1, layerVertices.length - 1)
}

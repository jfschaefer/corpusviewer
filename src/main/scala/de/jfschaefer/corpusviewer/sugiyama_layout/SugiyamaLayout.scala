package de.jfschaefer.corpusviewer.sugiyama_layout

import scala.collection.mutable
import scala.collection.immutable
import scala.collection.mutable.ArrayBuffer

class SugiyamaLayout[V, E] {
  private val vertexMap: mutable.Map[V, Vertex] = new mutable.HashMap
  private val edgeMap: mutable.Map[E, Edge] = new mutable.HashMap
  private var algorithmHasBeenRun = false
  private var numberOfLayers = -1

  private var layers: mutable.ArraySeq[mutable.ArrayBuffer[LayerVertex]] = null

  def addVertex(vertex: V): Unit = {
    assertHasntBeenRun()
    vertexMap += (vertex -> new Vertex)
  }

  def addEdge(edge: E, from: V, to: V): Unit = {
    assertHasntBeenRun()
    val newEdge = new Edge(vertexMap(from), vertexMap(to))
    edgeMap += (edge -> newEdge)
    if (from != to) {
      // we don't care about self-edges
      vertexMap(from).addOutgoingEdge(newEdge)
      vertexMap(to).addIngoingEdge(newEdge)
    }
  }

  private def assertHasntBeenRun(): Unit = {
    if (algorithmHasBeenRun) {
      System.err.println("de.jfschaefer.sugiyama_layout:" +
        "You cannot perform this action after running the layout algorithm")
      System.err.println(Thread.currentThread.getStackTrace)
    }
  }

  private var width = 512d
  private var height = 512d

  private var maxLayerLength = 1

  def setSize(w: Double, h: Double): Unit = {
    width = w
    height = h
  }

  def size: (Double, Double) = (width, height)

  def runAlgorithm(): Unit = {
    algorithmHasBeenRun = true

    removeCycles()

    minimalHeightLayering()

    layerFormalization()

    reorderLayersTopDown()
    // reorderLayersBottomUp()
    reorderLayersNeighbors()
    reorderLayersNeighbors()
    reorderLayersNeighbors()

    maxLayerLength = layers.maxBy(layer => layer.length).length
  }

  private def vltopos(vl: LayerVertex, layer: Int): (Double, Double) =
    //((vl.xpos + 1) * width/(maxLayerLength + 1), (layer + 1) * height / (layers.length + 1))
     ((vl.xpos + 1) * width/(layers(layer).length + 1), (layer + 1) * height / (layers.length + 1))

  def vertexPosition(vertex: V): (Double, Double) = {
    assert(algorithmHasBeenRun)
    val v = vertexMap(vertex)
    val vl = v.layerVertex
    vltopos(vl, v.layer)
  }

  def edgePosition(edge: E): Seq[(Double, Double)] = {
    assert(algorithmHasBeenRun)
    val e = edgeMap(edge)
    val vls = e.layerVertices
    var result: ArrayBuffer[(Double, Double)] = new ArrayBuffer

    var layer = e.from.layer
    for (vl <- vls) {
      //result.append(((vl.xpos + 1) * width/(maxLayerLength + 1), (layer + 1) * height / (layers.length + 1)))
      result.append(vltopos(vl, layer))
      layer += 1
    }

    if (e.isReversed) result.reverse
    else result
  }

  // Greedy implementation
  def removeCycles(): Unit = {
    var remainingVertices : immutable.Set[Vertex] = vertexMap.values.toSet

    while (remainingVertices.nonEmpty) {
      // remove sources and sinks
      var oldSize = remainingVertices.size
      do {
        oldSize = remainingVertices.size
        remainingVertices = remainingVertices.filterNot((v: Vertex) =>
          v.isSinkInSubgraph(remainingVertices.contains) || v.isSourceInSubgraph(remainingVertices.contains))
      } while (oldSize != remainingVertices.size)

      if (remainingVertices.nonEmpty) {
        // Now we're forced to reverse some edge
        // First find the vertex where the ratio of reversed edges vs total number of edges is minimal
        var bestRatio = 1d   // Worse than the worst case (we'd have to reverse every edge, which isn't gonna happen)
        var chosenVertex : Vertex = null
        for (vertex <- remainingVertices) {
          val indegree = vertex.subgraphIndegree(remainingVertices.contains)
          val outdegree = vertex.subgraphOutdegree(remainingVertices.contains)
          assert(indegree != 0)
          assert(outdegree != 0)
          val ratio = math.min(indegree, outdegree).toDouble / (indegree + outdegree)
          if (ratio < bestRatio) {
            bestRatio = ratio
            chosenVertex = vertex
          }
        }
        assert(chosenVertex != null)  // should be true, as we checked that there are vertices left

        val indegree = chosenVertex.subgraphIndegree(remainingVertices.contains)
        val outdegree = chosenVertex.subgraphOutdegree(remainingVertices.contains)
        if (indegree < outdegree) {
          for (edge <- chosenVertex.getIngoingSubgraphEdges(remainingVertices.contains)) {
            edge.reverse()
          }
        } else {
          for (edge <- chosenVertex.getOutgoingSubgraphEdges(remainingVertices.contains)) {
            edge.reverse()
          }
        }
      }
    }
  }

  def minimalHeightLayering(): Unit = {
    //var coveredVertices: mutable.Set[Vertex] = new mutable.HashSet
    var coveredVertices: immutable.Set[Vertex] = new immutable.HashSet
    var currentLayer: Int = 0
    while (coveredVertices.size != vertexMap.size) {   // vertexMap is injective
      var inThisLayer = new immutable.HashSet[Vertex]
      for (v <- vertexMap.values.filter(!coveredVertices.contains(_))) {
        // subgraph is complement of covered vertices
        if (v.isSourceInSubgraph(!coveredVertices.contains(_))) {
          inThisLayer = inThisLayer + v
          v.layer = currentLayer
        }
      }
      coveredVertices = inThisLayer.union(coveredVertices)
      currentLayer += 1
    }
    numberOfLayers = currentLayer
  }

  def layerFormalization(): Unit = {
    layers = new mutable.ArraySeq[mutable.ArrayBuffer[LayerVertex]](numberOfLayers)
    for (i <- 0 to numberOfLayers - 1) {
      layers(i) = new ArrayBuffer
    }
    for (v <- vertexMap.values) {
      layers(v.layer).append(v.layerVertex)
    }

    for (e <- edgeMap.values.filter(e => e.from != e.to)) {
      var layer = e.from.layer + 1
      for (v <- e.layerDummyVertices) {
        layers(layer).append(v)
        layer += 1
      }
    }
  }

  def reorderLayersTopDown(): Unit = {
    if (numberOfLayers <= 0) return

    // keep order of first layer
    var i = 0
    for (lv <- layers.head) {
      lv.xpos = i
      i += 1
    }

    for (j <- 1 to layers.length - 1) {
      // layer.sortBy(lv => lv.parentsMedian)
      layers(j) = layers(j).sortBy(lv => lv.parentArithmeticMean)
      // update x positions
      i = 0
      for (lv <- layers(j)) {
        lv.xpos = i
        i += 1
      }
    }
  }

  def reorderLayersBottomUp(): Unit = {
    if (numberOfLayers <= 0) return

    // keep order of first layer
    var i = 0
    for (lv <- layers.last) {
      lv.xpos = i
      i += 1
    }

    // for (layer <- layers.take(layers.length - 1).reverseIterator) {
    for (j <- (0 to layers.length - 2).reverseIterator) {
      // layer.sortBy(lv => lv.parentsMedian)
      layers(j) = layers(j).sortBy(lv => lv.childrenArithmeticMean)
      // update x positions
      i = 0
      for (lv <- layers(j)) {
        lv.xpos = i
        i += 1
      }
    }
  }

  def reorderLayersNeighbors(): Unit = {
    if (numberOfLayers <= 0) return
    var i = 0
    for (j <- layers.indices) {
      //layers(j) = layers(j).sortBy(lv => lv.totalArithmeticMean)
      layers(j) = layers(j).sortBy(lv => lv.improvedTotalAM(if (j == 0) 0 else layers(j-1).length,
                         if (j == layers.size - 1) 0 else layers(j+1).length))
      // update x positions
      i = 0
      for (lv <- layers(j)) {
        lv.xpos = i
        i += 1
      }
    }
  }
}


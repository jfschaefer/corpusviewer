package de.jfschaefer.corpusviewer.sugiyama_layout


class LayerVertex(dummy: Boolean) {
  var children: Set[LayerVertex] = new scala.collection.immutable.HashSet
  var parents: Set[LayerVertex] = new scala.collection.immutable.HashSet
  var xPos = -1
  var width = 24d

  def isDummy: Boolean = dummy

  def xpos: Int = {
    assert(xPos != -1)
    xPos
  }

  def setWidth(w: Double): Unit = {
    width = w
  }

  def xpos_=(x: Int): Unit = {xPos = x}

  def addChild(v: LayerVertex): Unit = {
    children = children + v
  }

  def addParent(v: LayerVertex): Unit = {
    parents = parents + v
  }

  def parentsMedian: Int = {
    val a = parents.toArray.sortBy(v => v.xpos)
    a(parents.size/2).xpos
  }

  def parentArithmeticMean: Double = if (parents.isEmpty) -1 else  //should never be empty, as we don't sort the first row
    parents.foldLeft(0)((a, b) => a + b.xpos).toDouble / parents.size

  def childrenArithmeticMean: Double = if (children.isEmpty) -1 else  //should never be empty, as we don't sort the first row
    children.foldLeft(0)((a, b) => a + b.xpos).toDouble / children.size

  def totalArithmeticMean: Double = if (parents.isEmpty && children.isEmpty) -1 else {
    // if (parents.size == 1) childrenArithmeticMean
    // else if (children.size == 1) parentArithmeticMean
    (parents.foldLeft(0)((a, b) => a + b.xpos)+ children.foldLeft(0)((a, b) => a + b.xpos)).toDouble / (parents.size + children.size)
  }

  def improvedTotalAM(parentLayerN: Int, childrenLayerN: Int): Double = if (parents.isEmpty && children.isEmpty) -parentLayerN - childrenLayerN else {
    (parents.foldLeft(0d)((a, b) => a + b.xpos - 0.5*parentLayerN)+ children.foldLeft(0d)((a, b) => a + b.xpos - 0.5*childrenLayerN)).toDouble / (parents.size + children.size)
  }
}

package de.jfschaefer.corpusviewer.visualization

import de.jfschaefer.layeredgraphlayout.layout.LayoutConfig
import de.jfschaefer.layeredgraphlayout.lgraph.LGraphConfig
import de.jfschaefer.layeredgraphlayout.tree.Tree
import de.jfschaefer.layeredgraphlayout.visualizationfx.{SimpleGraphFXEdgeFactory, SimpleGraphFXNodeFactory, GraphFX}

import scalafx.scene.layout.Pane

import scala.collection.mutable
import scala.collection.JavaConversions._
import scalafx.scene.paint.Color

class TreePane(tree : de.up.ling.tree.Tree[String]) extends Pane {
  styleClass.clear()
  val t = new Tree[de.up.ling.tree.Tree[String], Object](tree, tree.getLabel.length * 10 + 30, 30)
  fillTree(t, tree)
  def fillTree(t: Tree[de.up.ling.tree.Tree[String], Object], tree: de.up.ling.tree.Tree[String]): Unit = {
    for (child : de.up.ling.tree.Tree[String] <- tree.getChildren) {
      t.addChild(tree, child, child, child.getLabel.length * 10 + 30, 30)
      fillTree(t, child)
    }
  }

  val lconfig = new LGraphConfig
  val lgraph = t.generateLGraph(lconfig)

  lgraph.treePlacement()

  val layoutconfig = new LayoutConfig
  val layout = lgraph.getLayout(layoutconfig)

  val nodeNames : mutable.Map[de.up.ling.tree.Tree[String], String] = new mutable.HashMap[de.up.ling.tree.Tree[String], String]()
  val edgeNames : mutable.Map[Object, String] = new mutable.HashMap[Object, String]()

  for (x : de.up.ling.tree.Tree[String] <- tree.getAllNodes) {
    nodeNames.put(x, x.getLabel)
    edgeNames.put(x, "")
  }

  val nodeFactory = new SimpleGraphFXNodeFactory[de.up.ling.tree.Tree[String]](nodeNames, "", "")
  val edgeFactory = new SimpleGraphFXEdgeFactory[Object](edgeNames, Color.Black)
  val graph = new GraphFX[de.up.ling.tree.Tree[String], Object](layout, nodeFactory, edgeFactory)
  children.add(graph)

  minWidth(graph.getWidth)
  maxWidth(graph.getWidth)
  minHeight(graph.getHeight)
  maxHeight(graph.getHeight)

  def getWidth: Double = layout.getWidth

  def getHeight: Double = layout.getHeight
}

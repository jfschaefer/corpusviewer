package de.jfschaefer.corpusviewer.visualization

import de.jfschaefer.layeredgraphlayout.latex.LatexGenerator
import de.jfschaefer.layeredgraphlayout.layout.LayoutConfig
import de.jfschaefer.layeredgraphlayout.lgraph.LGraphConfig
import de.jfschaefer.layeredgraphlayout.tree.Tree
import de.jfschaefer.layeredgraphlayout.visualizationfx.{SimpleGraphFXEdgeFactory, SimpleGraphFXNodeFactory, GraphFX}

import scalafx.scene.layout.Pane

import scala.collection.mutable
import scala.collection.JavaConversions._
import javafx.scene.paint.Color

/** A Pane visualizing a [[de.up.ling.tree.Tree[String]] using [[de.jfschaefer.layeredgraphlayout]]
 *
 * @param tree the tree to be visualized
 */
class TreePane(tree : de.up.ling.tree.Tree[String]) extends Pane {
  /*
      IDEA: Since Tree[String] key seem to cause problems, use Integer keys instead,
      which can be distinct by simple counting, and map them back to the labels for the factories
      efficiently (e.g. using a vector)
   */
  styleClass.clear()
  val labelNames = new mutable.ArrayBuffer[String]()
  val labelKeys = new mutable.ArrayBuffer[Integer]()
  labelNames.add(tree.getLabel)
  labelKeys.add(new Integer(0))
  val t = new Tree[Integer, Integer](labelKeys.get(0), labelNames.get(0).length * 10 + 30, 30)
  fillTree(0, t, tree)
  def fillTree(pos: Int, t: Tree[Integer, Integer], tree: de.up.ling.tree.Tree[String]): Unit = {
    for (child : de.up.ling.tree.Tree[String] <- tree.getChildren) {
      labelNames.add(child.getLabel)
      val newChild = new Integer(labelKeys.size)
      labelKeys.add(newChild)

      t.addChild(labelKeys.get(pos), newChild, newChild, child.getLabel.length * 10 + 30, 30)
      fillTree(labelKeys.size - 1, t, child)
    }
  }

  val lconfig = new LGraphConfig
  val lgraph = t.generateLGraph(lconfig)

  lgraph.treePlacement()

  val layoutconfig = new LayoutConfig
  val layout = lgraph.getLayout(layoutconfig)

  val nodeNames = new mutable.HashMap[Integer, String]()
  val edgeNames = new mutable.HashMap[Integer, String]()

  for (i <- labelKeys.indices) {
    nodeNames.put(labelKeys.get(i), labelNames.get(i))
    edgeNames.put(labelKeys.get(i), "")
  }

  val nodeFactory = new SimpleGraphFXNodeFactory[Integer](nodeNames, "graph_node", "")
  val edgeFactory = new SimpleGraphFXEdgeFactory[Integer](layoutconfig, edgeNames, Color.BLACK)
  val graph = new GraphFX[Integer, Integer](layout, nodeFactory, edgeFactory)
  children.add(graph)

  /** Returns the latex representation of the current layout */
  def getLaTeX(): String = {
    LatexGenerator.generateLatex(layout, nodeNames, edgeNames, true)
  }

  def getWidth: Double = layout.getWidth

  def getHeight: Double = layout.getHeight
}

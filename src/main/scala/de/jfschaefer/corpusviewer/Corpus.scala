package de.jfschaefer.corpusviewer

import java.io.Reader
import java.util

import de.jfschaefer.corpusviewer.preview.Slider

import de.up.ling.irtg.algebra.graph.GraphAlgebra
import de.up.ling.irtg.InterpretedTreeAutomaton
import de.up.ling.irtg.algebra.{StringAlgebra, Algebra, SetAlgebra}
import de.up.ling.irtg.signature.Signature

import scalafx.scene.Group

class Corpus(reader: Reader) extends Group {
  val algebraMap : java.util.Map[String, Algebra[_]] = new util.HashMap()
  algebraMap.put("string", new StringAlgebra)
  algebraMap.put("graph", new GraphAlgebra)
  algebraMap.put("set", new SetAlgebra)
  val irtg = InterpretedTreeAutomaton.forAlgebras(algebraMap)
  val corpus = de.up.ling.irtg.corpus.Corpus.readCorpus(reader, irtg)

  println("Loaded " + corpus.getNumberOfInstances + " instances")

  val slider = new Slider {
    layoutX = Configuration.windowMargin
    layoutY = Configuration.windowMargin
    minHeight <== Main.stage.height - 2*Configuration.windowMargin
    maxHeight <== Main.stage.height - 2*Configuration.windowMargin
  }

  children.add(slider)

  println("Generating preview visualizations")

}

package de.jfschaefer.corpusviewer.opendialog

import scalafx.scene.Group
import java.io.{BufferedReader, FileReader}

import de.up.ling.irtg.algebra.graph.GraphAlgebra
import de.up.ling.irtg.InterpretedTreeAutomaton
import de.up.ling.irtg.algebra.{StringAlgebra, Algebra, SetAlgebra}

/*
  In the future, this should be a proper dialog for opening a corpus. Once a corpus is selected and loaded, the load function
  can be called to close the dialog and to display the corpus instead.
 */

class OpenCorpusDialog(load: java.util.Iterator[de.up.ling.irtg.corpus.Instance] => Unit) extends Group {
  //temporary solution
  val algebraMap : java.util.Map[String, Algebra[_]] = new java.util.HashMap()
  algebraMap.put("string", new StringAlgebra)
  algebraMap.put("graph", new GraphAlgebra)
  algebraMap.put("set", new SetAlgebra)
  val irtg = InterpretedTreeAutomaton.forAlgebras(algebraMap)
  val corpus = de.up.ling.irtg.corpus.Corpus.readCorpus(new BufferedReader(new FileReader("example.txt")), irtg)

  println("Loaded " + corpus.getNumberOfInstances + " instances")

  load(corpus.iterator)
}

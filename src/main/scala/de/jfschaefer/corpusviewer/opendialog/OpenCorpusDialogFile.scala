package de.jfschaefer.corpusviewer.opendialog

import de.jfschaefer.corpusviewer.{Configuration, Main}

import scalafx.scene.Group
import scalafx.scene.text.Text

import java.io.{BufferedReader, FileReader, File}
import java.nio.file.Paths

import de.up.ling.irtg.algebra.graph.GraphAlgebra
import de.up.ling.irtg.InterpretedTreeAutomaton
import de.up.ling.irtg.algebra.{StringAlgebra, Algebra, SetAlgebra}


/*
  In the future, this should be a proper dialog for opening a corpus. Once a corpus is selected and loaded, the load function
  can be called to close the dialog and to display the corpus instead.
 */

class OpenCorpusDialogFile(load: java.util.Iterator[de.up.ling.irtg.corpus.Instance] => Unit) extends Group {

  var pwd : File = Paths.get("").toAbsolutePath.toFile

  val pwdLabel = new Text("\n" + pwd.toString) {
    styleClass.clear()
    styleClass.add("open_corpus_pwd")
    layoutX = Configuration.windowMargin
    layoutY = Configuration.windowMargin
  }

  updateEverything()

  def updateEverything(): Unit = {
    children.clear()
    pwdLabel.text = "\n" + pwd.toString
    children.add(pwdLabel)

    val files : Seq[(File, String)] = pwd.listFiles.foldLeft(Seq.empty[(File, String)] :+ (pwd, ".") :+ (pwd.getParentFile, "..")) (
                                                    (seq, file) => seq :+ (file, file.getName))
    val colStart = 2 * Configuration.windowMargin + pwdLabel.boundsInParent.value.getHeight
    val colHeight = Main.stage.height.value - Configuration.windowMargin - colStart
    var ncols = Configuration.openCorpusCols
    while (1 + files.length / ncols > Configuration.openCorpusMaxEntriesPerCol) ncols += 1
    val colWidth = (Main.stage.width.value - (ncols + 1) * Configuration.windowMargin) / ncols
    val maxEntriesPerCol : Int = 1 + files.length / ncols
    val height = math.min(Configuration.openCorpusMaxEntryHeight, (colHeight - Configuration.openCorpusMargin * maxEntriesPerCol)/ maxEntriesPerCol)

    var counter = 0
    for (file <- files) {
      val d = new FileDisplay(file._1, file._2, openFile, height, colWidth)
      val col = counter / maxEntriesPerCol
      d.layoutX = col * (Configuration.windowMargin + colWidth) + Configuration.windowMargin
      d.layoutY = colStart + (counter % maxEntriesPerCol) * (height + Configuration.openCorpusMargin) + Configuration.openCorpusMargin
      counter += 1
      children.add(d)
    }
  }

  def openFile(f : File): Boolean = {
    if (f == null) return false
    if (f.isFile) {
      // temporary solution
      val algebraMap : java.util.Map[String, Algebra[_]] = new java.util.HashMap()
      algebraMap.put("string", new StringAlgebra)
      algebraMap.put("graph", new GraphAlgebra)
      algebraMap.put("set", new SetAlgebra)
      val irtg = InterpretedTreeAutomaton.forAlgebras(algebraMap)
      try {
        val corpus = de.up.ling.irtg.corpus.Corpus.readCorpus(new BufferedReader(new FileReader(f)), irtg)
        println("Loaded " + corpus.getNumberOfInstances + " instances")

        load(corpus.iterator)
      } catch {
        case exception : Exception =>
          exception.printStackTrace()
          return false
      }
    } else {
      pwd = f
      updateEverything()
    }
    true
  }
}

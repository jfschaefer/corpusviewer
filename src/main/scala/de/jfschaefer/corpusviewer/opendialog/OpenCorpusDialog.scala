package de.jfschaefer.corpusviewer.opendialog

import scalafx.scene.Group
import java.io.{Reader, BufferedReader, FileReader}

class OpenCorpusDialog(load: Reader => Unit) extends Group {
  //temporary solution
  load(new  BufferedReader(new FileReader("example.txt")))
}

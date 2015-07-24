package de.jfschaefer.corpusviewer.opendialog

import de.jfschaefer.corpusviewer.{Configuration, Main}

import scalafx.scene.control.Label
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.Pane
import scalafx.scene.shape.Rectangle
import scalafx.Includes._

import java.io.File

class FileDisplay(file : File, displayString : String, onClick : (File) => Boolean, _height : Double, _width : Double) extends Pane {
  val bgRect = new Rectangle {
    width = _width
    height = _height
    layoutX = 0
    layoutY = 0
    styleClass.clear()
    styleClass.add(if (file == null) "open_corpus_file_error" else if (file.isFile) "open_corpus_file_file" else "open_corpus_file_dir")
  }
  children.add(bgRect)

  val label = new Label(displayString) {
    styleClass.clear()
    styleClass.add("open_corpus_file_label")
  }
  label.layoutX = Configuration.openCorpusMargin
  label.boundsInParentProperty.onChange {
    label.layoutY = 0.5 * (_height - label.boundsInLocal.value.getHeight)
  }
  label.layoutY = 0.5 * (_height - label.boundsInLocal.value.getHeight)
  children.add(label)

  bgRect.onMouseClicked = { ev : MouseEvent =>
    if (!onClick(file)) {
      bgRect.styleClass.clear()
      bgRect.styleClass.add("open_corpus_file_error")
    }
  }

}

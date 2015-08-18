package de.jfschaefer.corpusviewer.opendialog

import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control._

import de.up.ling.tclup.perf.alto.{CorpusFromDb, GrammarMetadata}

/** A collection of useful functions for a dialog for opening a corpus */
object OpenCorpusUtil {
  /** Get a description for GrammarMetadata
    *
    * @param grammarMetadata the meta data
    * @return the description
    */
  def stringFromMeta(grammarMetadata: GrammarMetadata): String = {
    val s = new StringBuilder
    s.append("name:\t")
    s.append(grammarMetadata.name)
    s.append("\nid:\t")
    s.append(grammarMetadata.id)
    s.append("\ncomment:\t")
    s.append(grammarMetadata.comment)
    s.append("\ninterpretations:")
    for ((k, v) <- grammarMetadata.interpretations) {
      s.append("\n    " )
      s.append(k)
      s.append(":   ")
      s.append(v)
    }
    s.toString()
  }

  /** Get a description for corpus metadata
    *
    * @param corpusMeta the meta data
    * @return the description
    */
  def stringFromMeta(corpusMeta: CorpusFromDb#CorpusMetadata): String = {
    val s = new StringBuilder
    s.append("name:\t")
    s.append(corpusMeta.name)
    s.append("\nid:\t")
    s.append(corpusMeta.id)
    s.append("\ncomment:\t")
    s.append(corpusMeta.comments match {
      case Some(comment) => comment
      case None => "<no comment>"
    })
    s.append("\ninterpretations:")
    for (i <- corpusMeta.interpretations) {
      s.append("\n    " )
      s.append(i)
    }
    s.toString()
  }

  /** Displays an error message
    *
    * @param head the title
    * @param body the message
    */
  def showError(head : String, body : String): Unit = {
    val alert = new Alert(AlertType.Error)
    alert.setHeaderText(head)
    alert.setContentText(body)
    alert.showAndWait()
  }
}

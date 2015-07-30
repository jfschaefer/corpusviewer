package de.jfschaefer.corpusviewer.opendialog

import javafx.scene.layout

import de.jfschaefer.corpusviewer.{Main, Configuration}

import de.up.ling.tclup.perf.DatabaseConnection
import de.up.ling.tclup.perf.alto.CorpusFromDb
import de.up.ling.tclup.perf.alto.{GrammarMetadata, CorpusFromDb, GrammarFromDb}


import scalafx.collections.ObservableBuffer
import scalafx.event.ActionEvent
import scalafx.geometry.Pos
import scalafx.scene.{Scene, Group}
import scalafx.scene.layout.VBox
import scalafx.scene.text.Text
import scalafx.stage.{Stage, FileChooser}
import scalafx.scene.control.{ScrollPane, ChoiceBox, Label, Button}
import scalafx.Includes._

class OpenCorpusDialog extends Group {

  val statusText = new Text("Opening Corpus") {
    styleClass.clear()
    styleClass.add("open_corpus_phase_font")
  }
  children.add(statusText)
  updateStatusTextPos()
  statusText.boundsInLocal onChange updateStatusTextPos()

  def updateStatusTextPos(): Unit = {
    statusText.layoutX = Main.stage.getWidth * 0.5 - statusText.getBoundsInParent.getWidth * 0.5
    statusText.layoutY = Main.stage.getHeight * 0.5 - statusText.getBoundsInParent.getHeight * 0.5
  }


  start()

  def start(): Unit = {
    val pane : VBox = new VBox {
      spacing = 15
      alignment = Pos.Center
    }
    val fileChooser = new FileChooser

    val stage = new Stage {
      title = "Open Corpus"
      width = 600
      height = 500
      scene = new Scene {
        root = pane
      }
    }

    var db : DatabaseConnection = null

    stage.show()
    openDatabase()



    def openDatabase(): Unit = {
      pane.children.clear()

      val textField = new Text("The configuration file:\nnull")
      pane.children.append(textField)
      var config : java.io.File = null
      pane.children.append(Button.sfxButton2jfx(new Button("Change configuration file") {
        onAction = {
          (_: ActionEvent) =>
            val n = fileChooser.showOpenDialog(stage)
            if (n != null) {
              textField.text = "The configuration file:\n"+n
              config = n
            }
        }
        minWidth = 200
      }))
      pane.children.append(Button.sfxButton2jfx(new Button("Continue") {
        minWidth = 200
        onAction = {
          (_: ActionEvent) => {
            loadInstances(config.toString)
          }
        }
      }))
      println(pane.children)
      config = fileChooser.showOpenDialog(stage)
      textField.text = "The configuration file:\n" + config
    }

    def loadInstances(configPath : String): Unit = {
      pane.children.clear()
      db = new DatabaseConnection(configPath)

      /*
          GRAMMAR
       */
      val grammarMDs = new GrammarFromDb(db).allIrtgsMetadata
      val grammarCB: ChoiceBox[ChoiceBoxEntry[GrammarMetadata]] = new ChoiceBox
      val grammarCBItems = new ObservableBuffer[ChoiceBoxEntry[GrammarMetadata]]()
      for (md <- grammarMDs) {
        grammarCBItems.append(new ChoiceBoxEntry[GrammarMetadata](md, "Grammar " + md.id))
      }
      grammarCB.items = grammarCBItems

      pane.children.add(grammarCB)

      val grammarT = new Text("<No Grammar Selected>")
      val grammarSP = new ScrollPane
      grammarSP.content.value = grammarT
      grammarSP.setMinHeight(150)
      grammarSP.setMaxHeight(150)
      grammarSP.setMinWidth(pane.boundsInLocal.value.getWidth)
      grammarSP.setMaxWidth(pane.boundsInLocal.value.getWidth)

      pane.children.add(grammarSP)
      grammarCB.value onChange {
        (x, _, _) =>
          grammarT.text = OpenCorpusDialog.stringFromMeta(x.value.unwrap)
      }

      /*
          CORPUS
       */
      val corpusMDs = new CorpusFromDb(db).allCorporaMetadata
      val corpusCB: ChoiceBox[ChoiceBoxEntry[CorpusFromDb#CorpusMetadata]] = new ChoiceBox
      val corpusCBItems = new ObservableBuffer[ChoiceBoxEntry[CorpusFromDb#CorpusMetadata]]()
      for (md <- corpusMDs) {
        corpusCBItems.append(new ChoiceBoxEntry(md, "Corpus " + md.id))
      }
      corpusCB.items = corpusCBItems

      pane.children.add(corpusCB)

      val corpusT = new Text("<No Corpus Selected>")
      val corpusSP = new ScrollPane
      corpusSP.content.value = corpusT
      corpusSP.setMinHeight(150)
      corpusSP.setMaxHeight(150)
      corpusSP.setMinWidth(pane.boundsInLocal.value.getWidth)
      corpusSP.setMaxWidth(pane.boundsInLocal.value.getWidth)

      pane.children.add(corpusSP)
      corpusCB.value onChange {
        (x, _, _) =>
          corpusT.text = OpenCorpusDialog.stringFromMeta(x.value.unwrap)
      }

      pane.children.append(Button.sfxButton2jfx(new Button("Continue") {
        onAction = {
          (_: ActionEvent) => {

          }
        }
      }))
    }
  }
}

object OpenCorpusDialog {
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
}

class ChoiceBoxEntry[T](content : T, string : String) {
  def unwrap: T = content
  override def toString(): String = string
}

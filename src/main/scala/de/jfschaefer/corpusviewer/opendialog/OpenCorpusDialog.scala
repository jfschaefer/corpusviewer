package de.jfschaefer.corpusviewer.opendialog

import javafx.scene.layout

import de.jfschaefer.corpusviewer.{Main, Configuration}
import de.up.ling.irtg.InterpretedTreeAutomaton
import de.up.ling.irtg.algebra.Algebra

import de.up.ling.tclup.perf.DatabaseConnection
import de.up.ling.tclup.perf.alto.CorpusFromDb
import de.up.ling.tclup.perf.alto.{GrammarMetadata, CorpusFromDb, GrammarFromDb}


import scalafx.collections.ObservableBuffer
import scalafx.event.ActionEvent
import scalafx.geometry.Pos
import javafx.geometry.Insets
import scalafx.scene.{Scene, Group}
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.text.Text
import scalafx.stage.{Stage, FileChooser}
import scalafx.scene.control.{ScrollPane, ChoiceBox, Label, Button}
import scalafx.Includes._

class OpenCorpusDialog(load: (java.util.Iterator[de.up.ling.irtg.corpus.Instance], Map[String, String]) => Unit) extends Group {

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
      pane.padding = new Insets(5, 5, 5, 5)

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
      val grammarhbox = new HBox()
      val grammarCB: ChoiceBox[ChoiceBoxEntry[GrammarMetadata]] = new ChoiceBox
      val grammarCBItems = new ObservableBuffer[ChoiceBoxEntry[GrammarMetadata]]()
      for (md <- grammarMDs) {
        grammarCBItems.append(new ChoiceBoxEntry[GrammarMetadata](md, "" + md.id + ".: " + md.name))
      }
      grammarCB.items = grammarCBItems

      grammarhbox.children.add(new Label("Grammar: "))
      grammarhbox.children.add(grammarCB)
      pane.children.add(grammarhbox)

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
          grammarT.text = OpenCorpusUtil.stringFromMeta(x.value.unwrap)
      }

      /*
          CORPUS
       */
      val corpusMDs = new CorpusFromDb(db).allCorporaMetadata
      val corpusCB: ChoiceBox[ChoiceBoxEntry[CorpusFromDb#CorpusMetadata]] = new ChoiceBox
      val corpusCBItems = new ObservableBuffer[ChoiceBoxEntry[CorpusFromDb#CorpusMetadata]]()
      for (md <- corpusMDs) {
        corpusCBItems.append(new ChoiceBoxEntry(md, "" + md.id + ".: " + md.name))
      }
      corpusCB.items = corpusCBItems
      val corpushbox = new HBox()
      corpushbox.children.add(new Label("Corpus: "))

      corpushbox.children.add(corpusCB)
      pane.children.add(corpushbox)

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
          corpusT.text = OpenCorpusUtil.stringFromMeta(x.value.unwrap)
      }

      pane.children.append(Button.sfxButton2jfx(new Button("Continue") {
        onAction = {
          (_: ActionEvent) => {
            /* if (grammarCB.value == null) return
            if (corpusCB.value == null) return
            */
            try {

              //Generate algebra map
              val algebraMap : java.util.Map[String, Algebra[_]] = new java.util.HashMap()
              val mapping = grammarCB.value.value.unwrap.interpretations
              for ((key, className) <- mapping) {
                algebraMap.put(key, Class.forName(className).newInstance().asInstanceOf[Algebra[_]])
              }
              val irtg = InterpretedTreeAutomaton.forAlgebras(algebraMap)
              val corpusfromdb = new CorpusFromDb(db)
              val corpus = corpusfromdb.readCorpus(corpusCB.value.value.unwrap.id, irtg)

              println("Loaded " + corpus.getNumberOfInstances + " instances")

              load(corpus.iterator, grammarCB.value.value.unwrap.interpretations)
            } catch {
              case exception : Exception =>
                exception.printStackTrace()
            }
          }
        }
      }))
    }
  }
}




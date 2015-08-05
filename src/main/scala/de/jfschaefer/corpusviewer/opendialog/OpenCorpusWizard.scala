package de.jfschaefer.corpusviewer.opendialog

import de.jfschaefer.corpusviewer.{Main, Configuration}
import de.up.ling.irtg.InterpretedTreeAutomaton
import de.up.ling.irtg.algebra.Algebra
import de.up.ling.tclup.perf.DatabaseConnection
import de.up.ling.tclup.perf.alto.{CorpusFromDb, GrammarMetadata, GrammarFromDb}
import de.up.ling.irtg.corpus.{Instance, Corpus}

import scala.collection.mutable.ListBuffer
import scalafx.collections.ObservableBuffer
import scalafx.event.ActionEvent
import scalafx.geometry.Pos
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control._
import scalafx.scene.layout.{HBox, VBox, BorderPane}
import scalafx.scene.{Group, Node, Scene}
import scalafx.scene.text.Text
import scalafx.stage.{FileChooser, Stage}
import scalafx.Includes._

import scala.collection.mutable
import scala.collection.JavaConversions._

class OpenCorpusWizard(load: (Seq[de.up.ling.irtg.corpus.Instance], Map[String, String], Set[String]) => Unit) extends Group {

  val PADDING = 15d
  prepareGroup()

  val borderPane : BorderPane = new BorderPane

  val fileChooser = new FileChooser

  val stage = new Stage {
    title = "Open Corpus"
    width = 600
    height = 500
    scene = new Scene {
      root = borderPane
    }
  }

  stage.show()

  var nextButton : Button = null
  var prevButton : Button = null
  var nextFunction : () => Unit = { () => }
  var prevFunction : () => Unit = { () => }

  var filteredInstances : mutable.ArrayBuffer[de.up.ling.irtg.corpus.Instance] = null

  setupBorderPane()

  /*
      STATUS
   */

  //var configFile : java.io.File = new java.io.File(Configuration.openCorpusWizardDefaultConfigFile)
  var configFile : java.io.File = null
  var databaseConnection : DatabaseConnection = null
  var corpus : Corpus = null
  var interpretations : Map[String, String] = null
  var previewInterpretations : mutable.Set[String] = null

  chooseConfigFile()


  def chooseConfigFile(): Unit = {
    prevFunction = { () => }
    prevButton.disable = true
    nextButton.disable = configFile == null
    val vbox = new VBox {
      alignment = Pos.Center
      spacing = 15
    }
    val textField = new Text("The configuration file:\n"+configFile)
    textField.minWidth(200)
    textField.wrappingWidth = 200
    vbox.children.add(textField)
    vbox.children.append(Button.sfxButton2jfx(new Button("Change configuration file") {
      onAction = {
        (_: ActionEvent) =>
          val n = fileChooser.showOpenDialog(stage)
          if (n != null) {
            textField.text = "The configuration file:\n" + n
            configFile = n
            nextButton.disable = false
          }
      }
      minWidth = 200
    }))
    vbox.minWidth = stage.getWidth - 2 * PADDING

    updateCore(vbox)

    nextFunction = { () =>
      try {
        databaseConnection = new DatabaseConnection(configFile.toString)
        chooseCorpus()
      } catch {
        case e: Throwable =>
          val alert = new Alert(AlertType.Error)
          alert.setHeaderText("Error: Couldn't setup database connection")
          alert.setContentText(e.toString)
          e.printStackTrace()
          alert.showAndWait();
      }
    }
  }

  def chooseCorpus(): Unit = {
    prevButton.disable = false
    nextButton.disable = true
    prevFunction = chooseConfigFile

    var componentsSet = 0

    val vbox = new VBox {
      alignment = Pos.Center
      spacing = 15
    }

    /*
        GRAMMAR
     */
    val grammarMDs = new GrammarFromDb(databaseConnection).allIrtgsMetadata
    val grammarCB: ChoiceBox[ChoiceBoxEntry[GrammarMetadata]] = new ChoiceBox
    val grammarCBItems = new ObservableBuffer[ChoiceBoxEntry[GrammarMetadata]]()
    for (md <- grammarMDs) {
      grammarCBItems.append(new ChoiceBoxEntry[GrammarMetadata](md, "" + md.id + ".: " + md.name))
    }
    grammarCB.items = grammarCBItems
    grammarCB.minWidth <== stage.getWidth - 2 * PADDING

    val grammarLabel = new Label("Grammar:") { minWidth <== stage.width - 2*PADDING}
    vbox.children.add(grammarLabel)
    vbox.children.add(grammarCB)

    val grammarT = new Text("<No Grammar Selected>")
    val grammarSP = new ScrollPane
    grammarSP.content.value = grammarT
    grammarSP.setMinHeight(120)
    grammarSP.setMaxHeight(120)
    grammarSP.minWidth <== stage.getWidth - 2 * PADDING
    grammarSP.maxWidth <== stage.getWidth - 2 * PADDING

    vbox.children.add(grammarSP)
    grammarCB.value onChange {
      (x, _, _) =>
        grammarT.text = OpenCorpusUtil.stringFromMeta(x.value.unwrap)
        componentsSet |= 1
        if (componentsSet == 3) {
          nextButton.disable = false
        }
    }

    /*
        CORPUS
     */
    val corpusMDs = new CorpusFromDb(databaseConnection).allCorporaMetadata
    val corpusCB: ChoiceBox[ChoiceBoxEntry[CorpusFromDb#CorpusMetadata]] = new ChoiceBox
    corpusCB.minWidth <== stage.getWidth - 2 * PADDING
    val corpusCBItems = new ObservableBuffer[ChoiceBoxEntry[CorpusFromDb#CorpusMetadata]]()
    for (md <- corpusMDs) {
      corpusCBItems.append(new ChoiceBoxEntry(md, "" + md.id + ".: " + md.name))
    }
    corpusCB.items = corpusCBItems
    val corpusLabel = new Label("Corpus:") { minWidth <== stage.width - 2 * PADDING }
    vbox.children.add(corpusLabel)

    vbox.children.add(corpusCB)

    val corpusT = new Text("<No Corpus Selected>")
    val corpusSP = new ScrollPane
    corpusSP.content.value = corpusT
    corpusSP.setMinHeight(120)
    corpusSP.setMaxHeight(120)
    corpusSP.minWidth <== stage.width - 2 * PADDING
    corpusSP.maxWidth <== stage.width - 2 * PADDING

    vbox.children.add(corpusSP)
    corpusCB.value onChange {
      (x, _, _) =>
        corpusT.text = OpenCorpusUtil.stringFromMeta(x.value.unwrap)
        componentsSet |= 2
        if (componentsSet == 3) {
          nextButton.disable = false
        }
    }

    updateCore(vbox)

    nextFunction = { () =>
      try {
        //Generate algebra map
        val algebraMap : java.util.Map[String, Algebra[_]] = new java.util.HashMap()
        interpretations = grammarCB.value.value.unwrap.interpretations
        val mapping = grammarCB.value.value.unwrap.interpretations
        for ((key, className) <- mapping) {
          algebraMap.put(key, Class.forName(className).newInstance().asInstanceOf[Algebra[_]])
        }
        val irtg = InterpretedTreeAutomaton.forAlgebras(algebraMap)
        val corpusfromdb = new CorpusFromDb(databaseConnection)
        corpus = corpusfromdb.readCorpus(corpusCB.value.value.unwrap.id, irtg)
        chooseFilters()
      } catch {
        case e: Throwable =>
          val alert = new Alert(AlertType.Error)
          alert.setHeaderText("Error: Couldn't setup database connection")
          alert.setContentText(e.toString)
          e.printStackTrace()
          alert.showAndWait();
      }
    }
  }

  def chooseFilters(): Unit = {
    nextButton.disable = false
    prevButton.disable = false
    prevFunction = chooseCorpus

    val vbox = new VBox {
      alignment = Pos.Center
      spacing = 15
    }

    val label = new Label("Filter rule:") { minWidth <== stage.width - 2 * PADDING }
    val textArea = new TextArea(
      """
        |def filter(instance, interpretations):
        |    return True
      """.stripMargin) {
      minWidth <== stage.width - 2 * PADDING
      maxWidth <== stage.width - 2 * PADDING
      wrapText = true
      minHeight = 200
      maxHeight = 200
    }
    vbox.children.add(label)
    vbox.children.add(textArea)
    updateCore(vbox)

    //updateCore(new Label("Here, an amazing filter dialog will emerge"))
    nextFunction = {
      () =>
        val filterText = textArea.getText
        val filterResult = Filter.createFromJython(filterText)
        filterResult match {
          case FilterOk(filter) =>
            filteredInstances = new mutable.ArrayBuffer[Instance]
            for (instance <- corpus.iterator()) {
              if (filter.assess(instance)) {
                filteredInstances.append(instance)
              }
            }
            choosePreviewInterpretations()
          case FilterErr(message) =>
            val alert = new Alert(AlertType.Error)
            alert.setHeaderText("Error: Couldn't compile filter rule")
            alert.setContentText(message)
            alert.showAndWait()
        }
    }
  }


  def choosePreviewInterpretations(): Unit = {
    nextButton.disable = false
    prevButton.disable = false
    prevFunction = chooseFilters

    val recWidth = 500
    val vbox = new VBox {
      alignment = Pos.Center
      spacing = 15
    }

    val label = new Label("Which intepretations shall be visualized in the preview?") { minWidth = recWidth }
    val checkboxes = new mutable.HashSet[(CheckBox, String)]()
    vbox.children.add(label)
    for (interpretation <- interpretations.keys) {
      val box = new CheckBox(interpretation) { minWidth = recWidth }
      vbox.children.add(box)
      checkboxes.add((box, interpretation))
    }

    updateCore(vbox)

    nextFunction = { () =>
      previewInterpretations = new mutable.HashSet[String]()
      for ((checkBox, interpretation) <- checkboxes) {
        if (checkBox.isSelected) {
          previewInterpretations.add(interpretation)
        }
      }
      load(filteredInstances, interpretations, previewInterpretations.toSet)
      // load(corpus.iterator(), interpretations, previewInterpretations.toSet)
    }
  }



  def updateCore(n : Node): Unit = {
    borderPane.setCenter(n)
  }

  def setupBorderPane(): Unit = {
    val hbox = new HBox
    nextButton = new Button("Continue") {
      onAction = { (_: ActionEvent) =>
        nextFunction()
      }
      minWidth <== stage.width / 2
    }
    prevButton = new Button("Back") {
      onAction = { (_: ActionEvent) =>
        prevFunction()
      }
      minWidth <== stage.width / 2
    }
    hbox.children.add(prevButton)
    hbox.children.add(nextButton)
    hbox.minWidth <== stage.width
    borderPane.setBottom(hbox)
  }

  def prepareGroup(): Unit = {
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
  }
}

package de.jfschaefer.corpusviewer.opendialog

import java.io.{FileReader, BufferedReader}

import de.jfschaefer.corpusviewer.{Main, Configuration}
import de.up.ling.irtg.InterpretedTreeAutomaton
import de.up.ling.irtg.algebra.Algebra
import de.up.ling.irtg.codec.irtg.IrtgLexer
import de.up.ling.tclup.perf.DatabaseConnection
import de.up.ling.tclup.perf.alto.{CorpusFromDb, GrammarMetadata, GrammarFromDb}
import de.up.ling.irtg.corpus.{Instance, Corpus}
import org.antlr.v4.runtime.ANTLRInputStream

import scalafx.collections.ObservableBuffer
import scalafx.event.ActionEvent
import scalafx.geometry.Pos
import javafx.scene.control.Alert.AlertType
import scalafx.scene.control._
import javafx.scene.control.Alert
import scalafx.scene.layout.{HBox, VBox, BorderPane}
import scalafx.scene.{Group, Node, Scene}
import scalafx.scene.text.Text
import scalafx.stage.{FileChooser, Stage}
import scalafx.Includes._

import scala.collection.mutable
import scala.collection.JavaConversions._

/** A wizard for opening a corpus
  *
  * It should be rewritten in a cleaner way at some point.
  * There is one method for each step of the wizard.
  *
  * @param load the function to be called when done
  */
class OpenCorpusWizard(load: (Seq[de.up.ling.irtg.corpus.Instance], Map[String, String], Set[String]) => Unit) extends Group {

  // Should put some of these into the configuration file at some point
  val PADDING = 15d
  val MEDIUM_WIDTH = 400
  //val defaultFilterRule = "def filter(instance, interpretations):\n\treturn True\n"
  val defaultFilterRule = "# The default filter. It accepts all instances.\n\n# The function filter will be called on all instances.\n# Instances for which it returns False will be discarded.\n\n# There are a few example filter rules in the repository.\n\ndef filter(instance, interpretations):\n\treturn True"
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

  var corpusLocation : String = ""
  var corpusFile : java.io.File = null
  var interpretationsFile : java.io.File = null
  var configFile : java.io.File = null
  var filterRule : String = defaultFilterRule
  var databaseConnection : DatabaseConnection = null
  var corpus : Corpus = null
  var interpretations : Map[String, String] = null
  var previewInterpretations : mutable.Set[String] = null

  selectCorpusLocation()

  def selectCorpusLocation(): Unit = {
    prevButton.disable = true
    nextButton.disable = true

    val vbox = new VBox {
      alignment = Pos.Center
      spacing = 15
    }

    vbox.children.add(Button.sfxButton2jfx(new Button("Corpus from File") {
      minWidth = MEDIUM_WIDTH
      onAction = {
        (_: ActionEvent) =>
          corpusLocation = "file"
          takeCorpusFromFile()
      }
    }))

    vbox.children.add(Button.sfxButton2jfx(new Button("Corpus from Database") {
      minWidth = MEDIUM_WIDTH
      onAction = {
        (_: ActionEvent) =>
          corpusLocation = "database"
          chooseConfigFile()
      }
    }))

    updateCore(vbox)
  }

  def takeCorpusFromFile(): Unit = {
    prevFunction = selectCorpusLocation
    prevButton.disable = false
    nextButton.disable = corpusFile == null || interpretationsFile == null
    val vbox = new VBox {
      alignment = Pos.Center
      spacing = 15
    }

    val textFieldInterpretations = new Text("Interpretations File:\n"+interpretationsFile)
    textFieldInterpretations.minWidth(MEDIUM_WIDTH)
    textFieldInterpretations.wrappingWidth = MEDIUM_WIDTH
    vbox.children.add(textFieldInterpretations)
    vbox.children.append(Button.sfxButton2jfx(new Button("Change Interpretations") {
      onAction = {
        (_: ActionEvent) =>
          val n = fileChooser.showOpenDialog(stage)
          if (n != null) {
            textFieldInterpretations.text = "Interpretations File:\n" + n
            interpretationsFile = n
            nextButton.disable = corpusFile == null
          }
      }
      minWidth = MEDIUM_WIDTH
    }))

    val textFieldCorpus = new Text("Corpus File:\n"+corpusFile)
    textFieldCorpus.minWidth(MEDIUM_WIDTH)
    textFieldCorpus.wrappingWidth = MEDIUM_WIDTH
    vbox.children.add(textFieldCorpus)
    vbox.children.append(Button.sfxButton2jfx(new Button("Change corpus") {
      onAction = {
        (_: ActionEvent) =>
          val n = fileChooser.showOpenDialog(stage)
          if (n != null) {
            textFieldCorpus.text = "Corpus File:\n" + n
            corpusFile = n
            nextButton.disable = interpretationsFile == null
          }
      }
      minWidth = MEDIUM_WIDTH
    }))
    vbox.minWidth = stage.getWidth - 2 * PADDING

    updateCore(vbox)

    nextFunction = { () =>
      try {
        val iregex = """interpretation\s+([^\s:]+)\s*:\s*([^\s]+)""".r
        val interpretationsTmp = new mutable.HashMap[String, String]
        for (line <- scala.io.Source.fromFile(interpretationsFile).getLines()) {
          val trimmed = line.trim
          if (trimmed != "") {
            trimmed match {
              case iregex(key, value) => interpretationsTmp.put(key, value)
              case _ =>
                val alert = new Alert(AlertType.WARNING)
                alert.setHeaderText("Warning: Couldn't parse line in interpretations file (skipping it)")
                alert.setContentText(s"Line:\n$line")
                alert.showAndWait()
            }
          }
        }
        interpretations = interpretationsTmp.toMap

        try {
          val algebraMap: java.util.Map[String, Algebra[_]] = new java.util.HashMap()
          for ((key, className) <- interpretations) {
            algebraMap.put(key, Class.forName(className).newInstance().asInstanceOf[Algebra[_]])
          }
          val irtg = InterpretedTreeAutomaton.forAlgebras(algebraMap)
          corpus = de.up.ling.irtg.corpus.Corpus.readCorpus(new BufferedReader(new FileReader(corpusFile)), irtg)
          chooseFilters()
        } catch {
          case e: Throwable =>
            OpenCorpusUtil.showError("Error: Couldn't load corpus", e.toString)
        }
      } catch {
        case e: Throwable =>
          OpenCorpusUtil.showError("Error: Couldn't load interpretations", e.toString)
          return
      }

    }
  }



  /*
      DATABASE CONFIG
   */
  def chooseConfigFile(): Unit = {
    prevFunction = selectCorpusLocation
    prevButton.disable = false
    nextButton.disable = configFile == null
    val vbox = new VBox {
      alignment = Pos.Center
      spacing = 15
    }
    val textField = new Text("The configuration file:\n"+configFile)
    textField.minWidth(MEDIUM_WIDTH)
    textField.wrappingWidth = MEDIUM_WIDTH
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
      minWidth = MEDIUM_WIDTH
    }))
    vbox.minWidth = stage.getWidth - 2 * PADDING

    updateCore(vbox)

    nextFunction = { () =>
      try {
        databaseConnection = new DatabaseConnection(configFile.toString)
        chooseCorpusInDB()
      } catch {
        case e: Throwable =>
          OpenCorpusUtil.showError("Error: Couldn't setup database connection", e.toString)
      }
    }
  }

  /*
      DATABASE CORPUS
   */
  def chooseCorpusInDB(): Unit = {
    prevButton.disable = false
    nextButton.disable = true
    prevFunction = chooseConfigFile

    var componentsSet = 0

    val vbox = new VBox {
      alignment = Pos.Center
      spacing = 15
    }

    //    GRAMMAR
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

    //    CORPUS
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
        println(irtg.toString)
        val corpusfromdb = new CorpusFromDb(databaseConnection)
        corpus = corpusfromdb.readCorpus(corpusCB.value.value.unwrap.id, irtg)
        chooseFilters()
      } catch {
        case e: Throwable =>
          OpenCorpusUtil.showError("Error: Failed to load corpus", e.toString)
      }
    }
  }

  /*
      FILTERS
   */
  def chooseFilters(): Unit = {
    nextButton.disable = false
    prevButton.disable = false

    val vbox = new VBox {
      alignment = Pos.Center
      spacing = 15
    }

    val label = new Label("Filter rule:") { minWidth <== stage.width - 2 * PADDING }
    val textArea = new TextArea(filterRule) {
      minWidth <== stage.width - 2 * PADDING
      maxWidth <== stage.width - 2 * PADDING
      wrapText = false  //after all, it's code
      minHeight = 300
      maxHeight = 300
    }
    vbox.children.add(label)
    vbox.children.add(textArea)
    vbox.children.add(Button.sfxButton2jfx(new Button("Load filter rule from file") {
      onAction = {
        (_: ActionEvent) =>
          val file = fileChooser.showOpenDialog(stage)
          if (file != null) {
            try {
              val s = new StringBuilder()
              for (line <- scala.io.Source.fromFile(file).getLines()) {
                s.append(line.replaceAll("    ", "\t") + "\n")    //TODO: Find more flexible solution - maybe along with a decent editor
              }
              textArea.setText(s.toString())
            } catch {
              case e : Exception => OpenCorpusUtil.showError("Couldn't load filter rule", e.toString)
            }
          }
      }
      minWidth = MEDIUM_WIDTH
    }))
    updateCore(vbox)


    prevFunction = { () =>
      filterRule = textArea.getText
      if (corpusLocation == "file") takeCorpusFromFile() else chooseCorpusInDB()
    }

    nextFunction = {
      () =>
        filterRule = textArea.getText
        val filterResult = Filter.createFromJython(filterRule)
        filterResult match {
          case FilterOk(filter) =>
            filteredInstances = new mutable.ArrayBuffer[Instance]
            try {
              var instanceCounter = 1
              for (instance <- corpus.iterator()) {
                if (instance.getComments == null) {
                  instance.setComments(new java.util.HashMap[String, String])
                }
                instance.getComments.put("corpusviewer_id", instanceCounter.toString)
                instanceCounter += 1
                if (filter.assess(instance)) {
                  filteredInstances.append(instance)
                }
              }
              choosePreviewInterpretations()
            } catch {
              case e : FilterException =>
                OpenCorpusUtil.showError("Filter error", "An error occured in the filtering process.\nPlease fix the rules and try again.")
            }
          case FilterErr(message) =>
            OpenCorpusUtil.showError("Error: Couldn't compile filter rule", message)
        }
    }
  }


  /*
      PREVIEW INTERPRETATIONS
   */
  def choosePreviewInterpretations(): Unit = {
    nextButton.disable = false
    prevButton.disable = false
    prevFunction = chooseFilters

    val recWidth = stage.getWidth - 2 * PADDING
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


  /*
      UTILS
   */

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

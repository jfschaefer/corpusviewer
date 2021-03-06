package de.jfschaefer.corpusviewer

import de.jfschaefer.corpusviewer.opendialog._

import de.up.ling.gesture.JavaFxAdapter

import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.stage.{Screen, StageStyle}
import scalafx.Includes._


/** The main program */

object Main extends JFXApp {
  val corpusScene = new Scene {
    stylesheets += Configuration.stylesheet
    fill = Configuration.backgroundColor
  }

  val openCorpusScene = new Scene {
    stylesheets += Configuration.stylesheet
    fill = Configuration.backgroundColor
  }

  private var corpus: Corpus = null

  stage = new JFXApp.PrimaryStage {
    title.value = "Corpus Viewer"
    //Set default size
    width = 800
    height = 600
    scene = openCorpusScene
  }

  val adapter = JavaFxAdapter.start(stage.delegate, Configuration.gestureConfigProperties)

  val numberOfScreens = Screen.screens.size
  if (numberOfScreens > Configuration.displayScreen) {
    if (Configuration.fullscreen) fullscreen(Configuration.displayScreen)
  } else {
    System.err.println("de.jfschaefer.corpusviewer.Main: Invalid configuration 'display_screen':")
    System.err.println("Got '" + Configuration.displayScreen + "' (have only " + numberOfScreens + " screens)")
    System.err.println("Moving to screen 0 instead")
    fullscreen(0)
  }

  openCorpusScene.root = new OpenCorpusWizard(openCorpus)

  /** Opens a corpus and displays it
    *
    * @param instances the instances of the corpus
    * @param interpretations a map from the interpretation names to the corresponding algebra classes
    * @param previewInterpretations a subset of the interpretation names, which will be displayed in the preview
    */
  def openCorpus(instances: Seq[de.up.ling.irtg.corpus.Instance],
                 interpretations: Map[String, String], previewInterpretations: Set[String]): Unit = {
    corpus = new Corpus(instances, interpretations, previewInterpretations)
    corpusScene.root = corpus
    stage.scene = corpusScene
    corpus.previewGroup.update()
  }

  def getCorpus:Corpus = corpus

  /** Put stage into fullscreen
    *
    * @param displayId the screen on which it shall be displayed
    */
  def fullscreen(displayId:Int): Unit = {
    val targetScreen = Screen.screens.get(displayId)
    val bounds = targetScreen.getVisualBounds

    stage.setX(bounds.getMinX)
    stage.setY(bounds.getMinY)
    stage.setWidth(bounds.getWidth)
    stage.setHeight(bounds.getHeight)

    stage.initStyle(StageStyle.UNDECORATED)
    stage.setFullScreen(true)
  }
}
